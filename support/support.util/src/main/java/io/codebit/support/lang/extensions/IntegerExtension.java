package io.codebit.support.lang.extensions;

public class IntegerExtension {

	/**
	 * 문자열을 int 타입으로 변환합니다. 변환에 실패할 경우 NumberFormatException 을 발생합니다.
	 * 
	 * @param src
	 *            원본 문자열
	 * @return 문자열을 int 타입으로 변환한 값
	 * @exception NumberFormatException
	 *                문자를 숫자로 변환 시 실패할 경우 발생
	 */
	public static Integer toInt(String src)
	{
		return Integer.parseInt(src);
	}

	/**
	 * 문자열을 int타입으로 변환, 실패하면 null을 반환합니다.
	 * 
	 * @param src
	 *            Integer 변환할 문자열
	 * @return 변환에 실패하면 null 그렇지 않으면 변환된 Integer 값을 반환합니다.
	 */
	public static Integer tryInt(String src)
	{
		try
		{
			return Integer.parseInt(src);
		} catch (NumberFormatException e)
		{
			return null;
		}
	}

	/**
	 * 문자열을 int타입으로 변환, 실패하면 defaultValue을 반환합니다.
	 * 
	 * @param src
	 *            Integer 변환할 문자열
	 * @param defaultValue
	 *            변환에 실패할 경우 반환할 기본값
	 * @return 변환에 실패하면 defaultValue 그렇지 않으면 변환된 Integer 값을 반환합니다.
	 */
	public static Integer tryInt(String src, int defaultValue)
	{
		try
		{
			return Integer.parseInt(src);
		} catch (NumberFormatException e)
		{
			return defaultValue;
		}
	}
	
	public static Integer tryInt(Object src)
	{
		if(src == null)
			return null;
		if(src instanceof Integer)
			return (Integer) src;
		return tryInt(src.toString());
	}

	public static Integer tryInt(Object src, int defaultValue)
	{
		if(src == null)
			return null;
		if(src instanceof Integer)
			return (Integer) src;
		return tryInt(src.toString(), defaultValue);
	}
}
