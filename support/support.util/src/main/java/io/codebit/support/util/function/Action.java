package io.codebit.support.util.function;

import java.util.function.Consumer;

public interface Action
{
	//Runnable
	@FunctionalInterface
	public interface Zero extends Runnable
	{
		void accept();
		
		default void run()
		{
			accept();
		}
	}
	
	@FunctionalInterface
	public interface One<T> extends Consumer<T>
	{
	}
	
	@FunctionalInterface
	public interface Two<T1, T2>
	{
		void accept(T1 t1, T2 t2);
	}
	
	@FunctionalInterface
	public interface Three<T1, T2, T3>
	{
		void accept(T1 t1, T2 t2, T3 t3);
	}
	
	@FunctionalInterface
	public interface Four<T1, T2, T3, T4>
	{
		void accept(T1 t1, T2 t2, T3 t3, T4 t4);
	}
	
	@FunctionalInterface
	public interface Five<T1, T2, T3, T4, T5>
	{
		void accept(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5);
	}
	
	@FunctionalInterface
	public interface Six<T1, T2, T3, T4, T5, T6>
	{
		void apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6);
	}
	
	@FunctionalInterface
	public interface Seven<T1, T2, T3, T4, T5, T6, T7>
	{
		void apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7);
	}
	
	@FunctionalInterface
	public interface Eight<T1, T2, T3, T4, T5, T6, T7, T8>
	{
		void apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8);
	}
	
	@FunctionalInterface
	public interface Nine<T1, T2, T3, T4, T5, T6, T7, T8, T9>
	{
		void apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8, T9 t9);
	}
	
	@FunctionalInterface
	public interface Ten<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10>
	{
		void apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8, T9 t9, T10 t10);
	}
}
