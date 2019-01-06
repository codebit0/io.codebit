package io.codebit.support.io.extensions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public abstract class SerializableExtension
{
	/*public static <T extends Serializable> ByteArrayOutputStream serialize(T value)
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try (ObjectOutputStream out = new ObjectOutputStream(bos))
		{
			out.writeObject(value);
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		return bos;
	}*/
	
	public static <T extends Serializable> byte[] serialize(T value)
	{
		//ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutputStream out = new ObjectOutputStream(bos))
		{
			out.writeObject(value);
			return bos.toByteArray();
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Serializable> T deserialize(byte[] object)
	{
		try (ByteArrayInputStream bis = new ByteArrayInputStream(object); ObjectInputStream in = new ObjectInputStream(bis))
		{
			return (T) in.readObject();
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
