package io.codebit.support.query;


public class OffsetLimit
{
	private long limit;

	private long offset = 0L;

	public long offset() {
		return offset;
	}

	public long limit() {
		return limit;
	}

	public static OffsetLimit of(long offset, long limit)
	{
		return new OffsetLimit(offset, limit);
	}
	
	public static OffsetLimit of(int limit)
	{
		return new OffsetLimit(limit);
	}
	
	public static  OffsetLimit all()
	{
		return new OffsetLimit(Long.MAX_VALUE);
	}
	
	private OffsetLimit(long offset, long limit)
	{
		this.offset = offset;
		this.limit = limit;
	}
	
	private OffsetLimit(long limit)
	{
		this.limit = limit;
	}
	
	public boolean isAll()
	{
		return offset == 0L && limit == Long.MAX_VALUE;
	}
}
