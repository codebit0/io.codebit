package io.codebit.support.apt;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public abstract class BaseAbstractProcessor extends AbstractProcessor {

    private static final Pattern pattern = Pattern.compile("@Generated\\(value=\\\"(?<processor>[^\\\"]+)\\\", date=\\\"(?<date>[^\\\"]+)\\\", comments = \\\"(?<target>[^\\\"]+)\\\"\\)");

    private static boolean CLEAN_UP = false;

    protected Elements elementUtils;
    protected Types typeUtils;

    protected Messager messager;
    protected Filer filer;

    protected Set<File> geneatedFiles(){
        return Collections.emptySet();
    }

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        filer = env.getFiler();
        messager = env.getMessager();
        elementUtils = env.getElementUtils();
        typeUtils = env.getTypeUtils();

        if(!CLEAN_UP) {
            try {
                FileObject resource = filer.getResource(StandardLocation.SOURCE_OUTPUT, "", "__check_location__");
                URI uri = resource.toUri();
                Path outputPath = Paths.get(uri);
                outputPath = outputPath.getParent();

                Files.walk(outputPath)
                        .filter(Files::isRegularFile)
                        .filter(path->
                        {
                            try(Stream<String> lines = Files.lines(path)){
                                Optional<String> match = lines.filter(line -> {
                                    if(line.startsWith("@Generated")) {
                                        Matcher matcher = pattern.matcher(line);
                                        if(matcher.find()) {
//                                        String processor = matcher.group("processor");
//                                        String target = matcher.group("target");
                                            return true;
                                        }
                                    }
                                    return false;
                                }).findFirst();
                                return match.isPresent();
                            }catch (Exception e) {
                            }
                            return false;
                        })
                        .forEach(path->{
                            try {
                                Files.delete(path);
                                messager.printMessage(Diagnostic.Kind.NOTE, "Aspect file clean up: "+path);
                            } catch (IOException e) {
                            }
                        });
                CLEAN_UP = true;
            }catch (Exception e) {
            }
        }
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    protected void error(Element e, String msg, Object... args) {
        messager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args), e);
    }

    protected static String getTypeName(Element e) {
        TypeMirror typeMirror = e.asType();
        String[] split = typeMirror.toString().split("\\.");
        return split.length > 0 ? split[split.length - 1] : null;
    }
}

