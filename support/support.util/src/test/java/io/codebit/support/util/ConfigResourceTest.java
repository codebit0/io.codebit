package io.codebit.support.util;

import io.codebit.support.util.Config;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by bootcode on 2018-07-11.
 */
public class ConfigResourceTest {

    @Test
    public void testConstrutor() {
        Config resource = Config.of("classpath://application.yaml", "file://application2.yaml");
        assertNotNull(resource);
    }

    @Test
    public void testGetOrDefault() throws Exception {
        Config resource = Config.of("classpath://application2.yaml");
        assertNotNull(resource);
        String value = resource.getString("address.line1", "default");
        assertEquals(value, "My Address Line 1");
        value = resource.getString("address.state", "default");
        assertEquals(value, "none");
    }
}