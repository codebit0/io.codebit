package io.codebit.support.lang.extensions;

import java.nio.ByteBuffer;

public class LongExtension
{

	/**
	 * Long 을 byte배열로 변
	 * @param src 변환할 Long number
	 * @return byte array
	 */
	public static byte[] toBytes(Long src)
	{
		return ByteBuffer.allocate(8).putLong(src).array();
	}
	
	/**
	 * 문자열을 long 타입으로 변환합니다. 변환에 실패할 경우 NumberFormatException 을 발생합니다.
	 * 
	 * @param src
	 *            원본 문자열
	 * @return 문자열을 long 타입으로 변환한 값
	 * @exception NumberFormatException
	 *                문자를 숫자로 변환 시 실패할 경우 발생
	 */
	public static Long toLong(String src)
	{
		return Long.parseLong(src);
	}

	/**
	 * 문자열을 Long타입으로 변환 실패하면 null을 반환합니다.
	 * 
	 * @param src
	 *            Integer 변환할 문자열
	 * @return 변환에 실패하면 null 그렇지 않으면 변환된 Integer 값을 반환합니다.
	 */
	public static Long tryLong(String src)
	{
		try
		{
			return Long.parseLong(src);
		} catch (NumberFormatException e)
		{
			return null;
		}
	}

	/**
	 * 문자열을 Long타입으로 변환, 실패하면 defaultValue 반환합니다.
	 * 
	 * @param src
	 *            Integer 변환할 문자열
	 * @param defaultValue
	 *            변환에 실패할 경우 반환할 기본값
	 * @return 변환에 실패하면 defaultValue 그렇지 않으면 변환된 Integer 값을 반환합니다.
	 */
	public static Long tryLong(String src, long defaultValue)
	{
		try
		{
			return Long.parseLong(src);
		} catch (NumberFormatException e)
		{
			return defaultValue;
		}
	}

	public static Long tryLong(Object src)
	{
		if(src == null)
			return null;
		if(src instanceof Long)
			return (Long) src;
		return tryLong(src.toString());
	}
}
