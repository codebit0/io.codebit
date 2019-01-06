package io.codebit.support.aspect.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import in.java.support.aspect.cache.context.*;
import io.codebit.support.cache.ToStringGeneratedCacheKey;
import io.codebit.support.aspect.cache.context.*;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import javax.cache.Cache;
import javax.cache.annotation.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @see <a href="https://static.javadoc.io/javax.cache/cache-api/1.0.0/javax/cache/annotation/cacheRemoveAll.html">cacheRemoveAll</a>
 * CacheRemoveAll로 주석 된 메소드가 호출되면 지정된 캐시의 모든 요소가 Cache.findRemoveAllCache () 메소드를 통해 제거됩니다.
 * 기본 동작은 주석 된 메소드가 호출 된 후 Cache.findRemoveAllCache ()을 호출하는 것입니다.
 * afterInvocation ()을 false로 설정하면 주석이 달린 메소드가 호출되기 전에 Cache.findRemoveAllCache ()이 호출됩니다.
 * Exception Handling은 afterInvocation()이 true 인 경우에만 사용됩니다.
 * evictFor () 및 noEvictFor()가 모두 비어 있으면 모든 예외가 removeAll을 금지합니다.
 * evictFor 가 지정되고 noEvictFor 가 지정되지 않으면 evictFor 목록에 대한 instanceof 해당하는 예외 만 removeAll을 호출합니다.
 * noEvictFor 가 지정되고 evictFor 가 지정되지 않으면 noEvictFor 목록과 모두 instanceof 되지 않는 예외일때만 removeAll을 호출합니다.
 * evictFor 및 noEvictFor 가 모두 지정되면 evictFor 목록에 대해 instanceof 일치하는 예외가 있는 경우
 * noEvictFor 목록에 대한 instanceof 매치와 모두 일치하지 않은 경우 removeAll이 발생합니다.
 * @see <a href="https://static.javadoc.io/javax.cache/cache-api/1.0.0/javax/cache/annotation/CacheResult.html">CacheResult</a>
 * 캐시에 값이 있으면 반환되며 CacheResult가 선언된 메소드는 실제로 실행되지 않습니다.
 * 캐시 값이 없는 경우, 메소드가 실행되며 반환 값은 생성 된 키를 사용해 캐쉬에 저장됩니다.
 * 메서드에서 발생한 예외는 기본적으로 캐시되지 않습니다. 예외 캐싱은 exceptionCacheName()을 지정하여 활성화 할 수 있습니다.
 * 예외 캐쉬가 지정되어 있는 경우는, 메소드를 호출하기 전에 exceptionCacheName 을 체크해, 캐쉬 된 예외가 발견되었을 경우는 재 throw됩니다.
 * cachedExceptions() 및 nonCachedExceptions() 속성을 사용하여 캐시 된 예외와 그렇지 않은 예외를 제어 할 수 있습니다.
 * <p>
 * 메소드를 항상 호출하고 항상 결과 세트를 캐시하려면 skipGet()을 true로 설정하십시오. 그러면  Cache.get(Object) 호출이 비활성화됩니다.
 * exceptionCacheName() 역시 비활성화 됩니다.
 * 이 기능은 캐시 할 개체를 만들거나 업데이트하는 메서드에 유용합니다.
 * String 및 int 매개 변수에서 생성 된 키를 사용하여 Domain 객체를 캐싱하는 예제입니다.
 * cacheName()을 지정하지 않으면 캐시 이름이 "my.app.DomainDao.getDomain(java.lang.String, int)"이 생성됩니다.
 * <p>
 * 예외 캐싱이 exceptionCacheName ()을 통해 활성화 된 경우 throw 된 예외가 캐시되는지 여부를 결정하기 위해 다음 규칙이 사용됩니다.
 * cachedExceptions () 및 nonCachedExceptions ()가 모두 비어 있으면 모든 예외가 캐시됩니다
 * cachedExceptions ()가 지정되고 nonCachedExceptions )가 지정되지 않은 경우 cachedExceptions 목록에 대한 instanceof 검사를 통과 한 예외 만 캐시됩니다.
 * nonCachedExceptions ()가 지정되고 cachedExceptions ()가 지정되지 않으면 nonCachedExceptions 목록에 대한 instanceof 검사를 통과하지 않은 모든 예외가 캐시됩니다.
 * cachedExceptions 및 nonCachedExceptions  모두 지정된 경우 cachedExceptions 목록에 대해 instanceof 검사를 통과하지만 nonCachedExceptions 목록에 대한 instanceof 검사를 통과하지 않는 예외는 캐시됩니다
 * @see <a href="https://dzone.com/refcardz/java-caching?chapter=5">caching</a>
 * jsr 107
 */
@Aspect("issingleton()")
public class CacheProcessor {

    private static LoadingCache<CacheRemoveAllMethodDetail, Cache<Object, Object>> REMOVE_ALL_CACHE = CacheBuilder.newBuilder().weakKeys()
            .refreshAfterWrite(5, TimeUnit.MINUTES)
            .build(new CacheLoader<CacheRemoveAllMethodDetail, Cache<Object, Object>>() {
                @Override
                public Cache<Object, Object> load(CacheRemoveAllMethodDetail detail) throws Exception {
                    return findCache(detail);
                }
            });

    private static LoadingCache<CacheRemoveMethodDetail, Cache<Object, Object>> REMOVE_CACHE = CacheBuilder.newBuilder().weakKeys()
            .refreshAfterWrite(5, TimeUnit.MINUTES)
            .build(new CacheLoader<CacheRemoveMethodDetail, Cache<Object, Object>>() {
                @Override
                public Cache<Object, Object> load(CacheRemoveMethodDetail detail) throws Exception {
                    return findCache(detail);
                }
            });

    private static LoadingCache<CachePutMethodDetail, Cache<Object, Object>> PUT_CACHE = CacheBuilder.newBuilder().weakKeys()
            .refreshAfterWrite(5, TimeUnit.MINUTES)
            .build(new CacheLoader<CachePutMethodDetail, Cache<Object, Object>>() {
                @Override
                public Cache<Object, Object> load(CachePutMethodDetail detail) throws Exception {
                    return findCache(detail);
                }
            });

    private static LoadingCache<CacheResultMethodDetail, Cache<Object, Object>> RESULT_CACHE = CacheBuilder.newBuilder().weakKeys()
            .refreshAfterWrite(5, TimeUnit.MINUTES)
            .build(new CacheLoader<CacheResultMethodDetail, Cache<Object, Object>>() {
                @Override
                public Cache<Object, Object> load(CacheResultMethodDetail detail) throws Exception {
                    return findCache(detail);
                }
            });


    @Pointcut("execution(* *(..))")
    public void cacheMethod() {
    }

    /**
     * @{@link CacheResult} annotation 가 선언된 execution 메소드.
     * 캐시에서 키 (메서드 매개 변수)를 검색하고, 값을 찾을 수없는 경우 메서드를 호출하고 이후 호출에 대해 동일한 메서드를 캐시합니다.
     */
    @Pointcut("@annotation(javax.cache.annotation.CacheResult)")
    public void cacheResultMethod() {
    }

    /**
     * @{@link CachePut} annotation 가 선언된 execution 메소드.
     * 메소드 매개 변수에 key와 value를 가진 캐시 put을 실행합니다.
     */
    @Pointcut("@annotation(javax.cache.annotation.CachePut)")
    public void cachePutMethod() {
    }

    /**
     * @{@link CacheRemove} annotation 가 선언된 execution 메소드.
     * method 매개 변수에 지정된 키를 사용하여 캐시 항목을 제거합니다.
     */
    @Pointcut("@annotation(javax.cache.annotation.CacheRemove)")
    public void cacheRemoveMethod() {
    }


    /**
     * @{@link CacheRemoveAll} annotation 가 선언된 execution 메소드.
     * 모든 캐시 항목을 제거합니다.
     */
    @Pointcut("@annotation(javax.cache.annotation.CacheRemoveAll)")
    public void cacheRemoveAllMethod() {
    }

    /**
     * CacheResult annotaion GeneratedCacheKey에 의해 생성된 key를 생성 후 메소드 실행 전에 Cache.get(key) 를 호출한 후 반환합니다.
     *
     * @param point 조인 포인트
     * @return 메서드 반환값
     * @throws Throwable 예외
     */
    @Around("cacheMethod() && (cacheResultMethod() || cachePutMethod() || cacheRemoveMethod() || cacheRemoveAllMethod())")
    public Object cache(final ProceedingJoinPoint point) throws Throwable {
        Optional<Object> result = Optional.empty();
        Exception throwable = null;

        try {
            MethodSignature methodSignature = (MethodSignature) point.getSignature();
            Method method = methodSignature.getMethod();

            //cache default 설정을 찾음
            final CacheDefaults cacheDefaults = findCacheDefaults(method);
            //캐쉬 전채 삭제
            final CacheRemoveAll cacheRemoveAll = method.getAnnotation(CacheRemoveAll.class);
            //캐쉬 삭제
            final CacheRemove cacheRemove = method.getAnnotation(CacheRemove.class);
            //캐쉬 데이터 입력
            final CachePut cachePut = method.getAnnotation(CachePut.class);
            //캐쉬 데이터를 반환
            final CacheResult cacheResult = method.getAnnotation(CacheResult.class);

            CacheRemoveAllMethodDetail cacheRemoveAllMethodDetail = null;
            CacheRemoveMethodDetail cacheRemoveMethodDetail = null;
            CachePutMethodDetail cachePutMethodDetail = null;
            CacheResultMethodDetail cacheResultMethodDetail = null;

            //캐시 처리부터 먼저 추후 메서드 호출
            List<Annotation> afterInvocations = new ArrayList<>();
            // 메서드 호출 후 캐시처리
            List<Annotation> beforeInvocations = new ArrayList<>();

            if (cacheRemoveAll != null) {
                if (cacheRemoveAll.afterInvocation()) {
                    afterInvocations.add(cacheRemoveAll);
                } else {
                    beforeInvocations.add(cacheRemoveAll);
                }
                cacheRemoveAllMethodDetail = new CacheRemoveAllMethodDetail(point, cacheRemoveAll, cacheDefaults);
            }
            if (cacheRemove != null) {
                if(cacheRemoveAll != null && cacheRemoveAll.cacheName().equals(cacheRemove.cacheName())) {
                    //skip
                }else {
                    if (cacheRemove.afterInvocation()) {
                        afterInvocations.add(cacheRemove);
                    } else {
                        beforeInvocations.add(cacheRemove);
                    }
                    cacheRemoveMethodDetail = new CacheRemoveMethodDetail(point, cacheRemove, cacheDefaults);
                }
            }
            if (cachePut != null) {
                if (cachePut.afterInvocation()) {
                    afterInvocations.add(cachePut);
                } else {
                    beforeInvocations.add(cachePut);
                }
                cachePutMethodDetail = new CachePutMethodDetail(point, cachePut, cacheDefaults);
            }

            //캐시 선처리 후 메서드 호출
            for (Annotation annotation : beforeInvocations) {
                if (annotation instanceof CacheRemoveAll) {
                    Cache<Object, Object> cache = REMOVE_ALL_CACHE.get(cacheRemoveAllMethodDetail);
                    //무조건 삭제
                    cache.removeAll();
                } else if (annotation instanceof CacheRemove) {
                    Cache<Object, Object> cache = REMOVE_CACHE.get(cacheRemoveMethodDetail);
                    Class<? extends CacheKeyGenerator> keyGenerator = cacheRemoveMethodDetail.getCacheKeyGenerator();
                    CacheKeyGenerator cacheKeyGenerator = keyGenerator.newInstance();
                    GeneratedCacheKey generatedCacheKey = cacheKeyGenerator.generateCacheKey((CacheKeyInvocationContext<? extends Annotation>) cacheRemoveMethodDetail);
                    Object key;
                    if (generatedCacheKey instanceof ToStringGeneratedCacheKey) {
                        key = generatedCacheKey.toString();
                    } else {
                        key = generatedCacheKey.hashCode();
                    }
                    cache.remove(key);
                } else if (annotation instanceof CachePut) {
                    Cache<Object, Object> cache = PUT_CACHE.get(cachePutMethodDetail);
                    Class<? extends CacheKeyGenerator> keyGenerator = cachePutMethodDetail.getCacheKeyGenerator();
                    CacheKeyGenerator cacheKeyGenerator = keyGenerator.newInstance();
                    GeneratedCacheKey generatedCacheKey = cacheKeyGenerator.generateCacheKey((CacheKeyInvocationContext<? extends Annotation>) cachePutMethodDetail);
                    Object key;
                    if (generatedCacheKey instanceof ToStringGeneratedCacheKey) {
                        key = generatedCacheKey.toString();
                    } else {
                        key = generatedCacheKey.hashCode();
                    }
                    CacheInvocationParameter valueParameter = cachePutMethodDetail.getValueParameter();
                    if (valueParameter != null) {
                        Object value = valueParameter.getValue();
                        if (key != null && value != null)
                            cache.put(key, value);
                    }
                }
            }

            try {
                if (cacheResult != null) {
                    cacheResultMethodDetail = new CacheResultMethodDetail(point, cacheResult, cacheDefaults);
                    Cache<Object,Object> cache = RESULT_CACHE.get(cacheResultMethodDetail);
                    Class<? extends CacheKeyGenerator> keyGenerator = cacheResultMethodDetail.getCacheKeyGenerator();
                    CacheKeyGenerator cacheKeyGenerator = keyGenerator.newInstance();
                    GeneratedCacheKey generatedCacheKey = cacheKeyGenerator.generateCacheKey((CacheKeyInvocationContext<? extends Annotation>) cacheResultMethodDetail);

                    Object key = null;
                    if (generatedCacheKey instanceof ToStringGeneratedCacheKey) {
                        key = generatedCacheKey.toString();
                    } else {
                        key = generatedCacheKey.hashCode();
                    }

                    if (!cacheResult.skipGet()) {
                        //skipGet 이 false 인 경우 (default value) : 캐시를 값이 있으면 캐시값 반환, 캐시값없으면 excaption cache 탐색, 이도 저도 없으면 메서드 호출 후 결과값 저장
                        if(cache.containsKey(key)) {
                            Object value = cache.get(key);
                            result = Optional.ofNullable(value);
                        } else {
                            String exceptionCacheName = cacheResult.exceptionCacheName();
                            if (exceptionCacheName != null && !exceptionCacheName.isEmpty()) {
                                Class<? extends CacheResolverFactory> resolverFactoryType = cacheResultMethodDetail.getCacheAnnotation().cacheResolverFactory();
                                CacheResolverFactory cacheResolverFactory = resolverFactoryType.newInstance();
                                CacheResolver exceptionCacheResolver = cacheResolverFactory.getExceptionCacheResolver(cacheResultMethodDetail);
                                Cache<Object, Throwable> exceptionCache = exceptionCacheResolver.resolveCache(cacheResultMethodDetail);
                                if(exceptionCache.containsKey(exceptionCacheName)) {
                                    throwable = (Exception) exceptionCache.get(exceptionCacheName);
                                }
                            }
                        }
                    } else {
                        //skipGet 이 true이면 캐시에서 값을 가져오지 않고 항상 메서드를 호출한다.
                        try {
                            Object value = point.proceed();
                            result = Optional.ofNullable(value);
                            cache.put(key, value);
                        } catch (Exception e) {
                            throwable = e;
                            String exceptionCacheName = cacheResult.exceptionCacheName();
                            if (exceptionCacheName != null && !exceptionCacheName.isEmpty()) {
                                Class<? extends CacheResolverFactory> resolverFactoryType = cacheResultMethodDetail.getCacheAnnotation().cacheResolverFactory();
                                CacheResolverFactory cacheResolverFactory = resolverFactoryType.newInstance();
                                CacheResolver exceptionCacheResolver = cacheResolverFactory.getExceptionCacheResolver(cacheResultMethodDetail);
                                Cache<Object, Throwable> exceptionCache = exceptionCacheResolver.resolveCache(cacheResultMethodDetail);

                                Class<? extends Throwable>[] cachedExceptions = cacheResult.cachedExceptions();
                                Class<? extends Throwable>[] nonCachedExceptions = cacheResult.nonCachedExceptions();
                                Class<? extends Exception> eClass = throwable.getClass();
                                if (nonCachedExceptions.length > 0 && cachedExceptions.length > 0
                                        && (Arrays.stream(cachedExceptions).anyMatch(eClass::isAssignableFrom)
                                        && !Arrays.stream(nonCachedExceptions).anyMatch(eClass::isAssignableFrom))) {
                                    exceptionCache.put(key, throwable);
                                } else if (cachedExceptions.length > 0 && Arrays.stream(cachedExceptions).anyMatch(eClass::isAssignableFrom)) {
                                    //true 이면
                                    exceptionCache.put(key, throwable);
                                } else if (nonCachedExceptions.length > 0 && !Arrays.stream(nonCachedExceptions).anyMatch(eClass::isAssignableFrom)) {
                                    exceptionCache.put(key, throwable);
                                }
                            }
                        }
                    }
                } else {
                    result = Optional.ofNullable(point.proceed());
                }
            } catch (Exception e) {
                throwable = e;
            }
            for (Annotation annotation : afterInvocations) {

                if (annotation instanceof CacheRemoveAll) {
                    Cache<Object, Object> cache = REMOVE_ALL_CACHE.get(cacheRemoveAllMethodDetail);
                    if (throwable != null) {
                        Class<? extends Throwable>[] evictFor = cacheRemoveAll.evictFor();
                        Class<? extends Throwable>[] noEvictFor = cacheRemoveAll.noEvictFor();
                        Class<? extends Exception> eClass = throwable.getClass();
                        if (noEvictFor.length > 0 && evictFor.length > 0
                                && (Arrays.stream(evictFor).anyMatch(eClass::isAssignableFrom)
                                && !Arrays.stream(noEvictFor).anyMatch(eClass::isAssignableFrom))) {
                            cache.removeAll();
                        } else if (evictFor.length > 0 && Arrays.stream(evictFor).anyMatch(eClass::isAssignableFrom)) {
                            //true 이면
                            cache.removeAll();
                        } else if (noEvictFor.length > 0 && !Arrays.stream(noEvictFor).anyMatch(eClass::isAssignableFrom)) {
                            cache.removeAll();
                        }
                    } else {
                        cache.removeAll();
                    }
                } else if (annotation instanceof CacheRemove) {
                    Cache<Object, Object> cache = REMOVE_CACHE.get(cacheRemoveMethodDetail);
                    Class<? extends CacheKeyGenerator> keyGenerator = cacheRemoveMethodDetail.getCacheKeyGenerator();
                    CacheKeyGenerator cacheKeyGenerator = keyGenerator.newInstance();
                    GeneratedCacheKey generatedCacheKey = cacheKeyGenerator.generateCacheKey((CacheKeyInvocationContext<? extends Annotation>) cacheRemoveMethodDetail);
                    Object key = generatedCacheKey.hashCode();
                    if (key instanceof ToStringGeneratedCacheKey) {
                        key = generatedCacheKey.toString();
                    } else {
                        key = generatedCacheKey.hashCode();
                    }
                    if (throwable != null) {
                        Class<? extends Throwable>[] evictFor = cacheRemove.evictFor();
                        Class<? extends Throwable>[] noEvictFor = cacheRemove.noEvictFor();
                        Class<? extends Exception> eClass = throwable.getClass();
                        if (noEvictFor.length > 0 && evictFor.length > 0
                                && (Arrays.stream(evictFor).anyMatch(eClass::isAssignableFrom)
                                && !Arrays.stream(noEvictFor).anyMatch(eClass::isAssignableFrom))) {
                            cache.remove(key);
                        } else if (evictFor.length > 0 && Arrays.stream(evictFor).anyMatch(eClass::isAssignableFrom)) {
                            //true 이면
                            cache.remove(key);
                        } else if (noEvictFor.length > 0 && !Arrays.stream(noEvictFor).anyMatch(eClass::isAssignableFrom)) {
                            cache.remove(key);
                        }
                    } else {
                        cache.remove(key);
                    }
                } else if (annotation instanceof CachePut) {
                    Cache<Object, Object> cache = PUT_CACHE.get(cachePutMethodDetail);

                    Class<? extends CacheKeyGenerator> keyGenerator = cachePutMethodDetail.getCacheKeyGenerator();
                    CacheKeyGenerator cacheKeyGenerator = keyGenerator.newInstance();
                    GeneratedCacheKey generatedCacheKey = cacheKeyGenerator.generateCacheKey((CacheKeyInvocationContext<? extends Annotation>) cachePutMethodDetail);
                    Object key = generatedCacheKey.hashCode();
                    if (key instanceof ToStringGeneratedCacheKey) {
                        key = generatedCacheKey.toString();
                    } else {
                        key = generatedCacheKey.hashCode();
                    }
                    CacheInvocationParameter valueParameter = cachePutMethodDetail.getValueParameter();
                    if (valueParameter == null) {
                        continue;
                    }
                    if (throwable != null) {
                        Class<? extends Throwable>[] evictFor = cachePut.cacheFor();
                        Class<? extends Throwable>[] noEvictFor = cachePut.noCacheFor();
                        Class<? extends Throwable> eClass = throwable.getClass();
                        if (noEvictFor.length > 0 && evictFor.length > 0
                                && (Arrays.stream(evictFor).anyMatch(eClass::isAssignableFrom)
                                && !Arrays.stream(noEvictFor).anyMatch(eClass::isAssignableFrom))) {
                            cache.put(key, valueParameter.getValue());
                        } else if (evictFor.length > 0 && Arrays.stream(evictFor).anyMatch(eClass::isAssignableFrom)) {
                            //true 이면
                            cache.put(key, valueParameter.getValue());
                        } else if (noEvictFor.length > 0 && !Arrays.stream(noEvictFor).anyMatch(eClass::isAssignableFrom)) {
                            cache.put(key, valueParameter.getValue());
                        }
                    } else {
                        cache.put(key, valueParameter.getValue());
                    }
                }
            }
        }catch (Throwable t) {
            //캐시 처리 과정 중 에러가 발생하면 무시 처리
            if (!result.isPresent()) {
                return point.proceed();
            }
        }

        if (throwable != null) {
            throw throwable;
        }
        if (result.isPresent()) {
            return result.get();
        }
        return null;

        /*
        //@CacheKey 메서드 매개 변수를 캐시 키로 명시 적으로 지정하는 데 사용됩니다.
        //@CacheValue @CachePut 주석을 사용할 때 메서드 매개 변수를 캐시 값으로 명시 적으로 지정하는 데 사용됩니다.

        //순서 정리
        if (cacheRemoveAll != null) {
            //CacheRemoveAll로 주석 된 메소드가 호출되면 지정된 캐시의 모든 요소가 Cache.findRemoveAllCache () 메소드를 통해 제거됩니다.
            // 기본 동작은 주석 된 메소드가 호출 된 후 Cache.findRemoveAllCache ()을 호출하는 것입니다.
            // afterInvocation()을 false로 설정하면 주석이 달린 메소드가 호출되기 전에 Cache.findRemoveAllCache ()이 호출됩니다.
            // Exception Handling은 afterInvocation()이 true 인 경우에만 사용됩니다.
            // evictFor () 및 noEvictFor()가 모두 비어 있으면 모든 예외가 removeAll을 금지합니다.
            // evictFor 가 지정되고 noEvictFor 가 지정되지 않으면 evictFor 목록에 대한 instanceof 해당하는 예외 만 removeAll을 호출합니다.
            // noEvictFor 가 지정되고 evictFor 가 지정되지 않으면 noEvictFor 목록과 모두 instanceof 되지 않는 예외일때만 removeAll을 호출합니다.
            // evictFor 및 noEvictFor 가 모두 지정되면 evictFor 목록에 대해 instanceof 일치하는 예외가 있는 경우
            // noEvictFor 목록에 대한 instanceof 매치와 모두 일치하지 않은 경우 removeAll이 발생합니다.
//            REMOVE_ALL_CACHE.get(method, findRemoveAllByMethod);

            CacheRemoveAllMethodDetail detail = new CacheRemoveAllMethodDetail(point, cacheRemoveAll, cacheDefaults);
            Class<? extends CacheResolverFactory> resolverFactory = detail.getCacheResolverFactory();
            CacheResolverFactory factory = resolverFactory.newInstance();
            CacheResolver cacheResolver = factory.getCacheResolver(detail);
            Cache<Object, Object> cache = cacheResolver.resolveCache(detail);
            if (cacheRemoveAll.afterInvocation()) {
                //true 이면 메소드 실행이 후 캐시 동작 - 기본값
                try {
                    result = point.proceed();
                } catch (Exception e) {
                    Class<? extends Throwable>[] evictFor = cacheRemoveAll.evictFor();
                    Class<? extends Throwable>[] noEvictFor = cacheRemoveAll.noEvictFor();
                    Class<? extends Exception> eClass = e.getClass();
                    if (noEvictFor.length > 0 && evictFor.length > 0
                            && (Arrays.stream(evictFor).anyMatch(eClass::isAssignableFrom)
                            && !Arrays.stream(noEvictFor).anyMatch(eClass::isAssignableFrom))) {
                        cache.removeAll();
                    } else if (evictFor.length > 0 && Arrays.stream(evictFor).anyMatch(eClass::isAssignableFrom)) {
                        //true 이면
                        cache.removeAll();
                    } else if (noEvictFor.length > 0 && !Arrays.stream(noEvictFor).anyMatch(eClass::isAssignableFrom)) {
                        cache.removeAll();
                    }
                    throw e;
                } finally {
                    cache.removeAll();
                }
            } else {
                cache.removeAll();
                result = point.proceed();
            }
        }

        if (cacheRemove != null) {
            //아이디에 해당하는 캐쉬 삭제
            if (cacheRemoveAll != null && cacheRemoveAll.cacheName().equals(cacheRemove.cacheName())) {
                //skip
            } else {
                CacheRemoveMethodDetail methodDetails = new CacheRemoveMethodDetail(point, cacheRemove, cacheDefaults);
                cacheRemove = methodDetails.getCacheAnnotation();
                Class<? extends CacheResolverFactory> resolverFactoryType = methodDetails.getCacheResolverFactory();
                CacheResolverFactory cacheResolverFactory = resolverFactoryType.newInstance();
                CacheResolver cacheResolver = cacheResolverFactory.getCacheResolver(methodDetails);
                Cache<Object, Object> cache = cacheResolver.resolveCache(methodDetails);
                Class<? extends CacheKeyGenerator> keyGenerator = methodDetails.getCacheKeyGenerator();
                CacheKeyGenerator cacheKeyGenerator = keyGenerator.newInstance();
                GeneratedCacheKey generatedCacheKey = cacheKeyGenerator.generateCacheKey((CacheKeyInvocationContext<? extends Annotation>) this);
                Object key = generatedCacheKey.hashCode();
                if (key instanceof ToStringGeneratedCacheKey) {
                    key = generatedCacheKey.toString();
                } else {
                    key = generatedCacheKey.hashCode();
                }
                if (cacheRemove.afterInvocation()) {    //기본값
                    try {
                        result = point.proceed();
                    } catch (Exception e) {
                        Class<? extends Throwable>[] evictFor = cacheRemove.evictFor();
                        Class<? extends Throwable>[] noEvictFor = cacheRemove.noEvictFor();
                        Class<? extends Exception> eClass = e.getClass();
                        if (noEvictFor.length > 0 && evictFor.length > 0
                                && (Arrays.stream(evictFor).anyMatch(eClass::isAssignableFrom)
                                && !Arrays.stream(noEvictFor).anyMatch(eClass::isAssignableFrom))) {
                            cache.remove(key);
                        } else if (evictFor.length > 0 && Arrays.stream(evictFor).anyMatch(eClass::isAssignableFrom)) {
                            //true 이면
                            cache.remove(key);
                        } else if (noEvictFor.length > 0 && !Arrays.stream(noEvictFor).anyMatch(eClass::isAssignableFrom)) {
                            cache.remove(key);
                        }
                        throw e;
                    } finally {
                        cache.remove(key);
                    }
                } else {
                    cache.remove(key);
                    result = point.proceed();
                }
            }
        }

        if (cachePut != null) {
            // 캐쉬를 추가함
            // 메소드를 실행하고 결과를 캐쉬합니다. 또는 @CacheValue 를 저장함
            // 주석 된 메소드가 호출 된 후 Cache.put(Object, Object)를 호출하는 기본 동작은 afterInvocation()을 false로 설정하여 동작을 변경할 수 있습니다.
            // 이 경우 Cache.put(Object, Object)는 주석이 달린 메소드가 호출됩니다.
            // cacheFor () 및 noCacheFor ()가 모두 비어 있으면 모든 예외로 인해 put이 금지됩니다.
            // cacheFor ()가 지정되고 noCacheFor ()가 지정되지 않으면 cacheFor 목록에 대한 instanceof 검사를 통과하는 예외 만 put에 발생합니다.
            // noCacheFor 가 지정되고 cacheFor ()가 지정되지 않으면 put의 noCacheFor 결과에 대해 instanceof 검사를 통과하지 않는 모든 예외 cacheFor () 및 noCacheFor ()가 모두 지정되면
            // cacheFor 목록에 대해 instanceof 검사를 통과하는 예외가 지정되지만 do put에 noCacheFor리스트 결과에 대한 instanceof 점검을 전달하지 않음
            CachePutMethodDetail methodDetails = new CachePutMethodDetail(point, cachePut, cacheDefaults);
            cachePut = methodDetails.getCacheAnnotation();
            Class<? extends CacheResolverFactory> resolverFactoryType = cachePut.cacheResolverFactory();
            CacheResolverFactory cacheResolverFactory = resolverFactoryType.newInstance();
            CacheResolver cacheResolver = cacheResolverFactory.getCacheResolver(methodDetails);
            Cache<Object, Object> cache = cacheResolver.resolveCache(methodDetails);
            Class<? extends CacheKeyGenerator> keyGenerator = methodDetails.getCacheKeyGenerator();
            CacheKeyGenerator cacheKeyGenerator = keyGenerator.newInstance();
            GeneratedCacheKey generatedCacheKey = cacheKeyGenerator.generateCacheKey((CacheKeyInvocationContext<? extends Annotation>) this);
            Object key = generatedCacheKey.hashCode();
            if (key instanceof ToStringGeneratedCacheKey) {
                key = generatedCacheKey.toString();
            } else {
                key = generatedCacheKey.hashCode();
            }
            CacheInvocationParameter valueParameter = methodDetails.getValueParameter();
            if (cachePut.afterInvocation()) {
                //메소드 호출 후 cache.put 호출
                try {
                    result = point.proceed();
                }catch(Throwable e) {
                    Class<? extends Throwable>[] evictFor = cachePut.cacheFor();
                    Class<? extends Throwable>[] noEvictFor = cachePut.noCacheFor();
                    Class<? extends Throwable> eClass = e.getClass();
                    if (noEvictFor.length > 0 && evictFor.length > 0
                            && (Arrays.stream(evictFor).anyMatch(eClass::isAssignableFrom)
                            && !Arrays.stream(noEvictFor).anyMatch(eClass::isAssignableFrom))) {
                        cache.put(key, valueParameter.getValue());
                    } else if (evictFor.length > 0 && Arrays.stream(evictFor).anyMatch(eClass::isAssignableFrom)) {
                        //true 이면
                        cache.put(key, valueParameter.getValue());
                    } else if (noEvictFor.length > 0 && !Arrays.stream(noEvictFor).anyMatch(eClass::isAssignableFrom)) {
                        cache.put(key, valueParameter.getValue());
                    }
                    throw e;
                }
                cache.put(key, valueParameter.getValue());
            } else {
                cache.put(key, valueParameter.getValue());
                result = point.proceed();
            }
        }

        if (cacheResult != null) {
            // 캐시에 값이 있으면 반환되며 주석이 달린 메소드는 실제로 실행되지 않습니다.
            // 값이 발견되지 않는 경우, 주석 첨부 메소드가 불려가 돌려 주어진 값은 생성 된 키를 사용해 캐쉬에 격납됩니다.
            // 예외는 기본적으로 캐시되지 않습니다. 예외 캐싱은 exceptionCacheName ()을 지정하여 활성화 할 수 있습니다.
            // 예외 캐쉬가 지정되고있는 경우는, 주석 첨부 메소드를 호출하기 전에 체크해, 캐쉬 된 예외가 발견되었을 경우는 재 throw됩니다.
            // cachedExceptions () 및 nonCachedExceptions () 속성을 사용하여 캐시 된 예외와 그렇지 않은 예외를 제어 할 수 있습니다.
            // 주석 된 메소드를 항상 호출하고 여전히 결과 세트를 캐시하려면 skipGet ()을 true로 설정하십시오. 그러면  Cache.get (Object) 호출이 비활성화됩니다.
            // exceptionCacheName ()이 지정되면 역시 사용 불가능합니다. 이 기능은 캐시 할 개체를 만들거나 업데이트하는 메서드에 유용합니다.
            // String 및 int 매개 변수에서 생성 된 키를 사용하여 Domain 객체를 캐싱하는 예제입니다.
            // cacheName ()을 지정하지 않으면 캐시 이름이 "my.app.DomainDao.getDomain (java.lang.String, int)"이 생성됩니다.

            // 예외 캐싱이 exceptionCacheName ()을 통해 활성화 된 경우 throw 된 예외가 캐시되는지 여부를 결정하기 위해 다음 규칙이 사용됩니다.
            // cachedExceptions () 및 nonCachedExceptions ()가 모두 비어 있으면 모든 예외가 캐시됩니다
            // cachedExceptions ()가 지정되고 nonCachedExceptions )가 지정되지 않은 경우 cachedExceptions 목록에 대한 instanceof 검사를 통과 한 예외 만 캐시됩니다.
            // nonCachedExceptions ()가 지정되고 cachedExceptions ()가 지정되지 않으면 nonCachedExceptions 목록에 대한 instanceof 검사를 통과하지 않은 모든 예외가 캐시됩니다.
            // cachedExceptions 및 nonCachedExceptions  모두 지정된 경우 cachedExceptions 목록에 대해 instanceof 검사를 통과하지만 nonCachedExceptions 목록에 대한 instanceof 검사를 통과하지 않는 예외는 캐시됩니다
            CacheResultMethodDetail methodDetails = new CacheResultMethodDetail(point, cacheResult, cacheDefaults);
            //CacheDefauls 정보까지 통합한 CacheResult 정보를 반환함
            cacheResult = methodDetails.getCacheAnnotation();
            Class<? extends CacheResolverFactory> resolverFactoryType = cacheResult.cacheResolverFactory();
            CacheResolverFactory cacheResolverFactory = resolverFactoryType.newInstance();
            CacheResolver cacheResolver = cacheResolverFactory.getCacheResolver(methodDetails);
            Cache<Object, Object> cache = cacheResolver.resolveCache(methodDetails);
            Class<? extends CacheKeyGenerator> keyGenerator = methodDetails.getCacheKeyGenerator();
            CacheKeyGenerator cacheKeyGenerator = keyGenerator.newInstance();
            GeneratedCacheKey generatedCacheKey = cacheKeyGenerator.generateCacheKey((CacheKeyInvocationContext<? extends Annotation>) this);
            Object key = generatedCacheKey.hashCode();
            if (key instanceof ToStringGeneratedCacheKey) {
                key = generatedCacheKey.toString();
            } else {
                key = generatedCacheKey.hashCode();
            }
            *//*
            skipGet 을 true로 설정하면 Cache.get(Object) 호출을 건너 뛰고 메소드가 항상 실행되어 리턴 된 값을 캐시합니다.
            이는 항상 실행되어야하고 반환 값이 캐시에 저장되도록하는 생성 또는 업데이트 메소드에 유용합니다.
            또한 true이고 exceptionCacheName ()을 지정되어 있으면 이전에 발생하여 캐쉬되어 있는 예외에 대한 사전 호출 검사도 건너 뜁니다.
            호출 중에 예외가 발생하면 예외 캐쉬 규칙에 따라 예외 캐시 저장소에 예외가 캐쉬됩니다.
             *//*
            boolean skipGet = cacheResult.skipGet();
            if (skipGet) {
                //캐시에서 가져오지 않고 직접 실행
                try {
                    result = point.proceed();
                    cache.put(key, result);
                } catch (Exception e) {
                    //예외 발생시 예외 처리 규칙 처리
                    String exceptionCacheName = cacheResult.exceptionCacheName();
                    if (exceptionCacheName != null && !exceptionCacheName.isEmpty()) {
                        CacheResolver exceptionCacheResolver = cacheResolverFactory.getExceptionCacheResolver(methodDetails);
                        Cache<Object, Throwable> exceptionCache = exceptionCacheResolver.resolveCache(methodDetails);
                        Class<? extends Throwable>[] cachedExceptions = cacheResult.cachedExceptions();
                        Class<? extends Throwable>[] nonCachedExceptions = cacheResult.nonCachedExceptions();
                        Class<? extends Exception> eClass = e.getClass();
                        if (nonCachedExceptions.length > 0 && cachedExceptions.length > 0
                                && (Arrays.stream(cachedExceptions).anyMatch(eClass::isAssignableFrom)
                                && !Arrays.stream(nonCachedExceptions).anyMatch(eClass::isAssignableFrom))) {
                            cache.put(key, e);
                        } else if (cachedExceptions.length > 0 && Arrays.stream(cachedExceptions).anyMatch(eClass::isAssignableFrom)) {
                            //true 이면
                            cache.put(key, e);
                        } else if (nonCachedExceptions.length > 0 && !Arrays.stream(nonCachedExceptions).anyMatch(eClass::isAssignableFrom)) {
                            cache.put(key, e);
                        } else {

                        }
                    }
                    throw e;
                }
            } else {
                if (cache.containsKey(key)) {
                    result = cache.get(key);
                } else {
                    String exceptionCacheName = cacheResult.exceptionCacheName();
                    if (exceptionCacheName != null && !exceptionCacheName.isEmpty()) {
                        CacheResolver exceptionCacheResolver = cacheResolverFactory.getExceptionCacheResolver(methodDetails);
                        Cache<Object, Object> exceptionCache = exceptionCacheResolver.resolveCache(methodDetails);
                        if (exceptionCache.containsKey(key)) {
                            Object exception = exceptionCache.get(key);
                            if (exception != null && exception instanceof Throwable) {
                                //re-throw
                                throw (Throwable) exception;
                            }
                        }
                    }
                }
            }
        }
        */
    }

    /**
     * CacheDefaults annotation 을 찾습니다.
     *
     * @param method 클래스 default annotation 을 찾습니다.
     * @return default annotation
     */
    public static CacheDefaults findCacheDefaults(Method method) {
        Class<?> declaringClass = method.getDeclaringClass();
        CacheDefaults cacheDefaults = null;
        //루프를 돌며 CacheDefaults 를 찾음
        while (declaringClass != null) {
            cacheDefaults = declaringClass.getAnnotation(CacheDefaults.class);
            if (cacheDefaults == null) {
                if (declaringClass.equals(declaringClass.getDeclaredClasses())) {
                    break;
                }
                declaringClass = declaringClass.getDeclaringClass();
                continue;
            }
            break;
        }
        return cacheDefaults;
    }

    private static Cache<Object, Object> findCache(AbstractCacheMethodDetail methodDetails) {
        Class<? extends CacheResolverFactory> resolverFactory = methodDetails.getCacheResolverFactory();
        CacheResolverFactory factory = null;
        try {
            factory = resolverFactory.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
        CacheResolver cacheResolver = factory.getCacheResolver(methodDetails);
        Cache<Object, Object> cache = cacheResolver.resolveCache(methodDetails);
        return cache;
    }
}
