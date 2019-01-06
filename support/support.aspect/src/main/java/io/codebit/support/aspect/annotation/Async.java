/**
 */
package io.codebit.support.aspect.annotation;

import java.lang.annotation.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * method 를 asynchronous 실행 합니다.
 * 해당 method의 반환타입은 {@code void} 또는 {@link Future}
 * 이어야 합니다.
 * {@code Future} 타입을 반환 할 경우 Future#get으로 반환 값을 확인 할 수 있습니다.
 * <p>
 *
 * </p>
 *
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Async {

    /**
     * 실행 Thread를 데몬쓰레드로 생성합니다.
     * 데몬쓰레드는 일반 쓰레드(main 등)가 모두 종료되면 강제적으로 종료되는 특징을 가지고 있다.
     * @return ture이면 데몬쓰레드로 생성합니다. 기본값은 false입니다.
     */
    boolean daemon() default false;

    /**
     * 쓰레드의 우선순위입니다.  이 우선순위의 값에 따라 쓰레드가 얻는 실행시간이 달라진다
     * @return 기봅값은 5 (Thread.NORM_PRIORITY) 입니다.
     */
    int priority() default Thread.NORM_PRIORITY;

    int timeout() default  1;

    TimeUnit timeUnit() default TimeUnit.MINUTES;

    public static class Return {
        /**
         * annotaion Async 리턴 처리 function
         * @param value 반환할 값
         * @param <R> 반환할 타입
         * @return Future 타입의 반환 데이터
         */
        public static <R> Future<R> value(R value) {
            return new Future<R>() {
                public boolean cancel(boolean mayInterruptIfRunning) {
                    return false;
                }

                public boolean isCancelled() {
                    return false;
                }

                public boolean isDone() {
                    return false;
                }

                public R get() throws InterruptedException, ExecutionException {
                    return value;
                }

                public R get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                    return value;
                }
            };
        }
    }
}
