package io.codebit.support.io.extensions;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

public class InputStreamExtension
{
	public static byte[] getBytes(InputStream in, int length) throws IOException
	{
		byte[] bf = new byte[length];
		if(in.read(bf) < 0)
		{
			throw new UnsupportedOperationException("InputStream의 마지막에 도달함");
		}
		return bf;
	}
	
	/**
	 * Gets the unsigned bytes.
	 * 
	 * @param in
	 *            the in
	 * @param length
	 *            the length
	 * @return the unsigned bytes
	 * @throws IOException
	 *             the IO exception
	 */
	public static short[] getUBytes(InputStream in, int length) throws IOException
	{
		short[] bf = new short[length];
		for(int i=0; i < length; i++)
		{
			int read = in.read();
			if(read < 0)
			{
				read = 0;
			}
			bf[i] = (short)(read & 0xFF);
		}
		return bf;
	}
	
	/**
	 * Gets the chars.
	 * 
	 * @param in
	 *            the in
	 * @param length
	 *            the length
	 * @return the chars
	 * @throws IOException
	 *             the IO exception
	 */
	public static char[] getChars(InputStream in, int length) throws IOException
	{
		char[] bf = new char[length];
		for(int i=0; i < length;i++)
		{
			int read = in.read();
			if(read > -1)
			{
				bf[i] = (char)read;
			}else
			{
				break;
			}
		}
		return bf;
	}
	
	public static String getString(InputStream in, int length ) throws IOException
	{
		byte[] bf = new byte[length];
		if(in.read(bf) < 0)
		{
			throw new UnsupportedOperationException("InputStream의 마지막에 도달함");
		}
		return new String(bf);
	}
	
	public static String getString(InputStream in, long length ) throws IOException
	{
		length = (length > Integer.MAX_VALUE)? Integer.MAX_VALUE : length;
		StringBuffer bf = new StringBuffer((int) length);
		for(long i=0; i < length;i++)
		{
			int read = in.read();
			if(read > -1)
			{
				bf.append((char)read);
			}else
			{
				break;
			}
		}
		return bf.toString();
	}
	
	/**
	 * 0~ 18,446,744,073,709,551,615 unsigned long. 64-bit unsigned integer
	 * 입력 스트림에서 8byte를 읽어 BigInteger(java built type에 unsigned형이 없으므로)로 반환
	 * @param in
	 *            입력 스트림
	 * @return the unsigned long
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static BigInteger getULong(InputStream in) throws IOException
	{
		//8byte
		byte[] b = new byte[8];
		in.read(b);
		return  BigInteger.valueOf((b[0] & 0xFF) | ((b[1] & 0xFF)<<8) | ((b[2] & 0xFF)<<16) |((b[3] & 0xFF)<<24)
				| ((b[4] & 0xFF)<<32 ) | ((b[5] & 0xFF)<<40) | ((b[6] & 0xFF)<<48) |((b[7] & 0xFF)<<52));
	}
	
	/**
	 * 0 to 4,294,967,295 unsigned 32bit integer
	 * 입력 스트림에서 4byte를 읽어 short(java built type에 unsigned형이 없으므로)로 반환
	 * @param in
	 *            입력 스트림
	 * @param length
	 *            the length
	 * @return the unsigned int
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static long getUInt(InputStream in, int length) throws IOException
	{
		//4byte
		long rtn = 0L;
		for(int i=0; i < length; i++)
		{
			rtn += in.read() << ((i)*8);
		}
		return rtn;
	}
	
	/**
	 * 0 to 4,294,967,295 unsigned 32bit integer
	 * 입력 스트림에서 4byte를 읽어 long (java built type에 unsigned형이 없으므로)로 반환
	 * @param in
	 *            입력 스트림
	 * @return the unsigned int
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static long getUInt(InputStream in) throws IOException
	{
		//4byte
		byte[] b = new byte[4];
		if(in.read(b) < 0)
		{
			throw new UnsupportedOperationException("InputStream의 마지막에 도달함");
		}
		return (long) (b[0] & 0xFF) | ((b[1] & 0xFF)<<8) | ((b[2] & 0xFF)<<16) |((b[3] & 0xFF)<<24);
	}
	
	/*public static int getUnsignedShort(InputStream in, int length) throws IOException
	{
		int rtn = 0;
		for(int i=0; i < length; i++)
		{
			rtn += in.read() << ((i)*8);
		}
		return rtn;
	}*/
	
	/**
	 * 0 ~ 65,535 unsigned short. 16bit unsigned integer
	 * 입력 스트림에서 2byte를 읽어 int (java built type에 unsigned형이 없으므로)로 반환
	 * @param in
	 *            입력 스트림
	 * @return the unsigned short
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static int getUShort(InputStream in) throws IOException
	{
		//2byte
		byte[] b = new byte[2];
		if(in.read(b) < 0)
		{
			throw new UnsupportedOperationException("InputStream의 마지막에 도달함");
		}
		return (int) (b[0] & 0xFF) | ((b[1] & 0xFF)<<8);
	}
	
	/**
	 * 0~255 unsigned byte. 8-bit unsigned integer
	 * 입력 스트림에서 1byte를 읽어 short (java built type에 unsigned형이 없으므로)로 반환
	 * @param in
	 *            입력 스트림
	 * @return the unsigned byte
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static short getUByte(InputStream in) throws IOException
	{
		byte[] b = new byte[1];
		if(in.read(b) < 0)
		{
			throw new UnsupportedOperationException("InputStream의 마지막에 도달함");
		}
		return (short) (b[0] & 0xFF);
	}
}
