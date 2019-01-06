package io.codebit.support.image;

import javax.imageio.IIOImage;
import javax.imageio.metadata.IIOMetadata;

public class Layer
{

	public IIOMetadata getMetadata() {
		return metadata;
	}

	private IIOMetadata metadata;

	public Bitmap getBitmap() {
		return bitmap;
	}

	private Bitmap bitmap;

	public Layer(Bitmap bitmap)
	{
		this.bitmap = bitmap;
	}
	
	public Layer(Bitmap bitmap, IIOMetadata metadata)
	{
		this.bitmap = bitmap;
		this.metadata = metadata;
	}
	
	public Layer(IIOImage iioImage)
	{
		this.bitmap = new Bitmap(iioImage.getRenderedImage());
		this.metadata = iioImage.getMetadata();
	}
}
