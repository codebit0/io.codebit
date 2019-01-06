package io.codebit.support.ignite.cache;


import io.codebit.support.cache.DefaultCacheResolver;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.annotation.CacheMethodDetails;
import javax.cache.annotation.CacheResolver;
import javax.cache.annotation.CacheResolverFactory;
import javax.cache.annotation.CacheResult;
import java.lang.annotation.Annotation;
import java.util.logging.Logger;

/**
 * Default {@link CacheResolverFactory} that uses the default {@link CacheManager} and finds the {@link Cache}
 * using {@link CacheManager#getCache(String)}. Returns a {@link DefaultCacheResolver} that wraps the found
 * {@link Cache}
 *
 */
public class IgniteCacheResolverFactory implements CacheResolverFactory {
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private Ignite ignite = Ignition.start();

    /**
     * Constructs the resolver
     */
    public IgniteCacheResolverFactory() {
    }

    /* (non-Javadoc)
     * @see javax.cache.annotation.CacheResolverFactory#getCacheResolver(javax.cache.annotation.CacheMethodDetails)
     */
    @Override
    public CacheResolver getCacheResolver(CacheMethodDetails<? extends Annotation> cacheMethodDetails) {
        final String cacheName = cacheMethodDetails.getCacheName();
        Cache<?, ?> cache = ignite.getOrCreateCache(cacheName);
        if (cache == null) {
            logger.warning(cacheName + "을 생성할 수 없습니다.");
            throw new UnsupportedOperationException(cacheName + "의 Cache를 생성할 수 없습니다.");
        }
        return new DefaultCacheResolver(cache);
    }

    @Override
    public CacheResolver getExceptionCacheResolver(CacheMethodDetails<CacheResult> cacheMethodDetails) {
        final CacheResult cacheResultAnnotation = cacheMethodDetails.getCacheAnnotation();
        final String exceptionCacheName = cacheResultAnnotation.exceptionCacheName();
        if (exceptionCacheName == null || exceptionCacheName.isEmpty()) {
            throw new IllegalArgumentException("Can only be called when CacheResult.exceptionCacheName() is specified");
        }
        Cache<?, ?> cache = ignite.getOrCreateCache(exceptionCacheName);

        if (cache == null) {
            logger.warning(exceptionCacheName + "의 Cache를 생성할 수 없습니다.");
            throw new UnsupportedOperationException(exceptionCacheName + "의 Cache를 생성할 수 없습니다.");
        }
        //org.apache.ignite.cache.CachingProvider
        return new DefaultCacheResolver(cache);
    }
}