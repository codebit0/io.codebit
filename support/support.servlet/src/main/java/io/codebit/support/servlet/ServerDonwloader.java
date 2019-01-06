package io.codebit.support.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import javax.mail.internet.MimeUtility;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.codebit.support.io.RegularFile;
import io.codebit.support.util.Config;

public class ServerDonwloader
{
	private Config config = Config.of("classpath://com/tmoncorp/logistics/supprot/servlet/config.properties");
	{
		String allowPath = config.getString("allowed");
		String[] allows = allowPath.split(",");
	}
	//private Path allow = Paths.get(Config.of(bundle));

	private Map<String, String> headers = new HashMap<String,String>();
	
	/** 다운로드할 원본 파일의 경로. */
	Path orgin;
	
	/** 원본 파일의 재처리 후 경로. */
	Path process;
	
	public ServerDonwloader(RegularFile file)
	{
		this.orgin = file.toPath().toAbsolutePath().normalize();
		process = this.orgin; 
	}
	
	public ServerDonwloader(Path path)
	{
		this.orgin = path.toAbsolutePath().normalize();
		process = this.orgin; 
	}
	
	public ServerDonwloader addHeader(String key, String value)
	{
		headers.put(key, value);
		return this;
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
		makeDownloadHeader(request, this.orgin.getFileName().toString());
		process(request, response);
	}

	public void download(HttpServletRequest request, HttpServletResponse response, String downloadFileName) throws FileNotFoundException,
		IOException
    {
		makeDownloadHeader(request, downloadFileName);
    	process(request, response);
    }

    /**
	 * Make download header.
	 *	http://stackoverflow.com/questions/93551/how-to-encode-the-filename-parameter-of-content-disposition-header-in-http
	 *  http://stackoverflow.com/questions/93551/how-to-encode-the-filename-parameter-of-content-disposition-header-in-http
	 * @param request the request
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 */
	private void makeDownloadHeader(HttpServletRequest request, String downloadFileName) throws UnsupportedEncodingException
	{
		String userAgent = request.getHeader("User-Agent");

		//MimeUtility.encodeWord ( processFileName );
		//URI uri = new URI(null, null, "árvíztűrőtükörfúrógép.xls", null);
		//System.out.println(uri.toASCIIString());
		// attachment; 가 붙으면 IE의 경우 무조건 다운로드창이 뜬다. 상황에 따라 써야한다.
		if (userAgent != null && userAgent.indexOf("MSIE 5.5") > -1)
		{ // MS IE 5.5 이하
			String downloadName = URLEncoder.encode(downloadFileName, "UTF-8");
			this.addHeader("Content-Disposition", "attachment; filename=\"" + downloadName + "\";");
		} else
		{ 	
			// MS IE (보통은 6.x 이상 가정)
			// filename*=UTF-8''Na%C3%AFve%20file.txt;
//			this.addHeader("Content-Disposition", "attachment; filename="
//					+ downloadName + ";filename=*=UTF-8''"+ downloadName);
			String downloadName = MimeUtility.encodeWord ( downloadFileName, "utf-8", "Q");
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
		File file = this.process.toFile();

//		DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz");
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
	}
}
