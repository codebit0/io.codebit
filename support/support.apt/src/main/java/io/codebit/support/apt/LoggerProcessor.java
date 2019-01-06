package io.codebit.support.apt;


import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import io.codebit.support.apt.annotation.Log;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LoggerProcessor extends BaseAbstractProcessor {

    private Mustache mustache;

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);

        DefaultMustacheFactory mf = new DefaultMustacheFactory();
        mustache = mf.compile("template/Logger.tpl");
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element type : roundEnv.getElementsAnnotatedWith(Log.class)) {
            PackageElement packageOf = elementUtils.getPackageOf(type);
            String className = type.getSimpleName().toString();
            String aspectClassName = "LogAspect_" + className;
            String generatedFile = aspectClassName + ".aj";

            Map<String, Object> context = new HashMap<>();
            context.put("now", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            context.put("package", packageOf);
            context.put("className", className);
            context.put("aspectClassName", aspectClassName);
        }
        return true;
    }
}
