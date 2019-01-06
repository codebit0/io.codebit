package io.codebit.support.aspect.annotation;

import java.util.concurrent.TimeUnit;

public @interface Retry {

	public enum Order {
		ALLOW, DENY
	}
	
	public enum Condition {
		EXCEPTION,
		
		/**
		 * 반환값이 null 이거나 false 인 경우 재실행  
		 */
		RETURN_VALUE,
		
		/**
		 * 
		 */
		EXCEPTION_RETURN_VALUE
	}
	
	/**
	 * retry 횟수 
	 * @return default value 1
	 */
	int value() default 1;
	
	/**
	 * 지연 시간 간격 
	 * @return default value 1000
	 */
	long delay() default 1000;
	
	
	TimeUnit unit() default TimeUnit.MICROSECONDS;
	
	/**
     * When to retry (in case of what exception types).
     */
    Class<? extends Throwable>[] include() default { Throwable.class };

    /**
     * Exception types to ignore.
     */
    Class<? extends Throwable>[] ignore() default { };
    
    
    Condition condition() default Condition.EXCEPTION;
    
    /**
     * Shall it be fully verbose (show full exception trace) or just
     * exception message?
     */
    boolean verbose() default true;

    /**
     * Shall the time between retries by randomized.
     */
    boolean randomize() default true;
}
