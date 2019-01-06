package io.codebit.support.apt;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import io.codebit.support.apt.annotation.ToString;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@SupportedAnnotationTypes({"in.java.support.aspect.ToString"})
public class ToStringAnnotaionProcessor extends BaseAbstractProcessor {

    private Mustache mustache;

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);

        DefaultMustacheFactory mf = new DefaultMustacheFactory();
        mustache = mf.compile("template/ToString.tpl");
    }

    public boolean process(Set<? extends TypeElement> elements, RoundEnvironment env) {

        //ToString annotation
        for (Element type : env.getElementsAnnotatedWith(ToString.class)) {
            //toString 이 정의되어 있는지 확인
            //있으면 생성하지 않고 notice 메시지 출력
//            if(duplicateToString((TypeElement) type)) {
//                messager.printMessage(Kind.NOTE, type+"에 사용자 정의된 toString 이 있습니다.", element);
//                continue;
//            }
            PackageElement packageOf = elementUtils.getPackageOf(type);
            String className = type.getSimpleName().toString();
            String aspectClassName= "ToStringAspect_"+ className ;
            String generatedFile = aspectClassName + ".aj";

            Map<String, Object> context = new HashMap<>();
            context.put("now", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            context.put("package", packageOf);
            context.put("type", type);
            context.put("className", className);
            context.put("aspectClassName", aspectClassName);

            StringBuilder sb = new StringBuilder("\"{");

            //해당 타입의 맴버 정보
            SKIP:
            for (Element element : type.getEnclosedElements()) {

                if(element instanceof ExecutableElement
                        && !element.getModifiers().contains(Modifier.STATIC)
                        && element.getSimpleName().toString().equals("toString")) {
                    messager.printMessage(Kind.NOTE, type+"에 사용자 정의된 toString 이 있습니다.", element);
                    sb = null;
                    continue SKIP;
                }

                if(element instanceof VariableElement) {
                    VariableElement field = (VariableElement) element;
                    //TRANSIENT 가 아닌것만 대상으로 처리
                    if(!field.getModifiers().contains(Modifier.TRANSIENT)) {
                        VariableElement ele = (VariableElement) element;
                        TypeKind kind = ele.asType().getKind();
                        String format;
                        if(kind.isPrimitive()) {
                            format = String.format("\\\"%s\\\": \"+this.%s+\",", field.getSimpleName(), field.getSimpleName());
                        }else {
                            format = String.format("\\\"%s\\\": \\\"\"+this.%s+\"\\\",", field.getSimpleName(), field.getSimpleName());
                        }
                        sb.append(format);
                    }
                }
            }
            if(sb != null) {
                sb.delete(sb.length()-1, sb.length());
                sb.append("}\"");

                context.put("generatedProcessor", this.getClass().getCanonicalName());
                context.put("toString", sb.toString());

                String _package = packageOf.getQualifiedName().toString();
                FileObject file = null;
                try {
                    file = filer.createResource(StandardLocation.SOURCE_OUTPUT, _package, generatedFile, type);
                    mustache.execute(file.openWriter(), context).close();
                }catch(Exception e) {
                    if(!filer.getClass().getCanonicalName().startsWith("org.aspectj"))
                        messager.printMessage(Kind.WARNING, "ToString Aspect file Generator error:"+e.getMessage());
                }
            }
        }
        return true;
    }

    private boolean duplicateToString(TypeElement type) {
        Optional<? extends Element> find = elementUtils.getAllMembers(type).stream()
                .filter((element)->{
                        if(!(element instanceof ExecutableElement))
                            return false;
                        ExecutableElement ele = (ExecutableElement) element;
                        return !ele.getModifiers().contains(Modifier.STATIC)
                                && ele.getSimpleName().toString().equals("toString") && ele.getParameters().size() <= 0 ;
                }).findFirst();
        return find.isPresent();
    }
}
