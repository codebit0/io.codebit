package io.codebit.support.api.spring.json;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;


//  ObjectFactory  를 바꾸는 예재
//http://stackoverflow.com/questions/10420040/jackson-2-0-with-spring-3-1
// json filter
//http://madnix.tistory.com/archive/20140517

@Component
public class JsonTemplateFactory implements InitializingBean {
//    @Inject
//    private RequestMappingHandlerAdapter adapter;

    @Inject
    private ApplicationContext applicationContext;

    @Override
    public void afterPropertiesSet() throws Exception {
        decorateHandlers(applicationContext);
    }

    public static void install(ApplicationContext applicationContext) {
        decorateHandlers(applicationContext);
    }

    private static void decorateHandlers(ApplicationContext applicationContext) {
        RequestMappingHandlerAdapter adapter = applicationContext.getBean(RequestMappingHandlerAdapter.class);
        final List<HandlerMethodReturnValueHandler> handlers = new ArrayList<HandlerMethodReturnValueHandler>();
        for (final HandlerMethodReturnValueHandler handler : adapter.getReturnValueHandlers()) {
            if (handler instanceof RequestResponseBodyMethodProcessor) {
                handlers.add(new JsonViewHandler(handler, applicationContext));
            } else {
                handlers.add(handler);
            }
        }
        adapter.setReturnValueHandlers(handlers);
    }
}
