package io.codebit.support.spring.mvc.bind.annotation;

import org.springframework.web.bind.annotation.ValueConstants;

public @interface RequestPattern
{
	String value();
	 
    /**
     * Whether the parameter is required.
     * Default is true, leading to an exception thrown in case
     * of the parameter missing in the request. Switch this to
     * false if you prefer a null in case of the parameter missing.
     * Alternatively, provide a {@link #defaultValue() defaultValue},
     * which implicitly sets this flag to false.
     */
    boolean required() default true;
 
    /**
     * The default value to use as a fallback. Supplying a default value
     * implicitly sets {@link #required()} to false.
     */
    String defaultValue() default ValueConstants.DEFAULT_NONE;
}
