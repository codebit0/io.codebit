package io.codebit.support.cache;

import javax.cache.annotation.GeneratedCacheKey;
import java.util.Arrays;

public class HashCodeGeneratedCacheKey implements GeneratedCacheKey {

    private static final long serialVersionUID = 1L;

    private final Object[] parameters;
    private final int hashCode;

    public HashCodeGeneratedCacheKey(Object[] parameters) {
        this.parameters = parameters;
        this.hashCode = Arrays.deepHashCode(parameters);
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        if (this.hashCode != obj.hashCode())
            return false;
        HashCodeGeneratedCacheKey other = (HashCodeGeneratedCacheKey) obj;
        return Arrays.deepEquals(this.parameters, other.parameters);
    }
}