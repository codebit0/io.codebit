package io.codebit.support.image;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.imageio.IIOImage;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.metadata.IIOMetadata;

import com.sun.imageio.plugins.gif.GIFImageMetadata;
import com.sun.imageio.plugins.gif.GIFStreamMetadata;
import io.codebit.support.image.Gif.GifFrame.DisposalMethod;
import io.codebit.support.image.options.CropOption;
import io.codebit.support.image.options.IWriteOption;
import io.codebit.support.image.options.RatioOption;

public class Gif extends BitmapImage
{
	/**
	 * Gif 버전
	 */
	public enum Version
	{
		GIF89a("89a"), GIF87a("87a");
		
		private String version;

		private Version(String version)
		{
			this.version = version;
		}
		
		public String version()
		{
			return version;
		}
	}

	// NETSCAPE Application block
	private static byte[] NETSCAPE = new byte[] {
			'N', 'E', 'T', 'S', 'C', 'A', 'P', 'E'
	};
	private static byte[] NETSCAPE_CODE = new byte[] {
			'2', '.', '0'
	};

//	private HashMap<Integer, GifFrame> gifFrames = new HashMap<Integer, GifFrame>();
//	private List<GifFrame> gifFrames = new ArrayList<GifFrame>();
	private int loopCount = -1; // 0이면 무한 반복
	private int delay = 0;
	private Color transparentColor;
	private GifFrame.DisposalMethod disposalMethod = DisposalMethod.UNSPECIFIED;
	private Version version = Version.GIF89a;


	//global metadata
	private Dimension logicalScreenSize;

//	Gif(Bitmap bitmap) throws IOException
//	{
//		super(bitmap);
//	}
	
	@SuppressWarnings("restriction")
	Gif(ImageReader reader, Bitmap.Format format) throws IOException
	{
		super(reader, format);
		GIFImageMetadata firstMeta = (GIFImageMetadata) this.getLayer(0).getIIOMetadata();
		// 첫번째 프레임의 경우 application영역을 추출후
		// <ApplicationExtension applicationID="NETSCAPE"
		// authenticationCode="2.0"/> 영역 찾기
		List<?> applicationIDs = firstMeta.applicationIDs;
		if(applicationIDs != null)
		{
			int applicationBlock = applicationIDs.size();
			for (int i = 0; i < applicationBlock; i++)
			{
				if (Arrays.equals(NETSCAPE, (byte[]) applicationIDs.get(i))
						&& Arrays.equals(NETSCAPE_CODE, (byte[]) firstMeta.authenticationCodes.get(i)))
				{
					this.loopCount = readNetscapeExtLoopCount((byte[]) firstMeta.applicationData.get(i));
					break;
				}
			}
		}
		// firstFrameMetadata.applicationIDs.
		// firstFrameMetadata.applicationData.get(index);
		// firstFrameMetadata.
		// firstFrameMetadata.applicationData
		GIFStreamMetadata streamMetadata = (GIFStreamMetadata) super.StreamMetadata();
//		byte backGroundColor = streamMeta.globalColorTable[streamMeta.backgroundColorIndex];
//		System.out.println(new Color(backGroundColor));
//		streamMetadata.logicalScreenWidth;
//		streamMetadata.logicalScreenHeight;
		this.setLogicalScreenSize(new Dimension(streamMetadata.logicalScreenWidth, streamMetadata.logicalScreenHeight));
		// Logical Screen Descriptor
		this.version = Enum.valueOf(Version.class, "GIF" + streamMetadata.version);
	}
	
	@Override
	public GifFrame getLayer(int index)
	{
		Bitmap bitmap = super.getLayer(index);
		if(!(bitmap instanceof GifFrame))
		{
			BufferedImage image = bitmap.toBufferedImage();
			IIOMetadata iioMetadata = bitmap.getIIOMetadata();
			if (iioMetadata != null)
			{
				GIFImageMetadata meta = (GIFImageMetadata) iioMetadata;
				bitmap = new GifFrame(image, meta);
			} else
			{
				bitmap = new GifFrame(image);
			}
			this.layers.set(index, bitmap);
		}
		return (GifFrame) bitmap;
	}

	@Override
	protected Bitmap loadLayer(int index) throws IOException
	{
		Bitmap bitmap = super.loadLayer(index);
		BufferedImage image = bitmap.toBufferedImage();
		IIOMetadata iioMetadata = bitmap.getIIOMetadata();
		if (iioMetadata != null)
		{
			GIFImageMetadata meta = (GIFImageMetadata) iioMetadata;
//			log.debug("Gif loadFrame "+index +"-> "+meta.imageWidth + " "+meta.imageHeight);
			bitmap = new GifFrame(image, meta);
		} else
		{
			bitmap = new GifFrame(image);
		}
		return bitmap;
	}
	
	public Dimension getLogicalScreenSize()
	{
		return this.logicalScreenSize;
	}
	
	protected void setLogicalScreenSize(Dimension dimension)
	{
		GIFStreamMetadata streamMetadata = (GIFStreamMetadata) super.StreamMetadata();
		streamMetadata.logicalScreenWidth = dimension.width;
		streamMetadata.logicalScreenHeight = dimension.height;
		this.logicalScreenSize = dimension;
//		this.logicalScreenSize = new Dimension(dimension.width + 100, dimension.height);
	}
	
	protected void calcLogicalScreenSize()
	{
		Dimension dimension = new Dimension();
		int size = this.getFrameCount();
		for (int i = 0; i < size; i++)
		{
			GifFrame frame = this.getLayer(i);
			Point point = frame.getPosition();
			Dimension fdimension = frame.getDimension();
			fdimension.width += point.x;
			fdimension.height += point.y;
			if(dimension.width < fdimension.width)
				dimension.width = fdimension.width;
			if(dimension.height < fdimension.height)
				dimension.height = fdimension.height;
		}
		setLogicalScreenSize(dimension);
	}
	
	public void addLayer(GifFrame frame)
	{
		this.layers.add(this.getFrameCount(), frame);
	}

//	void setFrame(int index, GifFrame frame)
//	{
//	}

	private int readNetscapeExtLoopCount(byte[] block)
	{
		// blockSize =1
		// int blockSize = (int)block[0];
		int b1 = (block[1]) & 0xff;
		int b2 = (block[2]) & 0xff;
		return (b2 << 8) | b1;
	}

	public Version getVersion()
	{
		return version;
	}

	public void setVersion(Version version)
	{
		this.version = version;
	}

	/**
	 * Sets the delay time between each frame, or changes it for subsequent
	 * frames (applies to last frame added).
	 * 
	 * @param ms
	 *            int delay time in milliseconds
	 */
	public void setDelay(int ms)
	{
		if (ms > 0)
		{
			this.delay = Math.round(ms / 10.0f);
		}
	}

	/**
	 * Sets the GIF frame disposal code for the last added frame and any
	 * subsequent frames. Default is 0 if no transparent color has been set,
	 * otherwise 2.
	 *
	 * @param disposalMethod the new dispose
	 */
	public void setDispose(GifFrame.DisposalMethod disposalMethod)
	{
		this.disposalMethod = disposalMethod;
	}

	/**
	 * Sets the number of times the set of GIF frames should be played. Default
	 * is 1; 0 means play indefinitely. Must be invoked before the first image
	 * is added.
	 * 
	 * @param loopCount
	 *            int number of iterations.
	 */
	public void setLoopCount(int loopCount)
	{
		if (loopCount >= 0)
		{
			this.loopCount = loopCount;
		}
	}

	/**
	 * Gets the "Netscape" iteration count, if any. A count of 0 means repeat
	 * indefinitiely.
	 *
	 * @return iteration count if one was specified, else 1.
	 */
	public int getLoopCount()
	{
		return this.loopCount;
	}

	/**
	 * Sets the transparent color for the last added frame and any subsequent
	 * frames. Since all colors are subject to modification in the quantization
	 * process, the color in the final palette for each frame closest to the
	 * given color becomes the transparent color for that frame. May be set to
	 * null to indicate no transparent color.
	 *
	 * @param transparent the new transparent
	 */
	public void setTransparent(Color transparent)
	{
		this.transparentColor = transparent;
	}

	@Override
	public void rotate(double degrees)
	{
		frameCombined();
		super.rotate(degrees);
	}
	
	@Override
	public void resize(Dimension dimension, CropOption crop)
	{
		frameCombined();
		super.resize(dimension, crop);
		this.setLogicalScreenSize(dimension);
	}

	@Override
	public void resize(Dimension dimension, RatioOption option)
	{
		Dimension resize = option.dimension(this.getLogicalScreenSize(), dimension);
		frameCombined();
		//이미지 리사이즈시 좀 문제가 있음
		super.resize(dimension, option);
		this.setLogicalScreenSize(resize);
	}

	private int frameCombined()
	{
		int size = this.getFrameCount();
		for (int i = 0; i < size; i++)
		{
			GifFrame frame = this.getLayer(i);
			
//			System.out.println(this.getLogicalScreenSize().width+ " / "+this.getLogicalScreenSize().height);
			
//			if(i > 0)
//			{
    			BufferedImage combined = new BufferedImage(this.getLogicalScreenSize().width, this.getLogicalScreenSize().height, BufferedImage.TYPE_INT_ARGB);
    			Graphics g = combined.getGraphics();
    			if(i > 0)
    			{
    				BufferedImage beforeImage = this.getLayer(i-1).toBufferedImage();
    				g.drawImage(beforeImage, 0, 0, null);
    			}
				
    			BufferedImage currentImage = frame.toBufferedImage();
    			Point point = frame.getPosition();
				g.drawImage(currentImage, point.x, point.y, null);
//    			System.out.println(i+" = "+currentImage.getWidth()+ " / "+currentImage.getHeight()+ " "+point.x+" : "+point.y);
    			frame.setBufferedImage(combined);
    			try
				{
					frame.saveAs(new File("C:/Temp/test"+i+".jpg"), Bitmap.Format.JPEG);
				} catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//			}
//			else
//			{
//				try
//				{
//					frame.saveAs(new File("C:/Temp/test0.jpg"), Format.JPEG);
//				} catch (IOException e)
//				{
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
			frame.setPosition(0, 0);
		}
		return size;
	}
	
	@Override
	public void crop(Dimension dimension, CropOption crop)
	{
		Point p = crop.point(this.getLogicalScreenSize().width, this.getLogicalScreenSize().height, dimension.width, dimension.height);
		this.crop(new Rectangle(p, dimension));
	}
	
	@Override
	public void crop(Rectangle rectangle)
	{
		frameCombined();
		int size = this.getFrameCount();
		for (int i = 0; i < size; i++)
		{
			GifFrame frame = this.getLayer(i);
			frame.crop(rectangle);
		}
		this.setLogicalScreenSize(rectangle.getSize());
	}
	
	
	// // http://www.nayuki.io/res/gif-optimizer-java/OptimizeGif.java
//	@Override
//	public void write(OutputStream output, IWriteOption option) throws IOException
//	{
//		if(option != null)
//		{
//			if(!(option instanceof WriteOption))
//			{
//				super.write(output, option);
//				return;
//			}
//		}
//		ImageWriter imageWriter = null;
//
//		Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(Format.GIF.name());
//		while (writers.hasNext())
//		{
//			imageWriter = writers.next();
//			if (imageWriter.getClass().getName().equals("com.sun.imageio.plugins.gif.GIFImageWriter"))
//			{
//				break;
//			}
//		}
//		try (ImageOutputStream stream = ImageIO.createImageOutputStream(output))
//		{
//			imageWriter.setOutput(stream);
//			// imageWriter.getDefaultImageMetadata(imageType, param);
//			// imageWriter.getDefaultStreamMetadata(param);
//			ImageWriteParam writeParam = imageWriter.getDefaultWriteParam();
//			// System.out.println(writeParam.);
//			if (writeParam.canWriteProgressive())
//			{
//				writeParam.setProgressiveMode(ImageWriteParam.MODE_DEFAULT);
//				//writeParam.setProgressiveMode(ImageWriteParam.MODE_COPY_FROM_METADATA);
//			}
////			GIFStreamMetadata streamMetadata = (GIFStreamMetadata) super.getStreamMetadata();
//			imageWriter.prepareWriteSequence(streamMetadata);
////			imageWriter.prepareWriteSequence(null);
//
//			int frameCount = this.getFrameCount();
//			for (int i = 0; i < frameCount; i++)
//			{
//				GifFrame frame = this.getFrame(i);
////				try
////				{
////					frame.saveAs(new File("C:/data/images/momket/data/file/tmp/5-"+i+"-1.gif"));
////				} catch (IOException e)
////				{
////					// TODO Auto-generated catch block
////					e.printStackTrace();
////				}
////				GIFImageMetadata meta = (GIFImageMetadata) frame.getIIOMetadata();
////				//GIFImageMetadata frameMetadata = new GIFImageMetadata();
//////				meta.imageLeftPosition = frame.getLeftPosition();
//////				meta.imageTopPosition = frame.getTopPosition();
//////				meta.delayTime = frame.getDelayTime();
////				frame.setDisposalMethod(DisposalMethod.DO_NOT_DISPOSE.code());
////				frame.setTransparentColorFlag(false);
////				meta.imageWidth = frame.getWidth();
////				meta.imageHeight = frame.getHeight();
////				meta.interlaceFlag = frame.getUserInputFlag();
////				meta.transparentColorFlag = frame.getTransparencyFlag();
////				meta.transparentColorIndex = frame.getTransparentColorIndex();
//				//System.out.println(meta.imageWidth + " " + meta.imageHeight+" "+ meta.imageTopPosition + " / " + meta.imageLeftPosition);
//				// frameMetadata.
////				IIOImage image = new IIOImage(frame.toBufferedImage(), null, meta);
//				IIOImage image = frame.IIOImage();
//				imageWriter.writeToSequence(image, writeParam);
//			}
//			imageWriter.endWriteSequence();
//			output.flush();
//			imageWriter.dispose();
//		}
//	}

	
	// public static BufferedImage convertRGBAToGIF(BufferedImage src, int
	// transColor)
	// {
	// BufferedImage dst = new BufferedImage(src.getWidth(), src.getHeight(),
	// BufferedImage.TYPE_BYTE_INDEXED);
	// Graphics g = dst.getGraphics();
	// g.setColor(new Color(transColor));
	// g.fillRect(0, 0, dst.getWidth(), dst.getHeight());
	// {
	// IndexColorModel indexedModel = (IndexColorModel) dst.getColorModel();
	// WritableRaster raster = dst.getRaster();
	// int sample = raster.getSample(0, 0, 0);
	// int size = indexedModel.getMapSize();
	// byte[] rr = new byte[size];
	// byte[] gg = new byte[size];
	// byte[] bb = new byte[size];
	// indexedModel.getReds(rr);
	// indexedModel.getGreens(gg);
	// indexedModel.getBlues(bb);
	// IndexColorModel newModel = new IndexColorModel(8, size, rr, gg, bb,
	// sample);
	// dst = new BufferedImage(newModel, raster, dst.isAlphaPremultiplied(),
	// null);
	// }
	// dst.createGraphics().drawImage(src, 0, 0, null);
	// return dst;
	// }

	public static class GifFrame extends Bitmap
	{
		/**
		 * 다음 Frame을 표시하기 전의 처리 옵션 주로 용량을 줄이기 위한 기법으로
		 */
		public enum DisposalMethod
		{
			
			UNKNOWN(-1),
			
			/** 0= no action */
			UNSPECIFIED(0),

			/** 1= 현재 Frame에 아무 처리를 하지 않습니다 */
			DO_NOT_DISPOSE(1),

			/** 2=restore to bg; */
			RESTORE_TO_BACKGROUND_COLOR(2),

			/**
			 * 3= 현재 Frame의 배경을 바로 전 Frame으로 처리합니다. 이전 프레임의 중복되는 픽셀 부분들은 한번
			 * 렌더링한뒤 변경없는 프레임까지 화면에 갱신없이 남겨두고 실제 프레임에는 반투명(NULL)로 기록하는 방식
			 */
			RESTORE_TO_PREVIOUS(3);
			// UNDEFINED_DISPOSAL_METHOD4,
			// UNDEFINED_DISPOSAL_METHOD5,
			// UNDEFINED_DISPOSAL_METHOD6,
			// UNDEFINED_DISPOSAL_METHOD7
			
			private int code;
			private DisposalMethod(int code)
			{
				this.code = code;
			}
			
			public int code()
			{
				return this.code;
			}
		}

		//GIFImageMetadata metadata = null;
		
		// Fields from Image Descriptor
		// <ImageDescriptor imageLeftPosition="24" imageTopPosition="0"
		// imageWidth="352" imageHeight="316" interlaceFlag="FALSE"/>
		//private int imageLeftPosition;
		//private int imageTopPosition;
		// public boolean interlaceFlag = false;
		// public boolean sortFlag = false;
		// public byte[] localColorTable = null;

		// Fields from Graphic Control Extension
		// <GraphicControlExtension disposalMethod="doNotDispose"
		// userInputFlag="FALSE" transparentColorFlag="TRUE" delayTime="5"
		// transparentColorIndex="127"/>
		//private int disposalMethod = DisposalMethod.UNKNOWN.code();
		//private boolean userInputFlag = false;
		//private int delayTime = 0;
		//private boolean transparentColorFlag = false;

		// The transparent color value in RRGGBB format.
		// The highest order byte has no effect.
		//private int transparentColorIndex = 0;

//		public static final int USER_INPUT_NONE = 0;
//		public static final int USER_INPUT_EXPECTED = 1;
//
//		public static final int TRANSPARENCY_INDEX_NONE = 0;
//		public static final int TRANSPARENCY_INDEX_SET = 1;
//
//		public static final int TRANSPARENCY_COLOR_NONE = -1;

		public GifFrame(BufferedImage bitmap)
		{
			this(bitmap, 0, 0, 0, GifFrame.DisposalMethod.UNSPECIFIED.ordinal());
		}

		public GifFrame(BufferedImage bitmap, int delay)
		{
			this(bitmap, 0, 0, delay, GifFrame.DisposalMethod.UNSPECIFIED.ordinal());
		}

		public GifFrame(BufferedImage bitmap, int delay, GifFrame.DisposalMethod disposalMethod)
		{
			this(bitmap, 0, 0, delay, disposalMethod.ordinal());
		}

		public GifFrame(BufferedImage bitmap, Point position, int delay, GifFrame.DisposalMethod disposalMethod)
		{
			this(bitmap, (int) position.getX(), (int) position.getY(), delay, disposalMethod.ordinal(), false, false, 0);
		}

		public GifFrame(BufferedImage bitmap,
			Point position,
			int delay,
			DisposalMethod disposalMethod,
			boolean userInputFlag,
			Color transparentColor)
		{
			super(bitmap);
			GIFImageMetadata metadata = new GIFImageMetadata();
			//metadata.imageWidth;
			//metadata.imageHeight;
			metadata.imageLeftPosition = position.x;
			metadata.imageTopPosition = position.y;
			metadata.delayTime = delay;
			metadata.disposalMethod = disposalMethod.ordinal();
			metadata.userInputFlag = userInputFlag;
			// Color color = new Color(1,1,1);
			// int rgb = color.getRGB();
			if (transparentColor != null)
			{
				metadata.transparentColorIndex = transparentColor.getRGB();
				metadata.transparentColorFlag = true;
			}
			this.setIIOMetadata(metadata);
		}

		// 내부적으로 쓰이는 생성자
		public GifFrame(BufferedImage bitmap, int leftPosition, int topPosition, int delay, int disposalMethod)
		{
			this(bitmap, leftPosition, topPosition, delay, disposalMethod, false, false, 0);
		}

		// 내부적으로 쓰이는 생성자
		public GifFrame(BufferedImage bitmap,
			int leftPosition,
			int topPosition,
			int delay,
			int disposalMethod,
			boolean userInputFlag,
			boolean transparencyFlag,
			int transparentColor)
		{
			super(bitmap);
			GIFImageMetadata metadata = new GIFImageMetadata();
			metadata.imageLeftPosition = leftPosition;
			metadata.imageTopPosition = topPosition;
			metadata.delayTime = delay;
			metadata.disposalMethod = disposalMethod;
			metadata.userInputFlag = userInputFlag;
			metadata.transparentColorFlag = transparencyFlag;
			// Color color = new Color(1,1,1);
			// int rgb = color.getRGB();
			metadata.transparentColorIndex = transparentColor;
			this.setIIOMetadata(metadata);
		}
		
		public GifFrame(IIOImage iioImage)
		{
			super(iioImage);
		}
		
		public GifFrame(BufferedImage bitmap,
			GIFImageMetadata metadata)
		{
			super(bitmap);
			this.setIIOMetadata(metadata);
		}

		public int getDisposalMethod()
		{
			return metadata().disposalMethod;
		}

		@Override
		public BufferedImage toBufferedImage()
		{
			return super.toBufferedImage();
		}

		@Override
		public int getHeight()
		{
			return super.getHeight();
		}

		@Override
		public int getWidth()
		{
			return super.getWidth();
		}

		public Point getPosition()
		{
			return new Point(metadata().imageLeftPosition, metadata().imageTopPosition);
		}


		public Color getTransparentColor()
		{
			return new Color(metadata().transparentColorIndex);
		}

		public boolean getTransparencyFlag()
		{
			return metadata().transparentColorFlag;
		}

		public boolean getUserInputFlag()
		{
			return metadata().userInputFlag;
		}
		
		public void setPosition(int x, int y)
		{
			metadata().imageLeftPosition = x;
			metadata().imageTopPosition = y;
		}
	
		public void setPosition(Point point)
		{
			metadata().imageLeftPosition = point.x;
			metadata().imageTopPosition = point.y;
		}

		public int getDelayTime()
		{
			return metadata().delayTime;
		}

		public void setDelayTime(int delayTime)
		{
			metadata().delayTime = delayTime;
		}

		public boolean isTransparentColorFlag()
		{
			return metadata().transparentColorFlag;
		}

		public void setTransparentColorFlag(boolean transparentColorFlag)
		{
			metadata().transparentColorFlag = transparentColorFlag;
		}

		public int getTransparentColorIndex()
		{
			return metadata().transparentColorIndex;
		}

		public void setTransparentColor(Color transparentColor)
		{
			metadata().transparentColorIndex = transparentColor.getRGB();
		}
		
		public void setTransparentColorIndex(int transparentColorIndex)
		{
			metadata().transparentColorIndex = transparentColorIndex;
		}

		public void setDisposalMethod(int disposalMethod)
		{
			metadata().disposalMethod = disposalMethod;
		}

		public void setUserInputFlag(boolean userInputFlag)
		{
			metadata().userInputFlag = userInputFlag;
		}
		
		private GIFImageMetadata metadata()
		{
			return ((GIFImageMetadata)this.getIIOMetadata());
		}
	}

	
	public static class WriteOption implements IWriteOption
	{
		private Bitmap.Format format = Bitmap.Format.GIF;
		
		private boolean ignoreMetadata;
		
		@Override
		public boolean ignoreMetadata()
		{
			return this.ignoreMetadata;
		}
		
		@Override
		public WriteOption ignoreMetadata(boolean ignoreMetadata)
		{
			this.ignoreMetadata = ignoreMetadata;
			return this;
		}

		@Override
		public ImageWriteParam build(ImageWriteParam writeParam)
		{
			if (writeParam.canWriteProgressive())
			{
				writeParam.setProgressiveMode(ImageWriteParam.MODE_DEFAULT);
			}
			return writeParam;
		}

		public Bitmap.Format format() {
			return format;
		}
	}
}
