package io.codebit.support.spring.mvc.file.handler;

import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.FileSystemException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.mail.internet.MimeUtility;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.codebit.support.image.BitmapImage;
import io.codebit.support.lang.extensions.StringExtension;
import io.codebit.support.nio.extensions.PathExtension;
import io.codebit.support.spring.mvc.Config;
import io.codebit.support.spring.mvc.Messages;

import io.codebit.support.image.options.CropOption;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.Thumbnails.Builder;
import net.coobird.thumbnailator.geometry.Positions;

//@ExtensionMethod({PathExtension.class, StringExtension.class})
public class ImageDownloadHandler extends AbstractDownloadHandler<ImageDownloadHandler>
{

	public enum Crop
	{
		TOP_LEFT, TOP_CENTER, TOP_RIGHT, CENTER_LEFT, CENTER, CENTER_RIGHT, BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT;
	}

	public ImageDownloadHandler(String subPath, String fileName)
	{
		this(Paths.get(subPath), fileName);
	}

	public ImageDownloadHandler(Path subPath, String fileName)
	{
		super(Paths.get(Config.get("image_root_path")), subPath, fileName);
		this.MAX_DOWNLOAD_FILE_SIZE = Long.parseLong(Config.get("image_max_size"));
	}
	
	public ImageDownloadHandler thumbnail(Integer width, Integer height) throws FileNotFoundException, IOException
	{
		this.thumbnail(width, height, null);
		return this;
	}

	public ImageDownloadHandler thumbnail(Integer width, Integer height, Crop crop) throws FileNotFoundException, IOException
	{
		Path orgin = DEFAULT_SAVE_PATH.resolve(subPath).resolve(fileName);
		File file = orgin.toFile();
		if(!file.exists())
		{
			throw new FileSystemException(Messages.get("error_file_not_found")); //$NON-NLS-1$
		}
		//이미지의 포멧을 구함
		String thumbFormat = "jpg";
		try (FileInputStream in = new FileInputStream(file);
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
					case "bmp":
					case "wbmp":
						//thumbFormat = "jpg";
					break;
					case "png":
						thumbFormat = "png";
					break;
					case "gif":
						thumbFormat = "gif";
					break;
					default:
						throw new IOException(Messages.get("err_invalid_image_format"));
				}
				reader.dispose();
			}
		}
		String extension = PathExtension.getExtension(Paths.get(fileName));
		if(StringExtension.isNullOrEmpty(extension))
		{
			fileName += "."+ thumbFormat;
		}else if(!extension.equalsIgnoreCase(thumbFormat))
		{
			//썸네일 확장자를 교체함
			fileName = fileName.substring(0, fileName.length() - (extension.length()))+ thumbFormat;
		}
		
		String thumbFileName = (crop != null) ?
				String.format("%s_%s_%s-%s", width, height, crop, fileName) :
				String.format("%s_%s-%s", width, height, fileName);
		Path thumbPath = DEFAULT_SAVE_PATH.resolve(subPath).resolve("thumbnail").resolve(thumbFileName);

		if (!thumbPath.toFile().exists())
		{
			File thumbSaveDir = thumbPath.getParent().toFile();
			if (!thumbSaveDir.exists())
			{
				thumbSaveDir.mkdirs();
			}

			if(thumbFormat.equals("gif"))
			{
				try(BitmapImage image = BitmapImage.fromFile(orgin.toFile()))
				{
					if(width == null)
					{
						width = image.getWidth();
					}
					if(height == null)
					{
						height = image.getHeight();
					}
					if (crop != null)
					{
						CropOption option = CropOption.valueOf(crop.name());
						image.resize(new Dimension(width, height), option);
					}else
					{
						image.resize(new Dimension(width, height));
					}
					image.saveAs(thumbPath.toFile());
				}
			}else
			{
				Builder<File> builder = Thumbnails.of(orgin.toString());
				if(width != null && width > 0)
				{
					builder.width(width);
				}
				if(height != null && height > 0)
				{
					builder.height(height);
				}
				if (crop != null)
				{
					Positions positions = Positions.valueOf(crop.name());
					builder.crop(positions);
				}
				builder.outputFormat(thumbFormat);
				builder.toFile(thumbPath.toFile());
			}
		}
		setProcessFile(thumbPath.toFile());
		return this;
	}

	/**
	 * Make download header.
	 *	http://stackoverflow.com/questions/93551/how-to-encode-the-filename-parameter-of-content-disposition-header-in-http
	 *  http://stackoverflow.com/questions/93551/how-to-encode-the-filename-parameter-of-content-disposition-header-in-http
	 *  http://greenbytes.de/tech/tc2231/#attwithfilenameandextparam
	 *  http://loonyhyun.tistory.com/entry/java-file-download-%ED%95%9C%EA%B8%80-%EA%B9%A8%EC%A7%90-%ED%98%84%EC%83%81-%ED%95%B4%EA%B2%B0
	 * @param request the request
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 */
	private void makeDownloadHeader(HttpServletRequest request) throws UnsupportedEncodingException
	{
		String userAgent = request.getHeader("User-Agent");
		String processFileName = getFileName();
		String downloadName = URLEncoder.encode(processFileName, "UTF-8").replaceAll("\\+", "\\ ");
		//MimeUtility.encodeWord ( processFileName );
		//URI uri = new URI(null, null, "árvíztűrőtükörfúrógép.xls", null);
		// attachment; 가 붙으면 IE의 경우 무조건 다운로드창이 뜬다. 상황에 따라 써야한다.
		if (userAgent != null && userAgent.indexOf("MSIE 5.5") > -1)
		{ // MS IE 5.5 이하
			this.addHeader("Content-Disposition", "inline; filename=\"" + downloadName + "\";");
		} else
		{ 	
			// MS IE (보통은 6.x 이상 가정)
			// filename*=UTF-8''Na%C3%AFve%20file.txt;
			//downloadName = MimeUtility.encodeWord ( processFileName, "utf-8", "Q");
			this.addHeader("Content-Disposition", "inline; filename=\""+ 
					MimeUtility.encodeWord ( processFileName, "utf-8", "Q") + 
					"\";filename*=UTF-8''"+downloadName+";");
		}
	}
	
	public void display(HttpServletRequest request, HttpServletResponse response) throws FileNotFoundException,
			IOException
	{
		makeDownloadHeader(request);
		process(request, response);
	}
}
