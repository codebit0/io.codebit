package io.codebit.support.cache;

import javax.cache.Cache;
import javax.cache.annotation.CacheInvocationContext;
import javax.cache.annotation.CacheResolver;
import java.lang.annotation.Annotation;

public class DefaultCacheResolver implements CacheResolver {
    private final Cache<?, ?> cache;

    /**
     * Create a new default cache resolver that always returns the specified cache
     *
     * @param cache The cache to return for all calls to {@link #resolveCache(CacheInvocationContext)}
     */
    public DefaultCacheResolver(Cache<?, ?> cache) {
        if (cache == null) {
            throw new IllegalArgumentException("The Cache can not be null");
        }

        this.cache = cache;
    }

    /* (non-Javadoc)
     * @see javax.cache.annotation.CacheResolver#resolveCache(javax.cache.annotation.CacheInvocationContext)
     */
    @Override
    @SuppressWarnings("unchecked")
    public <K, V> Cache<K, V> resolveCache(CacheInvocationContext<? extends Annotation> cacheInvocationContext) {
        return (Cache<K, V>) this.cache;
    }
}