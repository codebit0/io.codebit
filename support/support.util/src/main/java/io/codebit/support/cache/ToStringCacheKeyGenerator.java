package io.codebit.support.cache;

import javax.cache.annotation.CacheInvocationParameter;
import javax.cache.annotation.CacheKeyGenerator;
import javax.cache.annotation.CacheKeyInvocationContext;
import javax.cache.annotation.GeneratedCacheKey;
import java.lang.annotation.Annotation;

/**
 * Created by bootcode on 2018-07-17.
 */
public class ToStringCacheKeyGenerator implements CacheKeyGenerator {

    /* (non-Javadoc)
     * @see javax.cache.annotation.CacheKeyGenerator#generateCacheKey(javax.cache.annotation.CacheInvocationContext)
     */
    @Override
    public GeneratedCacheKey generateCacheKey(CacheKeyInvocationContext<? extends Annotation> cacheKeyInvocationContext) {
        final CacheInvocationParameter[] keyParameters = cacheKeyInvocationContext.getKeyParameters();

        final Object[] parameters = new Object[keyParameters.length];
        for (int index = 0; index < keyParameters.length; index++) {
            parameters[index] = keyParameters[index].getValue();
        }

        return new ToStringGeneratedCacheKey(parameters);
    }
}