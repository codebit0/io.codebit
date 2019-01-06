package io.codebit.support.api.spring.json;

import static org.springframework.util.ClassUtils.isPrimitiveOrWrapper;
import static org.springframework.util.ReflectionUtils.findMethod;
import static org.springframework.util.ReflectionUtils.invokeMethod;
import static org.springframework.util.StringUtils.capitalize;

import java.lang.reflect.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Pattern;

import io.codebit.support.api.spring.annotation.JsonTemplate;
import io.codebit.support.api.spring.annotation.JsonTemplates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;

//import io.codebit.support.util.IAdditionalPropertyCollection;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * DynaFilterHandler. https://github.com/bmsantos/dynafilter-proj code fork
 */
class JsonTemplateHandler implements HandlerMethodReturnValueHandler {

    //로그
    static Logger log = LoggerFactory.getLogger(JsonTemplateHandler.class);

    //TODO 메시지 리소스 분리
    private static final String INPUT_OUTPUT_TYPE_DUP_ERROR = "input type %s 과 output type %s가 동일합니다.";

    public static final String SEPERATOR = "::";

    //parameter exclude filter 패턴식
    private static final Pattern PARAMETER_SPLIT = Pattern.compile("\\s*,\\s*\\$");
    private static final Pattern INDEX_BLOCK =  Pattern.compile("[\\d,: ]+");
    private static final Pattern QUOTE = Pattern.compile("([^,:]+)");
    private static final String FILDER_SEPERATOR = "\\.";

    private final HandlerMethodReturnValueHandler delegate;
    private final ApplicationContext applicationContext;

    //controllerAdvices 에 선언된 JsonTemplate 리스트
    private final Map<Class<?>, JsonTemplate> adviceJsonFilters = new HashMap<Class<?>, JsonTemplate>();

    private LoadingCache<Class<?>, Map<Class<?>, JsonTemplate>> CONTROLLER_CACHE = CacheBuilder.newBuilder().weakKeys()
            .refreshAfterWrite(5, TimeUnit.MINUTES)
            .build(new CacheLoader<Class<?>, Map<Class<?>, JsonTemplate>>() {
                @Override
                public Map<Class<?>, JsonTemplate> load(Class<?> containingClass) {
                    return controllerAnnotations(containingClass);
                }
            });

    private LoadingCache<MethodParameter, Map<Class<?>, JsonTemplate>> METHOD_CACHE = CacheBuilder.newBuilder().weakKeys()
            .refreshAfterWrite(10, TimeUnit.MINUTES)
            .build(new CacheLoader<MethodParameter, Map<Class<?>, JsonTemplate>>() {
                @Override
                public Map<Class<?>, JsonTemplate> load(MethodParameter methodInfo) {
                    return methodAnnotations(methodInfo);
                }
            });

    private LoadingCache<Class<?>, List<Field>> MEMBER_REF_CACHE = CacheBuilder.newBuilder().weakKeys()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build(new CacheLoader<Class<?>, List<Field>>() {
                @Override
                public List<Field> load(Class<?> klass) throws Exception {
                    return getAllFields(klass);
                }
            });

    /**
     * 액션메서드 데이터 DTO
     */
    static class DataBucket {
        /**
         * return Value
         */
        Object value;
        /**
         * return type
         */
        Class<?> type;

        /**
         * current this object
         */
        Object current;
        /**
         * 액션 메서드가 반환하는 최초 데이터
         */
        Object orginal;

        static class Node {
            public static final Node ROOT = new Node();

            int depth = 0;
            StringBuilder node;

            private Node() {
            }

            Node(String path) {
                this.node = new StringBuilder(path);
            }

            public Node resolve(String path) {
                StringBuilder builder;
                if(this.node == null) {
                    builder = new StringBuilder(path.length() + 1);
                } else {
                    builder = new StringBuilder(this.node.toString());
                    builder.append('.');
                }
                builder.append(path);
                Node node = new Node(builder.toString());
                node.depth = this.depth + 1;
                return node;
            }

            public int depth() {
                return this.depth;
            }

            public String toString(){
                if(this.node == null)
                    return "";
                return this.node.toString();
            }
        }

        int depth = 0;

        Node node = Node.ROOT;

        public DataBucket(Object value, Class<?> type, Object current, Object orginal, Node node) {
            this.value = value;
            this.type = type;
            this.current = current;
            this.orginal = orginal;
            this.node = node;
        }

        public Node node() {
            return this.node;
        }

        public String toString(){
            return type.getSimpleName() + " path : "+ this.node();
        }
    }

    static class ExcludePath {
        Pattern pattern;
        int depth;

        public ExcludePath(Pattern pattern, int depth) {
            this.pattern = pattern;
            this.depth = depth;
        }
    }

    static class MethodMeta {
        /**
         * action method infomation
         */
        MethodParameter methodInfo;
        /**
         * spring modelAndView
         */
        ModelAndViewContainer mavContainer;
        /**
         * request
         */
        NativeWebRequest webRequest;

        List<ExcludePath> excludePaths;

        public MethodMeta(final MethodParameter methodInfo,
                          final ModelAndViewContainer mavContainer,
                          final NativeWebRequest webRequest){
            this.methodInfo = methodInfo;
            this.mavContainer = mavContainer;
            this.webRequest = webRequest;

            //파라미터 필터 기능 추가
            String paramFields = webRequest.getParameter("filters");
            if(paramFields != null ){
//                Set<String> parameterFilters = new HashSet<String>();
                this.excludePaths = parameterParse(paramFields);
            }else {
                this.excludePaths = Collections.emptyList();
            }
        }
    }

    static class Template {
        enum ValueType {
            Path,
            Field,
            Method,
            MaV,
            Data;
        }

        final String name;
        Object value;
        ValueType type;

        Template(String name, Object value, ValueType type){
            this.name = name;
            this.value = value;
            this.type = type;
        }
    }

    /**
     * constructor
     * @param delegate
     * @param applicationContext
     */
    public JsonTemplateHandler(final HandlerMethodReturnValueHandler delegate, final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.delegate = delegate;
        //advice 에 있는 JsonView를 가져옴
        adviceAnnotations();
    }

    @Override
    public boolean supportsReturnType(final MethodParameter methodInfo) {
        return delegate.supportsReturnType(methodInfo);
    }

    @Override
    public void handleReturnValue(final Object returnValue, final MethodParameter methodInfo,
                                  final ModelAndViewContainer mavContainer, final NativeWebRequest webRequest) throws Exception {
        long start = System.currentTimeMillis();
        log.debug("handleReturnValue type: {} , support: {}", methodInfo, this.supportsReturnType(methodInfo));

        //TODO hotswap 기능으로 클래스 교체시 cache 데이터가 갱신되지 않음
        //TODO cache 와 비교하여 캐시 무효화 기능 구현 가능 :
        // annotaion이 없을 경우 Empty Map이 return
        final Map<Class<?>, JsonTemplate> jsonViewMap = METHOD_CACHE.get(methodInfo);
//        final Map<Class<?>, JsonTemplate> jsonViewMap = methodAnnotations(methodInfo);
        final Class<?> returnType = methodInfo.getMethod().getReturnType();

        Object newReturnValue = null;
        if (!(returnValue == null || returnType.getClass().equals(void.class))) {
            // 같은 타입을 다시 리턴하는 경우  무한루프를 돌수도 있다. (ex: Path)
            DataBucket dataBucket = new DataBucket(returnValue, returnType, returnValue, returnValue, DataBucket.Node.ROOT);
            MethodMeta meta = new MethodMeta(methodInfo, mavContainer, webRequest);

            newReturnValue = processValue(dataBucket, meta, jsonViewMap);
        }

        // 처리된 결과를 스프링으로 넘겨줌
        // void에 대한 처리를 위하여 위 코드를 수정함
        delegate.handleReturnValue(newReturnValue, methodInfo, mavContainer, webRequest);
        long end = System.currentTimeMillis();
        log.debug("jsonFilter start: {}, end: {} ({} ms)", start, end, end - start);
    }

    //returnValue가 null 경우는 존재 하지 않음
    @SuppressWarnings("unchecked")
    private Object processValue(final DataBucket bucket, final MethodMeta meta, final Map<Class<?>, JsonTemplate> jsonViewMap) {
        if(bucket.value == null)
            return null;

        if (Map.class.isAssignableFrom(bucket.type)) {  //map type 인 경우
            final Map<Object, Object> values = (Map<Object, Object>) bucket.value;
            final Map<Object, Object> data = new HashMap<Object, Object>(values.size());
            for (final Entry<Object, Object> entry : values.entrySet()) {
                String key = entry.getKey().toString();
                DataBucket.Node node = bucket.node().resolve(key);
                if(isExclude(meta, node)) {
                    continue;
                }
                if (entry.getValue() == null) {
                    data.put(key, null);
                    continue;
                }
                DataBucket dataBucket = new DataBucket(entry.getValue(), entry.getValue().getClass(), bucket.value, bucket.orginal, node);
                data.put(key, processValue(dataBucket, meta, jsonViewMap));
            }
            return data;
        } else if (Collection.class.isAssignableFrom(bucket.type) || bucket.type.isArray()) {
            final List<Object> data = new ArrayList<Object>();
            Collection<Object> values;
            if (bucket.type.isArray())
                values = Arrays.asList((Object[]) bucket.value);
            else
                values= ((Collection<Object>) bucket.value);
            int i = 0;
            for (final Object item : values) {
                DataBucket.Node node = bucket.node().resolve("[" + (i++) + "]");
                if(isExclude(meta, node)) {
                    continue;
                }
                if (item == null) {
                    data.add(null);
                    continue;
                }
                DataBucket dataBucket = new DataBucket(item, item.getClass(), bucket.value, bucket.orginal, node);
                data.add( processValue(dataBucket, meta, jsonViewMap));
            }
            return data;
        } else if(isPrimitiveOrWrapper(bucket.type) || String.class.isAssignableFrom(bucket.type)) {
            return bucket.value;
        }
        //class 타입 인 경우
        //final DataBucket bucket, final MethodMeta controllerInfo, final Map<Class<?>, JsonTemplate> jsonViewMap
        return filterData(bucket, meta, jsonViewMap);
    }

    /**
     * Filter data. JsonTemplate 어노테이션 필터 처리
     */
    @SuppressWarnings({
            "unchecked", "rawtypes"
    })
    private Object filterData(final DataBucket bucket, final MethodMeta meta, final Map<Class<?>, JsonTemplate> jsonViewMap) {

        //필드 분석 후 Member까지 구하기
        final Map<String, Object> data = new HashMap<String, Object>();
        final Map<Class, List<Template>> templates = new HashMap<Class, List<Template>>();
        if (jsonViewMap.size() > 0) {
            //반환 타입과 핕터 타입이 일치하는 경우
            String[] fields = null;
            JsonTemplate jsonTemplate = jsonViewMap.get(bucket.type);
            if (jsonTemplate != null) {
                fields = jsonTemplate.fields();
            } else {
                //반환 타입과 핕터 타입이 일치하지 않는 경우
                //반환 값의 클래스 타입가 필터 타입 매칭 시도
                Class<?> returnValueClass = bucket.value.getClass();
                jsonTemplate = jsonViewMap.get(returnValueClass);
                if (jsonTemplate != null) {
                    fields = jsonTemplate.fields();
                    bucket.type = returnValueClass;
                }
            }
            if(fields != null) {
                for(String field :  fields) {
                    templates.computeIfAbsent(bucket.type, (key)-> new ArrayList<Template>()).add(parse(field, bucket, meta));
                }
            }
        }

        // TODO 캐싱
        // 일치하는 JsonView가 없는 경우 가용한 모든 필드, 메서드를 추출 하여 필드로 설정
        if (templates.size() <= 0 ) {
            if (bucket.type.isEnum()) {
                return ((Enum)bucket.value).name();
            } else {
                try {
                    List<Field> _fields = MEMBER_REF_CACHE.get(bucket.type);
                    for(Field field : _fields) {
                        templates.computeIfAbsent(bucket.type, (key)-> new ArrayList<Template>()).add(parse(field));
                    }
                } catch (ExecutionException e) {
//                    e.printStackTrace();
                }
            }
        }

        if(templates.size() > 0) {
            for (List<Template> _templates : templates.values()) {
                for(Template template : _templates) {
                    Object result = bucket.value;
                    DataBucket.Node node = bucket.node().resolve(template.name);
                    //파라미터 파서
                    if(isExclude(meta, node)) {
                        continue;
                    }
                    switch (template.type) {
                        case Path:
                            Object _result = result;
                            for (String value : (String[]) template.value) {
                                _result = obtainValue(_result, value);
                                if (_result == null) {
                                    break;
                                }
                            }
                            result = _result;
                            break;
                        case Field:
                            result = extractField(result, (Field) template.value);
                            break;
                        case Method:
                            result = extractMethod(result, (Method) template.value);
                            break;
                        case MaV:
                            result = ((Function) template.value).apply(new JsonTemplate.Meta());
                            break;
                        case Data:
                            result =  template.value;
                            break;
                    }
                    if (result != null && !isPrimitiveOrWrapper(result.getClass()) && !String.class.isAssignableFrom(result.getClass())) {
                        DataBucket dataBucket = new DataBucket(result, result.getClass(), bucket.value, bucket.orginal, node);
                        result = processValue(dataBucket, meta, jsonViewMap);
                    }
                    if (result != null
                            || (jsonViewMap.get(bucket.type) != null && jsonViewMap.get(bucket.type).includeNulls())) {
                        data.put(template.name, result);
                    }
                }
            }
        }

        return data;
    }

    private static Object extractMethod(Object result, Method method) {
        try {
            Object _result;
            method.setAccessible(true);
            _result = method.invoke(result);
            if(result.getClass().equals(_result.getClass())) {
                //완전 동일한 타입이 나오면 무한 루프를 돌게됨
                throw new RuntimeException(String.format(INPUT_OUTPUT_TYPE_DUP_ERROR, result.getClass(), _result.getClass()));
            }
            return _result;
        } catch (IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }

    private static Object extractField(Object result, Field field) {
        try {
            Object _result;
            field.setAccessible(true);
            _result = field.get(result);
            if(_result != null && result.getClass().equals(_result.getClass())) {
                //완전 동일한 타입이 나오면 무한 루프를 돌게됨
                throw new RuntimeException(String.format(INPUT_OUTPUT_TYPE_DUP_ERROR, result.getClass(), _result.getClass()));
            }
            return _result;
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    /**
     * value 에서 데이터를 추출합니다.
     * @param value 리플렉션 대상 value
     * @param memberName 오브젝트 맴버
     * @return 추출값
     */
    private static Object obtainValue(Object value, String memberName) {
        if (value == null) {
            return null;
        }
        if (value.getClass().equals(Optional.class) && !((Optional) value).isPresent()) {
            return null;
        }
//            BiFunction<Object, String, Object> obtainValue = this::obtainValue;
//            Consumer print = System.out::print;
//            String::copyValueOf
//            () 끝나면 무조건 메소드 만 찾음
//            Pattern methodPattern = Pattern.compile("\\p{Alpha}+\\w?\\(\\s?\\)$");
        if(memberName.endsWith("()")) {
            //메소드인 경우
            Method method = findMethod(value.getClass(), memberName.replace("()", ""));
            if(method == null)
                return null;
            return extractMethod(value, method);
        }else {
            //먼저 필드를 찾아보고 다음 메소드를 찾아봄
            Field field = findField(value.getClass(), memberName);
            if(field != null) {
                return extractField(value, field);
            }
            Method method = findMethod(value.getClass(), memberName);
            if(method != null) {
                return extractMethod(value, method);
            }
            return null;
        }
    }

    private static Template parse(Field field) {
        Template template = new Template(field.getName(), field, Template.ValueType.Field);
        return template;
    }

    private static Template parse(String name, DataBucket bucket, final MethodMeta meta) {
        Class<?> type = bucket.type;
        name = name.trim();
        String fieldName = name;
        String fieldValue = name;
        // 필드의 값을 리플렉션을 통해 가져옴
        if (fieldName.indexOf(":") > 1) {
            // "pic = picture.toAbsolutePath" 같은 필드명 교체에 대응
            String[] _fields = fieldName.split(":", 2);
            fieldName = _fields[0].trim();
            fieldValue = _fields[1].trim();
        }
        String[] names = fieldName.split("\\.");
        if (fieldValue.startsWith("{") && fieldValue.endsWith("}")) {
            Object mav = meta.mavContainer.getModel().get(fieldValue.substring(1, fieldValue.length() - 1));
            if (Function.class.isInstance(mav))
                return new Template(names[0], mav, Template.ValueType.MaV);
            else
                return new Template(names[0], mav, Template.ValueType.Data);
        }else {
            String[] values = fieldValue.split("\\.");
            if(values.length > 0){
                //path 타입으로 후처리
                return new Template(names[0], values, Template.ValueType.Path);
            }else {
                //확정처리
                Member member = null;
                String value = values[0];
                if(values[0].endsWith("()")) {
                    member = findMethod(type, value.replace("()", ""));
                    return new Template(names[0], member, Template.ValueType.Method);
                }else {
                    member = findMethod(type, value);
                    Template template = new Template(names[0], member, Template.ValueType.Method);

                    if(member == null) {
                        member = findField(type, value);
                        template.value = member;
                        template.type = Template.ValueType.Field;
                    }
                    return template;
                }
            }
        }
    }

    private void adviceAnnotations() {
        Map<String, Object> controllerAdvices = applicationContext.getBeansWithAnnotation(ControllerAdvice.class);
        final Map<Class<?>, JsonTemplate> objectMapper = new HashMap<Class<?>, JsonTemplate>();
        // controllerAdvices 필터 맵핑
        for (Object advice : controllerAdvices.values()) {
            Map<Class<?>, JsonTemplate> adviceJsonFilter = controllerAnnotations(advice.getClass());
            if (adviceJsonFilter.size() > 0)
                adviceJsonFilters.putAll(adviceJsonFilter);
        }
    }

    /**
     * controller class에 선언된 JsonTemplate
     *
     * @param controllerClass controller class
     * @return
     */
    private Map<Class<?>, JsonTemplate> controllerAnnotations(Class<?> controllerClass) {
        final Map<Class<?>, JsonTemplate> objectMapper = new HashMap<Class<?>, JsonTemplate>();

        final JsonTemplates jsonTemplates = controllerClass.getAnnotation(JsonTemplates.class);
        if (jsonTemplates != null) {
            for (final JsonTemplate filter : jsonTemplates.value()) {
                objectMapper.put(filter.value(), filter);
            }
        } else {
            final JsonTemplate jsonTemplate = controllerClass.getAnnotation(JsonTemplate.class);
            if (jsonTemplate != null) {
                objectMapper.put(jsonTemplate.value(), jsonTemplate);
            }
        }
        return objectMapper;
    }

    /**
     * controllerAdvice , controller class , controller action method 에 설정된
     *
     * @param methodInfo
     * @return
     */
    private Map<Class<?>, JsonTemplate> methodAnnotations(final MethodParameter methodInfo) {
        final Map<Class<?>, JsonTemplate> jsonViewMap = new HashMap<Class<?>, JsonTemplate>();
        if (this.adviceJsonFilters.size() > 0) {
            jsonViewMap.putAll(this.adviceJsonFilters);
        }
        // Controller 클래스 필터 캐쉬
        try {
            Map<Class<?>, JsonTemplate> classFilters = CONTROLLER_CACHE.get(methodInfo.getContainingClass());
            if (classFilters != null && classFilters.size() > 0)
                jsonViewMap.putAll(classFilters);
        } catch (ExecutionException e) {
            log.error("Controller Class JsonTemplate cache error", e);
            /*Map<Class<?>, JsonTemplate> classFilters = controllerAnnotations(returnType.getContainingClass());
            if (classFilters != null && classFilters.size() > 0)
                jsonViewMap.putAll(classFilters);*/
        }

        final JsonTemplates jsonTemplates = methodInfo.getMethodAnnotation(JsonTemplates.class);
        if (jsonTemplates != null) {
            for (final JsonTemplate filter : jsonTemplates.value()) {
                jsonViewMap.put(filter.value(), filter);
            }
        } else {
            final JsonTemplate jsonTemplate = methodInfo.getMethodAnnotation(JsonTemplate.class);
            if (jsonTemplate != null) {
                jsonViewMap.put(jsonTemplate.value(), jsonTemplate);
            }
        }
        return jsonViewMap;
    }

    /**
     * 파라미터로 들어온 JsonPath 설정 구문은 패턴 매칭식으로 변경
     * @param parameter 인입된 패스 정보
     * @return 컴파일된 패턴 정보
     */
    static List<ExcludePath> parameterParse(String parameter) {
        List<ExcludePath> metas = new ArrayList<>();
        String[] split = PARAMETER_SPLIT.split(parameter);

        for (String path: split) {
            StringBuilder pattern = new StringBuilder();
            String[] nodes = path.trim().split(FILDER_SEPERATOR);
            int empty = 0;
            int depth =0;
//            boolean emptyOpen = false;
            //root 문자 무시
            for(int i = 1 ; i < nodes.length ; i++) {
                if(nodes[i].isEmpty()) {
                    //빈 블록 확인
                    if(empty == 0) {
                        pattern.append(".*");
                        pattern.append(FILDER_SEPERATOR);
                    }
                    empty++;
                    continue;
                }else {
                    int openBraket = nodes[i].indexOf('[');
                    //블록 구문이 있으면
                    if(openBraket >= 0) {
                        int endBraket = nodes[i].indexOf(']', openBraket);
//                        int endBraket = nodes[i].lastIndexOf(']');
                        if(endBraket > 1) {
                            String title = nodes[i].substring(0, openBraket);
                            String braketBlock = nodes[i].substring(openBraket, endBraket + 1);
                            StringBuilder builder = new StringBuilder(nodes[i]);
//                            CharSequence innerBlock = builder.subSequence(1, braketBlcok.length() - 1);
                            //블럭 내부 데이터 조사 : 블럭 내부가 숫자 , : 으로만 이루어지면 배열 탐색
                            //문자 , 로 만 이루어지면 필드 탐색
                            // 혼합되면 오류 json path의 다른 문법은 아직 지원하지 않음
                            String innerBlock = braketBlock.substring(1, braketBlock.length() - 1).trim();
                            if(innerBlock.equals("*")) {
                                braketBlock = "\\[[\\d+]\\]";
                            }else {
                                if(INDEX_BLOCK.matcher(innerBlock).find()) {
                                    innerBlock = innerBlock
                                            .replace(",", "|").replace(':','-');
                                    braketBlock = "\\[["+innerBlock+"]\\]";
                                }else {
                                    innerBlock = QUOTE.matcher(innerBlock)
                                            .replaceAll("\\\\Q$0\\\\E")
                                            .replace(",", "|");
                                    //문자열 블록인 경우 쿼터 처리할까?
                                    braketBlock = "(?:"+innerBlock+")";
                                }
                            }
                            if(openBraket != 0) {
                                braketBlock = "\\."+ braketBlock;
                            }
                            pattern.append(title);
                            pattern.append(braketBlock);
                        }
                    }else {
                        //조회식에 브라켓이 없으면
                        pattern.append( nodes[i] );
                        pattern.append( "(?:\\[\\d+\\])?" );
                    }
                }
                depth++;
                empty = 0;
                pattern.append( FILDER_SEPERATOR );
            }
            pattern.insert(0, "^");
            if(pattern.length() > 2)
                pattern.delete(pattern.length()-2, pattern.length());
            pattern.append('$');
            ExcludePath meta = new ExcludePath(Pattern.compile(pattern.toString()), depth);
            metas.add(meta);
        }
        return metas;
    }

    private static boolean isExclude(MethodMeta meta, DataBucket.Node node) {
        if(meta.excludePaths.size() > 0) {
            for (ExcludePath p : meta.excludePaths) {
                if(node.depth >= p.depth  && p.pattern.matcher(node.toString()).find()) {
                    return true;
                }
            }
        }
        return  false;
    }


    private boolean isCollection(final Object obj) {
        return Collection.class.isAssignableFrom(obj.getClass());
    }

    private boolean isArray(final Object obj) {
        return obj.getClass().isArray();
    }

    private boolean isMap(final Object obj) {
        return Map.class.isAssignableFrom(obj.getClass());
    }

    private static Member findMember(Class<?> type, String name) {
        Member member = findMethod(type, name);
        if(member == null)
            member = findField(type, name);
        return member;
    }

    private static Field findField(Class<?> type, String name) {
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            try {
                Field field = c.getDeclaredField(name);
                if (Modifier.isTransient(field.getModifiers())) {
                    continue;
                }
                return field;
            } catch (NoSuchFieldException e) {
                continue;
            }
        }
        return null;
    }

    private static Method findMethod(Class<?> type, String name) {
        String capitalize = capitalize(name);
        String[] names = new String[] {name, "get" + capitalize, "has" + capitalize, "is" + capitalize, };
        for(String _name: names) {
            for (Class<?> c = type; c != null; c = c.getSuperclass()) {
                try {
                    Method method = c.getDeclaredMethod(_name);
                    Class<?> returnType = method.getReturnType();
                    if (!returnType.equals(void.class) && method.getParameterCount() == 0) {
                        return method;
                    }
                } catch (NoSuchMethodException e) {
                    continue;
                }
            }
        }
        return null;
    }

    /**
     * 상속받은 모든 필드 중 transient 를 제외한 필드
     *
     * @param type
     * @return 상속받은 모든 필드
     */
    private static List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<Field>();
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                //transient 인 필드는 제외
                if (Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                fields.add(field);
            }
        }
        return fields;
    }

    /**
     * 상속받은 모든 메서드
     *
     * @param type class type
     * @return 상속받은 모든 메서드
     */
    private static List<Method> getAllMethods(Class<?> type) {
        List<Method> methods = new ArrayList<Method>();
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            for (Method method : c.getDeclaredMethods()) {
                Class<?> returnType = method.getReturnType();
                if (!returnType.equals(void.class) && method.getParameterCount() == 0) {
                    String name = method.getName();
                    if (name.equals("hashCode") || name.equals("getClass") || name.equals("toString")
                            || name.equals("getDeclaringClass")) {
                        continue;
                    }
                    methods.add(method);
                }
            }
        }
        return methods;
    }
}