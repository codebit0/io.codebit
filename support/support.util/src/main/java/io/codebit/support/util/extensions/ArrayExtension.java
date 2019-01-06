package io.codebit.support.util.extensions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The Class Array 기능 확장을 위한 Class.
 */
public abstract class ArrayExtension
{

	/**
	 * Array 의 데이터를 glue 으로 연결하는 하나의 문자열로 반환합니다.
	 * @param <T> 배열의 타입
	 * @param arrs
	 *            glue으로 연결한 문자배열입니다.
	 * @param glue
	 *            연결문자입니다.
	 * @return 배열을 glue로 연결한 문자입니다.
	 */
	public static <T> String join(T[] arrs, CharSequence glue)
	{
		return join(arrs, glue, 0, arrs.length -1);
	}
	
	public static <T> String join(T[] arrs, CharSequence glue, int start)
	{
		return join(arrs, glue, start, arrs.length -1);
	}
	
	public static <T> String join(T[] arrs, CharSequence glue, int start, int end)
	{
		StringBuffer rt = new StringBuffer();
		if(end > arrs.length - 1)
			end = arrs.length - 1;
		for (int i = start; i < end; i++)
		{
			rt.append(arrs[i].toString());
			rt.append(glue);
		}
		rt.append(arrs[end]);
		return rt.toString();
	}

	/**
	 * @param <T> 배열의 타입
	 * @param arrays 교집합을 구할 array
	 * @return array의 교집합
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] intersection(T[]... arrays)
	{
		List<T> list = new ArrayList<T>();

		int argsSize = arrays.length;
		for (int i = 0; i < argsSize; i++)
		{
			for (int j = 0; j < argsSize  ; j++)
			{
				if(j == i) 
					continue;
				T[] array1 = arrays[i];
				T[] array2 = arrays[j];
				for (int _i = 0; _i < array1.length; _i++)
				{
					for (int _j = 0; _j < array2.length; _j++)
					{
						if (array1[_i].equals(array2[_j]))
						{
							list.add((T) arrays[_i]);
						}
					}
				}
			}
		}

		return (T[]) list.toArray(new Object[list.size()]);
	}

	/**
	 * @param <T> 배열의 타입
	 * @param arrays
	 *            합집합을 구할 array
	 * @return array의 합집합
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] union(T[]... arrays)
	{
		Set<T> set = new HashSet<T>();

		for (T[] array : arrays)
		{
			for (T arr : array)
			{
				set.add(arr);
			}
		}
		return (T[]) set.toArray();
	}

	/**
	 * @param <T> 배열의 타입
	 * @param array
	 *            원본 배열
	 * @param value
	 *            찾을 값
	 * @return 배열에 value값이 있으면 true, 그렇지 않으면 false
	 */
	public static <T> boolean isDuplicated(T[] array, T value)
	{
		int j = array.length;
		for (int i = 0; i < j; i++)
			if (array[i].equals(value))
				return true;
		return false;
	}

	/**
	 * 배열을 List타입으로 변환
	 * @param <T> 배열의 타입
	 * @param array 배열
	 * @return 배열을 List 타입으로 변환하여 반환합니다.
	 */
	public static <T> List<T> asList(T[] array)
	{
		return (List<T>) Arrays.asList(array);
	}
}
