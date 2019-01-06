package io.codebit.support.api.spring.json;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Created by bootcode on 2018-08-27.
 */
public class JsonTemplateHandlerTest {

    @Test
    public void testParameterParse() throws Exception {
        String parameter = "$.store..price, $.[1].book , $..book[1:2].isbn";
        List<JsonViewHandler.ExcludePath> excludePaths =
                JsonViewHandler.parameterParse(parameter);
        Assert.assertNotNull(excludePaths);
    }

    private boolean isExclude(String node, List<JsonViewHandler.ExcludePath> paths ) {
        for(JsonViewHandler.ExcludePath p : paths) {
            if(p.pattern.matcher(node.toString()).find()) {
                return true;
            }
        }
        return false;
    }
}