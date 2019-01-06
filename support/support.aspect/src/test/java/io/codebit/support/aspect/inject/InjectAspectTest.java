package io.codebit.support.aspect.inject;

import io.codebit.support.aspect.test.inject.model.BetterEngine;
import io.codebit.support.aspect.test.inject.model.Seat;
import io.codebit.support.cdi.SpringCDIProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import io.codebit.support.aspect.test.inject.model.Car;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;

import javax.enterprise.inject.spi.CDI;


@RunWith(SpringJUnit4ClassRunner.class)
//ApplicationContext will be loaded from the static inner ContextConfiguration class
@ContextConfiguration(loader=AnnotationConfigContextLoader.class)
public class InjectAspectTest {
	
	@Configuration
	@ComponentScan("io.codebit.support.aspect.test")
//	@Profile("production")
    static class ContextConfiguration {
        // this bean will be injected into the OrderServiceTest class
		@Bean
		public Seat seat() {
			return new Seat();
		}
    }

	@Autowired
	ApplicationContext context;

	@Autowired
	BeanFactory beanFactory;
	
	@Before 
	public void setUp() {

	}
	
	@Test
	public void testModel() throws Exception {
		assertNotNull(context);
		assertNotNull(beanFactory);
		CDI.setCDIProvider(new SpringCDIProvider(context));

//		AspectClassFileTransformer transformer = new AspectClassFileTransformer(InjectAspect.class);
//		transformer.context().options("-verbose");
//		Instrument.transform(Arrays.asList(transformer), Arrays.asList("io.codebit.support.aspect.test..*"));

		Car car = new Car(new BetterEngine());
		Seat seat = car.seat();
		assertNotNull(seat);
		Seat seat2 = CDI.current().select(Seat.class).get();
		assertNotNull(seat2);
	}
}
