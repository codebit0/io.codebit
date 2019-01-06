package io.codebit.support.spring.mvc.file.handler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import io.codebit.support.lang.extensions.StringExtension;
import io.codebit.support.nio.extensions.PathExtension;
import io.codebit.support.spring.mvc.Config;
import io.codebit.support.spring.mvc.Messages;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

//@ExtensionMethod({StringExtension.class, PathExtension.class })
public abstract class AbstractUploadHandler<T extends StoredFileInfo>
{
	private static final Pattern DENIED_FILENAME_REGEX = Pattern.compile(Config.get("denied_filename_regex")); //$NON-NLS-1$

	private static Pattern REMOVE_CHAR_FILENAME_REGEX = Pattern.compile(Config.get("remove_char_filename_regex")); //$NON-NLS-1$

	private final static int MAX_FILENAME_LENGTH = Integer.parseInt(Config.get("max_filename_length"));
	private final static int MIN_FILENAME_LENGTH = Integer.parseInt(Config.get("min_filename_length"));
	
	private final Path ROOT_SAVE_PATH; //$NON-NLS-1$
	
	protected long MAX_UPLOAD_SIZE = Long.parseLong(Config.get("file_max_size"));

	private final Path subPath;

	private String prefix;

	protected AbstractUploadHandler(Path rootPath, Path subPath)
	{
		this.ROOT_SAVE_PATH = rootPath.toAbsolutePath();
		this.subPath = subPath;
	}

	public Path getSubPath() {
		return subPath;
	}

//	/**
//	 * 하위폴더 지정 subPath 경로에 ../ ./같은 상대경로 또는 root (/) 경로 또는 Empty문자를 지정하는 경우 예외
//	 * 처리
//	 * 
//	 * @param subPath
//	 *            하위 경로
//	 */
//	protected void setSubPath(String subPath)
//	{
//		this.subPath = Paths.get(subPath);
//	}
	
	/**
	 * Sets the prefix file name.
	 * 값을 설정하면 파일명을 내부적으로 난수처리를 하지 않음
	 *
	 * @param prefix the new prefix file name
	 */
	public void setPrefixFileName(String prefix)
	{
		this.prefix = prefix;
	}
	
	protected void setFileNameRemoveCharPattern(String pattern)
	{
		REMOVE_CHAR_FILENAME_REGEX = Pattern.compile(pattern);
	}
	
	protected UploadFileInfo getUploadFileInfo(MultipartFile file) throws IOException
	{
		try
		{
			//String fileName = file.getOriginalFilename();
			String _fileName = URLDecoder.decode(file.getOriginalFilename(), "UTF-8"); //$NON-NLS-1$
			String extension = PathExtension.getExtension(Paths.get(_fileName));
			return new UploadFileInfo(file, _fileName, extension, file.getSize(), file.getContentType());
		} catch (UnsupportedEncodingException e)
		{
		}
		return null;
	}
	
	protected boolean isAllowed(UploadFileInfo uploadFileInfo)
	{
		return true;
	}

	public T saveAs(MultipartFile file) throws IllegalStateException, IOException
	{
		Path saveFilePath = null;
		
		UploadFileInfo uploadFile = this.getUploadFileInfo(file);
		String saveFileName = uploadFile.getName();
		if(MAX_UPLOAD_SIZE < file.getSize())
		{
			throw new FileSystemException(Messages.get("error_over_file_size")); //$NON-NLS-1$
		}
		// 파일 escape문자 검사 Windows기준
		// < (less than)
		// > (greater than)
		// : (colon)
		// " (double quote)
		// / (forward slash)
		// \ (backslash)
		// | (vertical bar or pipe)
		// ? (question mark)
		// * (asterisk)
		Matcher mat = DENIED_FILENAME_REGEX.matcher(uploadFile.getName());
		if (StringExtension.isNullOrEmpty(uploadFile.getName()) 
			|| mat.find())
		{
			throw new FileSystemException(Messages.get("error_invalid_file_name")); //$NON-NLS-1$
		}
		// root경로가 명확하지 않거나
		// subPath 경로에 ../ ./같은 상대경로 또는 root (/) 경로를 지정하는 경우 예외 처리
		Path savePath = ROOT_SAVE_PATH.resolve(subPath).toAbsolutePath();
		if (ROOT_SAVE_PATH.getRoot() == null
				|| StringExtension.isNullOrEmpty(this.subPath.toString())
				|| this.subPath.getRoot() != null
				|| !savePath.normalize().toString().equals(savePath.toString())
				|| !savePath.startsWith(ROOT_SAVE_PATH))
		{
			throw new FileSystemException(Messages.get("error_invalid_path")); //$NON-NLS-1$
		}

		// 파일명 길이 체크
		if (uploadFile.getName().length() > MAX_FILENAME_LENGTH)
		{
			saveFileName = uploadFile.getName().substring(0, MAX_FILENAME_LENGTH);
		}
		if (saveFileName.length() < MIN_FILENAME_LENGTH)
		{
			throw new FileSystemException(String.format(Messages.get("error_less_file_name_length")
				, MIN_FILENAME_LENGTH)); //$NON-NLS-1$
		}
		// 파일 확장자 화이트리스트 같은 체크
		if (!this.isAllowed(uploadFile))
		{
			throw new FileSystemException(Messages.get("error_denied_file")); //$NON-NLS-1$
		}

		if (REMOVE_CHAR_FILENAME_REGEX.pattern().length() > 0)
		{
			mat = REMOVE_CHAR_FILENAME_REGEX.matcher(saveFileName);
			saveFileName = mat.replaceAll(""); //$NON-NLS-1$
		}

		saveFilePath = ROOT_SAVE_PATH.resolve(subPath);
		if(StringExtension.isNullOrEmpty(this.prefix))
		{
    		long timeMillis = System.currentTimeMillis();
    		// 파일명 변경
    		saveFilePath = saveFilePath.resolve(String.format("%s=%s", timeMillis, saveFileName));
		}else
		{
			saveFilePath = saveFilePath.resolve(String.format("%s=%s", this.prefix, saveFileName));
		}
		if(Files.notExists(savePath))
		{
			File saveDir = savePath.toFile();
			saveDir.setExecutable(false, false);
			saveDir.setReadable(true, true);
			saveDir.setWritable(true, true);
			saveDir.mkdirs();
		}
		File saveFile = saveFilePath.toFile();
		saveFile.setWritable(true, true);
		saveFile.setExecutable(false, false);
		saveFile.setReadable(true, true);
		
		return saveProcessing(uploadFile, saveFile);
	}
	
	public List<T> saveAs(Collection<MultipartFile> files) throws IllegalStateException, IOException
	{
		List<T> paths = new ArrayList<T>(files.size());
		for (MultipartFile file : files)
		{
			paths.add(this.saveAs(file));
		}
		return paths;
	}

	@Deprecated
	public T saveAs(URL url) throws IllegalStateException, IOException
	{
		URLConnection connection = url.openConnection();
		//connection.gu
//		connection.getInputStream();
//		connection.getContentLengthLong();
//		connection.getContentType();
//		HttpURLConnection httpConnection = (HttpURLConnection)connection;
		HttpURLConnection.guessContentTypeFromStream(connection.getInputStream());
		
		try(InputStream in = url.openStream())
		{
			MultipartFile file = new MultipartFile()
			{
				@Override
				public String getName()
				{
					// TODO Auto-generated method stub
					return "";
				}

				@Override
				public String getOriginalFilename()
				{
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public String getContentType()
				{
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public boolean isEmpty()
				{
					// TODO Auto-generated method stub
					return false;
				}

				@Override
				public long getSize()
				{
					return 0;
				}

				@Override
				public byte[] getBytes() throws IOException
				{
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public InputStream getInputStream() throws IOException
				{
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public void transferTo(File dest) throws IOException, IllegalStateException
				{
				}
			};
			return this.saveAs(file);
		}
	}
	
	public List<T> saveAs(HttpServletRequest request, String field) throws IllegalStateException, IOException
	{
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		return this.saveAs(multipartRequest.getFiles(field));
	}

	public List<T> saveAs(HttpServletRequest request, String... fields) throws IllegalStateException, IOException
	{
		List<T> paths = new ArrayList<T>();
		for (String field : fields)
		{
			paths.addAll(this.saveAs(request, field));
		}
		return paths;
	}
	
	@SuppressWarnings("unchecked")
	protected T saveProcessing(UploadFileInfo uploadFileInfo, File destination) throws IllegalStateException, IOException
	{
		uploadFileInfo.getMultipartFile().transferTo(destination);
		return (T) new StoredFileInfo(this.subPath, destination.getName(), this.subPath.resolve(destination.getName()), 
			destination.toPath(), uploadFileInfo.getSize(), uploadFileInfo);
	}
}
