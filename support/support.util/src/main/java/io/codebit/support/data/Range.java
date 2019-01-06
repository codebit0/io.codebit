package io.codebit.support.data;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Range<T extends Comparable<? super T>>
{

//	public static final byte MINIMUN_LOW_BOUNDS = 0x1;
//	public static final byte MINIMUN_UPPER_BOUNDS = 0x2;
//	public static final byte MAXIMUN_LOW_BOUNDS = 0x4;
//	public static final byte MAXIMUN_UPPER_BOUNDS = 0x8;

	public enum IntersectFlag
	{
		MINIMUN_LOW_BOUNDS((byte) 0x1), MINIMUN_UPPER_BOUNDS((byte) 0x2), MAXIMUN_LOW_BOUNDS((byte) 0x4),
		MAXIMUN_UPPER_BOUNDS((byte) 0x8);

		private byte flag;

		public byte getFlag() {
			return flag;
		}

		private IntersectFlag(byte flags)
		{
			this.flag = flags;
		}
	}

	private T minimum;

	private T maximum;

	//TODO 추후 지원
//	public static <T extends Comparable<? super T>> Range<T> ofMinimum(T sameMinMax)
//	{
//		return new Range<T>(sameMinMax, sameMinMax);
//	}
//	
//	public static <T extends Comparable<? super T>> Range<T> ofMaximum(T sameMinMax)
//	{
//		return new Range<T>(sameMinMax, sameMinMax);
//	}
	
	public static <T extends Comparable<? super T>> Range<T> of(T sameMinMax)
	{
		return new Range<T>(sameMinMax, sameMinMax);
	}
	
	public static <T extends Comparable<? super T>> Range<T> of(T minimum, T maximum)
	{
		return new Range<T>(minimum, maximum);
	}

	public Range(T minimum, T maximum)
	{
		if (minimum == null)
		{
			throw new IllegalArgumentException("minimum cannot be null.");
		}
		if (maximum == null)
		{
			throw new IllegalArgumentException("maximum cannot be null.");
		}
//		if (minimum == null && maximum == null)
//		{
//			throw new IllegalArgumentException("minimum and maximum cannot be null.");
//		}

		this.minimum = minimum;
		this.maximum = maximum;

		if (minimum.compareTo(maximum) > 0)
		{
			throw new UnsupportedOperationException("Invalid Range");
		}
	}

	public T Minimum()
	{
		return minimum;
	}

	public T Maximum()
	{
		return maximum;
	}

	public boolean contains(T value)
	{
		// this.minimum 이 value 보다 작으면 -1 같으면 0
		return this.minimum.compareTo(value) <= 0 && this.maximum.compareTo(value) >= 0;
	}

	/**
	 * Intersects with. 현재 Range객체와 파라미터 range객체의 교차여부를 구함 같으면 0 작으면 -1 min값이
	 * 작으면 1 max값이 크면 2 양쪽 모두 크면 3
	 * 
	 * @param range
	 *            the range
	 * @return the byte
	 */
	public byte intersectsFlags(Range<T> range)
	{
		byte flags = 0x0;
		int minimunCompare = this.minimum.compareTo(range.minimum);
		int maximunCompare = this.maximum.compareTo(range.maximum);
		if (minimunCompare > 0)
		{
			flags |= IntersectFlag.MINIMUN_LOW_BOUNDS.flag;
		} else if (minimunCompare < 0)
		{
			flags |= IntersectFlag.MINIMUN_UPPER_BOUNDS.flag;
		}
		if (maximunCompare > 0)
		{
			flags |= IntersectFlag.MAXIMUN_LOW_BOUNDS.flag;
		} else if (maximunCompare < 0)
		{
			flags |= IntersectFlag.MAXIMUN_UPPER_BOUNDS.flag;
		}
		return flags;
	}

	public EnumSet<IntersectFlag> intersects(Range<T> range)
	{
		EnumSet<IntersectFlag> set = EnumSet.noneOf(IntersectFlag.class);
		int minimunCompare = this.minimum.compareTo(range.minimum);
		int maximunCompare = this.maximum.compareTo(range.maximum);
		if (minimunCompare > 0)
		{
			set.add(IntersectFlag.MINIMUN_LOW_BOUNDS);
		} else if (minimunCompare < 0)
		{
			set.add(IntersectFlag.MINIMUN_UPPER_BOUNDS);
		}
		if (maximunCompare > 0)
		{
			set.add(IntersectFlag.MAXIMUN_LOW_BOUNDS);
		} else if (maximunCompare < 0)
		{
			set.add(IntersectFlag.MAXIMUN_UPPER_BOUNDS);
		}
		return set;
	}

	/**
	 * 합집합
	 *
	 * @param range
	 *            the range
	 * @return the range
	 */
	public Range<T> union(Range<T> range)
	{
		T minimum = this.minimum.compareTo(range.minimum) == -1 ? this.minimum : range.Minimum();
		T maximum = this.maximum.compareTo(range.maximum) == -1 ? this.maximum : range.Maximum();
		return new Range<T>(minimum, maximum);
	}

	/**
	 * 차집합
	 *
	 * @param range
	 *            the range
	 * @return the range
	 */
	public Range<T> relative(Range<T> range)
	{
		T minimum = this.minimum;
		T maximum = this.maximum;

		// 원본이 오른쪽에 잘라낼 영역이 왼쪽 위치
		int rightOrLeft = this.minimum.compareTo(range.minimum);
		if (rightOrLeft == 0)
			rightOrLeft = this.maximum.compareTo(range.maximum);
		if (rightOrLeft == -1)
		{
			maximum = range.minimum;
		} else if (rightOrLeft == 1)
		{
			minimum = range.maximum;
		} else
		{
			minimum = this.minimum;
			maximum = this.minimum;
		}
		return new Range<T>(minimum, maximum);
	}

	/**
	 * 교집합
	 *
	 * @param range
	 *            the range
	 * @return the range
	 */
	public Range<T> intersection(Range<T> range)
	{
		// minimum은 큰쪽은 maximun은 큰쪽을 사용
		T minimum = this.minimum.compareTo(range.minimum) == -1 ? range.Minimum() : this.minimum;
		T maximum = this.maximum.compareTo(range.maximum) == -1 ? this.maximum : range.Maximum();
		return new Range<T>(minimum, maximum);
	}

	public Range<T> extendTo(T value)
	{
		if (this.minimum.compareTo(value) > 0)
		{
			return new Range<T>(value, maximum);
		} else if (this.maximum.compareTo(value) < 0)
		{
			return new Range<T>(minimum, value);
		}
		return this;
	}

	public Stream<T> split(Function<T, T> spliter, boolean reverse)
	{
		Stream<T> stream = StreamSupport.stream(Spliterators.spliteratorUnknownSize(new Iterator<T>()
		{
			T start = (reverse) ? Range.this.maximum : Range.this.minimum;
			T current = start;

			@Override
			public boolean hasNext()
			{
				if ((!reverse && Range.this.maximum.compareTo(current) <= 0)
						|| (reverse && Range.this.minimum.compareTo(current) >= 0))
				{
					return false;
				}
				return true;
			}

			@Override
			public T next()
			{
				T next = spliter.apply(current);
				// 레퍼런스 equals 최초동작 확인
				if (this.current == start)
				{
					current = next;
					return start;
				}
				current = next;
				return current;
			}
		},
			Spliterator.IMMUTABLE),
			false);
		return stream;
	}

	public Stream<T> split(Function<T, T> spliter)
	{
		return split(spliter, false);
	}

//	// / <summary>
//	// / Compares the range to another range.
//	// / </summary>
//	// / <param name="range">A different range.</param>
//	// / <returns>A value indicating whether the ranges are equal.</returns>
//	public boolean equals(Range<T> range)
//	{
//		return this.equals((Object)range);
//	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean equals(Object obj)
	{
		if (obj == null || !(obj instanceof Range))
			return false;
		Range<T> range = null;
		try
		{
			range = (Range<T>) obj;
		} catch (Exception e)
		{
			return false;
		}
		return this == obj || this.maximum.equals(range.maximum) && this.minimum.equals(range.minimum);
	}

	@Override
	public int hashCode()
	{
		int num = 0x5374e861;
		num = (-1521134295 * num) + this.minimum.hashCode();
		return ((-1521134295 * num) + this.maximum.hashCode());
	}

	@Override
	public String toString()
	{
		return this.minimum.toString() + " " + this.maximum.toString();
	}
}
