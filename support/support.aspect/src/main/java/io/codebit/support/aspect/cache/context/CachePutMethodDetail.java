package io.codebit.support.aspect.cache.context;

import org.aspectj.lang.ProceedingJoinPoint;

import javax.cache.annotation.CacheDefaults;
import javax.cache.annotation.CachePut;

public class CachePutMethodDetail extends AbstractCacheKeyMethodDetail<CachePut> {

    public CachePutMethodDetail(ProceedingJoinPoint joinPoint, CachePut cacheResult, CacheDefaults cacheDefaults) {
        super(joinPoint, cacheResult, cacheDefaults);
    }
}
