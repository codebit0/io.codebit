package io.codebit.support.image;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;

import javax.imageio.*;
import javax.imageio.metadata.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.ImageObserver;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import io.codebit.support.image.options.CropOption;
import io.codebit.support.image.options.IWriteOption;
import io.codebit.support.image.options.RatioOption;
import io.codebit.support.image.options.RotateFlipType;

//******************************************************************************
//**  Image Utilities - By Peter Borissow remake
//******************************************************************************

public class BitmapImage implements AutoCloseable
{
	protected static final Bitmap.Format DEFAULT_FORMAT = Bitmap.Format.JPEG;
	// //https://kippler.com/doc/jpeg2000_vs_jpegxr/

		
	//[global image object]------------------------------------------------------
	private ImageReadParam DEFAULT_READ_PARAM;
	

	/** The image reader. */
	private ImageReader reader = null;

	private IIOMetadata streamMetadata;

	/** The format name. */
	private Bitmap.Format format = null;

	//@Getter(AccessLevel.PROTECTED)
	protected LayerList layers;


	private InputStream inputStream;


//	private String name;
	
	/**
	 * From stream.
	 *
	 * @param inputStream
	 *            the input stream
	 * @return the bitmap
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws UnsupportedOperationException
	 *             지원하지 않는 이미지 포맷
	 */
	public static BitmapImage fromStream(InputStream inputStream) throws IOException, UnsupportedOperationException
	{
		ImageInputStream stream = ImageIO.createImageInputStream(inputStream);
		Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);

		ImageReader imageReader = null;
		while (readers.hasNext())
		{
			imageReader = readers.next();
			// 일단 지원 포멧을 com.sun.imageio 만으로 제한
			if(imageReader.getClass().getName().startsWith("com.sun.imageio.plugins.gif.GIFImageReader"))
			{
				//Gif버그 패치 버전으로 교체
				imageReader = new PatchedGIFImageReader(imageReader.getOriginatingProvider());
				break;
				//continue;
			}
			if(imageReader != null)
				break;
		}
		if (imageReader == null)
		{
			// 지원하지 않는 포멧
			throw new UnsupportedOperationException("지원하지 않는 이미지 포멧");
//			throw new IIOException("지원하지 않는 이미지 포멧");
		}
		imageReader.setInput(stream, false, false);
		String formatName = imageReader.getFormatName();
		Bitmap.Format format =  null;
		for (Bitmap.Format _format : Bitmap.Format.values())
		{
			for (String extension : _format.getExtension())
			{
				if (extension.equalsIgnoreCase(formatName))
				{
					format = _format;
					break;
				}
			}
		}
		switch (format)
		{
			case GIF:
				return new Gif(imageReader, format);
			case JPEG:
			case JPEG2000:
				return new Jpeg(imageReader, format);
				// case BMP:
			case PNG:
				return new Png(imageReader, format);
				// case TIFF:
			default:
				return new BitmapImage(imageReader, format);
		}
	}
	
	/**
	 * From stream auto close.
	 *
	 * @param inputStream the input stream
	 * @return the bitmap image
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws UnsupportedOperationException
	 *             지원하지 않는 이미지 포맷
	 */
	private static BitmapImage fromStreamAutoClose(InputStream inputStream) throws IOException,UnsupportedOperationException
	{
		BitmapImage bitmapImage = fromStream(inputStream);
		bitmapImage.inputStream = inputStream;
		return bitmapImage;
	}
	
	/**
	 * From file.
	 *
	 * @param file            the file
	 * @return the bitmap
	 * @throws FileNotFoundException             the file not found exception
	 * @throws IOException             Signals that an I/O exception has occurred.
	 * @throws UnsupportedOperationException 지원하지 않는 이미지 포맷
	 */
	public static BitmapImage fromFile(File file) throws FileNotFoundException, IOException, UnsupportedOperationException
	{
		return fromStreamAutoClose(new FileInputStream(file));
	}

	/**
	 * From uri.
	 *
	 * @param uri
	 *            the uri
	 * @return the bitmap
	 * @throws MalformedURLException
	 *             the malformed url exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws UnsupportedOperationException 지원하지 않는 이미지 포맷
	 */
	public static BitmapImage fromURI(URI uri) throws MalformedURLException, IOException, UnsupportedOperationException
	{
//		String fileName = null;
//		URLConnection conn = uri.toURL().openConnection();
//		if (conn instanceof HttpURLConnection)
//		{
//			HttpURLConnection httpConn = (HttpURLConnection) conn;
//			int responseCode = httpConn.getResponseCode();
//			if (responseCode == HttpURLConnection.HTTP_OK)
//			{
//				String disposition = httpConn.getHeaderField("Content-Disposition");
//				if (disposition != null)
//				{
//					int index = disposition.indexOf("filename=");
//					if (index > 0)
//					{
//						fileName = disposition.substring(index + 10, disposition.length() - 1);
//					}
//				}
//			}
//		}
//		if (fileName == null)
//		{
//			String uriString = uri.toASCIIString();
//			int lastIndexOf = uriString.lastIndexOf("/");
//			int min = Math.min(250, uriString.length() - lastIndexOf);
//			fileName = uriString.substring(lastIndexOf + 1, lastIndexOf + min);
//		}
//		return fromStream(uri.toURL().openStream(), fileName);
		return fromStreamAutoClose(uri.toURL().openStream());
	}

	/**
	 * From byte.
	 *
	 * @param bytes            the bytes
	 * @return the bitmap
	 * @throws IOException             Signals that an I/O exception has occurred.
	 * @throws UnsupportedOperationException 지원하지 않는 이미지 포맷
	 */
	public static BitmapImage fromByte(byte[] bytes) throws IOException, UnsupportedOperationException
	{
		return fromStreamAutoClose(new ByteArrayInputStream(bytes));
	}

	// **************************************************************************
	// ** Constructor
	// **************************************************************************
	protected BitmapImage(ImageReader reader, Bitmap.Format format) throws IOException
	{
		this.reader = reader;
		this.format = format;
		
		DEFAULT_READ_PARAM = reader.getDefaultReadParam();
		int numImages = this.ImageReader().getNumImages(true);
		this.layers = new LayerList(numImages);
//		for (int i = 0; i < numImages; i++)
//		{
//			this.frames.add(null);
//		}
		try
		{
			this.streamMetadata = reader.getStreamMetadata();
		} catch (Exception e)
		{
			//FIXME LOG
//			log.debug("스트림 메타데이터 읽기 오류");
		}
	}
	
	protected BitmapImage(ImageReader reader, Bitmap.Format format, InputStream inputStream) throws IOException
	{
		this(reader, format);
		this.inputStream = inputStream;
	}

//	public String getName()
//	{
//		return this.name;
//	}
//	
//	public void setName(String name)
//	{
//		this.name = name;
//	}
	
	/**
	 * 메서드는 현재 Bitmap객체를 반환합니다.
	 *
	 * @param index            the index
	 * @return the frame
	 */
	public Bitmap getLayer(int index)
	{
		return this.layers.get(index);
	}

	protected Bitmap loadLayer(int index) throws IOException
	{
		IIOImage iioImage
		 = reader.readAll(index, DEFAULT_READ_PARAM);
		return new Bitmap(iioImage);
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
		return this.getLayer(0).getWidth();
	}

	public int getHeight()
	{
		return this.getLayer(0).getHeight();
	}

	public int getWidth(ImageObserver observer)
	{
		return this.getLayer(0).getWidth(observer);
	}

	public int getHeight(ImageObserver observer)
	{
		return this.getLayer(0).getHeight(observer);
	}

	/**
	 * Gets the frame count.
	 *
	 * @return the frame count
	 */
	public int getFrameCount()
	{
		return this.layers.size();
	}

	/**
	 * 포멧명을 반환합니다.
	 *
	 * @return 이미지소스가 스트림에서 포멧명을 아니면 null
	 */
	public Bitmap.Format getFormat()
	{
		return this.format;
	}

	protected ImageReader ImageReader()
	{
		return this.reader;
	}

	protected IIOMetadata StreamMetadata()
	{
		return this.streamMetadata;
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
		for (Bitmap bitmap : this.layers)
		{
			bitmap.flip();
		}
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
	 * @param degrees
	 *            the degrees
	 */
	public void rotate(double degrees)
	{
		for (Bitmap bitmap : this.layers)
		{
			bitmap.rotate(degrees);
		}
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

	private void scale(double ratio)
	{
		int width = this.getWidth();
		int height = this.getHeight();

		double dw = width * ratio;
		double dh = height * ratio;
		width = (int) Math.round(dw);
		height = (int) Math.round(dh);

		resize(width, height);
	}

	private void scale(double widthRatio, double heightRatio)
	{
		int width = this.getWidth();
		int height = this.getHeight();

		double dw = width * widthRatio;
		double dh = height * heightRatio;
		width = (int) Math.round(dw);
		height = (int) Math.round(dh);

		resize(width, height);
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
		this.resize(new Dimension(width, height), RatioOption.IgnoreAspectRatio);
	}
	
	public void resize(Dimension dimension)
	{
		this.resize(dimension, RatioOption.IgnoreAspectRatio);
	}

	
	public void resize(Dimension dimension, CropOption crop)
	{
		for (Bitmap bitmap : this.layers)
		{
			bitmap.resize(dimension, RatioOption.OuterAspectRatio);
			bitmap.crop(dimension, crop);
		}
	}

	public void resize(Dimension dimension, RatioOption option)
	{
		for (Bitmap bitmap : this.layers)
		{
			bitmap.resize(dimension, option);
		}
	}
	

	// **************************************************************************
	// ** Crop
	// **************************************************************************

	public void crop(Rectangle rectangle)
	{
		for (Bitmap bitmap : this.layers)
		{
			bitmap.crop(rectangle);
		}
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
		this.crop(new Rectangle(x,y,width, height));
	}

	public void crop(Dimension dimension, CropOption crop)
	{
		Point p = crop.point(this.getWidth(), this.getHeight(), dimension.width, dimension.height);
		this.crop(new Rectangle(p, dimension));
	}

	public void desaturate()
	{
		for (Bitmap bitmap : this.layers)
		{
			bitmap.desaturate();
		}
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
		for (Bitmap bitmap : this.layers)
		{
			bitmap.desaturate(percent);
		}
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

	/**
	 * ImageInputStream 및 ImageOutputStream 의 작성시에, 디스크 베이스의 캐쉬 파일을 사용
	 *
	 * @param cacheDirectory
	 *            캐쉬 파일이 작성되는 디렉토리를 설정합니다.
	 */
	public static void setCacheDirectory(Path cacheDirectory)
	{
		File dir = cacheDirectory.toFile();
		if (dir.isFile())
		{
			dir = dir.getParentFile();
		}
		dir.mkdirs();
		ImageIO.setUseCache(true);
		ImageIO.setCacheDirectory(dir);
	}

	public static Path getCacheDirectory()
	{
		File cacheDirectory = ImageIO.getCacheDirectory();
		if (cacheDirectory != null)
			return cacheDirectory.toPath();
		return null;
	}

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
		for (Bitmap bitmap : this.layers)
		{
			bitmap.setOpacity(percent);
		}
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

		for (Bitmap bitmap : this.layers)
		{
			bitmap.setCorners(x0, y0, x1, y1, x2, y2, x3, y3);
		}
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
		this.trim(0, 0, 0);
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
		for (Bitmap bitmap : this.layers)
		{
			bitmap.trim(r, g, g);
		}
	}

	public void trim(Color color)
	{
		trim(color.getRed(), color.getGreen(), color.getBlue());
	}


	// **************************************************************************
	// ** saveAs
	// **************************************************************************
	/**
	 * Exports the image to a file. Output format is determined by the output
	 * file extension.
	 *
	 * @param OutputFile
	 *            the output file
	 * @param option
	 *            the option
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void saveAs(File OutputFile, IWriteOption option) throws IOException
	{
		try (FileOutputStream fileOut = new FileOutputStream(OutputFile))
		{
			write(fileOut, option);
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
		this.saveAs(OutputFile, (IWriteOption) null);
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
	 * @param output
	 *            the output
	 * @param option
	 *            the option
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void write(OutputStream output, IWriteOption option) throws IOException
	{
		try
		{
			Bitmap.Format format = DEFAULT_FORMAT;
			ImageWriter imageWriter = null;
			if (option != null)
			{
				format = option.format();
			} else if (this.getFormat() != null)
			{
				format = this.getFormat();
			}
			Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(format.name());
			imageWriter = writers.next();

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
				boolean ignoreMetadata = false;
				if (option != null)
				{
					writeParam = option.build(writeParam);
					ignoreMetadata = option.ignoreMetadata();
				}
				//파일로 불러온 이미지와 저장 이미지가 다르면 metadata를 무시함
				if(!this.format.equals(format))
					ignoreMetadata = true;

				//TODO frameCount로 체크하는 것보다 format으로 체크하는게 좋을듯
				int frameCount = this.getFrameCount();
				if(frameCount > 1)
				{
					if(!ignoreMetadata)
						imageWriter.prepareWriteSequence(streamMetadata);
					else
						imageWriter.prepareWriteSequence(null);
					for (int i = 0; i < frameCount; i++)
					{
						Bitmap frame = this.getLayer(i);
						IIOImage image = frame.IIOImage();
						//FIXME 메타데이터를 null로 날려버리면 같은 파일을 다시 save할때 
						// 메타데이터가 날라감 복제하여 처리하여야함
						if(ignoreMetadata)
							frame.setIIOMetadata(null);
						imageWriter.writeToSequence(image, writeParam);
					}
					imageWriter.endWriteSequence();
				}else
				{
					Bitmap bitmap = this.getLayer(0);
					if(ignoreMetadata)
						bitmap.setIIOMetadata(null);
					IIOImage image = bitmap.IIOImage();
					imageWriter.write(null, image, writeParam);
				}
				output.flush();
				imageWriter.dispose();
			}
		} catch (IOException e)
		{
			throw e;
		}
	}
	
//	public InputStream toOutput(IWriteOption option) throws IOException
//	{
//		Path tempFile = Files.createTempFile("dd", "qq");
//		File file = tempFile.toFile();
//		FileOutputStream output =  new FileOutputStream(file);
//		this.write(output, option);
//		return new FileInputStream(file);
//	}

//	// **************************************************************************
//	// ** equals
//	// **************************************************************************
//	/**
//	 * Used to compare this image to another. If the ARGB values match, this
//	 * method will return true.
//	 *
//	 * @param obj
//	 *            the obj
//	 * @return true, if successful
//	 */
//	@Override
//	public boolean equals(Object obj)
//	{
//		if (obj != null)
//		{
//			if (obj instanceof BitmapImage)
//			{
//				BitmapImage image = (BitmapImage) obj;
//				if (image.getWidth() == this.getWidth() && image.getHeight() == this.getHeight())
//				{
//
//					// Iterate through all the pixels in the image and compare
//					// RGB values
//					for (int i = 0; i < image.getWidth(); i++)
//					{
//						for (int j = 0; j < image.getHeight(); j++)
//						{
//							if (!image.getColor(i, j).equals(this.getColor(i, j)))
//							{
//								return false;
//							}
//						}
//					}
//					return true;
//				}
//			}
//		}
//		return false;
//	}

	@Override
	public void close()
	{
		try
		{
			if(this.inputStream != null)
			{
				this.inputStream.close();
			}
		} catch (Exception e)
		{
			//log.error("Bitmap Close Error", e);
		}
		try
		{
			int size = this.layers.size();
			for (int i = 0; i < size; i++)
			{
				this.layers.remove(0);
			}
			this.layers.clear();
			this.reader.dispose();
//			((ImageInputStream)this.reader.getInput()).flush();
			((ImageInputStream)this.reader.getInput()).close();
		} catch (Exception e)
		{
//			log.error("Bitmap Close Error", e);
		}
	}
	
	//FIXME 이 구조로는 나중에 add Layer를 지원했을때 나중에 지연ㄹ 로드하는 인덱스에 문제가 생김
	class LayerList extends ArrayList<Bitmap>
	{
		private static final long serialVersionUID = 1L;
		public LayerList(int numImages)
		{
			super(numImages);
			for (int i = 0; i < numImages; i++)
			{
				super.add(null);
			}
		}
		
		@Override
		public Iterator<Bitmap> iterator()
		{
			return new Itr(super.iterator());
		}
		
		@Override
		public Bitmap get(int index) 
		{
			Bitmap bitmap = super.get(index);
	        if(bitmap == null)
	        {
	        	try
				{
					bitmap = loadLayer(index);
					this.set(index, bitmap);
				} catch (IOException e)
				{
					throw new RuntimeException(e);
				}
	        }
			return bitmap;
	    }
		
		/* 
		 * 무조건 null을 리턴함
		 */
		@Override
		public Bitmap remove(int index) 
		{
			Bitmap bitmap = super.get(index);
	        if(bitmap != null)
	        {
	        	bitmap.close();
	        }
	        super.remove(index);
			return null;
	    }
		
		public Bitmap unloadGet(int index) 
		{
			Bitmap bitmap = super.get(index);
			return bitmap;
	    }
		
		private class Itr implements Iterator<Bitmap> 
		{
			int cursor;
			int size = LayerList.this.size();
			
			private Iterator<Bitmap> iterator;

			public Itr(Iterator<Bitmap> iterator)
			{
				this.iterator = iterator;
			}

			@Override
			public boolean hasNext()
			{
				return cursor != size;
			}

			@Override
			public Bitmap next()
			{
				Bitmap bitmap = iterator.next();
				if(bitmap == null)
				{
					try
					{
						bitmap = loadLayer(cursor);
						LayerList.this.set(cursor, bitmap);
					} catch (IOException e)
					{
						throw new RuntimeException(e);
					}
				}
				cursor ++;
				return bitmap;
			}
		}
	}
} // end Bitmap class