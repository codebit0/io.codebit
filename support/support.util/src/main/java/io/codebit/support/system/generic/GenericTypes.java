package io.codebit.support.system.generic;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *  The Class GenericTypes Runtime Checker.
 *  <pre>{@code
 *  public static void main(String[] args)
 *	{
 *		Param2<Integer, String> param = new GenericTypes.Param2<Integer, String>(){};
 *		List<Type> typeArguments2 = param.TypeArguments();
 *		Class<?> i=  (Class<?>) typeArguments2.get(0);
 *		Class<String> s =  (Class<String>) typeArguments2.get(1);
 *		System.out.println(i+" "+ s);
 *	}
 *	}</pre>
 */
public abstract class GenericTypes
{
	private List<Type> _genericTypeParamsCache = null;
	private int size;
	
	private GenericTypes(){}
	
	public List<Type> TypeArguments()
	{
		if(_genericTypeParamsCache == null)
		{
			_genericTypeParamsCache = new ArrayList<Type>();
			Type superclass = this.getClass().getGenericSuperclass();
			Type[] typeArguments = ((java.lang.reflect.ParameterizedType)superclass).getActualTypeArguments();
			_genericTypeParamsCache = Collections.unmodifiableList(Arrays.asList(typeArguments));
		}
		return _genericTypeParamsCache;
	}
	
	public int size()
	{
		return size;
	}
	
	
	public static abstract class Param1<T1> extends GenericTypes 
	{
		public Param1()
		{
			super.size = 1;
		}
	}
	
	public static abstract class Param2<T1, T2> extends GenericTypes
	{
		public Param2()
		{
			super.size = 2;
		}
	}
	
	public static abstract class Param3<T1, T2, T3> extends GenericTypes
	{
		public Param3()
		{
			super.size = 3;
		}
	}
	
	public static abstract class Param4<T1, T2, T3, T4> extends GenericTypes
	{
		public Param4()
		{
			super.size = 4;
		}
	}
	
	public static abstract class Param5<T1, T2, T3, T4, T5> extends GenericTypes
	{
		public Param5()
		{
			super.size = 5;
		}
	}
	
	public static abstract class Param6<T1, T2, T3, T4, T5, T6> extends GenericTypes
	{
		public Param6()
		{
			super.size = 6;
		}
	}
	
	public static abstract class Param7<T1, T2, T3, T4, T5, T6, T7> extends GenericTypes
	{
		public Param7()
		{
			super.size = 7;
		}
	}
	
	public static abstract class Param8<T1, T2, T3, T4, T5, T6, T7, T8> extends GenericTypes
	{
		public Param8()
		{
			super.size = 8;
		}
	}
	
	public static abstract class Param9<T1, T2, T3, T4, T5, T6, T7, T8, T9> extends GenericTypes
	{
		public Param9()
		{
			super.size = 9;
		}
	}
	
	public static abstract class Param10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> extends GenericTypes
	{
		public Param10()
		{
			super.size = 10;
		}
	}
}
