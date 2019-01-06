/**
 * 
 */
package io.codebit.support.aspect.valid;

import static org.junit.Assert.*;

import java.util.List;

import javax.validation.ConstraintViolationException;

import io.codebit.support.aspect.test.model.Car;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * @author bootcode
 *
 */
public class VaildAspectTest {

	@BeforeClass
    public static void setUpBeforeClass() throws Exception {
//		AspectClassFileTransformer transformer = new AspectClassFileTransformer(ValidAspect.class);
//		transformer.context().options("-verbose");
//		Instrument.transform(Arrays.asList(transformer), Arrays.asList("io.codebit.support.aspect.test..*"));
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {

    }
    
	@Before 
	public void before() throws Exception {
	}

	/**
	 * 정상 케이스 
	 */
	@Test
	public void testBeforeConstructorArguments() {
		Car car = new Car("farrari", true);
		assertNotNull(car);
	}
	
	/**
	 * 모델명이 null 이어서 valid 실패 테스트 
	 * @throws Exception
	 */
	@Test(expected = ConstraintViolationException.class)
	public void testBeforeConstructorArgumentsNotVaildate() throws Exception {
		try {
			Car car = new Car(null, true);
			assertNotNull(car);
		}catch(ConstraintViolationException e) {
			assertEquals(e.getMessage(), "차량 모델은 필수 입니다.");
			throw e;
		}
		fail("testBeforeConstructorArgumentsNotVaildate fail");
	}

	@Test(expected = ConstraintViolationException.class)
	public void testAssertTrueField() throws Exception {
		Car car = null;
		try {
			car = new Car("farrari", false);
			assertNotNull(car);
		}catch(ConstraintViolationException e) {
			fail("값 입력 시점에는 체크 하지 않아야 함");
		}
		try	{
			boolean registered = car.isRegistered();
			//예외 발생으로 아래 로직은 패스
			assertTrue("무조건 실패합니다.", registered);
		}catch(ConstraintViolationException e) {
			assertEquals(e.getMessage(), "반드시 참(true)이어야 합니다.");
			throw e;
		}
		fail("testAssertTrueField fail");
	}
	
	@Test(expected = ConstraintViolationException.class)
	public void testVaildReturnValueMethod() throws Exception {
		Car car = new Car("farrari", false);
		List<String> options = car.options();
		//valid case
		assertNotNull(options);
		assertTrue(options.size() >= 2 && options.size() <= 5);
		fail("testVaildReturnValueMethod fail");
	}

	@After // tearDown()
	public void after() throws Exception {
	}
}
