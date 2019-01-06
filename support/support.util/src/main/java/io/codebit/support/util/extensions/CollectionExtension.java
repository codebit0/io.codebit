package io.codebit.support.util.extensions;

import java.util.Collection;
import java.util.Iterator;

public class CollectionExtension
{
	/**
	 * Array 의 데이터를 glue 으로 연결하는 하나의 문자열로 반환합니다.
	 * @param <T> 배열의 타입
	 * @param collection
	 *            glue으로 연결한 문자배열입니다.
	 * @param glue
	 *            연결문자입니다.
	 * @return 배열을 glue로 연결한 문자입니다.
	 */
	public static <T> String join(Collection<T> collection, CharSequence glue)
	{
		StringBuffer rt = new StringBuffer();
		Iterator<T> iterator = collection.iterator();
		while(iterator.hasNext())
		{
			rt.append(glue);
			rt.append(iterator.next());
		}
		if(rt.length() > 0 && glue.length() > 0)
		{
			rt.delete(0, glue.length());
		}
		return rt.toString();
	}
	
	public static boolean isNullOrEmpty(Collection<?> collection)
	{
		return (collection == null || collection.size() == 0);
	}
}
