package io.codebit.support.aspect.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface ToString {

    public enum Style {
        DEFAULT,
        JSON,
        JSON_PRETTY,
        MULTI_LINE
    }

    String[] include() default {};

    String[] exclude() default {};

    Style style() default Style.DEFAULT;

    int depth() default 1;
}