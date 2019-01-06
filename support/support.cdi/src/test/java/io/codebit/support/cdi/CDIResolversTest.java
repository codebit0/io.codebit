package io.codebit.support.cdi;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import javax.enterprise.inject.spi.CDI;
import javax.inject.Provider;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
//ApplicationContext will be loaded from the static inner ContextConfiguration class
@ContextConfiguration(loader=AnnotationConfigContextLoader.class)
public class CDIResolversTest {

	public static class OrderService {
		
	}
	
	@ComponentScan("in.java.support.cdi")
	@Configuration
//	@Profile("production")
    static class ContextConfiguration {
        // this bean will be injected into the OrderServiceTest class
        @Bean
        public OrderService orderService() {
            OrderService orderService = new OrderService();
            return orderService;
        }
    }

	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	@Component
	public static class SomeRequest {
	}

	@Service
	public static class SomeService {

		@Autowired
		Provider<SomeRequest> someRequestProvider;

		SomeRequest doSomething() {
			return someRequestProvider.get();
		}
	}
	
	@Autowired
	ApplicationContext context;

	@Autowired
	BeanFactory beanFactory;
			
    @Autowired
    private OrderService orderService;
    
	@Test
	public void testBeanResolver() throws Exception {
		//context 유무 체크 
		assertNotNull(context);
		//beanFactory 체크 
		assertNotNull(beanFactory);
		//spring autowired 체크 
		assertNotNull(orderService);
		CDI.setCDIProvider(new SpringCDIProvider(context));
		CDI<Object> current = CDI.current();
//		InjectResolvers.register(new SpringBeanResolver(context));
//		InjectResolvers.register(context);
		OrderService orderService2 = current.select(OrderService.class).get();
		assertNotNull(orderService2);
		
		SomeService someService1 = current.select(SomeService.class).get();
		assertNotNull(someService1);
		SomeService someService2 = current.select(SomeService.class).get();

		//prototype test
		SomeRequest someRequest1 = someService1.someRequestProvider.get();
		SomeRequest someRequest2 = someService2.someRequestProvider.get();
		assertNotEquals(someRequest1, someRequest2);
	}
}
