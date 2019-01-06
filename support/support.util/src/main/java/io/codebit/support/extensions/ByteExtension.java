package io.codebit.support.extensions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * The Class byte의 signed , unsigend 를 포함하는 형변환을 위한 확장 메서드.
 * http://www.javadom.com/tutorial/serialize/htonl.html
 */
public abstract class ByteExtension
{

	public static String toString(byte[] _byte)
	{
		return new String(_byte);
	}

	/**
	 * Gets the unsigned long.
	 *
	 * @param bytes the bytes
	 * @return the unsigned long
	 */
	public static BigInteger toULong(byte[] bytes)
	{
		return new BigInteger(bytes);
	}

	public static long toLong(byte[] bytes)
	{
		long val = 0;
		int j = bytes.length -1;
		j = Math.min(j, 7);
		for (int i = j; i >= 0; i--)
		{
			if(i != 7)
				val |= ((long)(bytes[j - i] & 0xFF) << ((i) * 8));
			else
				val |= ((long)(bytes[j - i] ) << ((i) * 8));
		}
		return val;
	}
	
	/**
	 * Gets the unsigned int.
	 *
	 * @param in            the in
	 * @return the unsigned int
	 */
	public static long toUInt(byte[] in)
	{
		// 4byte
		long rtn = 0L;
		int length = in.length;
		for (int i = 0; i < length; i++)
		{
			rtn |= (in[i] & 0xFF) << ((i) * 8);
		}
		return rtn;
	}

	public static int toInt(byte[] bytes)
	{
		int val = 0;
		int j = bytes.length -1;
		j = Math.min(j, 3);
		for (int i = j; i >= 0; i--)
		{
			if(i != 3)
				val |= (bytes[j - i] & 0xFF) << ((i) * 8);
			else
				val |= (bytes[j - i]) << ((i) * 8);
		}
		return val;
	}

	/**
	 * Gets the unsigned short.
	 *
	 * @param bytes the bytes
	 * @return the unsigned short
	 */
	public static int toUShort(byte[] bytes)
	{
		return new BigInteger(bytes).intValue();
	}

	public static short toShort(byte[] bytes)
	{
		return new BigInteger(bytes).shortValue();
	}

	/**
	 * Gets the unsigned byte.
	 * 
	 * @param _byte
	 *            the _byte
	 * @return the unsigned byte
	 */
	public static short toUByte(byte[] _byte)
	{
		// 1byte
		return (short) (_byte[0] & 0xFF);
	}

	public static byte toByte(byte[] bytes)
	{
		return bytes[0];
	}
	
	public static String toBase64String(byte[] bytes)
	{
		Encoder encoder = Base64.getEncoder();
		return encoder.encodeToString(bytes);
	}

	public static byte[] base64Encode(byte[] bytes)
	{
		Encoder encoder = Base64.getEncoder();
		return encoder.encode(bytes);
	}

	public static byte[] base64Decode(byte[] base64)
	{
		Decoder decoder = Base64.getDecoder();
		return decoder.decode(base64);
	}

	public static byte[] compress(byte[] src) throws IOException
	{

		Deflater deflater = new Deflater();

		deflater.setLevel(Deflater.BEST_COMPRESSION);
		deflater.setInput(src);
		deflater.finish();

		ByteArrayOutputStream bao = new ByteArrayOutputStream(src.length);
		byte[] buf = new byte[1024];
		while (!deflater.finished())
		{
			int compByte = deflater.deflate(buf);
			bao.write(buf, 0, compByte);
		}

		bao.close();
		deflater.end();

		return bao.toByteArray();
	}

	public static byte[] decompress(byte[] data) throws IOException, DataFormatException
	{
		Inflater inflater = new Inflater();
		inflater.setInput(data);

		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		while (!inflater.finished())
		{
			int compByte = inflater.inflate(buf);
			bao.write(buf, 0, compByte);
		}
		bao.close();

		inflater.end();

		return bao.toByteArray();
	}
}
