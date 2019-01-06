package io.codebit.support.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.regex.Pattern;

public abstract class FileSystem
{
	public enum ExtendCopyOption implements CopyOption 
	{
		DIRECTORY_RENAME,
		
		FILE_RENAME;
	}

	
	public static Directory ofDirectory(Path path)
	{
		return new Directory(path);
	}
	
	public static RegularFile ofFile(Path path)
	{
		return new RegularFile(path);
	}
	
	public static Directory ofDirectory(String path)
	{
		return new Directory(path);
	}
	
	public static RegularFile ofFile(String path)
	{
		return new RegularFile(path);
	}

	public static Directory ofDirectory(URI path)
	{
		return new Directory(path);
	}
	
	public static RegularFile ofFile(URI path)
	{
		return new RegularFile(path);
	}
	
	public static Directory ofDirectory(Path path, boolean ifExistCheck) throws FileNotFoundException, FileAlreadyExistsException
	{
		return new Directory(path, ifExistCheck);
	}
	
	public static RegularFile ofFile(Path path, boolean ifExistCheck) throws FileNotFoundException, FileAlreadyExistsException
	{
		return new RegularFile(path, ifExistCheck);
	}
	
	public static Directory ofDirectory(String path, boolean ifExistCheck) throws FileNotFoundException, FileAlreadyExistsException
	{
		return new Directory(path, ifExistCheck);
	}
	
	public static RegularFile ofFile(String path, boolean ifExistCheck) throws FileNotFoundException, FileAlreadyExistsException
	{
		return new RegularFile(path, ifExistCheck);
	}

	public static Directory ofDirectory(URI path, boolean ifExistCheck) throws FileNotFoundException, FileAlreadyExistsException
	{
		return new Directory(path, ifExistCheck);
	}
	
	public static RegularFile ofFile(URI path, boolean ifExistCheck) throws FileNotFoundException, FileAlreadyExistsException
	{
		return new RegularFile(path, ifExistCheck);
	}
	
    public abstract static class Entry
    {
    	Path path;
    	
    	File file;

		private LinkOption[] linkOptions;

		public LinkOption[] getLinkOptions() {
			return linkOptions;
		}

		public void setLinkOptions(LinkOption[] linkOptions) {
			this.linkOptions = linkOptions;
		}

    	public Entry(String path, LinkOption... linkOptions)
    	{
    		this(init(path), linkOptions);
    	}

    	public Entry(Path path, LinkOption... linkOptions)
    	{
    		this.path = path;
			this.linkOptions = linkOptions;
    	}
    	
    	public Entry(URI path, LinkOption... linkOptions)
    	{
    		this.path = Paths.get(path);
			this.linkOptions = linkOptions;
    	}
    	
    	/**
	     * Instantiates a new entry.
	     *
	     * @param path the path
	     * @param ifExistCheck 이 값이 true이면 파일이나 디렉토리가 존재하지 않으면 IOException을 발생
	     * 		이 값이 false이면 파일이나 디렉토리가 존재하면 IOException을 발생
	     * @param linkOptions link options 기본값
    	 * @throws FileNotFoundException  파일이 존재하지 않으면
    	 * @throws FileAlreadyExistsException 파일이 이미 존재하면
	     */
	    public Entry(Path path, boolean ifExistCheck, LinkOption... linkOptions) throws FileNotFoundException, FileAlreadyExistsException
    	{
    		this.path = path;
    		this.linkOptions = linkOptions;
    		boolean exists = Files.exists(path, linkOptions);
    		//파일이나 폴더가 없는지 체크
			if(ifExistCheck)
    		{
				//파일이나 폴더가 없으면
    			if(!exists)
    			{
    				if(this instanceof Directory)
    				{
    					throw new FileNotFoundException("디렉토리가 존재하지 않습니다.");
    				}else
    				{
    					throw new FileNotFoundException("파일이 존재하지 않습니다.");
    				}
    			}else
    			{
    				if(this instanceof Directory)
    				{
    					if(!Files.isDirectory(path, linkOptions))
    					{
    						throw new FileNotFoundException(this.path+"는 디렉토리가 아닙니다.");
    					}
    				}
    				if(this instanceof RegularFile)
    				{
    					if(!Files.isRegularFile(path, linkOptions))
    					{
    						throw new FileNotFoundException(this.path+"는 파일이 아닙니다.");
    					}
    				}
    			}
    		}else if(exists)
    		{
    			throw new FileAlreadyExistsException(path+"가 이미 존재합니다.");
    		}
    	}
    	
	    public Entry(String path, boolean ifExistCheck, LinkOption... linkOptions) throws FileNotFoundException, FileAlreadyExistsException
    	{
	    	this(init(path), ifExistCheck, linkOptions);
    	}
	    
	    public Entry(URI path, boolean ifExistCheck, LinkOption... linkOptions) throws FileNotFoundException, FileAlreadyExistsException
    	{
	    	this(Paths.get(path), ifExistCheck, linkOptions);
    	}
	    
    	private static Path init(String filePath)
    	{
    		Path path = null;
    		if(Pattern.matches("^\\w+:.*", filePath))
    		{
    			try
    			{
    				path = Paths.get(URI.create(filePath));
    			}catch(Exception e)
    			{
    				path = Paths.get(filePath);
    			}
    		}else
    		{
    			path = Paths.get(filePath);
    		}
    		return path;
    	}
    	
    	public Path toPath()
    	{
    		return this.path;
    	}
    	
    	public Directory getRoot()
    	{
    		Path root = this.path.getRoot();
    		if(root == null)
    			return null;
			return new Directory(root);
    	}
    	
    	public Path getName()
    	{
    		return this.path.getFileName();
    	}
    	
    	public Directory getDirectory()
    	{
    		return new Directory(path.getParent());
    	}
    	
    	public RegularFile createLink(Path linkPath) throws IOException
    	{
    		Path link = Files.createLink(linkPath, this.path);
    		return new RegularFile(link);
    	}
    	
    	public WatchKey register(WatchService watcher, Kind<?>[] events, Modifier... modifiers) throws IOException
    	{
    		return this.path.register(watcher, events, modifiers);
    	}

    	public WatchKey register(WatchService watcher, Kind<?>... events) throws IOException
    	{
    		return this.path.register(watcher, events);
    	}
    	
//    	public void copy(Path path) throws IOException
//    	{
//    		Files.copy(path(), path);
//    	}
//    
//    	public void copy(Path path, CopyOption... option) throws IOException
//    	{
//    		Files.copy(path(), path, option);
//    	}
//    	
//    	public void move(Path path) throws IOException
//    	{
//    		Files.move(path(), path);
//    	}
//    	
//    
//    	public void move(Path path, CopyOption... option) throws IOException
//    	{
//    		Files.move(path(), path, option);
//    	}
//    	
//    	public void delete() throws IOException
//		{
//			Files.delete(path());
//		}

		public boolean reName(String name)
    	{
    		return path.toFile().renameTo(path.resolveSibling(name).toFile());
    	}
    	
    	public void setAttribute(FileAttribute<?>... attrs) throws IOException
    	{
    		for (FileAttribute<?> fileAttribute : attrs)
    		{
    			Files.setAttribute(this.path, fileAttribute.name(), fileAttribute.value(), linkOptions);
    		}
    	}
    
    	/**
    	 * Sets the attribute.
    	 * 
    	 * WIndows Default file system supports: acl Default file system supports:
    	 * basic Default file system supports: owner Default file system supports:
    	 * user Default file system supports: dos
    	 * 
    	 * Linux Default file system supports: basic Default file system supports:
    	 * owner Default file system supports: user Default file system supports:
    	 * unix Default file system supports: dos Default file system supports:
    	 * posix
    	 * 
    	 * Zip archive ZIP file system supports: zip ZIP file system supports: basic
    	 *
    	 * http://jakubstas.com/file-attributes-nio-2/#.VZNMX_ntlBc
    	 * 
    	 * @param attribute
    	 *            the attribute
    	 * @param value
    	 *            the value
    	 * @throws IOException
    	 *             Signals that an I/O exception has occurred.
    	 */
    	public void setAttribute(String attribute, Object value) throws IOException
    	{
    		Files.setAttribute(this.path, attribute, value, linkOptions);
    	}
    
//    	public void setAttribute(String attribute, Object value, LinkOption... options) throws IOException
//    	{
//    		Files.setAttribute(this.path, attribute, value, options);
//    	}
    	
    	public Object getAttribute(String attribute) throws IOException
    	{
    		return Files.getAttribute(this.path, attribute, linkOptions);
    	}
    	
    	public String getUserAttribute(String attribute) throws IOException
    	{
    		UserDefinedFileAttributeView view = getUserDefinedFileAttributeView();
    		String name = attribute;
    		int size = view.size(name);
			ByteBuffer buf = ByteBuffer.allocate(size);
			view.read(name, buf);
    		buf.flip();
    		String value = StandardCharsets.UTF_8.decode(buf).toString();
    		return value;
    	}
    	
//    	public Map<String, Object> getUserAttributes() throws IOException
//    	{
//    		Map<String, Object> map = new HashMap<String, Object>();
//    		UserDefinedFileAttributeView view = getUserDefinedFileAttributeView();
//    		List<String> list = view.list();
//    		for (String name : list)
//			{
//    			ByteBuffer buf = ByteBuffer.allocate(view.size(name));
//    			view.read(name, buf);
//    			buf.flip();
//    			String value = StandardCharsets.UTF_8.decode(buf).toString();
//    			map.put(name, value);
//			}
//    		return Collections.unmodifiableMap(map);
//    	}
    	
//    	public Object getAttribute(String attribute, LinkOption... options) throws IOException
//    	{
//    		return Files.getAttribute(this.path, attribute, options);
//    	}
    	
    	public FileTime getLastModifiedTime()  throws IOException
	    {
	        return getBasicFileAttributes().lastModifiedTime();
	    }
    	
//    	public FileTime getLastModifiedTime(LinkOption... options)  throws IOException
//	    {
//	        return Files.readAttributes(this.path, BasicFileAttributes.class, options).lastModifiedTime();
//	    }
    	
    	public FileTime getCreationTime()  throws IOException
	    {
    		BasicFileAttributes attr = getBasicFileAttributes();
    		FileTime creationTime = attr.creationTime();
    		return creationTime;
	    }
    	
//    	public FileTime getCreationTime(LinkOption... options)  throws IOException
//	    {
//    		BasicFileAttributes attr = Files.readAttributes(this.path, BasicFileAttributes.class);
//    		FileTime creationTime = attr.creationTime();
//    		return creationTime;
//	    }
    	
    	public FileTime getLastAccessTime()  throws IOException
	    {
	        return getBasicFileAttributes().lastAccessTime();
	    }
    	
//    	public FileTime getLastAccessTime(LinkOption... options)  throws IOException
//	    {
//	        return Files.readAttributes(this.path, BasicFileAttributes.class, options).lastAccessTime();
//	    }
    	
    	public boolean isSymbolicLink()
		{
			return Files.isSymbolicLink(toPath());
		}

		public boolean isHidden()  throws IOException
	    {
    		return Files.readAttributes(this.path, DosFileAttributes.class, this.linkOptions).isHidden();
	    }
    	
//    	public boolean isHidden(LinkOption... options)  throws IOException
//	    {
//    		return Files.readAttributes(this.path, DosFileAttributes.class, options).isHidden();
//	    }
    	
    	public Object fileKey()  throws IOException
	    {
    		return Files.readAttributes(this.path, DosFileAttributes.class, linkOptions).fileKey();
	    }
    	
    	@Override
		public String toString()
    	{
    		return this.path.toString();
    	}
    	
    	
    	//[Cache code]----------------------------------------------------------------------
    	private BasicFileAttributes basicFileAttributes;
    	private BasicFileAttributes getBasicFileAttributes() throws IOException
    	{
    		if(basicFileAttributes == null)
    		{
    			basicFileAttributes = Files.readAttributes(this.path, BasicFileAttributes.class, linkOptions);
    		}
    		return basicFileAttributes;
    	}
    	
    	private UserDefinedFileAttributeView  userDefinedFileAttributeView ;
    	private UserDefinedFileAttributeView  getUserDefinedFileAttributeView () throws IOException
    	{
    		if(userDefinedFileAttributeView == null)
    		{
    			userDefinedFileAttributeView = Files.getFileAttributeView(this.path, UserDefinedFileAttributeView.class, linkOptions);
    		}
    		return userDefinedFileAttributeView;
    	}
    }
}
