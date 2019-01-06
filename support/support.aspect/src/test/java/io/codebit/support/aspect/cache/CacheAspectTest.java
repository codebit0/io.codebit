/**
 *
 */
package io.codebit.support.aspect.cache;

import io.codebit.support.aspect.test.model.Car;
import org.junit.*;

import java.util.Collections;

import static org.junit.Assert.*;


/**
 * @author bootcode
 */
public class CacheAspectTest {

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
     *
     * @throws Exception
     */
    @Test
    public void testCachePut() throws Exception {
        Car car = new Car("람보르기니", true);
        car.options(Collections.emptyList());

    }


    @After // tearDown()
    public void after() throws Exception {
    }
}
