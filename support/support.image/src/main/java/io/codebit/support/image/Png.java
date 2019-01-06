package io.codebit.support.image;

import java.io.IOException;

import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;

import io.codebit.support.image.options.IWriteOption;


public class Png extends BitmapImage
{
//	public Png(Bitmap bitmap) throws IOException
//	{
//		super(bitmap);
//	}
	
	Png(ImageReader reader, Bitmap.Format format) throws IOException
	{
		super(reader, format);
	}
	
	public static class WriteOption implements IWriteOption
	{
		public WriteOption(Bitmap.Format format) {
			this.format = format;
		}

		private Bitmap.Format format = Bitmap.Format.PNG;

		private boolean ignoreMetadata = false;

		public Bitmap.Format format() {
			return format;
		}

		public WriteOption(boolean ignoreMetadata) {
			this.ignoreMetadata = ignoreMetadata;
		}

		public boolean ignoreMetadata() {
			return ignoreMetadata;
		}

		public WriteOption ignoreMetadata(boolean ignoreMetadata)
		{
			this.ignoreMetadata = ignoreMetadata;
			return this;
		}

		@Override
		public ImageWriteParam build(ImageWriteParam writeParam)
		{
			return writeParam;
		}
	}
}
