package io.codebit.support.aspect.cache.context;

import org.aspectj.lang.ProceedingJoinPoint;

import javax.cache.annotation.*;
import java.lang.annotation.Annotation;
import java.util.*;

public class AbstractCacheKeyMethodDetail<T extends Annotation>
        extends AbstractCacheMethodDetail<T> implements CacheKeyInvocationContext<T> {

    private final CacheInvocationParameter[] cacheKeyParameters;
    private CacheInvocationParameter cacheValueParameter;

    private CacheKey[] cacheKeys;
    private CacheValue cacheValue;

    public AbstractCacheKeyMethodDetail(ProceedingJoinPoint joinPoint, T cacheAnnotation, CacheDefaults cacheDefaults) {
        super(joinPoint, cacheAnnotation, cacheDefaults);

        Set<CacheInvocationParameter> keys = new HashSet<>();
        Set<CacheKey> _keys = new HashSet<>();
        CacheInvocationParameter[] allParameters = super.getAllParameters();
        for (CacheInvocationParameter parameter : allParameters) {
            Set<Annotation> annotations = parameter.getAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation instanceof CacheKey) {
                    keys.add(parameter);
                    _keys.add((CacheKey) annotation);
                }
                if (annotation instanceof CacheValue) {
                    cacheValueParameter = parameter;
                    cacheValue = (CacheValue) annotation;
                }
            }
        }

        int keySize = keys.size();
        cacheKeyParameters = keys.toArray(new CacheInvocationParameter[keySize]);
        cacheKeys = _keys.toArray(new CacheKey[keySize]);
    }

    public CacheKey[] getCacheKeys() {
        return this.cacheKeys;
    }

    public CacheValue getCacheValue() {
        return this.cacheValue;
    }

    @Override
    public CacheInvocationParameter[] getKeyParameters() {
        return this.cacheKeyParameters;
    }

    @Override
    public CacheInvocationParameter getValueParameter() {
        return cacheValueParameter;
    }
}
