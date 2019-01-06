package io.codebit.support.aspect.util;

import java.lang.reflect.Method;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import io.codebit.support.aspect.annotation.Retry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

@Aspect
public class RetryAspect {

	private final transient Logger log = LogManager.getLogManager().getLogger(RetryAspect.class.getName());
	
	@Around("execution(* * (..)) && @annotation(io.codebit.support.aspect.annotation.Retry)")
	public Object retry(final ProceedingJoinPoint point) throws Throwable {

		final Method method = MethodSignature.class.cast(point.getSignature()).getMethod();
		Retry retry = method.getAnnotation(Retry.class);
//		Async async = method.getAnnotation(Async.class);
		
		// 반복 횟수
		int attempt = retry.value();
		final long begin = System.nanoTime();
		final Class<? extends Throwable>[] include = retry.include();
		final Class<? extends Throwable>[] exclude = retry.ignore();
		
		while (attempt > 0) {
			try {

//				Logger.warn(point.targetize(),
//						"#%s(): attempt #%d of %d failed in %[nano]s (%[nano]s waiting already) with %[exception]s",
//						method.getName(), attempt, rof.attempts(), System.nanoTime() - start,
//						System.nanoTime() - begin, ex);
				return point.proceed();
			} catch (Throwable e) {
				//마지막 loop 횟수이면 throw 
				if(attempt <= 1) {
					throw e;
				}
				//retry 제외 타입인 경우 
				if(matches(e, exclude)) {
					throw e;
				}
				//retry 실행 타입 
				if(matches(e, include)) {
					retry.unit().sleep(retry.delay());
//					new RuntimeException(e);
				}
				
			}
			attempt--;
		}
		return null;
	}

	private static boolean matches(final Throwable thrown, final Class<? extends Throwable>... types) {
		if(types.length <= 0) 
			return false; 
		boolean matches = false;
		Class<? extends Throwable> throwable = thrown.getClass();
		for (final Class<? extends Throwable> type : types) {
			if (type.isAssignableFrom(throwable)) {
				matches = true;
				break;
			}
		}
		return matches;
	}
}
