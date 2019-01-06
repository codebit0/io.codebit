package io.codebit.support.system;

import java.lang.ref.SoftReference;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class Async {

//	ThreadPoolExecutor defaultThreadPool = new ThreadPoolExecutor(100, 200, 50000L,
//			TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(100));

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

    private static synchronized ExecutorService daemonPool() {
        ExecutorService executor = DAEMON.get();
        if(executor == null) {
            synchronized (DAEMON) {
                executor = DAEMON.get();
                if(executor == null) {
                    executor = Executors.newCachedThreadPool(new PriorityOrderThreadFactory("Async-Deamon-", true));
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
                    executor = Executors.newCachedThreadPool(new PriorityOrderThreadFactory("Async-", false));
                    NORMAL = new SoftReference<ExecutorService>(executor);
                }
            }
        }
        return executor;
    }

    static class PriorityOrderThreadFactory implements ThreadFactory {
        private ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private String namePrefix;
        private ThreadFactory threadFactory;
        private final boolean isDeamon;

        PriorityOrderThreadFactory(String namePrefix, boolean isDeamon) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            this.namePrefix = namePrefix;
            this.isDeamon  =isDeamon;
        }

        PriorityOrderThreadFactory(ThreadFactory threadFactory, boolean isDeamon) {
            this.threadFactory = threadFactory;
            this.isDeamon  =isDeamon;
        }

        public Thread newThread(Runnable r) {
            Thread t;
            if(threadFactory != null) {
                t = threadFactory.newThread(r);
            }else {
                t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            }
            if(isDeamon)
                t.setDaemon(true);
            else
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }

    static class PriorityOrderFunction<V> implements Callable<V>, Runnable {
        private final Object[] args;
        private final Object func;
        private int priority = Thread.NORM_PRIORITY;
        private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;

        PriorityOrderFunction(Object func, Object ... args){
            this.func = func;
            this.args = args;
        }

        public void setPriority(int priority){
            this.priority = priority;
        }

        public void setUncaughtExceptionHandler(Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
            this.uncaughtExceptionHandler = uncaughtExceptionHandler;
        }

        @Override
        public V call() throws Exception {
            //https://funofprograming.wordpress.com/2016/10/08/priorityexecutorservice-for-java/
            Thread thread = Thread.currentThread();
            Thread.UncaughtExceptionHandler beforeUncaughtExceptionHandler = null;
            if(uncaughtExceptionHandler != null) {
                beforeUncaughtExceptionHandler = thread.getUncaughtExceptionHandler();
                thread.setUncaughtExceptionHandler(this.uncaughtExceptionHandler);
            }

            int beforePriority = thread.getPriority();
            if(beforePriority != this.priority) {
                thread.setPriority(this.priority);
                //설정을 바꾸고 작업을 양보함
                Thread.yield();
            }

            Object proceed = null;
            try {
                if(func instanceof Runnable) {
                    ((Runnable) func).run();
                }else if(func instanceof Consumer){
                    ((Consumer) func).accept(args[0]);
                }else if(func instanceof Callable) {
                    proceed = ((Callable) func).call();
                }else if(func instanceof Function) {
                    proceed = ((Function) func).apply(args[0]);
                }else if(func instanceof BiFunction){
                    proceed = ((BiFunction) func).apply(args[0], args[1]);
                }
                if (proceed instanceof Future) {
                    proceed = ((Future<?>) proceed).get();
                }
            } catch (Throwable e) {
                throw new IllegalStateException(e);
            }
            thread.setPriority(beforePriority);
            if(beforeUncaughtExceptionHandler != null) {
                thread.setUncaughtExceptionHandler(beforeUncaughtExceptionHandler);
            }
            return (V) proceed;
        }

        @Override
        public void run() {
            try {
                call();
            } catch (Exception e) {
            }
        }
    }

    private ExecutorService EXECUTOR = null;

    public Async() {
        this(false);
    }

    public Async(boolean isDaemon) {
        this((isDaemon)? daemonPool(): normalPool());
    }

    public Async(ExecutorService executorService){
        this(executorService, false);
    }

    public Async(ExecutorService executorService, boolean isDaemon) {
        if(executorService instanceof ThreadPoolExecutor) {
            ThreadFactory threadFactory = ((ThreadPoolExecutor) executorService).getThreadFactory();
            ((ThreadPoolExecutor)executorService).setThreadFactory(new PriorityOrderThreadFactory(threadFactory, isDaemon));
        }
        EXECUTOR = executorService;
    }

    private ExecutorService executor() {
        return EXECUTOR;
    }

    /**
     * Runnable 을 비동기 호출합니다.
     *
     * @param func the func
     */
    public void run(Runnable func) {
        run(func, Thread.NORM_PRIORITY, null);
    }

    public void run(Runnable func, int priority) {
        run(func, priority, null);
    }

    public void run(Runnable func, Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
        run(func, Thread.NORM_PRIORITY, uncaughtExceptionHandler);
    }

    public void run(Runnable func, int priority, Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
        PriorityOrderFunction function = new PriorityOrderFunction(func);
        function.setPriority(priority);
        if(uncaughtExceptionHandler != null)
            function.setUncaughtExceptionHandler(uncaughtExceptionHandler);
        executor().execute(function);
    }

    /**
     * Consumer 을 비동기 호출합니다.
     *
     * @param <TInput> consumer parameter 타입
     * @param func     consumer function
     * @param o        parameter
     */
    public <TInput> void run(Consumer<TInput> func, TInput o) {
        run(func, o, Thread.NORM_PRIORITY, null);
    }

    public <TInput> void run(Consumer<TInput> func, TInput o, int priority) {
        run(func, o, priority, null);
    }

    public <TInput> void run(Consumer<TInput> func, TInput o, Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
        run(func, o, Thread.NORM_PRIORITY, uncaughtExceptionHandler);
    }

    public <TInput> void run(Consumer<TInput> func, TInput o, int priority, Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
        PriorityOrderFunction function = new PriorityOrderFunction(func, o);
        function.setPriority(priority);
        if(uncaughtExceptionHandler != null)
            function.setUncaughtExceptionHandler(uncaughtExceptionHandler);
        executor().execute(function);
    }

    /**
     * Callable 을 비동기 호출합니다.
     *
     * @param <R>  the generic type
     * @param func the func
     * @return the future
     */
    public <R> Future<R> run(Callable<R> func) {
        return run(func, Thread.NORM_PRIORITY);
    }

    public <R> Future<R> run(Callable<R> func, int priority) {
        PriorityOrderFunction<R> function = new PriorityOrderFunction<R>(func);
        function.setPriority(priority);
        return executor().submit((Callable<R>) function);
    }

    /**
     * Consumer 을 비동기 호출합니다.
     *
     * @param <T>      the generic type
     * @param <R> the generic type
     * @param func     the func
     * @param o        the o
     * @return the future
     */
    public <T, R> Future<R> run(Function<T, R> func, T o) {
        return run(func, o, Thread.NORM_PRIORITY);
    }

    public <T, R> Future<R> run(Function<T, R> func, T o, int priority) {
        PriorityOrderFunction<R> function = new PriorityOrderFunction<R>(func, o);
        function.setPriority(priority);
        return executor().submit((Callable<R>) function);
    }

    /**
     * BiFunction 을 비동기 호출합니다..
     *
     * @param <T1> the generic type
     * @param <T2> the generic type
     * @param <R>  the generic type
     * @param func the func
     * @param o1   the o1
     * @param o2   the o2
     * @return the future
     */
    public <T1, T2, R> Future<R> run(BiFunction<T1, T2, R> func, T1 o1, T2 o2) {
        return run(func, o1, o2, Thread.NORM_PRIORITY);
    }

    public <T1, T2, R> Future<R> run(BiFunction<T1, T2, R> func, T1 o1, T2 o2, int priority) {
        PriorityOrderFunction<R> function = new PriorityOrderFunction<R>(func, o1, o2);
        function.setPriority(priority);
        return executor().submit((Callable<R>) function);
    }
}
