package io.codebit.support.system;

import io.codebit.support.util.function.Func;


/**
 * Created by bootcode on 2018. 3. 19..
 */
public class Lazy<R> {

    public enum ThreadSafetyMode {
        ExecutionAndPublication,
        PublicationOnly,
        None
    }

    private Object t1;
    private Object t2;
    private Object t3;
    private Object t4;
    private Object t5;
    private Object t6;
    private Object t7;
    private Object t8;
    private Object t9;
    private Object t10;

    private ThreadSafetyMode mode;

    private boolean isValueCreated = false;

    private final Object valueFactory;

    private R holder;

    public static <R> Lazy<R> wrap(R value)
    {
        return new Lazy<R>(value);
    }

    private Lazy(R value)
    {
        this.valueFactory = null;
        this.holder = value;
        this.isValueCreated = true;
    }

    public Lazy(Func.Zero<R> valueFactory, ThreadSafetyMode mode) {
        this.valueFactory = valueFactory;
        this.mode = mode;
    }

    private <T> Lazy(Func.One<T, R> valueFactory, T arg, ThreadSafetyMode mode) {
        this.valueFactory = valueFactory;
        this.t1 = arg;
    }

    public <T1, T2> Lazy(Func.Two<T1, T2, R> valueFactory, T1 arg1, T2 arg2, ThreadSafetyMode mode) {
        this.valueFactory = valueFactory;
        this.t1 = arg1;
        this.t2 = arg2;
    }

    public <T1, T2, T3> Lazy(Func.Three<T1, T2, T3, R> valueFactory, T1 arg1, T2 arg2, T3 arg3, ThreadSafetyMode mode) {
        this.valueFactory = valueFactory;
        t1 = arg1;
        t2 = arg2;
        t3 = arg3;
    }

    public <T1, T2, T3, T4> Lazy(
            Func.Four<T1, T2, T3, T4, R> valueFactory,
            T1 arg1, T2 arg2, T3 arg3, T4 arg4,
            ThreadSafetyMode mode) {
        this.valueFactory = valueFactory;
        t1 = arg1;
        t2 = arg2;
        t3 = arg3;
        t4 = arg4;
    }

    public <T1, T2, T3, T4, T5> Lazy(
            Func.Five<T1, T2, T3, T4, T5, R> valueFactory,
            T1 arg1, T2 arg2, T3 arg3, T4 arg4, T5 arg5,
            ThreadSafetyMode mode) {
        this.valueFactory = valueFactory;
        t1 = arg1;
        t2 = arg2;
        t3 = arg3;
        t4 = arg4;
        t5 = arg5;
    }

    public <T1, T2, T3, T4, T5, T6> Lazy(
            Func.Six<T1, T2, T3, T4, T5, T6, R> valueFactory,
            T1 arg1, T2 arg2, T3 arg3, T4 arg4, T5 arg5, T6 arg6,
            ThreadSafetyMode mode) {
        this.valueFactory = valueFactory;
        t1 = arg1;
        t2 = arg2;
        t3 = arg3;
        t4 = arg4;
        t5 = arg5;
        t6 = arg6;
    }

    public <T1, T2, T3, T4, T5, T6, T7> Lazy(
            Func.Seven<T1, T2, T3, T4, T5, T6, T7, R> valueFactory,
            T1 arg1, T2 arg2, T3 arg3, T4 arg4, T5 arg5, T6 arg6, T7 arg7,
            ThreadSafetyMode mode) {
        this.valueFactory = valueFactory;
        t1 = arg1;
        t2 = arg2;
        t3 = arg3;
        t4 = arg4;
        t5 = arg5;
        t6 = arg6;
        t7 = arg7;
    }

    public <T1, T2, T3, T4, T5, T6, T7, T8> Lazy(
            Func.Eight<T1, T2, T3, T4, T5, T6, T7, T8, R> valueFactory,
            T1 arg1, T2 arg2, T3 arg3, T4 arg4, T5 arg5, T6 arg6, T7 arg7, T8 arg8,
            ThreadSafetyMode mode) {
        this.valueFactory = valueFactory;
        t1 = arg1;
        t2 = arg2;
        t3 = arg3;
        t4 = arg4;
        t5 = arg5;
        t6 = arg6;
        t7 = arg7;
        t8 = arg8;
    }

    public <T1, T2, T3, T4, T5, T6, T7, T8, T9> Lazy(
            Func.Nine<T1, T2, T3, T4, T5, T6, T7, T8, T9, R> valueFactory,
            T1 arg1, T2 arg2, T3 arg3, T4 arg4, T5 arg5, T6 arg6, T7 arg7, T8 arg8, T9 arg9,
            ThreadSafetyMode mode) {
        this.valueFactory = valueFactory;
        t1 = arg1;
        t2 = arg2;
        t3 = arg3;
        t4 = arg4;
        t5 = arg5;
        t6 = arg6;
        t7 = arg7;
        t8 = arg8;
        t9 = arg9;
    }

    public <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> Lazy(
            Func.Ten<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R> valueFactory,
            T1 arg1, T2 arg2, T3 arg3, T4 arg4, T5 arg5, T6 arg6, T7 arg7, T8 arg8, T9 arg9, T10 arg10,
            ThreadSafetyMode mode) {
        this.valueFactory = valueFactory;
        t1 = arg1;
        t2 = arg2;
        t3 = arg3;
        t4 = arg4;
        t5 = arg5;
        t6 = arg6;
        t7 = arg7;
        t8 = arg8;
        t9 = arg9;
        t10 = arg10;
    }

    /**
     * @param valueFactory lazy - initialized function
     * @param R            반환 타입
     */
    public Lazy(Func.Zero<R> valueFactory) {
        this(valueFactory, ThreadSafetyMode.ExecutionAndPublication);
    }

    private <T> Lazy(Func.One<T, R> valueFactory, T arg) {
        this.valueFactory = valueFactory;
        this.t1 = arg;
    }

    public <T1, T2> Lazy(Func.Two<T1, T2, R> valueFactory, T1 arg1, T2 arg2) {
        this.valueFactory = valueFactory;
        this.t1 = arg1;
        this.t2 = arg2;
    }

    public <T1, T2, T3> Lazy(Func.Three<T1, T2, T3, R> valueFactory, T1 arg1, T2 arg2, T3 arg3) {
        this.valueFactory = valueFactory;
        t1 = arg1;
        t2 = arg2;
        t3 = arg3;
    }

    public <T1, T2, T3, T4> Lazy(Func.Four<T1, T2, T3, T4, R> valueFactory, T1 arg1, T2 arg2, T3 arg3, T4 arg4) {
        this.valueFactory = valueFactory;
        t1 = arg1;
        t2 = arg2;
        t3 = arg3;
        t4 = arg4;
    }

    public <T1, T2, T3, T4, T5> Lazy(Func.Five<T1, T2, T3, T4, T5, R> valueFactory,
                                     T1 arg1, T2 arg2, T3 arg3, T4 arg4, T5 arg5) {
        this.valueFactory = valueFactory;
        t1 = arg1;
        t2 = arg2;
        t3 = arg3;
        t4 = arg4;
        t5 = arg5;
    }

    public <T1, T2, T3, T4, T5, T6> Lazy(Func.Six<T1, T2, T3, T4, T5, T6, R> valueFactory,
                                         T1 arg1,
                                         T2 arg2,
                                         T3 arg3,
                                         T4 arg4,
                                         T5 arg5,
                                         T6 arg6) {
        this.valueFactory = valueFactory;
        t1 = arg1;
        t2 = arg2;
        t3 = arg3;
        t4 = arg4;
        t5 = arg5;
        t6 = arg6;
    }

    public <T1, T2, T3, T4, T5, T6, T7> Lazy(Func.Seven<T1, T2, T3, T4, T5, T6, T7, R> valueFactory,
                                             T1 arg1,
                                             T2 arg2,
                                             T3 arg3,
                                             T4 arg4,
                                             T5 arg5,
                                             T6 arg6,
                                             T7 arg7) {
        this.valueFactory = valueFactory;
        t1 = arg1;
        t2 = arg2;
        t3 = arg3;
        t4 = arg4;
        t5 = arg5;
        t6 = arg6;
        t7 = arg7;
    }

    public <T1, T2, T3, T4, T5, T6, T7, T8> Lazy(Func.Eight<T1, T2, T3, T4, T5, T6, T7, T8, R> valueFactory,
                                                 T1 arg1,
                                                 T2 arg2,
                                                 T3 arg3,
                                                 T4 arg4,
                                                 T5 arg5,
                                                 T6 arg6,
                                                 T7 arg7,
                                                 T8 arg8) {
        this.valueFactory = valueFactory;
        t1 = arg1;
        t2 = arg2;
        t3 = arg3;
        t4 = arg4;
        t5 = arg5;
        t6 = arg6;
        t7 = arg7;
        t8 = arg8;
    }

    public <T1, T2, T3, T4, T5, T6, T7, T8, T9> Lazy(Func.Nine<T1, T2, T3, T4, T5, T6, T7, T8, T9, R> valueFactory,
                                                     T1 arg1,
                                                     T2 arg2,
                                                     T3 arg3,
                                                     T4 arg4,
                                                     T5 arg5,
                                                     T6 arg6,
                                                     T7 arg7,
                                                     T8 arg8,
                                                     T9 arg9) {
        this.valueFactory = valueFactory;
        t1 = arg1;
        t2 = arg2;
        t3 = arg3;
        t4 = arg4;
        t5 = arg5;
        t6 = arg6;
        t7 = arg7;
        t8 = arg8;
        t9 = arg9;
    }

    public <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> Lazy(Func.Ten<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R> valueFactory,
                                                          T1 arg1,
                                                          T2 arg2,
                                                          T3 arg3,
                                                          T4 arg4,
                                                          T5 arg5,
                                                          T6 arg6,
                                                          T7 arg7,
                                                          T8 arg8,
                                                          T9 arg9,
                                                          T10 arg10) {
        this.valueFactory = valueFactory;
        t1 = arg1;
        t2 = arg2;
        t3 = arg3;
        t4 = arg4;
        t5 = arg5;
        t6 = arg6;
        t7 = arg7;
        t8 = arg8;
        t9 = arg9;
        t10 = arg10;
    }

    public R value() {
        //매번 호출하여 생성하는 값을 Thread mode에 따라 적용되도록 적용
        if (!isValueCreated) {
            if (this.valueFactory instanceof Func.Zero) {
                holder = ((Func.Zero<R>) this.valueFactory).apply();
            } else if (this.valueFactory instanceof Func.One) {
                holder = (R) ((Func.One) this.valueFactory).apply(t1);
            } else if (this.valueFactory instanceof Func.Two) {
                holder = (R) ((Func.Two) this.valueFactory).apply(t1, t2);
            } else if (this.valueFactory instanceof Func.Three) {
                holder = (R) ((Func.Three) this.valueFactory).apply(t1, t2, t3);
            } else if (this.valueFactory instanceof Func.Four) {
                holder = (R) ((Func.Four) this.valueFactory).apply(t1, t2, t3, t4);
            } else if (this.valueFactory instanceof Func.Five) {
                holder = (R) ((Func.Five) this.valueFactory).apply(t1, t2, t3, t4, t5);
            } else if (this.valueFactory instanceof Func.Five) {
                holder = (R) ((Func.Six) this.valueFactory).apply(t1, t2, t3, t4, t5, t6);
            } else if (this.valueFactory instanceof Func.Five) {
                holder = (R) ((Func.Seven) this.valueFactory).apply(t1, t2, t3, t4, t5, t6, t7);
            } else if (this.valueFactory instanceof Func.Five) {
                holder = (R) ((Func.Eight) this.valueFactory).apply(t1, t2, t3, t4, t5, t6, t7, t8);
            } else if (this.valueFactory instanceof Func.Five) {
                holder = (R) ((Func.Nine) this.valueFactory).apply(t1, t2, t3, t4, t5, t6, t7, t8, t9);
            } else if (this.valueFactory instanceof Func.Five) {
                holder = (R) ((Func.Ten) this.valueFactory).apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10);
            }
            isValueCreated = true;
        }
        return holder;
    }
}
