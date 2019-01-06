package io.codebit.support.query;

import java.util.Optional;


public class RowRange<T extends Number>
{
	private Optional<T> offset = Optional.empty();

	private T limit = null;

	public Optional<T> offset() {
		return offset;
	}

	public T limit() {
		return limit;
	}

	public static <T extends Number> RowRange<T> of(T offset, T limit)
	{
		return new RowRange<T>(offset == null? Optional.empty(): Optional.of(offset), limit);
	}
	
	public static <T extends Number> RowRange<T> of(T limit)
	{
		return new RowRange<T>(limit);
	}
	
	public static <T extends Number> RowRange<T> all()
	{
		return new RowRange<T>();
	}
	
	private RowRange(Optional<T> offset, T limit)
	{
		this.offset = offset;
		this.limit = limit;
	}
	
	private RowRange(T limit)
	{
		this.limit = limit;
	}
	
	private RowRange()
	{
	}
	
	public boolean isAll()
	{
		return offset.equals(Optional.empty()) && limit == null;
	}
}
