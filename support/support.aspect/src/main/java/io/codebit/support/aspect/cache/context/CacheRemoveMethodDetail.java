package io.codebit.support.aspect.cache.context;

import org.aspectj.lang.ProceedingJoinPoint;

import javax.cache.annotation.CacheDefaults;
import javax.cache.annotation.CacheRemove;

public class CacheRemoveMethodDetail extends AbstractCacheKeyMethodDetail<CacheRemove> {

    public CacheRemoveMethodDetail(ProceedingJoinPoint joinPoint, CacheRemove cacheResult, CacheDefaults cacheDefaults) {
        super(joinPoint, cacheResult, cacheDefaults);
    }
}
