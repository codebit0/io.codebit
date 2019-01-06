package io.codebit.support.aspect.cache.context;

import org.aspectj.lang.ProceedingJoinPoint;

import javax.cache.annotation.*;

public class CacheResultMethodDetail extends AbstractCacheKeyMethodDetail<CacheResult> {

    public CacheResultMethodDetail(ProceedingJoinPoint joinPoint, CacheResult cacheResult, CacheDefaults cacheDefaults) {
        super(joinPoint, cacheResult, cacheDefaults);
    }
}
