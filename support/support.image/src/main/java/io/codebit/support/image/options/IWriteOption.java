package io.codebit.support.image.options;

import javax.imageio.ImageWriteParam;

import io.codebit.support.image.Bitmap;

public interface IWriteOption
{
	public Bitmap.Format format();
	
	public boolean ignoreMetadata();

	public IWriteOption ignoreMetadata(boolean ignoreMetadata);
	
	public default ImageWriteParam build(ImageWriteParam writeParam)
	{
		return writeParam;
	}
}
