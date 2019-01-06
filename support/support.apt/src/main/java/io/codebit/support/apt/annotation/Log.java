package io.codebit.support.apt.annotation;

import java.lang.annotation.*;

@Documented
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Log {
    String value() default "";

    Class<?> name() default Log.class;
}
