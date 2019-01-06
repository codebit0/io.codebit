package io.codebit.support.apt.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface Setter {

    AccessLevel value() default AccessLevel.PUBLIC;

    @Target({ElementType.PACKAGE, ElementType.FIELD, ElementType.TYPE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Config {

        boolean fluent() default false;

        boolean chain() default false;
    }

    public interface IMethodNamer {
        default String define(Setter.Config config, Field field) {
            String name = field.getName();
            if(!config.fluent()) {
                String prefix =  (field.getType().equals(Boolean.class))? "is" : "set";
                int length = prefix.length();
                StringBuilder sb = new StringBuilder(length + name.length());
                sb.append(prefix)
                .append(name)
                .setCharAt(length, Character.toTitleCase(sb.charAt(length)));
                return sb.toString();
            }
            return name;
        }
    }
}
