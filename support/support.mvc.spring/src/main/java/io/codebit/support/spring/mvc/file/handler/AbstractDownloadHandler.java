package io.codebit.support.spring.mvc.file.handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.FileSystemException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
//import javax.mail.internet.MimeUtility;


import javax.mail.internet.MimeUtility;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.codebit.support.lang.extensions.StringExtension;
import io.codebit.support.spring.mvc.Config;
import io.codebit.support.spring.mvc.Messages;

@SuppressWarnings("rawtypes")
//@ExtensionMethod({ StringExtension.class })
public abstract class AbstractDownloadHandler<T extends AbstractDownloadHandler>
{
	private final static int MIN_FILENAME_LENGTH = Integer.parseInt(Config.get("min_filename_length"));
	
	// 20M
	protected long MAX_DOWNLOAD_FILE_SIZE = Long.parseLong(Config.get("file_max_size"));
	
	protected final Path DEFAULT_SAVE_PATH;

	protected Path subPath;

	protected String fileName;

	private File processFile;
	
	private Map<String, String> headers = new HashMap<String,String>();

//	protected AbstractDownloadHandler(String rootPath, String subPath, String fileName)
//	{
//		this.DEFAULT_SAVE_PATH = Paths.get(rootPath).toAbsolutePath();
//		this.subPath = Paths.get(subPath);
//		this.fileName = fileName;
//		Path filePath = DEFAULT_SAVE_PATH.resolve(subPath).resolve(fileName);
//		this.setProcessFile(filePath.toFile());
//	}

	protected AbstractDownloadHandler(Path rootPath, Path subPath, String fileName)
	{
		this.DEFAULT_SAVE_PATH = rootPath.toAbsolutePath();
		this.subPath = subPath;
		this.fileName = fileName;
		Path filePath = DEFAULT_SAVE_PATH.resolve(subPath).resolve(fileName);
		this.setProcessFile(filePath.toFile());
	}
	
	/**
	 * 다운로드 처리할 파일의 정보를 반환
	 *
	 * @return the process file
	 */
	protected File getProcessFile()
	{
		return processFile;
	}

	/**
	 * 다운로드 처리할 파일을 설정함
	 *
	 * @param file
	 *            the new process file
	 */
	protected void setProcessFile(File file)
	{
		processFile = file;
	}

	/**
	 * 파일이 다운로드 가능한 파일인지 여부를 체크
	 *
	 * @param file
	 *            the file
	 * @return true, if is allowed file
	 */
	protected boolean isAllowed(File file)
	{
		if (file == null || !file.exists() || file.length() <= 0
				|| file.length() > MAX_DOWNLOAD_FILE_SIZE
				|| file.isDirectory() || file.getName().indexOf('=') < 0 
				|| getFileName().length() < MIN_FILENAME_LENGTH )
		{
			// throw new
			// IOException("파일 객체가 Null 혹은 존재하지 않거나 길이가 0, 혹은 파일이 아닌 디렉토리이다.");
			return false;
		}
		return true;
	}

	protected String getFileName()
	{
		String processFileName = getProcessFile().getName();
		return processFileName.substring(processFileName.indexOf('=') + 1);
	}

	@SuppressWarnings("unchecked")
	public T addHeader(String key, String value)
	{
		headers.put(key, value);
		return (T) this;
	}

	/**
	 * 파일 다운로드.
	 * @param request the request
	 * @param response the response
	 * @throws FileNotFoundException             the file not found exception
	 * @throws IOException             Signals that an I/O exception has occurred.
	 */
	public void download(HttpServletRequest request, HttpServletResponse response) throws FileNotFoundException,
			IOException
	{
		makeDownloadHeader(request);
		process(request, response);
	}

	/**
	 * Make download header.
	 *	http://stackoverflow.com/questions/93551/how-to-encode-the-filename-parameter-of-content-disposition-header-in-http
	 *  http://stackoverflow.com/questions/93551/how-to-encode-the-filename-parameter-of-content-disposition-header-in-http
	 * @param request the request
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 */
	private void makeDownloadHeader(HttpServletRequest request) throws UnsupportedEncodingException
	{
		String userAgent = request.getHeader("User-Agent");
		String processFileName = getFileName();

		//MimeUtility.encodeWord ( processFileName );
		//URI uri = new URI(null, null, "árvíztűrőtükörfúrógép.xls", null);
		//System.out.println(uri.toASCIIString());
		// attachment; 가 붙으면 IE의 경우 무조건 다운로드창이 뜬다. 상황에 따라 써야한다.
		if (userAgent != null && userAgent.indexOf("MSIE 5.5") > -1)
		{ // MS IE 5.5 이하
			String downloadName = URLEncoder.encode(processFileName, "UTF-8");
			this.addHeader("Content-Disposition", "attachment; filename=\"" + downloadName + "\";");
		} else
		{ 	
			// MS IE (보통은 6.x 이상 가정)
			// filename*=UTF-8''Na%C3%AFve%20file.txt;
//			this.addHeader("Content-Disposition", "attachment; filename="
//					+ downloadName + ";filename=*=UTF-8''"+ downloadName);
			String downloadName = MimeUtility.encodeWord ( processFileName, "utf-8", "Q");
			this.addHeader("Content-Disposition", "attachment; filename=\""+ downloadName + "\";");
		}
		this.addHeader("Content-Transfer-Encoding", "binary");
	}

	/**
	 * 파일 출력 프로세스.
	 *
	 * @param request the request
	 * @param response the response
	 * @throws FileNotFoundException             the file not found exception
	 * @throws IOException             Signals that an I/O exception has occurred.
	 */
	protected void process(HttpServletRequest request, HttpServletResponse response) throws FileNotFoundException,
			IOException
	{
		File file = getProcessFile();

		Path savePath = DEFAULT_SAVE_PATH.resolve(subPath).toAbsolutePath();
		if (DEFAULT_SAVE_PATH.getRoot() == null
				|| StringExtension.isNullOrEmpty(this.subPath.toString())
				|| this.subPath.getRoot() != null
				|| !savePath.normalize().toString().equals(savePath.toString())
				|| !file.toPath().toAbsolutePath().startsWith(savePath))
		{
			throw new FileSystemException(Messages.get("error_invalid_path")); //$NON-NLS-1$
		}

		// "Last-Modified", dateFormat.format(new Date())
		// long lastModified = file.lastModified();
		// mime type를 얻어옴
		ServletContext context = request.getServletContext();
		String mimeType = context.getMimeType(file.toString());
		if (mimeType == null)
		{
			mimeType = "application/octet-stream";
		}
		response.setCharacterEncoding("UTF-8");
		//setContentType("applicaiton/download;charset=utf-8");
		response.setContentType(mimeType);
		if (!isAllowed(file))
		{
			throw new FileSystemException(Messages.get("error_denied_file"));
		}
		long fileSize = file.length();
		response.setHeader("Content-Length", Long.toString(fileSize));
		
		for(String key :this.headers.keySet())
		{
			response.addHeader(key, this.headers.get(key));
		}
		
		try (FileInputStream fileStream = new FileInputStream(file);
				OutputStream outStream = response.getOutputStream())
		{
			byte[] buffer = new byte[1024];
			while(fileStream.read(buffer) != -1)
			{
				outStream.write(buffer);
			}
			outStream.flush();
		} catch (Exception e)
		{
			throw e;
		}
		
//		try (FileInputStream fileStream = new FileInputStream(file);
//				FileChannel fileChannel = fileStream.getChannel();
//				OutputStream outStream = response.getOutputStream();
//				WritableByteChannel outChannel = Channels.newChannel(outStream);)
//		{
//			ByteBuffer buf = ByteBuffer.allocate((int) fileChannel.size());
//
//			while (fileChannel.read(buf) != -1)
//			{
//				buf.flip();
//				outChannel.write(buf);
//				buf.clear();
//			}
//			outStream.flush();
//		} catch (Exception e)
//		{
//			throw e;
//		}
	}
}
