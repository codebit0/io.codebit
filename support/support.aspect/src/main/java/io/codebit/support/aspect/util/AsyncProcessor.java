package io.codebit.support.aspect.util;

import io.codebit.support.aspect.annotation.Async;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.ref.SoftReference;
import java.lang.reflect.Method;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Aspect
public class AsyncProcessor {
	/**
	 * daemon쓰레드 풀
	 * {@link Async} 의 daemon 이 true일때 사용하는 쓰레드 풀
	 */
	private static transient SoftReference<ExecutorService> DAEMON = new SoftReference<ExecutorService>(null);

	/**
	 * Normal 쓰레드 풀
	 * {@link Async} 의 daemon 이 false일때 사용하는 쓰레드 풀
	 */
	private static transient SoftReference<ExecutorService> NORMAL = new SoftReference<ExecutorService>(null);

	public static Thread.UncaughtExceptionHandler UncaughtExceptionHandler;

	static class DefaultThreadFactory implements ThreadFactory {
		private final ThreadGroup group;
		private final AtomicInteger threadNumber = new AtomicInteger(1);
		private final String namePrefix;
		private final boolean isDeamon;

		DefaultThreadFactory(String namePrefix, boolean isDeamon) {
			SecurityManager s = System.getSecurityManager();
			group = (s != null) ? s.getThreadGroup() :
					Thread.currentThread().getThreadGroup();
			this.namePrefix = namePrefix;
			this.isDeamon  =isDeamon;
		}

		public Thread newThread(Runnable r) {
			Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
			if(isDeamon)
				t.setDaemon(true);
			else
				t.setDaemon(false);
			if (t.getPriority() != Thread.NORM_PRIORITY)
				t.setPriority(Thread.NORM_PRIORITY);
			return t;
		}
	}

	private static synchronized ExecutorService daemonPool() {
		ExecutorService executor = DAEMON.get();
		if(executor == null) {
			synchronized (DAEMON) {
				executor = DAEMON.get();
				if(executor == null) {
					executor = Executors.newCachedThreadPool(new DefaultThreadFactory("Async-Deamon-", true));
					DAEMON = new SoftReference<ExecutorService>(executor);
				}
			}
		}
		return executor;
	}

	private static synchronized ExecutorService normalPool() {
		ExecutorService executor = NORMAL.get();
		if(executor == null) {
			synchronized (NORMAL) {
				executor = NORMAL.get();
				if(executor == null) {
					executor = Executors.newCachedThreadPool(new DefaultThreadFactory("Async-", false));
					NORMAL = new SoftReference<ExecutorService>(executor);
				}
			}
		}
		return executor;
	}

	/**
	 * method async 처리
	 * 반환 타입이 Void 인 경우 nonblock
	 * @param point 조인 포인트
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
     */
	@Around("execution(@io.codebit.support.aspect.annotation.Async * * (..))")
    public Object execution(final ProceedingJoinPoint point) throws InterruptedException, ExecutionException, TimeoutException {
		MethodSignature signature = MethodSignature.class.cast(point.getSignature());
		Method method = MethodSignature.class.cast(point.getSignature()).getMethod();
		//리턴타입 확인
        final Class<?> returnType = method.getReturnType();
        /*if (!Future.class.isAssignableFrom(returnType) && !returnType.equals(Void.TYPE)) {
            throw new IllegalStateException(
                String.format("%s: Return type is %s, not void or Future, cannot use @Async", method.getName(),
						returnType.getCanonicalName()
                )
            );
        }*/

		Async async = method.getAnnotation(Async.class);
		ExecutorService pool = (async.daemon())? daemonPool(): normalPool();

        final Future<?> result = pool.submit(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				//https://funofprograming.wordpress.com/2016/10/08/priorityexecutorservice-for-java/
				Thread thread = Thread.currentThread();
				if(returnType.equals(Void.TYPE)) {
					if(UncaughtExceptionHandler != null) {
						thread.setUncaughtExceptionHandler(UncaughtExceptionHandler);
					}
				}

				int oldPriority = thread.getPriority();
				if(oldPriority != async.priority()) {
					thread.setPriority(async.priority());
					//설정을 바꾸고 작업을 양보함
					Thread.yield();
				}

				Object proceed = null;
				try {
					proceed = point.proceed();
					if (proceed instanceof Future) {
						proceed = ((Future<?>) proceed).get();
                    }
				} catch (Throwable e) {
					throw new IllegalStateException(e);
				}
				thread.setPriority(oldPriority);
				return proceed;
			}
        });
        if (Future.class.isAssignableFrom(returnType)) {
			return result;
        }else if(returnType.equals(Void.TYPE)){
			return null;
		}
		//반환타입이 void 또는 Future가 아닌 경우 blocking 후 값을 반환
        return result.get(async.timeout(), async.timeUnit());
    }

	public static Thread.UncaughtExceptionHandler UncaughtExceptionHandler() {
		return UncaughtExceptionHandler;
	}

	public Thread.UncaughtExceptionHandler UncaughtExceptionHandler(Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
		UncaughtExceptionHandler = uncaughtExceptionHandler;
		return UncaughtExceptionHandler;
	}
}
