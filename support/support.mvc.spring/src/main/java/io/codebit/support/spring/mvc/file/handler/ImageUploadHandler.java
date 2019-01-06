package io.codebit.support.spring.mvc.file.handler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import io.codebit.support.spring.mvc.Config;
import io.codebit.support.spring.mvc.Messages;
import org.springframework.web.multipart.MultipartFile;

import io.codebit.support.image.Bitmap.Format;
import io.codebit.support.image.BitmapImage;
import io.codebit.support.image.Jpeg;
import io.codebit.support.image.options.IWriteOption;
import io.codebit.support.image.options.RotateFlipType;

public class ImageUploadHandler extends AbstractUploadHandler<StoredImageInfo>
{
	private float quality = Float.parseFloat(Config.get("image_compress_quality"));
	private RotateFlipType rotate = null;

	public ImageUploadHandler(String subPath)
	{
		this(Paths.get(subPath));
	}

	public ImageUploadHandler(Path subPath)
	{
		super(Paths.get(Config.get("image_root_path")), subPath);
		this.MAX_UPLOAD_SIZE = Long.parseLong(Config.get("image_max_size"));
	}
	
	public ImageUploadHandler quality(float quality)
	{
		this.quality = quality;
		return this;
	}
	
	public ImageUploadHandler rotate(RotateFlipType rotate)
	{
		this.rotate = rotate;
		return this;
	}

	@Override
	protected UploadFileInfo getUploadFileInfo(MultipartFile file) throws IOException
	{
		UploadFileInfo uploadInfo = super.getUploadFileInfo(file);
		String extension = uploadInfo.getExtension();
		if(extension == null || extension.isEmpty())
		{
			try (InputStream in = file.getInputStream();
					ImageInputStream iis = ImageIO.createImageInputStream(in))
			{
				Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
				iis.flush();
				String contentType = "";
				if (readers.hasNext())
				{
					ImageReader reader = readers.next();
					contentType = reader.getFormatName().toLowerCase();
					switch (contentType)
					{
						case "jpg":
						case "jpeg":
							uploadInfo.setContentType("image/jpeg");
							break;
						case "png":
							uploadInfo.setContentType("image/png");
							break;
						case "gif":
							uploadInfo.setContentType("image/gif");
							break;
						case "bmp":
						case "wbmp":
							uploadInfo.setContentType("image/bmp");
							break;
					}
					reader.dispose();
					return uploadInfo;
				}
				throw new IOException(Messages.get("err_invalid_image_format"));
			}
		}else
		{
			switch (extension)
			{
				case "jpg":
				case "jpeg":
					uploadInfo.setContentType("image/jpeg");
					break;
				case "png":
					uploadInfo.setContentType("image/png");
					break;
				case "gif":
					uploadInfo.setContentType("image/gif");
					break;
				case "bmp":
				case "wbmp":
					uploadInfo.setContentType("image/bmp");
					break;
			}
		}
		
		return uploadInfo;
	}

	@Override
	protected boolean isAllowed(UploadFileInfo uploadFileInfo)
	{
		if (uploadFileInfo == null
				|| !uploadFileInfo.getContentType().toLowerCase().startsWith("image/"))
			return false;
		return true;
	}

	@Override
	protected StoredImageInfo saveProcessing(UploadFileInfo uploadFileInfo, File destination)
			throws IllegalStateException, IOException
	{
		try (InputStream isSource = uploadFileInfo.getMultipartFile().getInputStream())
		{
			//String contentType = uploadFileInfo.getContentType();
			//String format = contentType.substring(contentType.indexOf('/') + 1);
//			BufferedImage bufSource = ImageIO.read(isSource);
//			int width = bufSource.getWidth();
//			int height = bufSource.getHeight();
			try(BitmapImage bitmap = BitmapImage.fromStream(isSource))
			{
				IWriteOption writeOption = null;
				if(bitmap.getFormat().equals(Format.JPEG))
				{
					if(this.rotate == null)
					{
						((Jpeg) bitmap).rotate();
					}else 
					{
						((Jpeg) bitmap).rotate(this.rotate);
					}
					if (this.quality < 1.0f
						&& uploadFileInfo.getSize() > Long.parseLong(Config.get("image_compress_size")))
					{
						writeOption = new Jpeg.WriteOption().setQuality(this.quality);
					}
				}
				int width = bitmap.getWidth();
				int height = bitmap.getHeight();
				if(bitmap.getFormat().equals(Format.JPEG))
				{
					bitmap.saveAs(destination, writeOption);
				}else
				{
					uploadFileInfo.getMultipartFile().transferTo(destination);
				}
				long newSize = destination.length();
				return new StoredImageInfo(this.getSubPath(), destination.getName(), 
					this.getSubPath().resolve(destination.getName()), destination.toPath(), 
					newSize, width, height, uploadFileInfo);
			}
			
			/*//기본값 2M가 넘어가면 퀄리티 축소
			if (this.quality < 1.0f
					&& format.equals("jpeg") 
					&& uploadFileInfo.getSize() > Long.parseLong(Config.get("image_compress_size")))
			{
				// format을 구함
				Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(format);

				if (!writers.hasNext())
					throw new IllegalStateException("No writers found");

				ImageWriter writer = writers.next();
				ImageWriteParam writeParam = writer.getDefaultWriteParam();
				writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
				writeParam.setCompressionQuality(quality);

				try (FileImageOutputStream output = new FileImageOutputStream(destination))
				{
					writer.setOutput(output);
					
					IIOImage image = new IIOImage(bufSource, null, null);
					writer.write(null, image, writeParam);
					output.flush();
					writer.dispose();
					long newSize = output.getFlushedPosition();
					return new StoredImageInfo(this.getSubPath(), destination.getName(), 
						this.getSubPath().resolve(destination.getName()), destination.toPath(), 
						newSize, width, height, uploadFileInfo);
				}catch (Exception e) 
				{
					throw e;
				}
			} else
			{
				StoredFileInfo info = super.saveProcessing(uploadFileInfo, destination);
				return new StoredImageInfo(this.getSubPath(), destination.getName(), 
					this.getSubPath().resolve(destination.getName()), destination.toPath(), 
					info.getSize(), width, height, uploadFileInfo);
			}*/
		} catch (Exception e)
		{
			throw e;
		}
	}
}
