package io.codebit.support.cache;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.annotation.CacheMethodDetails;
import javax.cache.annotation.CacheResolver;
import javax.cache.annotation.CacheResolverFactory;
import javax.cache.annotation.CacheResult;
import javax.cache.spi.CachingProvider;
import java.lang.annotation.Annotation;

public class DefaultCacheResolverFactory implements CacheResolverFactory {
    @Override
    public CacheResolver getCacheResolver(CacheMethodDetails<? extends Annotation> cacheMethodDetails) {
        Cache<Object, Object> cache = resolveCache(cacheMethodDetails.getCacheName());
        return new DefaultCacheResolver(cache);
    }

    @Override
    public CacheResolver getExceptionCacheResolver(CacheMethodDetails<CacheResult> cacheMethodDetails) {
        Cache<Object, Object> cache = resolveCache(cacheMethodDetails.getCacheAnnotation().exceptionCacheName());
        return  new DefaultCacheResolver(cache);
    }

    private static Cache<Object,Object> resolveCache(String cacheName){
        CachingProvider provider = Caching.getCachingProvider();
        if(provider == null)
            return null;
        CacheManager manager = provider.getCacheManager();
        if(manager == null)
            return null;
        Cache<Object, Object> cache = null;
        try {
            cache = manager.getCache(cacheName);
        }catch (Exception e) {
        }
        if(cache == null) {
            try {
                cache = manager.createCache(cacheName, null);
            }catch (Exception e1) {
            }
        }
        return cache;
    }
}
