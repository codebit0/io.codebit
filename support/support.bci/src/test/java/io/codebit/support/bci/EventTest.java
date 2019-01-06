package io.codebit.support.bci;

import io.codebit.support.aspect.util.EventAspect;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


import java.util.Arrays;

import static org.junit.Assert.assertNotNull;

public class EventTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
		AspectClassFileTransformer transformer = new AspectClassFileTransformer(EventAspect.class);
//        AspectClassFileTransformer.WeavingContext.WDefinition context =  new AspectClassFileTransformer.WeavingContext.WDefinition();

		transformer.context().options("-verbose");
//        Arrays.asList(transformer), Arrays.asList("io.codebit.support.aspect.test..*");
        LoadtimeInstrument.transform(Arrays.asList(transformer));
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

        EventHandle1 eventHandle1 = new EventHandle1();
        System.out.println(eventHandle1);
    }
}
