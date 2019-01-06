package io.codebit.support.util.function;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;


public interface Func
{
	//Supplier or Callable
	@FunctionalInterface
	public interface Zero<R> extends Supplier<R> 
	{
		R apply();
		
		default R get()
		{
			return apply();
		}
	}
	
	@FunctionalInterface
	public interface One<T, R> extends Function<T, R>
	{
	}
	
	@FunctionalInterface
	public interface Two<T1, T2, R> extends BiFunction<T1, T2, R>
	{
	}
	
	@FunctionalInterface
	public interface Three<T1, T2, T3, R>
	{
		R apply(T1 t1, T2 t2, T3 t3);
		
//		default <V, V2, V3> Three<V, V2, V3, R> compose(Function<? super V, ? extends T1> before) {
//	        Objects.requireNonNull(before);
//	        return (V m, V2 n, V3 v) -> apply(m, n, before.apply(v));
//	    }
//
//	    default <V> Three<T, V> andThen(Function<? super R, ? extends V> after) {
//	        Objects.requireNonNull(after);
//	        return (ModelAndViewContainer m, NativeWebRequest n, T t)  -> after.apply(apply(m, n ,t));
//	    }
	}
	
	@FunctionalInterface
	public interface Four<T1, T2, T3, T4, R>
	{
		R apply(T1 t1, T2 t2, T3 t3, T4 t4);
	}
	
	@FunctionalInterface
	public interface Five<T1, T2, T3, T4, T5, R>
	{
		R apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5);
	}
	
	@FunctionalInterface
	public interface Six<T1, T2, T3, T4, T5, T6, R>
	{
		R apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6);
	}
	
	@FunctionalInterface
	public interface Seven<T1, T2, T3, T4, T5, T6, T7, R>
	{
		R apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7);
	}
	
	@FunctionalInterface
	public interface Eight<T1, T2, T3, T4, T5, T6, T7, T8, R>
	{
		R apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8);
	}
	
	@FunctionalInterface
	public interface Nine<T1, T2, T3, T4, T5, T6, T7, T8, T9, R>
	{
		R apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8, T9 t9);
	}
	
	@FunctionalInterface
	public interface Ten<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R>
	{
		R apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8, T9 t9, T10 t10);
	}
}
