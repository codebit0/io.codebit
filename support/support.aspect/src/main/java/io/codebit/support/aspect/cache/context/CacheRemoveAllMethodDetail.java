package io.codebit.support.aspect.cache.context;

import org.aspectj.lang.ProceedingJoinPoint;

import javax.cache.annotation.CacheDefaults;
import javax.cache.annotation.CacheRemoveAll;

public class CacheRemoveAllMethodDetail extends AbstractCacheMethodDetail<CacheRemoveAll> {

    public CacheRemoveAllMethodDetail(ProceedingJoinPoint joinPoint, CacheRemoveAll cacheResult, CacheDefaults cacheDefaults) {
        super(joinPoint, cacheResult, cacheDefaults);
    }
}
