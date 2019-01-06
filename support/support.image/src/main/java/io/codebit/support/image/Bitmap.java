package io.codebit.support.image;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;

import javax.imageio.*;
import javax.imageio.metadata.*;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.color.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
//import java.awt.*;

import com.jhlabs.image.UnsharpFilter;
import com.mortennobel.imagescaling.ResampleOp;
import io.codebit.support.image.options.CropOption;
import io.codebit.support.image.options.IWriteOption;
import io.codebit.support.image.options.RatioOption;
import io.codebit.support.image.options.RotateFlipType;

import javax.imageio.stream.ImageOutputStream;

//******************************************************************************
//**  Image Utilities - Copy from Peter Borissow 
//******************************************************************************
/**
 * Bitmap 픽셀 이미지의 변형 및 픽셀 데이터의 정보를 담당하는 클래스
 * 메타데이터를 보유하고 있지 않음
 * Gif같은 여러장의 이미지를 보유하는 이미지 포멧의 경우
 * Bitmap객체는 한장당 하나씩 생성됨
 * 
 * create, resize, rotate, crop and save images. BMP, GIF, JPG, PNG 및 TIFF 를 지원합니다.
 ******************************************************************************/

public class Bitmap extends Image implements AutoCloseable, Cloneable
{
//	protected static final Format DEFAULT_FORMAT = Format.PNG;
//	private int DEFAULT_TYPE = BufferedImage.TYPE_INT_ARGB;

	protected static final Format DEFAULT_FORMAT = Format.JPEG;
	private int DEFAULT_TYPE = BufferedImage.TYPE_INT_RGB;

	// //https://kippler.com/doc/jpeg2000_vs_jpegxr/
	/**
	 * The Enum Format.
	 */
	public enum Format
	{
		/** The jpeg. */
		JPEG("jpeg", "jpg", "jpe", "jff", "JPEG", "JPG", "JPE", "JFF"),
		/** The JPE g2000. */
		JPEG2000("jp2", "j2k", "jpc", "jpx", "JP2", "J2K", "JPC", "JPX"),
		// MNG
		/** The png. */
		PNG("png", "PNG"),
		/** The tiff. */
		TIFF("tiff", "tif", "TIFF", "TIF"),
		/** The bmp. */
		BMP("bmp", "wbmp", "BMP", "WBMP"),
		/** The gif. */
		GIF("gif", "GIF");

		/**
		 * Gets the extension.
		 *
		 * @return the extension
		 */
		private String[] extension;

		public String[] getExtension() {
			return extension;
		}

		/**
		 * Instantiates a new format.
		 *
		 * @param extension
		 *            the extension
		 */
		private Format(String... extension)
		{
			this.extension = extension;
		}
	}

	/** The corners. */
	private ArrayList<Float> corners = null;

	// [current frame object]------------------------------------------

	// private Rectangle framePosition;

	private IIOImage iioImage;

	/**
	 * Instantiates a new bitmap.
	 *
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 */
	public Bitmap(int width, int height)
	{
		IIOImage iioImage = new IIOImage(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB), null, null);
		this.iioImage = iioImage;
	}

	// **************************************************************************
	// ** Constructor
	// **************************************************************************
	/**
	 * Creates a new instance of this class using a block of text.
	 *
	 * @param text            the text
	 * @param fontName            Name of the font you with to use. Note that you can get a list
	 *            of available fonts like this:
	 * 
	 *            <pre>
	 * for (String fontName : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames())
	 * {
	 * 	System.out.println(fontName);
	 * }
	 * </pre>
	 * @param fontSize the font size
	 * @param fontColor the font color
	 */
	public Bitmap(String text, String fontName, int fontSize, Color fontColor)
	{
		this(text, new Font(fontName, Font.TRUETYPE_FONT, fontSize), fontColor);
	}

	/**
	 * Instantiates a new bitmap.
	 *
	 * @param text            the text
	 * @param font            the font
	 * @param fontColor the font color
	 */
	public Bitmap(String text, Font font, Color fontColor)
	{
		// Get Font Metrics
		Graphics2D t = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics();
		t.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		FontMetrics fm = t.getFontMetrics(font);
		int width = fm.stringWidth(text);
		int height = fm.getHeight();
		int descent = fm.getDescent();

		t.dispose();

		// Create Image
		BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = getGraphics();

		// Add Text
		// float alpha = 1.0f; // Set alpha. 0.0f is 100% transparent and 1.0f
		// is
		float alpha = fontColor.getAlpha();
		// 100% opaque.
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
		g2d.setColor(fontColor);
		g2d.setFont(font);
		g2d.drawString(text, 0, height - descent);
		IIOImage iioImage = new IIOImage(bufferedImage, null, null);
		this.iioImage = iioImage;
	}

	// //
	// **************************************************************************
	// // ** setWidth
	// //
	// **************************************************************************
	// /**
	// * Resizes the image to a given width. The original aspect ratio is
	// * maintained.
	// *
	// * @param Width
	// * the new width
	// */
	// public void setWidth(int Width)
	// {
	// double ratio = (double) Width / (double) this.getWidth();
	//
	// double dw = this.getWidth() * ratio;
	// double dh = this.getHeight() * ratio;
	//
	// int outputWidth = (int) Math.round(dw);
	// int outputHeight = (int) Math.round(dh);
	//
	// resize(outputWidth, outputHeight);
	// }
	//
	// //
	// **************************************************************************
	// // ** setHeight
	// //
	// **************************************************************************
	// /**
	// * Resizes the image to a given height. The original aspect ratio is
	// * maintained.
	// *
	// * @param Height
	// * the new height
	// */
	// public void setHeight(int Height)
	// {
	// double ratio = (double) Height / (double) this.getHeight();
	//
	// double dw = this.getWidth() * ratio;
	// double dh = this.getHeight() * ratio;
	//
	// int outputWidth = (int) Math.round(dw);
	// int outputHeight = (int) Math.round(dh);
	//
	// resize(outputWidth, outputHeight);
	// }

	/**
	 * Instantiates a new bitmap.
	 *
	 * @param bufferedImage
	 *            the buffered image
	 */
	public Bitmap(BufferedImage bufferedImage)
	{
		if (bufferedImage == null)
			throw new IllegalArgumentException("Null input image");
		IIOImage iioImage = new IIOImage(bufferedImage, null, null);
		this.iioImage = iioImage;
	}

	/**
	 * Instantiates a new bitmap.
	 *
	 * @param img
	 *            the img
	 */
	public Bitmap(RenderedImage img)
	{
		BufferedImage bufferedImage;
		if (img instanceof BufferedImage)
		{
			bufferedImage = (BufferedImage) img;
		} else
		{
			ColorModel cm = img.getColorModel();
			WritableRaster raster = cm.createCompatibleWritableRaster(img.getWidth(), img.getHeight());
			boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
			Hashtable<String, Object> properties = new Hashtable<String, Object>();
			String[] keys = img.getPropertyNames();
			if (keys != null)
			{
				for (int i = 0; i < keys.length; i++)
				{
					properties.put(keys[i], img.getProperty(keys[i]));
				}
			}
			BufferedImage result = new BufferedImage(cm, raster, isAlphaPremultiplied, properties);
			img.copyData(raster);
			bufferedImage = result;
		}
		// this.screenSize = new Dimension(bufferedImage.getWidth(),
		// bufferedImage.getHeight());
		IIOImage iioImage = new IIOImage(bufferedImage, null, null);
		this.iioImage = iioImage;
	}

	// Frame Bitmap
	protected Bitmap(IIOImage iioImage)
	{
		this.iioImage = iioImage;
	}

	// **************************************************************************
	// ** getWidth
	// **************************************************************************
	/**
	 * Returns the width of the image, in pixels.
	 *
	 * @return the width
	 */

	public Dimension getDimension()
	{
		return new Dimension(this.getWidth(), this.getHeight());
	}

	public int getWidth()
	{
		// return getPosition().width;
		return this.IIOImage().getRenderedImage().getWidth();
	}

	// **************************************************************************
	// ** getHeight
	// **************************************************************************
	/**
	 * Returns the height of the image, in pixels.
	 *
	 * @return the height
	 */

	public int getHeight()
	{
		// return getPosition().width;
		return this.IIOImage().getRenderedImage().getHeight();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Image#getWidth(java.awt.image.ImageObserver)
	 */
	@Override
	public int getWidth(ImageObserver observer)
	{
		return this.toBufferedImage().getHeight(observer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Image#getHeight(java.awt.image.ImageObserver)
	 */
	@Override
	public int getHeight(ImageObserver observer)
	{
		return this.toBufferedImage().getHeight(observer);
	}

	protected IIOImage IIOImage()
	{
		return this.iioImage;
	}

	// **************************************************************************
	// ** getIIOMetadata
	// **************************************************************************
	/**
	 * Returns the raw, javax.imageio.metadata.IIOMetadata associated with this
	 * image. You can iterate through the metadata using an xml parser like
	 * this:
	 * 
	 * <pre>
	 * IIOMetadata metadata = image.getMetadata().getIIOMetadata();
	 * for (String name : metadata.getMetadataFormatNames())
	 * {
	 * 	System.out.println(&quot;Format name: &quot; + name);
	 * 	org.w3c.dom.Node metadataNode = metadata.getAsTree(name);
	 * 	System.out.println(javaxt.xml.DOM.getNodeValue(metadataNode));
	 * }
	 * </pre>
	 *
	 * @return the IIO metadata
	 */
	public IIOMetadata getIIOMetadata()
	{
		return this.IIOImage().getMetadata();
	}

	// **************************************************************************
	// ** setIIOMetadata
	// **************************************************************************
	/**
	 * Used to set/update the raw javax.imageio.metadata.IIOMetadata associated
	 * with this image.
	 *
	 * @param metadata
	 *            the new IIO metadata
	 */
	public void setIIOMetadata(IIOMetadata metadata)
	{
		this.IIOImage().setMetadata(metadata);
	}

	/**
	 * Returns the image type. If it is not one of the known types,
	 * TYPE_INT_ARGB is returned.
	 * 
	 * @return the image type of this <code>BufferedImage</code>.
	 * @see BufferedImage#TYPE_INT_RGB
	 * @see BufferedImage#TYPE_INT_ARGB
	 * @see BufferedImage#TYPE_INT_ARGB_PRE
	 * @see BufferedImage#TYPE_INT_BGR
	 * @see BufferedImage#TYPE_3BYTE_BGR
	 * @see BufferedImage#TYPE_4BYTE_ABGR
	 * @see BufferedImage#TYPE_4BYTE_ABGR_PRE
	 * @see BufferedImage#TYPE_BYTE_GRAY
	 * @see BufferedImage#TYPE_BYTE_BINARY
	 * @see BufferedImage#TYPE_BYTE_INDEXED
	 * @see BufferedImage#TYPE_USHORT_GRAY
	 * @see BufferedImage#TYPE_USHORT_565_RGB
	 * @see BufferedImage#TYPE_USHORT_555_RGB
	 * @see BufferedImage#TYPE_CUSTOM
	 */
	public int getType()
	{
		int _type = this.toBufferedImage().getType();
		if (_type != 0)
		{
			this.DEFAULT_TYPE = _type;
		}
		return this.DEFAULT_TYPE;
	}

	// **************************************************************************
	// ** getGraphics
	// **************************************************************************
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Image#getGraphics()
	 */
	@Override
	public Graphics2D getGraphics()
	{
		// if (g2d == null)
		// {
		// g2d =
		// ((BufferedImage)this.frameImages.get(index).getRenderedImage()).createGraphics();
		// // Enable anti-alias
		// g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		// RenderingHints.VALUE_ANTIALIAS_ON);
		// }
		BufferedImage bufferedImage = this.toBufferedImage();

		Graphics2D g2d = bufferedImage.createGraphics();
		// Enable anti-alias
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		return g2d;
	}

	// **************************************************************************
	// ** getColor
	// **************************************************************************
	/**
	 * Used to retrieve the color (ARGB) values for a specific pixel in the
	 * image. Returns a java.awt.Color object. Note that input x,y values are
	 * relative to the upper left corner of the image, starting at 0,0.
	 *
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @return the color
	 */
	public Color getColor(int x, int y)
	{
		BufferedImage bufferedImage = this.toBufferedImage();
		int pixel = bufferedImage.getRGB(x, y);
		return new Color(pixel);
	}

	// **************************************************************************
	// ** Get Corners
	// **************************************************************************
	/**
	 * Used to retrieve the corner coordinates of the image. Coordinates are
	 * supplied in clockwise order starting from the upper left corner. This
	 * information is particularly useful for generating drop shadows, inner and
	 * outer glow, and reflections. NOTE: Coordinates are not updated after
	 * resize(), rotate(), or addImage()
	 *
	 * @return the corners
	 */
	public float[] getCorners()
	{
		if (corners == null)
		{
			float w = getWidth();
			float h = getHeight();
			corners = new ArrayList<Float>();
			corners.add(0f);
			corners.add(0f);
			corners.add(w);
			corners.add(0f);
			corners.add(w);
			corners.add(h);
			corners.add(0f);
			corners.add(h);
		}

		Object[] arr = corners.toArray();
		float[] ret = new float[arr.length];
		for (int i = 0; i < arr.length; i++)
		{
			Float f = (Float) arr[i];
			ret[i] = f.floatValue();
		}
		return ret;
	}

	// **************************************************************************
	// ** addText
	// **************************************************************************
	/**
	 * Used to add text to the image at a given position.
	 *
	 * @param text
	 *            the text
	 * @param x
	 *            Lower left coordinate of the text
	 * @param y
	 *            Lower left coordinate of the text
	 */
	public void addText(String text, int x, int y)
	{
		addText(text, x, y, new Font("SansSerif", Font.TRUETYPE_FONT, 12), 0, 0, 0);
	}

	// **************************************************************************
	// ** addText
	// **************************************************************************
	/**
	 * Used to add text to the image at a given position.
	 *
	 * @param text
	 *            the text
	 * @param x
	 *            Lower left coordinate of the text
	 * @param y
	 *            Lower left coordinate of the text
	 * @param fontName
	 *            Name of the font face (e.g. "Tahoma", "Helvetica", etc.)
	 * @param fontSize
	 *            Size of the font
	 * @param r
	 *            Value for the red channel (0-255)
	 * @param g
	 *            Value for the green channel (0-255)
	 * @param b
	 *            Value for the blue channel (0-255)
	 */
	public void addText(String text, int x, int y, String fontName, int fontSize, int r, int g, int b)
	{
		addText(text, x, y, new Font(fontName, Font.TRUETYPE_FONT, fontSize), r, g, b);
	}

	// **************************************************************************
	// ** addText
	// **************************************************************************
	/**
	 * Used to add text to the image at a given position.
	 *
	 * @param text
	 *            the text
	 * @param x
	 *            Lower left coordinate of the text
	 * @param y
	 *            Lower left coordinate of the text
	 * @param font
	 *            Font
	 * @param r
	 *            Value for the red channel (0-255)
	 * @param g
	 *            Value for the green channel (0-255)
	 * @param b
	 *            Value for the blue channel (0-255)
	 */
	public void addText(String text, int x, int y, Font font, int r, int g, int b)
	{
		Graphics2D g2d = getGraphics();
		// g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		// RenderingHints.VALUE_ANTIALIAS_ON);

		g2d.setColor(new Color(r, g, b));
		g2d.setFont(font);
		g2d.drawString(text, x, y);
	}

	// **************************************************************************
	// ** addPoint
	// **************************************************************************
	/**
	 * Simple drawing function used to set color of a specific pixel in the
	 * image.
	 *
	 * @param x            the x
	 * @param y            the y
	 * @param color the color
	 */
	public void addPoint(int x, int y, Color color)
	{
		setPixel(x, y, color);
	}

	// **************************************************************************
	// ** addImage
	// **************************************************************************
	/**
	 * Used to add an image "overlay" to the existing image at a given position.
	 * This method can also be used to create image mosiacs.
	 *
	 * @param in
	 *            the in
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param expand
	 *            the expand
	 */
	public void addImage(BufferedImage in, int x, int y, boolean expand)
	{
		int x2 = 0;
		int y2 = 0;
		int w = this.getWidth();
		int h = this.getHeight();

		if (expand)
		{

			// Update Width and Horizontal Position of the Original Image
			if (x < 0)
			{
				w = w + -x;
				if (in.getWidth() > w)
				{
					w = w + (in.getWidth() - w);
				}
				x2 = -x;
				x = 0;
			} else if (x > w)
			{
				w = (w + (x - w)) + in.getWidth();
			} else
			{
				if ((x + in.getWidth()) > w)
				{
					w = w + ((x + in.getWidth()) - w);
				}
			}

			// Update Height and Vertical Position of the Original Image
			if (y < 0)
			{
				h = h + -y;
				if (in.getHeight() > h)
				{
					h = h + (in.getHeight() - h);
				}
				y2 = -y;
				y = 0;
			} else if (y > h)
			{
				h = (h + (y - h)) + in.getHeight();
			} else
			{
				if ((y + in.getHeight()) > h)
				{
					h = h + ((y + in.getHeight()) - h);
				}
			}

		}

		// Create new image "collage"
		if (w > in.getWidth() || h > in.getHeight())
		{
			int imageType = this.getType();
			// if (imageType == 0 || imageType == 12)
			// {
			// // imageType = BufferedImage.TYPE_INT_ARGB;
			// imageType = this.getType();
			// }
			BufferedImage bi = new BufferedImage(w, h, imageType);
			Graphics2D g2d = bi.createGraphics();
			Image img = this.toBufferedImage();
			g2d.drawImage(img, x2, y2, null);
			img = in;
			g2d.drawImage(img, x, y, null);
			g2d.dispose();
			this.setBufferedImage(bi);
		} else
		{
			Graphics2D g2d = this.toBufferedImage().createGraphics();
			Image img = in;
			g2d.drawImage(img, x, y, null);
			g2d.dispose();
		}
	}

	// **************************************************************************
	// ** addImage
	// **************************************************************************
	/**
	 * Used to add an image "overlay" to the existing image at a given position.
	 * This method can also be used to create image mosiacs.
	 *
	 * @param in
	 *            the in
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param expand
	 *            the expand
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void addImage(Bitmap in, int x, int y, boolean expand) throws IOException
	{
		addImage(in.toBufferedImage(), x, y, expand);
	}

	// **************************************************************************
	// ** setBackgroundColor
	// **************************************************************************
	/**
	 * Used to set the background color. Creates an image layer and inserts it
	 * under the existing graphic. This method should only be called once.
	 *
	 * @param color
	 *            the new background color
	 */
	public void setBackgroundColor(Color color)
	{
		/*
		 * Color org = g2d.getColor(); g2d.setColor(new Color(r,g,b));
		 * g2d.fillRect(1,1,width-2,height-2); //g2d.fillRect(0,0,width,height);
		 * g2d.setColor(org);
		 */
		int width = this.getWidth();
		int height = this.getHeight();

		BufferedImage bi = new BufferedImage(width, height, this.getType());
		Graphics2D g2d = bi.createGraphics();
		if(color.getAlpha() > 0)
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, color.getAlpha()));
			//g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
		g2d.setColor(color);
		g2d.fillRect(0, 0, width, height);

		Image img = this.toBufferedImage();
		g2d.drawImage(img, 0, 0, null);
		this.setBufferedImage(bi);
		g2d.dispose();
	}

	// **************************************************************************
	// ** setPixel
	// **************************************************************************
	/**
	 * 해당 Bitmap에서 지정된 픽셀의 색을 설정합니다.
	 *
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param color
	 *            the color
	 */
	public void setPixel(int x, int y, Color color)
	{
		Graphics2D g2d = getGraphics();
		Color org = g2d.getColor();
		g2d.setColor(color);
		g2d.fillRect(x, y, 1, 1);
		g2d.setColor(org);
	}

	// **************************************************************************
	// ** Flip (Horizonal)
	// **************************************************************************
	/**
	 * Used to flip an image along it's y-axis (horizontal). Vertical flipping
	 * is supported via the rotate method (i.e. rotate +/-180).
	 */
	public void flip()
	{
		// new Consumer<Bitmap>()
		// {
		// @Override
		// public void accept(Bitmap t)
		// {
		// BufferedImage out = new BufferedImage(getWidth(), getHeight(),
		// bufferedImage.getType());
		// AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
		// tx.translate(-bufferedImage.getWidth(), 0);
		// AffineTransformOp op = new AffineTransformOp(tx,
		// AffineTransformOp.TYPE_BICUBIC);
		// bufferedImage = op.filter(bufferedImage, out);
		// }
		// };
		BufferedImage bufferedImage = this.toBufferedImage();
		BufferedImage out = new BufferedImage(getWidth(), getHeight(), this.getType());
		AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
		tx.translate(-bufferedImage.getWidth(), 0);
		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BICUBIC);
		bufferedImage = op.filter(bufferedImage, out);
	}

	public void rotate(RotateFlipType rotateFlip)
	{
		switch (rotateFlip)
		{
			case Rotate90FlipNone:
			case Rotate270FlipXY:
				// 270+180
				rotate(90);
			break;
			case Rotate180FlipNone:
			case RotateNoneFlipXY:
				rotate(180);
			break;
			case Rotate270FlipNone:
			case Rotate90FlipXY:
				rotate(270);
			break;
			case RotateNoneFlipY:
			case Rotate180FlipX:
				flip();
			break;
			case Rotate90FlipY:
			case Rotate270FlipX:
				flip();
				rotate(90);
			break;
			case Rotate180FlipY:
			case RotateNoneFlipX:
				flip();
				rotate(180);
			break;
			case Rotate90FlipX:
			case Rotate270FlipY:
				flip();
				rotate(270);
			break;
			case Rotate180FlipXY:
			case RotateNoneFlipNone:
			break;
		}
	}

	// **************************************************************************
	// ** Rotate
	// **************************************************************************
	/**
	 * Used to rotate the image (clockwise). Rotation angle is specified in
	 * degrees relative to the top of the image.
	 *
	 * @param Degrees
	 *            the degrees
	 */
	public void rotate(double Degrees)
	{
		// Define Image Center (Axis of Rotation)
		int width = this.getWidth();
		int height = this.getHeight();
		int cx = width / 2;
		int cy = height / 2;

		// create an array containing the corners of the image (TL,TR,BR,BL)
		int[] corners = {
				0, 0, width, 0, width, height, 0, height
		};

		// Define bounds of the image
		int minX, minY, maxX, maxY;
		minX = maxX = cx;
		minY = maxY = cy;
		double theta = Math.toRadians(Degrees);
		for (int i = 0; i < corners.length; i += 2)
		{
			// Rotates the given point theta radians around (cx,cy)
			int x = (int) Math
				.round(Math.cos(theta) * (corners[i] - cx) - Math.sin(theta) * (corners[i + 1] - cy) + cx);

			int y = (int) Math
				.round(Math.sin(theta) * (corners[i] - cx) + Math.cos(theta) * (corners[i + 1] - cy) + cy);

			// Update our bounds
			if (x > maxX)
				maxX = x;
			if (x < minX)
				minX = x;
			if (y > maxY)
				maxY = y;
			if (y < minY)
				minY = y;
		}

		// Update Image Center Coordinates
		cx = cx - minX;
		cy = cy - minY;

		// Create Buffered Image
		BufferedImage result = new BufferedImage(maxX - minX, maxY - minY, this.getType());

		// Create Graphics
		Graphics2D g2d = result.createGraphics();

		// Enable anti-alias and Cubic Resampling
		// g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		// RenderingHints.VALUE_ANTIALIAS_ON);
		// g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
		// RenderingHints.VALUE_INTERPOLATION_BICUBIC);

		// Rotate the image
		AffineTransform xform = new AffineTransform();
		xform.rotate(theta, cx, cy);
		g2d.setTransform(xform);
		g2d.drawImage(this.toBufferedImage(), -minX, -minY, null);
		g2d.dispose();

		this.setBufferedImage(result);

		// Delete Heavy Objects
		result = null;
		xform = null;
	}

	// **************************************************************************
	// ** Rotate Clockwise
	// **************************************************************************
	/**
	 * Rotates the image 90 degrees clockwise.
	 */

	public void rotateClockwise()
	{
		rotate(90);
	}

	// **************************************************************************
	// ** Rotate Counter Clockwise
	// **************************************************************************
	/**
	 * Rotates the image -90 degrees.
	 */

	public void rotateCounterClockwise()
	{
		rotate(-90);
	}

	// **************************************************************************
	// ** setOpacity
	// **************************************************************************

	/*
	 * public void setCacheDirectory(File cacheDirectory){ try{ if
	 * (cacheDirectory.isFile()){ cacheDirectory =
	 * cacheDirectory.getParentFile(); } cacheDirectory.mkdirs();
	 * ImageIO.setUseCache(true); this.cacheDirectory = cacheDirectory; }
	 * catch(Exception e){ this.cacheDirectory = null; } }
	 * 
	 * public File getCacheDirectory(){ return cacheDirectory; }
	 */

	public void scale(int percent)
	{
		double ratio = percent / 100;
		scale(ratio);
	}

	public void scale(int widthPercent, int heightPercent)
	{
		double widthRatio = widthPercent / 100;
		double heightRatio = heightPercent / 100;
		scale(widthRatio, heightRatio);
	}

	protected void scale(double ratio)
	{
		int width = this.getWidth();
		int height = this.getHeight();

		double dw = width * ratio;
		double dh = height * ratio;
		width = (int) Math.ceil(dw);
		height = (int) Math.ceil(dh);

		resize(new Dimension(width, height), RatioOption.IgnoreAspectRatio);
	}

	protected void scale(double widthRatio, double heightRatio)
	{
		int width = this.getWidth();
		int height = this.getHeight();

		double dw = width * widthRatio;
		double dh = height * heightRatio;
		width = (int) Math.ceil(dw);
		height = (int) Math.ceil(dh);

		resize(new Dimension(width, height),  RatioOption.IgnoreAspectRatio);
	}

	// **************************************************************************
	// ** Resize (Overloaded Member)
	// **************************************************************************
	/**
	 * Used to resize an image. Does NOT automatically retain the original
	 * aspect ratio.
	 *
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 */
	public void resize(int width, int height)
	{
		resize(new Dimension(width, height), RatioOption.AspectRatio);
	}
	
	public void resize(Dimension dimension)
	{
		resize(dimension, RatioOption.AspectRatio);
	}

	public void resize(Dimension dimension, CropOption crop)
	{
		// 이미지를 최대한 확장하여 리사이즈함
		this.resize(dimension, RatioOption.OuterAspectRatio);
		this.crop(dimension, crop);
	}

	public void resize(Dimension dimension, RatioOption ratioOption)
	{
		try
		{
			Dimension current = this.getDimension();

			Dimension desc = ratioOption.dimension(current, dimension);
			ResampleOp resample = new ResampleOp(desc.width, desc.height);
			//이미지의 가로세로 사이즈에 따라 이미지 리사이즈에 처리할 쓰레드 수 결정
			double p = current.width * current.height;
			int threadCount = (int) Math.ceil(p / 1000000);
			resample.setNumberOfThreads(threadCount);
			
			// http://docs.autodesk.com/ACAD_E/2012/KOR/filesAUG/WS1a9193826455f5ffa23ce210c4a30acaf-7c17.htm
			// http://naiyumie.tistory.com/66
			// 리샘플링 필터(앤티엘리어싱)
			// resampleOp.setFilter(ResampleFilters.getBiCubicFilter());
			// http://enow.kr/263
			// 이미지의 경계면을 좀 더 선명하게 만들기 위해 적용
			double wratio = (double) desc.width / (double) current.width;
			double hratio = (double) desc.height / (double) current.height;
			double ratio = Math.min(wratio, hratio);
			if (ratio < 0.5)
			{
				resample.setUnsharpenMask(ResampleOp.UnsharpenMask.VerySharp);
			} else if (ratio < 1)
			{
				resample.setUnsharpenMask(ResampleOp.UnsharpenMask.Normal);
			} else if (ratio < 2)
			{
				resample.setUnsharpenMask(ResampleOp.UnsharpenMask.Soft);
			}
			BufferedImage bufferedImage = resample.filter(this.toBufferedImage(), null);
			this.setBufferedImage(bufferedImage);
//			try
//			{
//				this.saveAs(new File("C:/Temp/test.jpg"), Format.JPEG);
//			} catch (IOException e)
//			{
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			// resizeJava(desc.width, desc.height);
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	protected void resizeJava(int width, int height)
	{
		// Resize the image (create new buffered image)
		Image outputImage = this.toBufferedImage().getScaledInstance(width, height, Image.SCALE_AREA_AVERAGING);
		// BufferedImage.TYPE_4BYTE_ABGR;
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g2d = bi.createGraphics();

		// progressive bilinear scaling
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.drawImage(outputImage, 0, 0, null);
		g2d.dispose();

		// jai 필터 jai_core.jar에 포함됨
		// UnsharpMaskDescriptor unsharpMaskDescriptor = new
		// UnsharpMaskDescriptor();
		// unsharpMaskDescriptor.create(source0, kernel, gain, hints)
		UnsharpFilter unsharpFilter = new UnsharpFilter();
		unsharpFilter.setRadius(2.0F);
		// None(0),
		// Soft(0.15f),
		// Normal(0.3f),
		// VerySharp(0.45f),
		// Oversharpened(0.60f);
		unsharpFilter.setAmount(0.15f);
		unsharpFilter.setThreshold(10);
		this.setBufferedImage(unsharpFilter.filter(bi, null));
		this.setBufferedImage(bi);
		// this.bufferedImage = bi;
		outputImage = null;
		bi = null;
		g2d = null;
	}

	// **************************************************************************
	// ** Crop
	// **************************************************************************

	public void crop(Rectangle rectangle)
	{
		if (rectangle.x >= 0 && rectangle.y >= 0 && rectangle.width > 0 && rectangle.height > 0)
		{
			this.setBufferedImage(getSubimage(rectangle));
		}
		// this.crop(rectangle.x, rectangle.y, rectangle.width,
		// rectangle.height);
	}

	/**
	 * Used to subset or crop an image.
	 *
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 */
	public void crop(int x, int y, int width, int height)
	{
		this.crop(new Rectangle(x, y, width, height));
	}

	public void crop(Dimension dimension, CropOption crop)
	{
		Point p = crop.point(this.getWidth(), this.getHeight(), dimension.width, dimension.height);
		this.crop(new Rectangle(p, dimension));
	}

	// public void unsharpenMask(ResampleOp.UnsharpenMask unsharpenMask)
	// {
	// ResampleOp resampleOp = new ResampleOp(this.getWidth(),
	// this.getHeight());
	// resampleOp.setUnsharpenMask(unsharpenMask);
	// this.setBufferedImage(resampleOp.filter(this.toBufferedImage(), null));
	// }
	//
	// /**
	// * Sets the resample filter.
	// * http://docs.autodesk.com/ACAD_E/2012/KOR/filesAUG
	// * /WS1a9193826455f5ffa23ce210c4a30acaf-7c17.htm
	// * http://naiyumie.tistory.com/66 리샘플링 필터(앤티엘리어싱)
	// *
	// * @param resampleFilter
	// * the new resample filter
	// */
	// public void setResample(ResampleFilter resampleFilter)
	// {
	// ResampleOp resampleOp = new ResampleOp(this.getWidth(),
	// this.getHeight());
	// resampleOp.setFilter(resampleFilter);
	// this.setBufferedImage(resampleOp.filter(this.toBufferedImage(), null));
	// }
	//
	// public void effect(AbstractBufferedImageOp effectFilter)
	// {
	// BufferedImage bufferedImage = this.toBufferedImage();
	// effectFilter.filter(bufferedImage, bufferedImage);
	//
	// this.setBufferedImage(bufferedImage);
	// }

	// public void setFilter(AbstractBufferedImageOp effectFilter)
	// {
	// effectFilter.filter(this.bufferedImage, this.bufferedImage);
	// }

	// **************************************************************************
	// ** Desaturate
	// **************************************************************************
	/** Used to completely desaturate an image (creates a gray-scale image). */

	public void desaturate()
	{
		BufferedImage bufferedImage = this.toBufferedImage();
		bufferedImage = desaturate(bufferedImage);
		this.setBufferedImage(bufferedImage);
	}

	// **************************************************************************
	// ** Desaturate
	// **************************************************************************
	/**
	 * Used to desaturate an image by a specified percentage (expressed as a
	 * double or float). The larger the percentage, the greater the desaturation
	 * and the "grayer" the image. Valid ranges are from 0-1.
	 *
	 * @param percent
	 *            the percent
	 */
	public void desaturate(double percent)
	{
		float alpha = (float) (percent);
		Image overlay = desaturate(this.toBufferedImage());
		Graphics2D g2d = this.toBufferedImage().createGraphics();
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
		g2d.drawImage(overlay, 0, 0, null);
		g2d.dispose();
	}

	/**
	 * Convenience function called by the other 2 desaturation methods.
	 *
	 * @param in
	 *            the in
	 * @return the buffered image
	 */

	private BufferedImage desaturate(BufferedImage in)
	{
		BufferedImage out = new BufferedImage(in.getWidth(), in.getHeight(), this.toBufferedImage().getType());
		BufferedImageOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
		return op.filter(in, out);
	}

	// //
	// **************************************************************************
	// // ** Sharpen
	// //
	// **************************************************************************
	// /** Used to sharpen the image using a 3x3 kernal. */
	//
	// public void sharpen()
	// {
	//
	// int width = this.getWidth();
	// int height = this.getHeight();
	//
	// // define kernal
	// Kernel kernel = new Kernel(3, 3, new float[] {
	// 0.0f, -0.2f, 0.0f, -0.2f, 1.8f, -0.2f, 0.0f, -0.2f, 0.0f
	// });
	//
	// // apply convolution
	// BufferedImage out = new BufferedImage(width, height,
	// bufferedImage.getType());
	// BufferedImageOp op = new ConvolveOp(kernel);
	// out = op.filter(bufferedImage, out);
	//
	// // replace 2 pixel border created via convolution
	// Image overlay = out.getSubimage(2, 2, width - 4, height - 4);
	// Graphics2D g2d = bufferedImage.createGraphics();
	// g2d.drawImage(overlay, 2, 2, null);
	// g2d.dispose();
	// }

	// **************************************************************************
	// ** setOpacity
	// **************************************************************************
	/**
	 * Sets the opacity.
	 *
	 * @param percent
	 *            the new opacity
	 */
	public void setOpacity(double percent)
	{
		if (percent > 1)
			percent = percent / 100;
		float alpha = (float) (percent);
		int imageType = this.getType();
		// if (imageType == 0)
		// {
		// imageType = BufferedImage.TYPE_INT_ARGB;
		// }
		BufferedImage out = new BufferedImage(getWidth(), getHeight(), imageType);
		Graphics2D g2d = out.createGraphics();
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
		g2d.drawImage(this.toBufferedImage(), 0, 0, null);
		g2d.dispose();
		this.setBufferedImage(out);
	}

	// **************************************************************************
	// ** getHistogram
	// **************************************************************************
	/**
	 * Returns an array with 4 histograms: red, green, blue, and average
	 * 
	 * <pre>
	 * ArrayList&lt;int[]&gt; histogram = image.getHistogram();
	 * int[] red = histogram.get(0);
	 * int[] green = histogram.get(1);
	 * int[] blue = histogram.get(2);
	 * int[] average = histogram.get(3);
	 * </pre>
	 *
	 * @return the histogram
	 */
	public ArrayList<int[]> getHistogram()
	{
		return Bitmap.getHistogram(this.toBufferedImage());
	}

	/**
	 * Histogram equalization.
	 *
	 * @param original
	 *            the original
	 * @return the buffered image
	 */
	public static BufferedImage histogramEqualization(BufferedImage original)
	{
		int red;
		int green;
		int blue;
		int alpha;
		int newPixel = 0;

		// Get the Lookup table for histogram equalization
		ArrayList<int[]> histLUT = histogramEqualizationLUT(original);

		BufferedImage histogramEQ = new BufferedImage(original.getWidth(), original.getHeight(), original.getType());

		for (int i = 0; i < original.getWidth(); i++)
		{
			for (int j = 0; j < original.getHeight(); j++)
			{
				// Get pixels by R, G, B
				int rgb = original.getRGB(i, j);
				Color color = new Color(rgb);
				alpha = color.getAlpha();
				red = color.getRed();
				green = color.getGreen();
				blue = color.getBlue();

				// Set new pixel values using the histogram lookup table
				red = histLUT.get(0)[red];
				green = histLUT.get(1)[green];
				blue = histLUT.get(2)[blue];

				// Return back to original format
				newPixel = new Color(red, green, blue, alpha).getRGB();

				// Write pixels into image
				histogramEQ.setRGB(i, j, newPixel);
			}
		}

		return histogramEQ;
	}

	// Get the histogram equalization lookup table for separate R, G, B channels
	/**
	 * Histogram equalization lut.
	 *
	 * @param input
	 *            the input
	 * @return the array list
	 */
	private static ArrayList<int[]> histogramEqualizationLUT(BufferedImage input)
	{
		// Get an image histogram - calculated values by R, G, B channels
		ArrayList<int[]> imageHist = Bitmap.getHistogram(input);

		// Create the lookup table
		ArrayList<int[]> imageLUT = new ArrayList<int[]>();

		// Fill the lookup table
		int[] rhistogram = new int[256];
		int[] ghistogram = new int[256];
		int[] bhistogram = new int[256];
		Arrays.fill(rhistogram, 0);
		Arrays.fill(ghistogram, 0);
		Arrays.fill(bhistogram, 0);

		long sumr = 0;
		long sumg = 0;
		long sumb = 0;

		// Calculate the scale factor
		float scale_factor = (float) (255.0 / (input.getWidth() * input.getHeight()));

		for (int i = 0; i < rhistogram.length; i++)
		{
			sumr += imageHist.get(0)[i];
			int valr = (int) (sumr * scale_factor);
			if (valr > 255)
			{
				rhistogram[i] = 255;
			} else
				rhistogram[i] = valr;

			sumg += imageHist.get(1)[i];
			int valg = (int) (sumg * scale_factor);
			if (valg > 255)
			{
				ghistogram[i] = 255;
			} else
				ghistogram[i] = valg;

			sumb += imageHist.get(2)[i];
			int valb = (int) (sumb * scale_factor);
			if (valb > 255)
			{
				bhistogram[i] = 255;
			} else
				bhistogram[i] = valb;
		}

		imageLUT.add(rhistogram);
		imageLUT.add(ghistogram);
		imageLUT.add(bhistogram);

		return imageLUT;
	}

	// Return an ArrayList containing histogram values for separate R, G, B
	// channels
	/**
	 * Image histogram.
	 *
	 * @param input
	 *            the input
	 * @return the array list
	 */
	public static ArrayList<int[]> getHistogram(BufferedImage input)
	{
		int[] rhistogram = new int[256];
		int[] ghistogram = new int[256];
		int[] bhistogram = new int[256];
		int[] average = new int[256];

		Arrays.fill(rhistogram, 0);
		Arrays.fill(ghistogram, 0);
		Arrays.fill(bhistogram, 0);
		Arrays.fill(average, 0);

		for (int i = 0; i < input.getWidth(); i++)
		{
			for (int j = 0; j < input.getHeight(); j++)
			{
				int red = new Color(input.getRGB(i, j)).getRed();
				int green = new Color(input.getRGB(i, j)).getGreen();
				int blue = new Color(input.getRGB(i, j)).getBlue();
				int avg = Math.round((red + green + blue) / 3);
				average[avg] = average[avg] + 1;
				// Increase the values of colors
				rhistogram[red]++;
				ghistogram[green]++;
				bhistogram[blue]++;
			}
		}

		ArrayList<int[]> hist = new ArrayList<int[]>();
		hist.add(rhistogram);
		hist.add(ghistogram);
		hist.add(bhistogram);
		hist.add(average);
		return hist;
	}

	// **************************************************************************
	// ** Set/Update Corners (Skew)
	// **************************************************************************
	/**
	 * Used to skew an image by updating the corner coordinates. Coordinates are
	 * supplied in clockwise order starting from the upper left corner.
	 *
	 * @param x0
	 *            the x0
	 * @param y0
	 *            the y0
	 * @param x1
	 *            the x1
	 * @param y1
	 *            the y1
	 * @param x2
	 *            the x2
	 * @param y2
	 *            the y2
	 * @param x3
	 *            the x3
	 * @param y3
	 *            the y3
	 */
	public void setCorners(float x0, float y0, // UL
		float x1, float y1, // UR
		float x2, float y2, // LR
		float x3, float y3)
	{ // LL

		Skew skew = new Skew(this.toBufferedImage());
		this.setBufferedImage(skew.setCorners(x0, y0, x1, y1, x2, y2, x3, y3));

		if (corners == null)
			corners = new ArrayList<Float>();
		else
			corners.clear();
		corners.add(x0);
		corners.add(y0);
		corners.add(x1);
		corners.add(y1);
		corners.add(x2);
		corners.add(y2);
		corners.add(x3);
		corners.add(y3);
	}

	// **************************************************************************
	// ** getSubimage
	// **************************************************************************
	/**
	 * Returns a copy of the image at a given rectangle. In Java 1.6, the
	 * BufferedImage.getSubimage() throws an exception if the rectangle falls
	 * outside the image bounds. This method was written to overcome this
	 * limitation.
	 *
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 * @return the subimage
	 */
	public BufferedImage getSubimage(int x, int y, int width, int height)
	{
		return this.getSubimage(new Rectangle(x, y, width, height));
	}

	public BufferedImage getSubimage(Rectangle rectangle)
	{
		int x = rectangle.x;
		int y = rectangle.y;
		int width = rectangle.width;
		int height = rectangle.height;
		// if(x < 0)
		// x = Math.abs(x);
		// if(y < 0)
		// y = Math.abs(y);
		// return bufferedImage.getSubimage(x, y, width, height);
		BufferedImage image = this.toBufferedImage();
		Rectangle rect1 = new Rectangle(0, 0, image.getWidth(), image.getHeight());
		Rectangle rect2 = rectangle;

		// If the requested rectangle falls inside the image bounds, simply
		// return
		// the subimage
		if (rect1.contains(rect2))
		{
			return (image.getSubimage(x, y, width, height));
		} else
		{ // requested rectangle falls outside the image bounds!

			// Create buffered image
			// int imageType = this.bufferedImage.getType();
			// if (imageType == 0 || imageType == 12)
			// {
			// imageType = BufferedImage.TYPE_INT_ARGB;
			// }
			BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

			// If the requested rectangle intersects the image bounds, crop the
			// the source image and insert it into the BufferedImage
			if (rect1.intersects(rect2))
			{

				Graphics2D g2d = bi.createGraphics();
				BufferedImage subImage = null;
				int X;
				int Y;

				if (x < 0)
				{
					int x1 = 0;
					int y1;
					int h;
					int w;

					if (y < 0)
					{
						y1 = 0;
						h = y + height;
						Y = height - h;
					} else
					{
						y1 = y;
						h = height;
						Y = 0;
					}

					if (h + y1 > image.getHeight())
						h = image.getHeight() - y1;

					w = x + width;
					if (w > image.getWidth())
						w = image.getWidth();

					subImage = image.getSubimage(x1, y1, w, h);

					X = width - w;
				} else
				{
					int x1 = x;
					int y1;
					int h;
					int w;

					if (y < 0)
					{
						y1 = 0;
						h = y + height;
						Y = height - h;
					} else
					{
						y1 = y;
						h = height;
						Y = 0;
					}

					if (h + y1 > image.getHeight())
						h = image.getHeight() - y1;

					w = width;
					if (w + x1 > image.getWidth())
						w = image.getWidth() - x1;

					X = 0;

					subImage = image.getSubimage(x1, y1, w, h);
				}

				g2d.drawImage(subImage, X, Y, null);
				g2d.dispose();

			}

			return bi;
		}
		// BufferedImage image = this.toBufferedImage();
		// return image.getSubimage((int) rectangle.getX(),
		// (int) rectangle.getX(),
		// (int) rectangle.getWidth(),
		// (int) rectangle.getHeight());
	}

	// **************************************************************************
	// ** trim
	// **************************************************************************
	/**
	 * Used to remove excess pixels around an image by cropping the image to its
	 * "true" extents. Crop bounds are determined by finding the first non-null
	 * or non-black pixel on each side of the image.
	 */
	public void trim()
	{
		trim(0, 0, 0);
	}

	// **************************************************************************
	// ** trim
	// **************************************************************************
	/**
	 * Used to remove excess pixels around an image by cropping the image to its
	 * "true" extents. Crop bounds are determined by finding pixels that *don't*
	 * match the input color. For example, you can trim off excess black pixels
	 * around an image by specifying an rgb value of 0,0,0. Similarly, you can
	 * trim off pure white pixels around an image by specifying an rgb value of
	 * 255,255,255. Note that transparent pixels are considered as null values
	 * and will be automatically trimmed from the edges.
	 *
	 * @param r
	 *            the r
	 * @param g
	 *            the g
	 * @param b
	 *            the b
	 */
	public void trim(int r, int g, int b)
	{
		int top = 0;
		int bottom = 0;
		int left = 0;
		int right = 0;
		BufferedImage bufferedImage = this.toBufferedImage();
		for (int y = 0; y < bufferedImage.getHeight(); y++)
		{
			for (int x = 0; x < bufferedImage.getWidth(); x++)
			{
				if (hasColor(bufferedImage.getRGB(x, y), r, g, b))
				{
					bottom = y;
					break;
				}
			}
		}

		for (int y = bufferedImage.getHeight() - 1; y > -1; y--)
		{
			for (int x = 0; x < bufferedImage.getWidth(); x++)
			{
				if (hasColor(bufferedImage.getRGB(x, y), r, g, b))
				{
					top = y;
					break;
				}
			}
		}

		for (int x = 0; x < bufferedImage.getWidth(); x++)
		{
			for (int y = 0; y < bufferedImage.getHeight(); y++)
			{
				if (hasColor(bufferedImage.getRGB(x, y), r, g, b))
				{
					right = x;
					break;
				}
			}
		}

		for (int x = bufferedImage.getWidth() - 1; x > -1; x--)
		{
			for (int y = 0; y < bufferedImage.getHeight(); y++)
			{
				if (hasColor(bufferedImage.getRGB(x, y), r, g, b))
				{
					left = x;
					break;
				}
			}
		}

		if (left == right || top == bottom)
		{
			bufferedImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		} else
		{
			bufferedImage = bufferedImage.getSubimage(left, top, right - left, bottom - top);
		}
		this.setBufferedImage(bufferedImage);
	}

	public void trim(Color color)
	{
		trim(color.getRed(), color.getGreen(), color.getBlue());
	}

	// **************************************************************************
	// ** getBufferedImage
	// **************************************************************************
	/**
	 * Returns the BufferedImage represented by the current image.
	 *
	 * @return the buffered image
	 */
	public BufferedImage toBufferedImage()
	{
		return (BufferedImage) this.IIOImage().getRenderedImage();
	}

	protected void setBufferedImage(BufferedImage bufferedImage)
	{
		this.IIOImage().setRenderedImage(bufferedImage);
	}

	// /**
	// * Gets the buffered image.
	// *
	// * @param index
	// * the index
	// * @return the buffered image
	// * @throws IOException
	// * Signals that an I/O exception has occurred.
	// */
	// public BufferedImage getBufferedImage(int index)
	// {
	// try
	// {
	// return this.getImageReader().read(index);
	// } catch (IOException e)
	// {
	// return null;
	// }
	// }

	// **************************************************************************
	// ** getImage
	// **************************************************************************
	/**
	 * Returns a RenderedImage copy of the current image.
	 *
	 * @return the rendered image
	 */

	public RenderedImage toRenderedImage()
	{
		return toBufferedImage();
	}

	// **************************************************************************
	// ** getByteArray
	// **************************************************************************
	/**
	 * Returns the image as a jpeg byte array. Output quality is set using the
	 * setOutputQuality method.
	 *
	 * @return the byte array
	 */
	public byte[] toByteArray()
	{
		BufferedImage bufferedImage = this.toBufferedImage();
		DataBuffer dataBuffer = bufferedImage.getData().getDataBuffer();
		return ((DataBufferByte) dataBuffer).getData();
	}

	// **************************************************************************
	// ** getByteArray
	// **************************************************************************
	/**
	 * Returns the image as a byte array.
	 *
	 * @param option
	 *            the option
	 * @return the byte array
	 */
	public byte[] toByteArray(IWriteOption option)
	{
		try (ByteArrayOutputStream os = new ByteArrayOutputStream())
		{
			write(os, DEFAULT_FORMAT);
			return os.toByteArray();
		} catch (IOException e)
		{
			return new byte[0];
		}
	}

	// **************************************************************************
	// ** saveAs
	// **************************************************************************
	/**
	 * Exports the image to a file. Output format is determined by the output
	 * file extension.
	 *
	 * @param OutputFile            the output file
	 * @param format the format
	 * @throws IOException             Signals that an I/O exception has occurred.
	 */
	public void saveAs(File OutputFile, Format format) throws IOException
	{
		try (FileOutputStream fileOut = new FileOutputStream(OutputFile))
		{
			write(fileOut, format);
		}
	}

	/**
	 * Save as.
	 *
	 * @param OutputFile
	 *            the output file
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void saveAs(File OutputFile) throws IOException
	{
		this.saveAs(OutputFile, DEFAULT_FORMAT);
	}

	/**
	 * Write.
	 *
	 * @param output
	 *            the output
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void write(OutputStream output) throws IOException
	{
		write(output, null);
	}

	/**
	 * Write.
	 *
	 * @param output            the output
	 * @param format the format
	 * @throws IOException             Signals that an I/O exception has occurred.
	 */
	public void write(OutputStream output, Format format) throws IOException
	{
		try
		{
//			for (String string : ImageIO.getReaderFormatNames())
//			{
//				System.out.println(string);
//			}
			if(format.equals(Format.JPEG2000))
			{
				format = Format.JPEG;
			}
			Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(format.name());
			ImageWriter imageWriter = writers.next();

			if (!ImageIO.getUseCache())
			{
				Runtime runtime = Runtime.getRuntime();
				long freeMemory = runtime.freeMemory() / 1048576;
				if (freeMemory < 100)
				{
					ImageIO.setUseCache(true);
				}
			}
			try (ImageOutputStream stream = ImageIO.createImageOutputStream(output))
			{
				imageWriter.setOutput(stream);
				ImageWriteParam writeParam = imageWriter.getDefaultWriteParam();
				
//				this.setIIOMetadata(null);
				IIOImage image = this.IIOImage();
				
				boolean isJpeg = format.equals(Format.JPEG) || format.equals(Format.JPEG2000);
//				int t = this.toBufferedImage().getTransparency();
//				if (t==BufferedImage.BITMASK) System.out.println("BITMASK");
//				if (t==BufferedImage.OPAQUE) System.out.println("OPAQUE");

				if(
					//t == Transparency.TRANSLUCENT
					image.getRenderedImage().getColorModel().hasAlpha() 
					&& (isJpeg || format.equals(Format.BMP))
					//&& !writeParam.getDestinationType().getColorModel().hasAlpha()	
				)
				{
					if(isJpeg)
					{
						((JPEGImageWriteParam)writeParam).setOptimizeHuffmanTables(true);
					}
					//alpha채널을 지원하지 않는 포맷인 경우 BufferedImage의 타입을 변경함
    				int w = image.getRenderedImage().getWidth();
    			    int h = image.getRenderedImage().getHeight();
    			    BufferedImage image2 = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    			    Graphics2D g = image2.createGraphics();
    			    g.drawImage(this.toBufferedImage(), 0, 0, null);
//				    g.setColor(fillColor);
//    			    g.fillRect(0,0,w,h);
//    			    g.drawRenderedImage(image.getRenderedImage(), null);
    			    g.dispose();
    			    image = new IIOImage(image2, null, null);
				}else
				{
					image = new IIOImage(image.getRenderedImage(), null, null);
				}

				imageWriter.write(null, image, writeParam);

				output.flush();
				imageWriter.dispose();
			}
		} catch (IOException e)
		{
			throw e;
		}
	}

	// //
	// **************************************************************************
	// // ** isJPEG
	// //
	// **************************************************************************
	// /**
	// * Used to determine whether to create a custom jpeg compressed image.
	// *
	// * @param FileExtension
	// * the file extension
	// * @return true, if is jpeg
	// */
	//
	// public static boolean isJPEG(String FileExtension)
	// {
	// for (String format : DEFAULT_FORMAT.getExtension())
	// {
	// if (format.equals(FileExtension))
	// {
	// return true;
	// }
	// }
	// return false;
	// }
	//
	// //
	// **************************************************************************
	// // ** isJPEG2000
	// //
	// **************************************************************************
	// /**
	// * Used to determine whether to create a custom jpeg compressed image.
	// *
	// * @param FileExtension
	// * the file extension
	// * @return true, if is JPE g2000
	// */
	//
	// public static boolean isJPEG2000(String FileExtension)
	// {
	// for (String format : Format.JPEG2000.getExtension())
	// {
	// if (format.equals(FileExtension))
	// {
	// return true;
	// }
	// }
	// return false;
	// }
	//
	// /**
	// * Checks if is gif.
	// *
	// * @param FileExtension
	// * the file extension
	// * @return true, if is gif
	// */
	// public static boolean isGIF(String FileExtension)
	// {
	// for (String format : Format.JPEG2000.getExtension())
	// {
	// if (format.equals(FileExtension))
	// {
	// return true;
	// }
	// }
	// return false;
	// }
	//
	// /**
	// * Checks if is png.
	// *
	// * @param FileExtension
	// * the file extension
	// * @return true, if is png
	// */
	// public static boolean isPNG(String FileExtension)
	// {
	// for (String format : Format.PNG.getExtension())
	// {
	// if (format.equals(FileExtension))
	// {
	// return true;
	// }
	// }
	// return false;
	// }
	//
	// /**
	// * Checks if is bmp.
	// *
	// * @param FileExtension
	// * the file extension
	// * @return true, if is bmp
	// */
	// public boolean isBMP(String FileExtension)
	// {
	// for (String format : Format.BMP.getExtension())
	// {
	// if (format.equals(FileExtension))
	// {
	// return true;
	// }
	// }
	// return false;
	// }

	// //
	// **************************************************************************
	// // ** getJPEGByteArray
	// //
	// **************************************************************************
	// /**
	// * Returns a JPEG compressed byte array.
	// *
	// * @param outputQuality
	// * the output quality
	// * @return the JPEG byte array
	// * @throws IOException
	// * Signals that an I/O exception has occurred.
	// */
	//
	// private byte[] getJPEGByteArray(float outputQuality) throws IOException
	// {
	// if (outputQuality >= 0f && outputQuality <= 1.2f)
	// {
	// ByteArrayOutputStream bas = new ByteArrayOutputStream();
	// BufferedImage bi = bufferedImage;
	// int t = bufferedImage.getTransparency();
	//
	// // if (t==BufferedImage.BITMASK) System.out.println("BITMASK");
	// // if (t==BufferedImage.OPAQUE) System.out.println("OPAQUE");
	//
	// if (t == Transparency.TRANSLUCENT)
	// {
	// bi = new BufferedImage(getWidth(), getHeight(),
	// BufferedImage.TYPE_INT_RGB);
	// Graphics2D biContext = bi.createGraphics();
	// biContext.drawImage(bufferedImage, 0, 0, null);
	// }
	//
	// // If the com.sun.image.codec.jpeg package is not found or if the
	// // compression failed, we will use the JPEGImageWriteParam class.
	// if (bas.size() == 0)
	// {
	// if (outputQuality > 1f)
	// outputQuality = 1f;
	//
	// ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
	// JPEGImageWriteParam params = (JPEGImageWriteParam)
	// writer.getDefaultWriteParam();
	// params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
	// params.setCompressionQuality(outputQuality);
	// writer.setOutput(ImageIO.createImageOutputStream(bas));
	// writer.write(null, new IIOImage(bi, null, null), params);
	// writer.dispose();
	// }
	//
	// bas.flush();
	// return bas.toByteArray();
	// } else
	// {
	// return toByteArray();
	// }
	// }

	// **************************************************************************
	// ** hasColor
	// **************************************************************************
	/**
	 * Used to determine whether a given pixel has a color value. Returns false
	 * if the pixel is white or transparent.
	 *
	 * @param pixel
	 *            the pixel
	 * @param red
	 *            the red
	 * @param green
	 *            the green
	 * @param blue
	 *            the blue
	 * @return true, if successful
	 */
	private boolean hasColor(int pixel, int red, int green, int blue)
	{
		int a = (pixel >> 24) & 0xff;
		int r = (pixel >> 16) & 0xff;
		int g = (pixel >> 8) & 0xff;
		int b = (pixel) & 0xff;

		if ((r == red && g == green && b == blue) || a == 0)
		{
			return false;
		}
		return true;
	}

//	// **************************************************************************
//	// ** getMetadataByTagName
//	// **************************************************************************
//	/**
//	 * Returns a list of IIOMetadataNodes for a given tag name (e.g. "Chroma",
//	 * "Compression", "Data", "Dimension", "Transparency", etc).
//	 * 
//	 * <pre>
//	 * // Print unknown tags
//	 * for (IIOMetadataNode unknownNode : metadata.getMetadataByTagName(&quot;unknown&quot;))
//	 * {
//	 * 	int marker = Integer.parseInt(javaxt.xml.DOM.getAttributeValue(unknownNode, &quot;MarkerTag&quot;));
//	 * 	System.out.println(marker + &quot;\t&quot; + &quot;0x&quot; + Integer.toHexString(marker));
//	 * }
//	 * </pre>
//	 *
//	 * @param tagName
//	 *            the tag name
//	 * @return the metadata by tag name
//	 */
//	public IIOMetadataNode[] getMetadataByTagName(String tagName)
//	{
//		IIOMetadata metadata = this.IIOImage().getMetadata();
//		ArrayList<IIOMetadataNode> tags = new ArrayList<IIOMetadataNode>();
//		if (metadata != null)
//		{
//			for (String name : metadata.getMetadataFormatNames())
//			{
//				IIOMetadataNode node = (IIOMetadataNode) metadata.getAsTree(name);
//				Node[] unknownNodes = getElementsByTagName(tagName, node);
//				for (Node unknownNode : unknownNodes)
//				{
//					tags.add((IIOMetadataNode) unknownNode);
//				}
//			}
//		}
//		return tags.toArray(new IIOMetadataNode[tags.size()]);
//	}
//
//	// **************************************************************************
//	// ** getElementsByTagName (Copied from javaxt.xml.DOM)
//	// **************************************************************************
//	/**
//	 * Returns an array of nodes that match a given tagName (node name). The
//	 * results will include all nodes that match, regardless of namespace. To
//	 * narrow the results to a specific namespace, simply include the namespace
//	 * prefix in the tag name (e.g. "t:Contact"). Returns an empty array if no
//	 * nodes are found.
//	 *
//	 * @param tagName
//	 *            the tag name
//	 * @param node
//	 *            the node
//	 * @return the elements by tag name
//	 */
//	protected static Node[] getElementsByTagName(String tagName, Node node)
//	{
//		ArrayList<Node> nodes = new ArrayList<Node>();
//		getElementsByTagName(tagName, node, nodes);
//		return nodes.toArray(new Node[nodes.size()]);
//	}
//
//	/**
//	 * Gets the elements by tag name.
//	 *
//	 * @param tagName
//	 *            the tag name
//	 * @param node
//	 *            the node
//	 * @param nodes
//	 *            the nodes
//	 * @return the elements by tag name
//	 */
//	private static void getElementsByTagName(String tagName, Node node, ArrayList<Node> nodes)
//	{
//		if (node != null && node.getNodeType() == 1)
//		{
//
//			String nodeName = node.getNodeName().trim();
//			if (nodeName.contains(":") && !tagName.contains(":"))
//			{
//				nodeName = nodeName.substring(nodeName.indexOf(":") + 1);
//			}
//
//			if (nodeName.equalsIgnoreCase(tagName))
//			{
//				nodes.add(node);
//			}
//
//			NodeList childNodes = node.getChildNodes();
//			for (int i = 0; i < childNodes.getLength(); i++)
//			{
//				getElementsByTagName(tagName, childNodes.item(i), nodes);
//			}
//		}
//	}
//
//	// **************************************************************************
//	// ** getAttributeValue (Copied from javaxt.xml.DOM)
//	// **************************************************************************
//	/**
//	 * Used to return the value of a given node attribute. The search is case
//	 * insensitive. If no match is found, returns an empty string.
//	 *
//	 * @param attrCollection
//	 *            the attr collection
//	 * @param attrName
//	 *            the attr name
//	 * @return the attribute value
//	 */
//	public static String getAttributeValue(NamedNodeMap attrCollection, String attrName)
//	{
//
//		if (attrCollection != null)
//		{
//			for (int i = 0; i < attrCollection.getLength(); i++)
//			{
//				Node node = attrCollection.item(i);
//				if (node.getNodeName().equalsIgnoreCase(attrName))
//				{
//					return node.getNodeValue();
//				}
//			}
//		}
//		return "";
//	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Image#getSource()
	 */
	@Override
	public ImageProducer getSource()
	{
		return this.toBufferedImage().getSource();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Image#getProperty(java.lang.String,
	 * java.awt.image.ImageObserver)
	 */
	@Override
	public Object getProperty(String name, ImageObserver observer)
	{
		return this.toBufferedImage().getProperty(name, observer);
	}

	// **************************************************************************
	// ** equals
	// **************************************************************************
	/**
	 * Used to compare this image to another. If the ARGB values match, this
	 * method will return true.
	 *
	 * @param obj
	 *            the obj
	 * @return true, if successful
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (obj != null)
		{
			if (obj instanceof Bitmap)
			{
				Bitmap image = (Bitmap) obj;
				if (image.getWidth() == this.getWidth() && image.getHeight() == this.getHeight())
				{

					// Iterate through all the pixels in the image and compare
					// RGB values
					for (int i = 0; i < image.getWidth(); i++)
					{
						for (int j = 0; j < image.getHeight(); j++)
						{
							if (!image.getColor(i, j).equals(this.getColor(i, j)))
							{
								return false;
							}
						}
					}
					return true;
				}
			}
		}
		return false;
	}

	// **************************************************************************
	// ** copy
	// **************************************************************************
	/**
	 * Returns a copy of the current image. clone를 구현하는게 나을듯
	 * 
	 * @return the bitmap
	 */
	@Override
	public Bitmap clone()
	{
		return new Bitmap(this.toBufferedImage());
	}

	// **************************************************************************
	// ** copyRect
	// **************************************************************************
	/**
	 * Returns a copy of the image at a given rectangle.
	 *
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 * @return the bitmap
	 */

	public Bitmap clone(int x, int y, int width, int height)
	{
		return new Bitmap(getSubimage(x, y, width, height));
	}
	
	public Bitmap clone(Rectangle rectangle)
	{
		return new Bitmap(getSubimage(rectangle));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.AutoCloseable#close()
	 */
	@Override
	public void close()
	{
		this.toBufferedImage().flush();
		this.iioImage = null;
	}

	// ***************************************************************************
	// ** Skew Class
	// ***************************************************************************
	/**
	 * Used to skew an image. Adapted from 2 image processing classes developed
	 * by Jerry Huxtable (http://www.jhlabs.com) and released under the Apache
	 * License, Version 2.0.
	 *
	 ***************************************************************************/

	private class Skew
	{

		/** The Constant ZERO. */
		public final static int ZERO = 0;

		/** The Constant CLAMP. */
		public final static int CLAMP = 1;

		/** The Constant WRAP. */
		public final static int WRAP = 2;

		/** The Constant NEAREST_NEIGHBOUR. */
		public final static int NEAREST_NEIGHBOUR = 0;

		/** The Constant BILINEAR. */
		public final static int BILINEAR = 1;

		/** The edge action. */
		protected int edgeAction = ZERO;

		/** The interpolation. */
		protected int interpolation = BILINEAR;

		/** The transformed space. */
		protected Rectangle transformedSpace;

		/** The original space. */
		protected Rectangle originalSpace;

		/** The y3. */
		private float x0, y0, x1, y1, x2, y2, x3, y3;

		/** The dy3. */
		private float dx1, dy1, dx2, dy2, dx3, dy3;

		/** The i. */
		private float A, B, C, D, E, F, G, H, I;

		/** The src. */
		private BufferedImage src;

		/** The dst. */
		private BufferedImage dst;

		/**
		 * Instantiates a new skew.
		 *
		 * @param src
		 *            the src
		 */
		public Skew(BufferedImage src)
		{
			this.src = src;
			this.dst = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
		}

		/**
		 * Instantiates a new skew.
		 *
		 * @param src
		 *            the src
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		public Skew(Bitmap src) throws IOException
		{
			this(src.toBufferedImage());
		}

		/**
		 * 설정 corners.
		 *
		 * @param x0
		 *            the x0
		 * @param y0
		 *            the y0
		 * @param x1
		 *            the x1
		 * @param y1
		 *            the y1
		 * @param x2
		 *            the x2
		 * @param y2
		 *            the y2
		 * @param x3
		 *            the x3
		 * @param y3
		 *            the y3
		 * @return the buffered image
		 */
		public BufferedImage setCorners(float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3)
		{
			this.x0 = x0;
			this.y0 = y0;
			this.x1 = x1;
			this.y1 = y1;
			this.x2 = x2;
			this.y2 = y2;
			this.x3 = x3;
			this.y3 = y3;

			dx1 = x1 - x2;
			dy1 = y1 - y2;
			dx2 = x3 - x2;
			dy2 = y3 - y2;
			dx3 = x0 - x1 + x2 - x3;
			dy3 = y0 - y1 + y2 - y3;

			float a11, a12, a13, a21, a22, a23, a31, a32;

			if (dx3 == 0 && dy3 == 0)
			{
				a11 = x1 - x0;
				a21 = x2 - x1;
				a31 = x0;
				a12 = y1 - y0;
				a22 = y2 - y1;
				a32 = y0;
				a13 = a23 = 0;
			} else
			{
				a13 = (dx3 * dy2 - dx2 * dy3) / (dx1 * dy2 - dy1 * dx2);
				a23 = (dx1 * dy3 - dy1 * dx3) / (dx1 * dy2 - dy1 * dx2);
				a11 = x1 - x0 + a13 * x1;
				a21 = x3 - x0 + a23 * x3;
				a31 = x0;
				a12 = y1 - y0 + a13 * y1;
				a22 = y3 - y0 + a23 * y3;
				a32 = y0;
			}

			A = a22 - a32 * a23;
			B = a31 * a23 - a21;
			C = a21 * a32 - a31 * a22;
			D = a32 * a13 - a12;
			E = a11 - a31 * a13;
			F = a31 * a12 - a11 * a32;
			G = a12 * a23 - a22 * a13;
			H = a21 * a13 - a11 * a23;
			I = a11 * a22 - a21 * a12;

			return filter(src, dst);
		}

		/**
		 * Transform space.
		 *
		 * @param rect
		 *            the rect
		 */
		protected void transformSpace(Rectangle rect)
		{
			rect.x = (int) Math.min(Math.min(x0, x1), Math.min(x2, x3));
			rect.y = (int) Math.min(Math.min(y0, y1), Math.min(y2, y3));
			rect.width = (int) Math.max(Math.max(x0, x1), Math.max(x2, x3)) - rect.x;
			rect.height = (int) Math.max(Math.max(y0, y1), Math.max(y2, y3)) - rect.y;
		}

		/**
		 * Gets the origin x.
		 *
		 * @return the origin x
		 */
		public float getOriginX()
		{
			return x0 - (int) Math.min(Math.min(x0, x1), Math.min(x2, x3));
		}

		/**
		 * Gets the origin y.
		 *
		 * @return the origin y
		 */
		public float getOriginY()
		{
			return y0 - (int) Math.min(Math.min(y0, y1), Math.min(y2, y3));
		}

		/**
		 * Filter.
		 *
		 * @param src
		 *            the src
		 * @param dst
		 *            the dst
		 * @return the buffered image
		 */
		private BufferedImage filter(BufferedImage src, BufferedImage dst)
		{
			int width = src.getWidth();
			int height = src.getHeight();
			// int type = src.getType();
			// WritableRaster srcRaster = src.getRaster();

			originalSpace = new Rectangle(0, 0, width, height);
			transformedSpace = new Rectangle(0, 0, width, height);
			transformSpace(transformedSpace);

			if (dst == null)
			{
				ColorModel dstCM = src.getColorModel();
				dst = new BufferedImage(dstCM, dstCM.createCompatibleWritableRaster(transformedSpace.width,
					transformedSpace.height), dstCM.isAlphaPremultiplied(), null);
			}
			// WritableRaster dstRaster = dst.getRaster();

			int[] inPixels = getRGB(src, 0, 0, width, height, null);

			if (interpolation == NEAREST_NEIGHBOUR)
				return filterPixelsNN(dst, width, height, inPixels, transformedSpace);

			int srcWidth = width;
			int srcHeight = height;
			int srcWidth1 = width - 1;
			int srcHeight1 = height - 1;
			int outWidth = transformedSpace.width;
			int outHeight = transformedSpace.height;
			int outX, outY;
			// int index = 0;
			int[] outPixels = new int[outWidth];

			outX = transformedSpace.x;
			outY = transformedSpace.y;
			float[] out = new float[2];

			for (int y = 0; y < outHeight; y++)
			{
				for (int x = 0; x < outWidth; x++)
				{
					transformInverse(outX + x, outY + y, out);
					int srcX = (int) Math.floor(out[0]);
					int srcY = (int) Math.floor(out[1]);
					float xWeight = out[0] - srcX;
					float yWeight = out[1] - srcY;
					int nw, ne, sw, se;

					if (srcX >= 0 && srcX < srcWidth1 && srcY >= 0 && srcY < srcHeight1)
					{
						// Easy case, all corners are in the image
						int i = srcWidth * srcY + srcX;
						nw = inPixels[i];
						ne = inPixels[i + 1];
						sw = inPixels[i + srcWidth];
						se = inPixels[i + srcWidth + 1];
					} else
					{
						// Some of the corners are off the image
						nw = getPixel(inPixels, srcX, srcY, srcWidth, srcHeight);
						ne = getPixel(inPixels, srcX + 1, srcY, srcWidth, srcHeight);
						sw = getPixel(inPixels, srcX, srcY + 1, srcWidth, srcHeight);
						se = getPixel(inPixels, srcX + 1, srcY + 1, srcWidth, srcHeight);
					}
					outPixels[x] = bilinearInterpolate(xWeight, yWeight, nw, ne, sw, se);
				}
				setRGB(dst, 0, y, transformedSpace.width, 1, outPixels);
			}
			return dst;
		}

		/**
		 * Gets the pixel.
		 *
		 * @param pixels
		 *            the pixels
		 * @param x
		 *            the x
		 * @param y
		 *            the y
		 * @param width
		 *            the width
		 * @param height
		 *            the height
		 * @return the pixel
		 */
		final private int getPixel(int[] pixels, int x, int y, int width, int height)
		{
			if (x < 0 || x >= width || y < 0 || y >= height)
			{
				switch (edgeAction)
				{
					case ZERO:
					default:
						return 0;
					case WRAP:
						return pixels[(mod(y, height) * width) + mod(x, width)];
					case CLAMP:
						return pixels[(clamp(y, 0, height - 1) * width) + clamp(x, 0, width - 1)];
				}
			}
			return pixels[y * width + x];
		}

		/**
		 * Filter pixels nn.
		 *
		 * @param dst
		 *            the dst
		 * @param width
		 *            the width
		 * @param height
		 *            the height
		 * @param inPixels
		 *            the in pixels
		 * @param transformedSpace
		 *            the transformed space
		 * @return the buffered image
		 */
		protected BufferedImage filterPixelsNN(BufferedImage dst, int width, int height, int[] inPixels,
			Rectangle transformedSpace)
		{
			int srcWidth = width;
			int srcHeight = height;
			int outWidth = transformedSpace.width;
			int outHeight = transformedSpace.height;
			int outX, outY, srcX, srcY;
			int[] outPixels = new int[outWidth];

			outX = transformedSpace.x;
			outY = transformedSpace.y;
			int[] rgb = new int[4];
			float[] out = new float[2];

			for (int y = 0; y < outHeight; y++)
			{
				for (int x = 0; x < outWidth; x++)
				{
					transformInverse(outX + x, outY + y, out);
					srcX = (int) out[0];
					srcY = (int) out[1];
					// int casting rounds towards zero, so we check out[0] < 0,
					// not srcX < 0
					if (out[0] < 0 || srcX >= srcWidth || out[1] < 0 || srcY >= srcHeight)
					{
						int p;
						switch (edgeAction)
						{
							case ZERO:
							default:
								p = 0;
							break;
							case WRAP:
								p = inPixels[(mod(srcY, srcHeight) * srcWidth) + mod(srcX, srcWidth)];
							break;
							case CLAMP:
								p = inPixels[(clamp(srcY, 0, srcHeight - 1) * srcWidth) + clamp(srcX, 0, srcWidth - 1)];
							break;
						}
						outPixels[x] = p;
					} else
					{
						int i = srcWidth * srcY + srcX;
						rgb[0] = inPixels[i];
						outPixels[x] = inPixels[i];
					}
				}
				setRGB(dst, 0, y, transformedSpace.width, 1, outPixels);
			}
			return dst;
		}

		/**
		 * Transform inverse.
		 *
		 * @param x
		 *            the x
		 * @param y
		 *            the y
		 * @param out
		 *            the out
		 */
		protected void transformInverse(int x, int y, float[] out)
		{
			out[0] = originalSpace.width * (A * x + B * y + C) / (G * x + H * y + I);
			out[1] = originalSpace.height * (D * x + E * y + F) / (G * x + H * y + I);
		}

		/*
		 * public Rectangle2D getBounds2D( BufferedImage src ) { return new
		 * Rectangle(0, 0, src.getWidth(), src.getHeight()); }
		 * 
		 * public Point2D getPoint2D( Point2D srcPt, Point2D dstPt ) { if (
		 * dstPt == null ) dstPt = new Point2D.Double(); dstPt.setLocation(
		 * srcPt.getX(), srcPt.getY() ); return dstPt; }
		 */

		/**
		 * A convenience method for getting ARGB pixels from an image. This
		 * tries to avoid the performance penalty of BufferedImage.getRGB
		 * unmanaging the image.
		 *
		 * @param image
		 *            the image
		 * @param x
		 *            the x
		 * @param y
		 *            the y
		 * @param width
		 *            the width
		 * @param height
		 *            the height
		 * @param pixels
		 *            the pixels
		 * @return the rgb
		 */
		public int[] getRGB(BufferedImage image, int x, int y, int width, int height, int[] pixels)
		{
			int type = image.getType();
			if (type == BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB)
				return (int[]) image.getRaster().getDataElements(x, y, width, height, pixels);
			return image.getRGB(x, y, width, height, pixels, 0, width);
		}

		/**
		 * A convenience method for setting ARGB pixels in an image. This tries
		 * to avoid the performance penalty of BufferedImage.setRGB unmanaging
		 * the image.
		 *
		 * @param image
		 *            the image
		 * @param x
		 *            the x
		 * @param y
		 *            the y
		 * @param width
		 *            the width
		 * @param height
		 *            the height
		 * @param pixels
		 *            the pixels
		 */
		public void setRGB(BufferedImage image, int x, int y, int width, int height, int[] pixels)
		{
			int type = image.getType();
			if (type == BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB)
				image.getRaster().setDataElements(x, y, width, height, pixels);
			else
				image.setRGB(x, y, width, height, pixels, 0, width);
		}

		/**
		 * Clamp a value to an interval.
		 *
		 * @param x
		 *            the input parameter
		 * @param a
		 *            the lower clamp threshold
		 * @param b
		 *            the upper clamp threshold
		 * @return the clamped value
		 */
		private float clamp(float x, float a, float b)
		{
			return (x < a) ? a : (x > b) ? b : x;
		}

		/**
		 * Clamp a value to an interval.
		 *
		 * @param x
		 *            the input parameter
		 * @param a
		 *            the lower clamp threshold
		 * @param b
		 *            the upper clamp threshold
		 * @return the clamped value
		 */
		private int clamp(int x, int a, int b)
		{
			return (x < a) ? a : (x > b) ? b : x;
		}

		/**
		 * Return a mod b. This differs from the % operator with respect to
		 * negative numbers.
		 * 
		 * @param a
		 *            the dividend
		 * @param b
		 *            the divisor
		 * @return a mod b
		 */
		private double mod(double a, double b)
		{
			int n = (int) (a / b);

			a -= n * b;
			if (a < 0)
				return a + b;
			return a;
		}

		/**
		 * Return a mod b. This differs from the % operator with respect to
		 * negative numbers.
		 * 
		 * @param a
		 *            the dividend
		 * @param b
		 *            the divisor
		 * @return a mod b
		 */
		private float mod(float a, float b)
		{
			int n = (int) (a / b);

			a -= n * b;
			if (a < 0)
				return a + b;
			return a;
		}

		/**
		 * Return a mod b. This differs from the % operator with respect to
		 * negative numbers.
		 * 
		 * @param a
		 *            the dividend
		 * @param b
		 *            the divisor
		 * @return a mod b
		 */
		private int mod(int a, int b)
		{
			int n = a / b;

			a -= n * b;
			if (a < 0)
				return a + b;
			return a;
		}

		/**
		 * Bilinear interpolation of ARGB values.
		 *
		 * @param x
		 *            the X interpolation parameter 0..1
		 * @param y
		 *            the y interpolation parameter 0..1
		 * @param nw
		 *            the nw
		 * @param ne
		 *            the ne
		 * @param sw
		 *            the sw
		 * @param se
		 *            the se
		 * @return the interpolated value
		 */
		private int bilinearInterpolate(float x, float y, int nw, int ne, int sw, int se)
		{
			float m0, m1;
			int a0 = (nw >> 24) & 0xff;
			int r0 = (nw >> 16) & 0xff;
			int g0 = (nw >> 8) & 0xff;
			int b0 = nw & 0xff;
			int a1 = (ne >> 24) & 0xff;
			int r1 = (ne >> 16) & 0xff;
			int g1 = (ne >> 8) & 0xff;
			int b1 = ne & 0xff;
			int a2 = (sw >> 24) & 0xff;
			int r2 = (sw >> 16) & 0xff;
			int g2 = (sw >> 8) & 0xff;
			int b2 = sw & 0xff;
			int a3 = (se >> 24) & 0xff;
			int r3 = (se >> 16) & 0xff;
			int g3 = (se >> 8) & 0xff;
			int b3 = se & 0xff;

			float cx = 1.0f - x;
			float cy = 1.0f - y;

			m0 = cx * a0 + x * a1;
			m1 = cx * a2 + x * a3;
			int a = (int) (cy * m0 + y * m1);

			m0 = cx * r0 + x * r1;
			m1 = cx * r2 + x * r3;
			int r = (int) (cy * m0 + y * m1);

			m0 = cx * g0 + x * g1;
			m1 = cx * g2 + x * g3;
			int g = (int) (cy * m0 + y * m1);

			m0 = cx * b0 + x * b1;
			m1 = cx * b2 + x * b3;
			int b = (int) (cy * m0 + y * m1);

			return (a << 24) | (r << 16) | (g << 8) | b;
		}

	} // end skew class
} // end Bitmap class