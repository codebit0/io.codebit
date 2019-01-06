package io.codebit.support.lang;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public interface IEnum<T extends Enum<T>>
{
	//public int code = 0;
	//private static T t = 
	public default int code()
	{
		T t =  (T) this;
		//a.getDeclaringClass().
		//return (1L << (((Enum<T>)this).ordinal() -1));
		return t.ordinal();
	}
	
	@SuppressWarnings("unchecked")
	public default T toEnum(int code)
	{
		Class<?> class1 = getClass();
		return null;
		/*T t =  (T) this;
		if(!t.getClass().getSuperclass().equals(Enum.class))
		{
			throw new RuntimeException("not Enum Class"); 
		}
		try
		{
			Method method = t.getDeclaringClass().getMethod("values");
			T[] invoke = (T[])method.invoke(null);
			for (T t2 : invoke)
			{
				if(((IEnum<T>)t2).code() == code)
				{
					return t2;
				}
			}
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{
		}*/
//		throw new IllegalArgumentException(
//			String.format("Enumeration '%s' has no value for 'code = %s'", 
//				t.getDeclaringClass().getName(), 
//				code));
	}
}
