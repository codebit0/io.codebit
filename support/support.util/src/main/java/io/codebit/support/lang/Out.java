package io.codebit.support.lang;

public class Out<T>
{
	public T Value;

	public static <T> Out<T> of(T value)
	{
		return new Out<T>(value);
	}
	
	public Out()
	{
		this.Value = null;
	}
	
	public Out(T value)
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