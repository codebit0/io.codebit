package io.codebit.support.apt;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GenerateProcessFactory extends AbstractProcessor {

    private final List<Processor> processors;

    public GenerateProcessFactory(){
        this.processors = Arrays.asList (
            new PropertyAnnotaionProcessor(),
                new ToStringAnnotaionProcessor(),
                new LoggerProcessor()
        );
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        for(Processor processor : processors){
            processor.init(env);
        }
    }

    public Set<String> getSupportedAnnotationTypes() {
        return processors.stream().flatMap(p->p.getSupportedAnnotationTypes().stream()).collect(Collectors.toSet());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for(Processor processor : processors){
            Set<TypeElement> elements = annotations.stream().filter(annotation -> {
                String name = annotation.getQualifiedName().toString();
                return processor.getSupportedAnnotationTypes().contains(name);
            }).collect(Collectors.toSet());
            if(elements.size() > 0) {
                processor.process(elements, roundEnv);
            }
        }
        return true;
    }
}
