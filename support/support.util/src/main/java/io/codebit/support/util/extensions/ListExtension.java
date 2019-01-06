package io.codebit.support.util.extensions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class ListExtension
{

	/**
	 * 리스트의 합집합을 계산합니다.
	 *
	 * @param <T>
	 *            the generic type
	 * @param list1
	 *            the list1
	 * @param list2
	 *            the list2
	 * @return 리스트의 합집합을 반환
	 */
	public static <T> List<T> union(List<T> list1, List<T> list2)
	{
		Set<T> set = new HashSet<T>();

		set.addAll(list1);
		set.addAll(list2);

		return new ArrayList<T>(set);
	}

	/**
	 * List의 교집합을 계산합니다.
	 *
	 * @param <T>
	 *            the generic type
	 * @param list1
	 *            the list1
	 * @param list2
	 *            the list2
	 * @return List의 교집합 반환
	 */
	public static <T> List<T> intersection(List<T> list1, List<T> list2)
	{
		List<T> list = new ArrayList<T>();

		for (T t : list1)
		{
			if (list2.contains(t))
			{
				list.add(t);
			}
		}

		return list;
	}
	
	
	/**
	 * 차집합을 구함
	 *
	 * @param <T> the generic type
	 * @param list1 the list1
	 * @param list2 the list2
	 * @return the list
	 */
	public static <T> List<T> subtract(List<T> list1, List<T> list2)
	{
		ArrayList<T> tmp = new ArrayList<T>(list1);
		tmp.removeAll(list2);
		return tmp;
	}
	
	/**
	 * 유일값을 계산함
	 *
	 * @param <T> the generic type
	 * @param list the list
	 * @return the list
	 */
	public static <T> List<T> distinct(List<T> list)
	{
		List<T> listTmp = new ArrayList<T>();

		for (T t : list)
		{
			if (!listTmp.contains(t))
			{
				listTmp.add(t);
			}
		}

		return listTmp;
	}
}
