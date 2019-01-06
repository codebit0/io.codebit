package io.codebit.support.image;

import java.io.IOException;

import javax.imageio.ImageReader;

import io.codebit.support.image.options.AbWriteOption;

public class BMP extends BitmapImage
{
	BMP(ImageReader reader, Bitmap.Format format) throws IOException
	{
		super(reader, format);
	}

	public static class WriteOption extends AbWriteOption
	{
		@Override
		public Bitmap.Format format()
		{
			return Bitmap.Format.BMP;
		}
	}
}
