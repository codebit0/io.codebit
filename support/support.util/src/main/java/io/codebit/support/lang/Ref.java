package io.codebit.support.lang;

public class Ref<T>
{
	public T Value;

	public static <T> Ref<T> of(T value)
	{
		return new Ref<T>(value);
	}
	
	public Ref()
	{
		this.Value = null;
	}
	
	public Ref(T value)
	{
		this.Value = value;
	}

	/*
	 * public T get() { return Value; }
	 * 
	 * public void set(T anotherValue) { Value = anotherValue; }
	 */

	@Override
	public String toString()
	{
		return Value.toString();
	}

	@Override
	public boolean equals(Object obj)
	{
		return Value.equals(obj);
	}

	@Override
	public int hashCode()
	{
		return Value.hashCode();
	}
}