package io.codebit.support.apt;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import io.codebit.support.apt.annotation.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.lang.annotation.Annotation;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

//https://github.com/vert-x3/vertx-codegen/blob/master/src/main/java/io/vertx/codegen/CodeGenProcessor.java
//https://medium.com/@jason_kim/annotation-processing-101-%EB%B2%88%EC%97%AD-be333c7b913
//http://www.baeldung.com/java-annotation-processing-builder
//https://medium.com/@iammert/annotation-processing-dont-repeat-yourself-generate-your-code-8425e60c6657
//https://stackoverflow.com/questions/5404851/generate-aspect-from-a-java-class
@SupportedAnnotationTypes({
        "maio.codebit.apt.Getter",
        "maio.codebit.apt.Setter",
        "maio.codebit.apt.annotation.Date",
        "maio.codebit.apt.Value"
})
public class PropertyAnnotaionProcessor extends BaseAbstractProcessor {

    /**
     * getter setter 생성 정보 클래스
     */
    class Target {
        TypeElement type;

        boolean isValueType = false;

        Set<Entity> getters = new HashSet<>();
        Set<Entity> setters = new HashSet<>();

        Target(TypeElement type) {
            this.type = type;
        }
    }

    abstract class Entity {
        Getter getter;
        Setter setter;
        VariableElement field;
        AccessLevel level;

        Entity(VariableElement field) {
            this.field = field;
        }

        public String comment(){
            String comment = elementUtils.getDocComment(this.field);
            if(comment == null || comment.isEmpty())
                return "";
            String[] strings = comment.split("\n");
            StringBuilder sb = new StringBuilder(comment.length()+ strings.length+ 10);
            sb.append("/**\n");
            for (String s: strings) {
                sb.append("\t *");
                sb.append(s);
                sb.append("\n");
            }
            sb.append("\t */");
            return sb.toString();
        }

        public VariableElement element(){
            return this.field;
        }

        public String modifier() {
            if(level != null) {
                return level.toString();
            }
            return AccessLevel.PUBLIC.toString();
        }

        public abstract String methodName();

        @Override
        public int hashCode(){
            return this.field.hashCode();
        }

        @Override
        public boolean equals(Object obj){
            return obj != null && (obj instanceof Entity) && this.field.equals(((Entity) obj).field);
        }

        <R extends Annotation> R getConfig(VariableElement field, Class<R> configType ) {
            R config = field.getAnnotation(configType);
            if( config == null) {
                //type accessors
                config = field.getEnclosingElement().getAnnotation(configType);
                //package
                if(config == null) {
                    PackageElement packageOf = elementUtils.getPackageOf(field.getEnclosingElement());
                    config = packageConfig(packageOf, configType);
                }
            }
            return config;
        }

        <R extends Annotation> R packageConfig(PackageElement packageElement, Class<R> configType ) {
            R config = packageElement.getAnnotation(configType);
            if(config != null)
                return config;
            String name = packageElement.getQualifiedName().toString();
            int lastIndex = name.lastIndexOf('.');
            if(lastIndex < 0)
                return null;
            name = name.substring(0, lastIndex);
            packageElement = elementUtils.getPackageElement(name);
            return packageConfig(packageElement, configType);
        }
    }

    /**
     * Getter Entity
     */
    class GetterEntity extends  Entity{
        Getter.Config config;
        private GetterEntity(VariableElement element){
            super(element);
            this.getter = field.getAnnotation(Getter.class);
            if(this.getter != null)
                this.level = this.getter.value();
            this.config = getConfig(field, Getter.Config.class);
         }

        public String methodName(){
            boolean fluent = (this.config == null)? false: this.config.fluent();
            String name = this.field.getSimpleName().toString();
            if(!fluent) {
                String prefix ="get";
                if(this.field.asType().getKind().equals(TypeKind.BOOLEAN)) {
                    prefix = "is";
                }
                name = prefix+Character.toUpperCase(name.charAt(0)) + name.substring(1);
            }
            return name;
        }
    }

    /**
     *  Setter Entity
     */
    class SetterEntity extends  Entity{
        Setter.Config config;
        SetterEntity(VariableElement element){
            super(element);
            this.setter = field.getAnnotation(Setter.class);
            if(this.setter != null)
                this.level = this.setter.value();
            this.config = getConfig(field, Setter.Config.class);
        }

        public String methodName(){
            boolean fluent = (this.config == null)? false: this.config.fluent();
            String name = this.field.getSimpleName().toString();
            if(!fluent) {
                String prefix = "set";
                if(this.field.asType().getKind().equals(TypeKind.BOOLEAN)) {
                    prefix = "is";
                }
                name = prefix+Character.toUpperCase(name.charAt(0)) + name.substring(1);
            }
            return name;
        }
    }

    private Mustache mustache;

    private Function<TypeElement, Target> targetFunction = type -> new Target(type);
    private Function<String,String> commaTrimFunction = s -> s.substring(0, s.length()-2);

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
//        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
//        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
//        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
////        fileManager.getFileForInput(StandardLocation.SOURCE_PATH)
//        Iterable<? extends JavaFileObject> comUnit =  fileManager.getJavaFileObjects("Demo.java");
//        System.out.println(comUnit);
//        Trees.instance(evn);
        //http://trimou.org/doc/2.2.0.Final/trimou-doc.html#_simple_lambdas
        DefaultMustacheFactory mf = new DefaultMustacheFactory();
        mustache = mf.compile("template/Property.tpl");
    }

    public boolean process(Set<? extends TypeElement> elements, RoundEnvironment env) {
        //summary 용 hashMap
        HashMap<TypeElement, Target> targets = new HashMap<TypeElement, Target>();
        //Value annotation
        //equals hashcode 구현 추가
        for (Element type : env.getElementsAnnotatedWith(Value.class)) {
            //final 이 아닌 필드가 있으면 에러 생성
            if(!type.getModifiers().contains(Modifier.FINAL)) {
                messager.printMessage(Kind.ERROR, "@Value annotation은 final class type 이어야 합니다.", type);
                break;
            }
            for (Element element : type.getEnclosedElements()) {
                //get만 대상임
                if(element instanceof VariableElement) {
                    VariableElement field = (VariableElement) element;
                    if(!field.getModifiers().contains(Modifier.FINAL)) {
                        messager.printMessage(Kind.ERROR, "@Value annotation의 모든 필드는 final 이어야 합니다.", field);
                        break;
                    }
                    //getter만 구현
                    insertGetters(targets, (TypeElement) type, field);
                }
            }
            Target target = targets.computeIfAbsent((TypeElement) type, targetFunction);
            target.isValueType = true;
        }

        //Data annotation
        for (Element type : env.getElementsAnnotatedWith(Data.class)) {
            for (Element element : type.getEnclosedElements()) {
                //get set 모두 대상임
                if(element instanceof VariableElement) {
                    VariableElement field = (VariableElement) element;
                    insertGetters(targets, (TypeElement) type, field);
                    insertSetters(targets, (TypeElement) type, field);
                }
            }
        }

        //Setter
        for (Element element : env.getElementsAnnotatedWith(Setter.class)) {
            ElementKind kind = element.getKind();
            if (kind == ElementKind.CLASS) {
                List<VariableElement> fields = ElementFilter.fieldsIn(element.getEnclosedElements());
                for(VariableElement field : fields) {
                    insertSetters(targets, (TypeElement) element, field);
                }
            }else{
                TypeElement type = (TypeElement) element.getEnclosingElement();
                VariableElement field = (VariableElement) element;
                insertSetters(targets, type, field);
            }
        }

        //Getter
        for (Element element : env.getElementsAnnotatedWith(Getter.class)) {
            ElementKind kind = element.getKind();
            if (kind == ElementKind.CLASS) {
                List<VariableElement> fields = ElementFilter.fieldsIn(element.getEnclosedElements());
                for(VariableElement field : fields) {
                    insertGetters(targets, (TypeElement) element, field);
                }
            }else if (kind == ElementKind.FIELD) {
                TypeElement type = (TypeElement) element.getEnclosingElement();
                VariableElement field = (VariableElement) element;
                insertGetters(targets, type, field);
            }
        }

        if(targets.size() > 0) {
            try{
                for (Map.Entry<TypeElement, Target> targetEntry : targets.entrySet()) {
                    TypeElement type = targetEntry.getKey();
                    PackageElement packageOf = elementUtils.getPackageOf(type);
                    String packageName = packageOf.getQualifiedName().toString();
                    Target target = targetEntry.getValue();
                    String classNameQualified = type.getQualifiedName().toString().replace(packageName+".", "");
                    String className = classNameQualified.replace('.','_');
                    String aspectClassName= "Property_"+ className + "_Aspect";
                    String generatedFile = aspectClassName + ".aj";

                    if(target.getters.size() == 0 && target.setters.size() == 0) {
                        //aop 파일 삭제
                    }else {
                        //이미 선언된 동일 메서드는 생성 제외
                        List<Element> members = (List<Element>) elementUtils.getAllMembers(type);
                        members = members.stream().filter((element)->{
                            return element instanceof ExecutableElement;
                        }).collect(Collectors.toList());

                        Map<String, Object> context = new HashMap<>();
                        context.put("now", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
                        context.put("generatedProcessor", this.getClass().getCanonicalName());
                        context.put("value", target.isValueType);
                        context.put("package", packageOf);
                        context.put("aspectClassName", aspectClassName);
                        context.put("className", className);
                        context.put("classNameQualified", classNameQualified);
                        context.put("getters", target.getters);
                        context.put("setters", target.setters);
                        context.put("commaTrim", commaTrimFunction);

                        FileObject file = null;
                        try {
                            file = filer.createResource(StandardLocation.SOURCE_OUTPUT, packageName, generatedFile, type);
//                            super.addGeneratedFile(type, file.toUri());
                            mustache.execute(file.openWriter(), context).close();
                        }catch(Exception e) {
                            if(!filer.getClass().getCanonicalName().startsWith("org.aspectj"))
                                messager.printMessage(Kind.WARNING, "Property Aspect file Generator error: "+e.getMessage());
                        }
                    }
                }
            }catch (Exception e) {
                messager.printMessage(Kind.ERROR, "Property Generator error: "+e.getMessage());
            }
        }
        return true;
    }

    private void insertGetters(HashMap<TypeElement, Target> targets, TypeElement type, VariableElement field) {
        Entity entity = new GetterEntity(field);
        //이미 선언된 동일 메서드는 생성 제외
        if(!checkGetterMethod(type, entity)) {
            Target target = targets.computeIfAbsent(type, targetFunction);
            target.getters.add(entity);
        }else {
            messager.printMessage(Kind.NOTE, "duplicate getter method name", field);
        }
    }

    private void insertSetters(HashMap<TypeElement, Target> targets, TypeElement type, VariableElement field) {
        //final 인건 제외
        if(!field.getModifiers().contains(Modifier.FINAL)){
            Entity entity = new SetterEntity(field);
            if(!checkSetterMethod(type, entity)) {
                targets.computeIfAbsent(type, targetFunction).setters.add(entity);
            }else {
                messager.printMessage(Kind.NOTE, "duplicate setter method name", field);
            }
        }
    }

    /**
     * 이미 생성된 동일 메서드가 있는지 확인
     * @param type class type
     * @param entity 생성 요청 정보
     * @return 동일 메서드가 있으면 ture, 그렇지 않으면 false
     */
    private boolean checkGetterMethod(TypeElement type, Entity entity) {
        Optional<? extends Element> find = elementUtils.getAllMembers(type).stream().filter((element)->{
                    if(!(element instanceof ExecutableElement))
                        return false;
                    ExecutableElement ele = (ExecutableElement) element;
                    return entity.methodName().equals(ele.getSimpleName()) && ele.getParameters().size() == 0;
                }).findFirst();
        return find.isPresent();
    }

    private boolean checkSetterMethod(TypeElement type, Entity entity) {
        Optional<? extends Element> find = elementUtils.getAllMembers(type).stream().filter((element)->{
            if(!(element instanceof ExecutableElement))
                return false;
            ExecutableElement ele = (ExecutableElement) element;
            if(ele.getParameters().size() != 1)
                return false;
            return entity.methodName().equals(ele.getSimpleName()) && ele.getParameters().get(0).asType().equals(entity.element().asType());
        }).findFirst();
        return find.isPresent();
    }
}
