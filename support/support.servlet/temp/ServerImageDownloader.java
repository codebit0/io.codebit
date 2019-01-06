package io.codebit.support.servlet;

import java.awt.Dimension;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;

import javax.mail.internet.MimeUtility;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.codebit.support.image.BitmapImage;
import io.codebit.support.image.Gif;
import io.codebit.support.image.Jpeg;
import io.codebit.support.image.options.IWriteOption;
import io.codebit.support.image.options.RatioOption;
import io.codebit.support.system.Async;

import io.codebit.support.image.Bitmap.Format;
import io.codebit.support.image.options.CropOption;
import io.codebit.support.io.RegularFile;

public class ServerImageDownloader extends ServerDonwloader
{
	private static Path DEFAULT_SAVE_PATH = Paths.get(System.getProperty("java.io.tmpdir")).resolve("thumbnail");
	
	static Async async = new Async(Executors.newSingleThreadExecutor());
	
	protected static class CleanVisitor extends SimpleFileVisitor<Path>
	{
	//		private LocalDateTime StartCleaningTime = LocalDateTime.now();
			
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
		{
//			if(StartCleaningTime.isAfter(LocalDateTime.now().minus(Duration.ofSeconds(100))))
//			{
//				return FileVisitResult.TERMINATE;
//			}
			Instant accessTime = attrs.lastAccessTime().toInstant();
			//마지막 Access가 10일이 지난 파일을 삭제함
			Instant day = Instant.now().minus(Duration.ofDays(10));
			if (accessTime.isBefore(day))
			{
				file.toFile().delete();
				log.debug("temp file delete {} , lastAccessTime {}", file, accessTime);
			}
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
		{
			if (exc == null)
			{
				try
				{
					Files.delete(dir);
				}catch(Exception e)
				{
					//무시
					log.error("썸네일 파일 Cleaning 삭제 오류",e);
				}
				return FileVisitResult.CONTINUE;
			}
			throw exc;
		}
	}

	public ServerImageDownloader(RegularFile file)
	{
		super(file);
	}
	
	public ServerImageDownloader(Path path)
	{
		super(path);
	}
	
	public ServerImageDownloader thumbnail(Dimension dimension) throws FileNotFoundException, IOException
	{
		return this._thumbnail(dimension, RatioOption.AspectRatio, (CropOption)null, (Format)null);
	}

	public ServerImageDownloader thumbnail(Dimension dimension, RatioOption ratioOption) throws FileNotFoundException, IOException
	{
		return this._thumbnail(dimension, ratioOption, (CropOption)null, (Format)null);
	}

	public ServerImageDownloader thumbnail(Dimension dimension, CropOption cropOption) throws FileNotFoundException, IOException
	{
		return this._thumbnail(dimension, RatioOption.AspectRatio, cropOption, (Format)null);
	}

	public ServerImageDownloader thumbnail(Dimension dimension, Format format) throws FileNotFoundException, IOException
	{
		return this._thumbnail(dimension, RatioOption.AspectRatio, (CropOption)null,format);
	}

	public ServerImageDownloader thumbnail(Dimension dimension, RatioOption ratioOption, Format format) throws FileNotFoundException, IOException
	{
		return this._thumbnail(dimension, ratioOption, (CropOption)null, format);
	}

	public ServerImageDownloader thumbnail(Dimension dimension, CropOption cropOption, Format format) throws FileNotFoundException, IOException
	{
		return this._thumbnail(dimension, RatioOption.AspectRatio, cropOption, format);
	}

	private ServerImageDownloader _thumbnail(@NonNull Dimension dimension, @NonNull RatioOption ratioOption, CropOption cropOption, Format format) throws FileNotFoundException,
		IOException
	{
		Path _fileName = orgin.getFileName();
		String fileName = _fileName.toString();
		
		//Thumbnail 저장 경로를 Web서버 Temp로 하고 주기적으로 지워주는게 어떨런지
		//해당 경로에 이미지가 있는지 확인
		String thumbFileName = (cropOption != null) ?
				String.format("%s_%s_%s-%s", dimension.width, dimension.height, cropOption, fileName) :
				String.format("%s_%s-%s", dimension.width, dimension.height, fileName);
		thumbFileName = String.format("%s_%s", dimension.width, dimension.height);
		if(cropOption != null)
		{
			thumbFileName += "_"+ cropOption;
		}
		if(!ratioOption.equals(RatioOption.AspectRatio))
		{
			thumbFileName += "_"+ ratioOption.ordinal();
		}
		if(format != null)
		{
			thumbFileName += "_"+ format.name();
		}
		thumbFileName += "-"+ fileName;
		
		Path thumDir = DEFAULT_SAVE_PATH.resolve(this.orgin.getRoot().relativize(this.orgin));
		this.process = DEFAULT_SAVE_PATH.resolveSibling(orgin.getParent()).resolve(thumbFileName);
	
		//썸네일 패스에 파일이 존재하면 썸네일 파일이존재하면 새로 생성하지 않고 종료
		if(Files.exists(this.process))
		{
			return this;
		}
		
		//구버전 호환성을 위해
		if(Files.exists(this.orgin.resolve("thumbnail").resolve(thumbFileName)))
		{
			this.process = this.orgin.resolve("thumbnail").resolve(thumbFileName);
			return this;
		}
		
		//[썸네일 폴더에 파일이 존재하지 않는 경우 새로 생성]
		Files.createDirectories(thumDir);
		
		long unallocatedSpace = Files.getFileStore(DEFAULT_SAVE_PATH).getUnallocatedSpace();
		//tmp폴더가 500MB아래로 떨어지면
		if(unallocatedSpace < 524288000)
		{
			thumbnalCleaning(1);
		}
		
		try(InputStream stream = Files.newInputStream(orgin);
			BitmapImage image = BitmapImage.fromStream(stream))
		{
			if(format == null)
				format = image.getFormat();
			IWriteOption writeOption = null;
			switch(format)
			{
				case GIF:
					writeOption = new Gif.WriteOption();
					break;
				//나머지는 모두 jpg로 변환
				case PNG:
				case BMP:
				case JPEG:
				case JPEG2000:
				case TIFF:
					writeOption = new Jpeg.WriteOption();
					writeOption.ignoreMetadata(true);
				default:
					break;
			}
			if (cropOption != null)
			{
				CropOption option = CropOption.valueOf(cropOption.name());
				//이미지를 dimension사이즈로 축소하고 crop함
				image.resize(dimension, option);
			}else
			{
				image.resize(dimension, ratioOption);
			}
			try(FileOutputStream output = new FileOutputStream(process.toFile()))
			{
				image.write(output, writeOption);
			}
			//image.saveAs(processPath.toFile(), writeOption);
		}
		thumbnalCleaning(100);
		return this;
	}

	public void display(HttpServletRequest request, HttpServletResponse response) throws FileNotFoundException, IOException
	{
		makeDisplayHeader(request, this.orgin.getFileName().toString());
		process(request, response);
	}

	public void display(HttpServletRequest request, HttpServletResponse response, String downloadFileName) throws FileNotFoundException, IOException
	{
		makeDisplayHeader(request, downloadFileName);
		process(request, response);
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
	private void makeDisplayHeader(HttpServletRequest request, String downloadFileName) throws UnsupportedEncodingException
	{
		String userAgent = request.getHeader("User-Agent");
		String downloadName = URLEncoder.encode(downloadFileName, "UTF-8").replaceAll("\\+", "\\ ");
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
					MimeUtility.encodeWord(downloadFileName, "utf-8", "Q") +	"\";filename*=UTF-8''"+downloadName+";");
		}
	}

	private void thumbnalCleaning(int randomBand) throws IOException
	{
		try
		{
			async.run(randomBand,() -> {
				try
				{
					Files.walkFileTree(DEFAULT_SAVE_PATH, new CleanVisitor());
				} catch (IOException e)
				{
					log.error("썸네일 파일 Cleaning 삭제 오류",e);
				}
			});
		}catch(Exception e)
		{
			log.error("Async run 오류", e);
		}
	}
}