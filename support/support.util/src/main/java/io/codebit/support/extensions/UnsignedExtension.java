package io.codebit.support.extensions;

public abstract class UnsignedExtension
{
	public static long toUnsigned(int n)
	{
		return n & 0xFFFFFFFFL;
	}

	public static int toUnsigned(short n)
	{
		return n & 0xFFFF;
	}

	public static short toUnsigned(byte n)
	{
		return (short) (n & 0xFF);
	}

	public static boolean addOverflow(int left, int right)
	{
		if (right < 0 && right != Integer.MIN_VALUE)
		{
			throw new RuntimeException();
		} else
		{
			return (~(left ^ right) & (left ^ (left + right))) < 0;
		}
	}

	public static boolean subOverflow(int left, int right)
	{
		if (right < 0)
		{
			throw new RuntimeException();
		} else
		{
			return ((left ^ right) & (left ^ (left - right))) < 0;
		}
	}
}
