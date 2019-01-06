package io.codebit.support.api.spring.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.web.bind.annotation.Mapping;

@Target(value = { METHOD, TYPE })
@Retention(value = RUNTIME)
@Inherited
@Mapping
//@ContainerFor(JsonTemplate.class)
public @interface JsonTemplates {
	JsonTemplate[] value();
}
