package io.codebit.support.aspect.cache.context;

import io.codebit.support.cache.HashCodeCacheKeyGenerator;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import javax.cache.annotation.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public abstract class AbstractCacheMethodDetail<T extends Annotation> implements CacheInvocationContext<T> {

    private final ProceedingJoinPoint joinPoint;
    private final CacheInvocationParameter[] cacheInvocationParameters;
    private Method method;
    private T cacheAnnotation;
    private Set<Annotation> annotations;

    private String cacheName;
    private Class<? extends CacheResolverFactory> cacheResolverFactory;
    private Class<? extends CacheKeyGenerator> cacheKeyGenerator;

    public AbstractCacheMethodDetail(ProceedingJoinPoint joinPoint, T cacheAnnotation, CacheDefaults cacheDefaults) {
        this.joinPoint = joinPoint;
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        this.method = methodSignature.getMethod();
        this.cacheAnnotation = cacheAnnotation;

        if(cacheAnnotation instanceof CacheResult) {
            cacheName = ((CacheResult) cacheAnnotation).cacheName();
            cacheKeyGenerator = ((CacheResult) cacheAnnotation).cacheKeyGenerator();
            cacheResolverFactory = ((CacheResult) cacheAnnotation).cacheResolverFactory();
        }else if(cacheAnnotation instanceof CachePut) {
            cacheName = ((CachePut) cacheAnnotation).cacheName();
            cacheKeyGenerator = ((CachePut) cacheAnnotation).cacheKeyGenerator();
            cacheResolverFactory = ((CachePut) cacheAnnotation).cacheResolverFactory();
        }else if(cacheAnnotation instanceof CacheRemove){
            cacheName = ((CacheRemove) cacheAnnotation).cacheName();
            cacheKeyGenerator = ((CacheRemove) cacheAnnotation).cacheKeyGenerator();
            cacheResolverFactory = ((CacheRemove) cacheAnnotation).cacheResolverFactory();
        }else if(cacheAnnotation instanceof CacheRemoveAll) {
            cacheName = ((CacheRemoveAll) cacheAnnotation).cacheName();
            cacheResolverFactory = ((CacheRemoveAll) cacheAnnotation).cacheResolverFactory();
//            cacheKeyGenerator = ((CacheRemoveAll)cacheAnnotation).cacheKeyGenerator();
        }

        if(cacheDefaults != null) {
            if(cacheName == null || cacheName.isEmpty()) {
                cacheName = cacheDefaults.cacheName();
            }
            if(CacheKeyGenerator.class.equals(cacheKeyGenerator)) {
                //기본값이면
                cacheKeyGenerator = cacheDefaults.cacheKeyGenerator();
            }
            if(CacheResolverFactory.class.equals(cacheResolverFactory)) {
                //type에 기본설정이 있는지 확인
                cacheResolverFactory = cacheDefaults.cacheResolverFactory();
            }
        }

        if(CacheKeyGenerator.class.equals(cacheKeyGenerator)) {
            //기본값이면
            cacheKeyGenerator = HashCodeCacheKeyGenerator.class;
        }

        //cache name이 없으면 메소드 signature를 사용함
        if(cacheName == null || cacheName.isEmpty()) {
            cacheName = methodSignature.toLongString();
        }

//        cacheKeyInvocationContext = new InternalCacheKeyInvocationContext<>(joinPoint, this);
        Parameter[] parameters = method.getParameters();
        Object[] args = joinPoint.getArgs();
        List<CacheInvocationParameter> invocationParams =  new ArrayList<>();
        for(int i = 0; i < args.length; i++) {
            Parameter parameter = parameters[i];
            HashSet<Annotation> paramAnnotations = new HashSet<Annotation>(Arrays.asList(parameter.getAnnotations()));
            InternalCacheInvocationParameter cacheInvocationParameter = new InternalCacheInvocationParameter(args[i], parameter.getType(), paramAnnotations, i);
            invocationParams.add(cacheInvocationParameter);
        }
        this.cacheInvocationParameters = invocationParams.toArray(new CacheInvocationParameter[invocationParams.size()]);
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public Set<Annotation> getAnnotations() {
        if(this.annotations == null) {
            Annotation[] _annotations = getMethod().getAnnotations();
            this.annotations = new HashSet<Annotation>(Arrays.asList(_annotations));
        }
        return this.annotations;
    }

    @Override
    public T getCacheAnnotation() {
        return this.cacheAnnotation;
    }

    @Override
    public String getCacheName() {
        return this.cacheName;
    }

    @Override
    public Object getTarget() {
        return joinPoint.getTarget();
    }

    @Override
    public CacheInvocationParameter[] getAllParameters() {
        return this.cacheInvocationParameters;
    }

    @Override
    public <T1> T1 unwrap(Class<T1> cls) {
        return null;
    }

    public Class<? extends CacheResolverFactory> getCacheResolverFactory() {
        return this.cacheResolverFactory;
    }

    public Class<? extends CacheKeyGenerator> getCacheKeyGenerator(){
        return this.cacheKeyGenerator;
    }

    @Override
    public int hashCode() {
        return this.method.hashCode();
    }

    @Override
    public boolean equals(Object obj){
        if(obj  == null || !this.getClass().equals(obj.getClass())) {
            return false;
        }
        AbstractCacheMethodDetail _obj = (AbstractCacheMethodDetail) obj;
        return this.cacheName.equals(_obj.cacheName) && this.method.equals(_obj.method);
    }
}
