package io.codebit.support.cache;

import javax.cache.annotation.GeneratedCacheKey;

public class ToStringGeneratedCacheKey implements GeneratedCacheKey {

    private static final long serialVersionUID = 1L;
    private final Object[] parameters;

    private StringBuilder sb = new StringBuilder();

    /**
     *
     * @param parameters the paramters to use
     */
    public ToStringGeneratedCacheKey(Object[] parameters) {
        this.parameters = parameters;
        for(Object parameter : parameters) {
           sb.append(parameter);
           sb.append(",");
        }
        sb.deleteCharAt(sb.length());
    }

    @Override
    public int hashCode() {
        return this.parameters.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        if (this.hashCode() != obj.hashCode())
            return false;
        ToStringGeneratedCacheKey other = (ToStringGeneratedCacheKey) obj;
        return parameters.equals(other.parameters);
    }
}