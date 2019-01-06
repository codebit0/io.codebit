package io.codebit.support.apt.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by bootcode on 2018. 5. 30..
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface  Data {
}
