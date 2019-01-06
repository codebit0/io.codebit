package io.codebit.support.lang;

import java.util.BitSet;


public interface IEnumFlags<T extends Enum<T> & IEnumFlags<T>>
{
	public class EnumFlags<T extends Enum<T> & IEnumFlags<T>>
	{
		private long bits = 0;
		
		BitSet flags = new BitSet(3);
		
		EnumFlags(IEnumFlags<T> iEnumFlags, T enum2)
		{
			this.bits = iEnumFlags.code() & enum2.code();
		}
		
		public EnumFlags<T> or(T enumType)
		{
			this.bits |= enumType.code();
			return this;
		}
	}
	
	@SuppressWarnings("unchecked")
	public default long code()
	{
		return (1L << (((Enum<T>)this).ordinal() -1));
	}
	
	public default EnumFlags<T> or(T enumType)
	{
		return new EnumFlags<T>(this, enumType);
	}
	
	public default EnumFlags<T> and(T enumType)
	{
		return new EnumFlags<T>(this, enumType);
	}
}
