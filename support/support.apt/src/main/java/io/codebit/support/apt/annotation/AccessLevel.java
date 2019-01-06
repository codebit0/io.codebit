package io.codebit.support.apt.annotation;

public enum AccessLevel {
    PUBLIC,
    MODULE,
    //PROTECTED,
    PACKAGE,
    PRIVATE;

    public String toString() {
        if(this.equals(MODULE) || this.equals(PACKAGE))
            return "";
        return name().toLowerCase(java.util.Locale.US);
    }
}