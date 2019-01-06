package io.codebit.support.api.spring.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.function.Function;

import org.springframework.web.bind.annotation.Mapping;

/**
 * https://github.com/bmsantos/dynafilter-proj 코드를 기반으로 기능을 추가
 * TODO JsonFomat 으로 이름 변경 예정 
 */
@Target(value = { METHOD, TYPE })
@Retention(value = RUNTIME)
@Inherited
@Mapping
@Repeatable(JsonViews.class)
public @interface JsonTemplate
{
    Class<?> value();

    String[] fields() default {};

    Class<?>[] converters() default {JsonTemplate.class};
//    String cast() default "";
    
    boolean includeNulls() default false;

    @FunctionalInterface
    public interface TypeConveter<R>  extends Function<Meta, R>  {
    }

    public static class Meta {

    }
}
