package io.codebit.support.aspect.cache.context;

import javax.cache.annotation.CacheInvocationParameter;
import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * Created by bootcode on 2018-08-01.
 */
public class InternalCacheInvocationParameter implements CacheInvocationParameter {

    private final Object value;
    private final Class<?> rowTyep;
    private final Set<Annotation> annotations;
    private final int position;

    public InternalCacheInvocationParameter(Object value, Class<?> rowTyep, Set<Annotation> annotations, int position) {
        this.value = value;
        this.rowTyep = rowTyep;
        this.annotations = annotations;
        this.position = position;
    }

    @Override
    public Class<?> getRawType() {
        return this.rowTyep;
    }

    @Override
    public Object getValue() {
        return this.value;
    }

    @Override
    public Set<Annotation> getAnnotations() {
        return this.getAnnotations();
    }

    @Override
    public int getParameterPosition() {
        return this.position;
    }
}
