package io.codebit.support.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLConnection;
import java.nio.file.CopyOption;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Directory extends FileSystem.Entry
{
	public enum ExtendCopyOption implements CopyOption 
	{
		DIRECTORY_RENAME,
		
		FILE_RENAME;
	}
	
	//[static]------------------------------------------
	private static final DateTimeFormatter  RENAME_FORMAT = DateTimeFormatter.ofPattern("yyMMddHHmmssSSS");

	//임시 파일, 폴더 생성용
	private static final int TEMP_MAX_RADIX = Character.MAX_RADIX;
	private static SoftReference<SecureRandom> secureRandom = new SoftReference<SecureRandom>(null);

	private static Function<? super Path, ? extends FileSystem.Entry> ENTITY_CONVERT = p -> {
		File f = p.toFile();
		if (f.isDirectory())
		{
			return new Directory(p);
		} else
		{
			return new RegularFile(p);
		}
	};
	
	private static Function<? super Path, ? extends Directory> DIR_CONVERT = p -> {
		return new Directory(p);
	};
	
	private static Predicate<Path> IS_DIR_FILTER = Files::isDirectory;
	
	private static Function<? super Path, ? extends RegularFile> FILE_CONVERT = p -> {
		try
		{
			return new RegularFile(p);
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	};

	private static Predicate<Path> IS_FILE_FILTER = Files::isRegularFile;

	//[field]------------------------------------------	
	private final RegularFiles files = new RegularFiles();
	
	private final Directories dirs = new Directories();

	public RegularFiles files() {
		return new RegularFiles();
	}

	public Directories dirs() {
		return new Directories();
	}
	
	public Directory(String dirPath, LinkOption... linkOptions)
	{
		super(dirPath, linkOptions);
	}
	
	public Directory(Path dirPath, LinkOption... linkOptions)
	{
		super(dirPath, linkOptions);
	}
	
	public Directory(URI dirPath, LinkOption... linkOptions)
	{
		super(dirPath, linkOptions);
	}

	public Directory(Path path, boolean ifExistCheck, LinkOption... linkOptions) throws FileNotFoundException, FileAlreadyExistsException
	{
		super(path, ifExistCheck, linkOptions);
	}

	public Directory(String path, boolean ifExistCheck, LinkOption... linkOptions) throws FileNotFoundException, FileAlreadyExistsException
	{
		super(path, ifExistCheck, linkOptions);
	}
	
	public Directory(URI path, boolean ifExistCheck, LinkOption... linkOptions) throws FileNotFoundException, FileAlreadyExistsException
	{
		super(path, ifExistCheck, linkOptions);
	}
	
	public long size()
	{
		try
		{
			long size = Files.walk(toPath())
					.parallel()
	                .filter(IS_FILE_FILTER)
	                .mapToLong(p -> p.toFile().length())
	                .sum();
			return size;
		} catch (IOException e)
		{
			return 0;
		}
	}

	public Directory create() throws IOException
	{
		Files.createDirectories(toPath());
		return this;
	}

	public void delete(boolean recursive) throws IOException
	{
		if (recursive)
			this.clean();
		Files.delete(this.path);
	}

	public boolean exists()
	{
		return Files.exists(path);
	}

	@Override
	public Directory getRoot()
	{
		return new Directory(this.path.getRoot());
	}

	public Directory getParent()
	{
		return new Directory(this.path.getParent());
	}
	
	

//	public WatchKey register(WatchService watcher, Kind<?>[] events, Modifier... modifiers) throws IOException
//	{
//		return this.path.register(watcher, events, modifiers);
//	}
//
//	public WatchKey register(WatchService watcher, Kind<?>... events) throws IOException
//	{
//		return this.path.register(watcher, events);
//	}

	public Stream<FileSystem.Entry> list() throws IOException
	{
		return Files.list(toPath()).map(ENTITY_CONVERT);
	}

	public Stream<FileSystem.Entry> list(FileVisitOption... visitOption) throws IOException
	{
		Stream<FileSystem.Entry> stream = Files.walk(toPath(), visitOption).map(ENTITY_CONVERT);
		return stream;
	}

	public Stream<FileSystem.Entry> list(int maxDepth) throws IOException
	{
		Stream<FileSystem.Entry> stream = Files.walk(toPath(), maxDepth).map(ENTITY_CONVERT);
		return stream;
	}

	public Stream<FileSystem.Entry> list(int maxDepth, FileVisitOption... visitOption) throws IOException
	{
		Stream<FileSystem.Entry> stream = Files.walk(toPath(), maxDepth, visitOption).map(ENTITY_CONVERT);
		return stream;
	}

	public Stream<FileSystem.Entry> list(Filter.Depth depth, FileVisitOption... visitOption) throws IOException
	{
		Stream<FileSystem.Entry> stream = null;
		if (depth.equals(Filter.Depth.AllDirectories))
		{
			stream = Files.walk(toPath(), visitOption).map(ENTITY_CONVERT);
		} else
		{
			stream = Files.walk(toPath(), 1, visitOption).map(ENTITY_CONVERT);
		}
		return stream;
	}

	public Stream<FileSystem.Entry> list(Predicate<Path> searchFilter) throws IOException
	{
		Stream<FileSystem.Entry> stream = Files.walk(toPath()).filter(searchFilter).map(ENTITY_CONVERT);
		return stream;
	}

	public Stream<FileSystem.Entry> list(Predicate<Path> searchFilter, int maxDepth) throws IOException
	{
		Stream<FileSystem.Entry> stream = Files.walk(toPath(), maxDepth).filter(searchFilter).map(ENTITY_CONVERT);
		return stream;
	}

	public Stream<FileSystem.Entry>
		list(Predicate<Path> searchFilter, int maxDepth, FileVisitOption... visitOption) throws IOException
	{
		Stream<FileSystem.Entry> stream = Files.walk(toPath(), maxDepth, visitOption).filter(searchFilter).map(ENTITY_CONVERT);
		return stream;
	}

	/* 
	 * Current Directory 하부 Entry를 dir 로 복사함
	 */
	public Directory copy(Directory dir) throws IOException
	{
		Path path = Files.walkFileTree(toPath(), new CopyDirVisitor(toPath(), dir.toPath(), false));
		return new Directory(path);
	}

	/* 
	 * Current Directory 하부 Entry를 dir 로 복사함
	 */
	public Directory copy(Directory dir, CopyOption... option) throws IOException
	{
		Path path = Files.walkFileTree(toPath(), new CopyDirVisitor(toPath(), dir.toPath(), false, option));
		return new Directory(path);
	}

	/* 
	 * Current Directory 하부 Entry를 dir 로 이동함
	 */
	public Directory move(Directory dir) throws IOException
	{
		Path path = Files.walkFileTree(toPath(), new MoveDirVisitor(toPath(), dir.toPath(), false));
		return new Directory(path);
	}

	/* 
	 * Current Directory 하부 Entry를 dir 로 이동함
	 */
	public Directory move(Directory dir, CopyOption... option) throws IOException
	{
		Path path = Files.walkFileTree(toPath(), new MoveDirVisitor(toPath(), dir.toPath(), false, option));
		return new Directory(path);
	}

	/* 
	 * Current Directory 하부 Entry를 삭제함
	 */
	public boolean clean() throws IOException
	{
		Files.walkFileTree(toPath(), new DeleteDirVisitor());
		return true;
	}

	public boolean exists(String name)
	{
		return Files.exists(path.resolve(name), Directory.this.getLinkOptions());
	}
	
	public boolean isDirectory(String name)
	{
//		exists(name);
		boolean exists = Files.isDirectory(path.resolve(name), Directory.this.getLinkOptions());
		return exists;
	}

	public boolean isFile(String name)
	{
		boolean exists = Files.isRegularFile(path.resolve(name), Directory.this.getLinkOptions());
		return exists;
	}



	public class Directories
	{
		public Directory create(String directoryName) throws IOException
		{
			Path dir = Files.createDirectories(toPath().resolve(directoryName));
			return new Directory(dir);
		}
		
		public Directory create(String directoryName, FileAttribute<?>... attrs) throws IOException
		{
			Path dir = Files.createDirectories(toPath().resolve(directoryName), attrs);
			return new Directory(dir);
		}
		
		/**
		 * 기본 Java 와는 다르게 Temp 명이 진법이 36진법을 사용
		 *
		 * @param prefix the prefix
		 * @param attrs the attrs
		 * @return the directory
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public Directory createTemp(String prefix, FileAttribute<?>... attrs) throws IOException
		{
			Path generatePath = generatePath(toPath(), prefix, null);
			Path tempDirectory = Files.createDirectories(generatePath, attrs);
//			Path tempDirectory = Files.createTempDirectory(toPath(), prefix, attrs);
			return new Directory(tempDirectory);
		}
		
		public Directory createTemp(String prefix, String suffix, FileAttribute<?>... attrs) throws IOException
		{
			Path generatePath = generatePath(toPath(), prefix, suffix);
			Path tempDirectory = Files.createDirectories(generatePath, attrs);
//			Path tempDirectory = Files.createTempDirectory(toPath(), prefix, attrs);
			return new Directory(tempDirectory);
		}
		
		public Stream<Directory> list() throws IOException
		{
			return Files.list(toPath()).filter(IS_DIR_FILTER).map(DIR_CONVERT);
		}

		public Stream<Directory> list(FileVisitOption... visitOption) throws IOException
		{
			Stream<Directory> stream = Files.walk(toPath(), visitOption).filter(IS_DIR_FILTER).map(DIR_CONVERT);
			return stream;
		}

		public Stream<Directory> list(int maxDepth) throws IOException
		{
			Stream<Directory> stream = Files.walk(toPath(), maxDepth).filter(IS_DIR_FILTER).map(DIR_CONVERT);
			return stream;
		}

		public Stream<Directory> list(int maxDepth, FileVisitOption... visitOption) throws IOException
		{
			Stream<Directory> stream = Files.walk(toPath(), maxDepth, visitOption).filter(IS_DIR_FILTER).map(DIR_CONVERT);
			return stream;
		}

		public Stream<Directory> list(Filter.Depth depth, FileVisitOption... visitOption) throws IOException
		{
			Stream<Directory> stream = null;
			if (depth.equals(Filter.Depth.AllDirectories))
			{
				stream = Files.walk(toPath(), visitOption).filter(IS_DIR_FILTER).map(DIR_CONVERT);
			} else
			{
				stream = Files.walk(toPath(), 1, visitOption).filter(IS_DIR_FILTER).map(DIR_CONVERT);
			}
			return stream;
		}

		public Stream<Directory> list(Predicate<Path> searchFilter) throws IOException
		{
			searchFilter = IS_DIR_FILTER.and(searchFilter);
			Stream<Directory> stream = Files.walk(toPath()).filter(searchFilter).map(DIR_CONVERT);
			return stream;
		}

		public Stream<Directory> list(Predicate<Path> searchFilter, int maxDepth) throws IOException
		{
			searchFilter = IS_DIR_FILTER.and(searchFilter);
			Stream<Directory> stream = Files.walk(toPath(), maxDepth).filter(searchFilter).map(DIR_CONVERT);
			return stream;
		}

		public Stream<Directory>
		list(Predicate<Path> searchFilter, int maxDepth, FileVisitOption... visitOption) throws IOException
		{
			searchFilter = IS_DIR_FILTER.and(searchFilter);
			Stream<Directory> stream = Files.walk(toPath(), maxDepth, visitOption).filter(searchFilter).map(DIR_CONVERT);
			return stream;
		}

		public Stream<Directory> list(Predicate<Path> searchFilter, Filter.Depth depth,
			FileVisitOption... visitOption) throws IOException
		{
			searchFilter = IS_DIR_FILTER.and(searchFilter);
			Stream<Directory> stream = null;
			if (depth.equals(Filter.Depth.AllDirectories))
			{
				stream = Files.walk(toPath(), visitOption).filter(searchFilter).map(DIR_CONVERT);
			} else
			{
				stream = Files.walk(toPath(), 1, visitOption).filter(searchFilter).map(DIR_CONVERT);
			}
			return stream;
		}

		public Directory get(String name)
		{
			if(Files.exists(path) && Files.isDirectory(path))
			{
				return new Directory(toPath().resolve(name));
			}
			return null;
		}
		
		/* 
		 * param dir 을 포함하여 폴더를 복사함
		 */
		public Directory copy(Directory dir) throws IOException
		{
			Path path = Files.walkFileTree(toPath(), new CopyDirVisitor(toPath(), dir.toPath(), true));
			return new Directory(path);
		}

		public Directory copy(Directory dir, CopyOption... option) throws IOException
		{
			Path path = Files.walkFileTree(toPath(), new CopyDirVisitor(toPath(), dir.toPath(), true, option));
			return new Directory(path);
		}

		public Directory move(Directory dir) throws IOException
		{
			Path path = Files.walkFileTree(toPath(), new MoveDirVisitor(toPath(), dir.toPath(), true));
			return new Directory(path);
		}

		public Directory move(Directory dir, CopyOption... option) throws IOException
		{
			Path path = Files.walkFileTree(toPath(), new MoveDirVisitor(toPath(), dir.toPath(), true, option));
			return new Directory(path);
		}
		
		public boolean delete(String dir, boolean recusive) throws IOException
		{
			Directory directory = this.get(dir);
			if(directory != null)
				directory.delete(recusive);
			else 
				return false;
			return true;
		}
	}

//	/* 
//	 * param dir 을 포함하여 폴더를 복사함
//	 */
//	public void copy(Path dir) throws IOException
//	{
//		Files.walkFileTree(path(), new CopyDirVisitor(path(), dir, true));
//	}
//
//	public void copy(Path dir, CopyOption... option) throws IOException
//	{
//		Files.walkFileTree(path(), new CopyDirVisitor(path(), dir, true, option));
//	}
//
//	public void move(Path dir) throws IOException
//	{
//		Files.walkFileTree(path(), new MoveDirVisitor(path(), dir));
//	}
//
//	public void move(Path dir, CopyOption... option) throws IOException
//	{
//		Files.walkFileTree(path(), new MoveDirVisitor(path(), dir, option));
//	}

//	public class Entries
//	{
//	}

	public class RegularFiles
	{
//		public RegularFile create(String fileName) throws IOException
//		{
//			Path createFile = Files.createFile(Directory.this.toPath().resolve(fileName));
//			return new RegularFile(createFile);
//		}
		
		public RegularFile create(String fileName, FileAttribute<?>... attrs) throws IOException
		{
			Path createFile = Files.createFile(Directory.this.toPath().resolve(fileName), attrs);
			return new RegularFile(createFile);
		}
		
//		public RegularFile create(String fileName, InputStream stream) throws IOException
//		{
//			Path createFile = Files.createFile(Directory.this.toPath().resolve(fileName));
//			Files.copy(stream, createFile, StandardCopyOption.REPLACE_EXISTING);
//			return new RegularFile(createFile);
//		}
		
		public RegularFile create(String fileName, InputStream stream, FileAttribute<?>... attrs) throws IOException
		{
			Path createFile = Files.createFile(Directory.this.toPath().resolve(fileName), attrs);
			Files.copy(stream, createFile, StandardCopyOption.REPLACE_EXISTING);
			return new RegularFile(createFile);
		}
		
		public RegularFile create(URI uri, FileAttribute<?>... attrs) throws IOException
		{
			return create(null, uri, attrs);
		}

		public RegularFile create(String fileName, URI uri, FileAttribute<?>... attrs) throws IOException
		{
			URLConnection conn = uri.toURL().openConnection();
			if (fileName == null && conn instanceof HttpURLConnection)
			{
				HttpURLConnection httpConn = (HttpURLConnection) conn;
				int responseCode = httpConn.getResponseCode();
				if (responseCode == HttpURLConnection.HTTP_OK)
				{
					String disposition = httpConn.getHeaderField("Content-Disposition");
					if (disposition != null)
					{
						int index = disposition.indexOf("filename=");
						if (index > 0)
						{
							fileName = disposition.substring(index + 10, disposition.length() - 1);
						}
					}
				}
			}
			if (fileName == null)
			{
				String uriString = uri.toASCIIString();
				int lastIndexOf = uriString.lastIndexOf("/");
				int min = Math.min(200, uriString.length() - lastIndexOf);
				fileName = uriString.substring(lastIndexOf + 1, lastIndexOf + min);
			}
			try(InputStream stream = uri.toURL().openStream())
			{
				return create(fileName, stream, attrs);
			}
		}
		
		/**
		 * 확장자가 tmp이 Temp 파일을 생성함
		 * 기본 Java 와는 다르게 Temp 명이 진법이 36진법을 사용
		 * @param attrs the attrs
		 * @return the regular file
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public RegularFile createTemp(FileAttribute<?>... attrs) throws IOException
		{
			Path generatePath = generatePath(toPath(), null, null);
			Path tempDirectory = Files.createFile(generatePath, attrs);
			return new RegularFile(tempDirectory);
		}
		
		public RegularFile createTemp(String prefix, String suffix, FileAttribute<?>... attrs) throws IOException
		{
			Path generatePath = generatePath(toPath(), prefix, suffix);
			Path tempDirectory = Files.createFile(generatePath, attrs);
//			Path tempDirectory = Files.createTempFile(Directory.this.toPath(), prefix, suffix, attrs);
			return new RegularFile(tempDirectory);
		}
		
		/**
		 * prefix를 앞에 덧 붙이고 확장자가 suffix인 임시파일을 생성하고 stream을 기록합니다.
		 * 기본 Java 와는 다르게 Temp파일명이 진법이 36진법을 사용
		 *
		 * @param prefix 앞쪽에 덧붙일 문자열
		 * @param suffix 확장자 또는 파일명에 덧붙일 문자
		 * @param stream 생성한 임시파일에 기록할 스트림 데이터
		 * @param attrs 파일 속성
		 * @return 생성된 임시 파일을 지칭하는 RegularFile 객체
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public RegularFile createTemp(String prefix, String suffix, InputStream stream, FileAttribute<?>... attrs) throws IOException
		{
			Path generatePath = generatePath(toPath(), prefix, suffix);
			Path tempDirectory = Files.createFile(generatePath, attrs);
//			Path temp = Files.createTempFile(Directory.this.toPath(), prefix, suffix, attrs);
			Files.copy(stream, tempDirectory, StandardCopyOption.REPLACE_EXISTING);
			return new RegularFile(tempDirectory);
		}
		
		/**
		 * 확장자가 suffix인 임시파일을 생성하고 stream을 기록합니다.
		 * 기본 Java 와는 다르게 Temp파일명이 진법이 36진법을 사용
		 *
		 * @param suffix 확장자 또는 파일명에 덧붙일 문자
		 * @param stream 생성한 임시파일에 기록할 스트림 데이터
		 * @param attrs 파일 속성
		 * @return 생성된 임시 파일을 지칭하는 RegularFile 객체
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public RegularFile createTemp(String suffix, InputStream stream, FileAttribute<?>... attrs) throws IOException
		{
			Path generatePath = generatePath(toPath(), null, suffix);
			Path temp = Files.createFile(generatePath, attrs);
//			Path temp = Files.createTempFile(Directory.this.toPath(), null, suffix, attrs);
			Files.copy(stream, temp, StandardCopyOption.REPLACE_EXISTING);
			return new RegularFile(temp);
		}
		
//		public RegularFile createTemp(String prefix, String suffix, URL url, FileAttribute<?>... attrs) throws IOException
//		{
//			try(InputStream stream = url.openStream())
//			{
//				return createTemp(prefix, suffix, stream, attrs);
//			}
//		}
//		
//		public RegularFile createTemp(String suffix, URL url, FileAttribute<?>... attrs) throws IOException
//		{
//			return createTemp(null, suffix, url, attrs);
//		}
		
		public RegularFile createTemp(String prefix, String suffix, URI uri, FileAttribute<?>... attrs) throws IOException
		{
			try(InputStream stream = uri.toURL().openStream())
			{
				return createTemp(prefix, suffix, stream, attrs);
			}
		}
		
		public RegularFile createTemp(String suffix, URI uri, FileAttribute<?>... attrs) throws IOException
		{
			return createTemp(null, suffix, uri, attrs);
		}
		
		public Stream<RegularFile> list() throws IOException
		{
			return Files.list(toPath()).filter(IS_FILE_FILTER).map(FILE_CONVERT);
		}

		public Stream<RegularFile> list(Predicate<Path> searchFilter, int maxDepth) throws IOException
		{
			Stream<RegularFile> stream = Files.list(Directory.this.toPath()).filter(IS_FILE_FILTER.and(searchFilter))
				.map(FILE_CONVERT);
			return stream;
		}

		public RegularFile get(String name) throws IOException
		{
			Path resolve = toPath().resolve(name);
			if(Files.exists(resolve))
			{
				return new RegularFile(resolve);
			}
			return null;
		}
		
		public RegularFile copy(RegularFile file) throws IOException
		{
			Path copy = Files.copy(toPath(), file.toPath());
			return new RegularFile(copy);
		}

		public RegularFile copy(RegularFile file, CopyOption... options) throws IOException
		{
			boolean isFileRename = false;
			if(options.length > 0)
			{
    			int i = 0;
    			for (CopyOption option : options)
    			{
    				if(option instanceof ExtendCopyOption)
    				{
    					//ExtendCopyOption opt = (ExtendCopyOption) option;
    					switch((ExtendCopyOption) option)
    					{
    						case FILE_RENAME:
    							isFileRename = true;
    							break;
    						default:
    							break;
    					}
    				}else
    				{
    					options[i] = option;
    					i++;
    				}
    			}
    			if(i < options.length)
    				options = Arrays.copyOf(options, i);
			}
			Path destfile = path.resolve(file.getName());
			if(isFileRename && Files.exists(destfile))
			{
				String newName = _MakeFileReName(file.toPath());
				destfile = destfile.resolveSibling(newName);
			}
			Path copy = Files.copy(file.toPath(), destfile, options);
			return new RegularFile(copy);
		}

		public RegularFile move(RegularFile file) throws IOException
		{
			Path move = Files.move(toPath(), file.toPath());
			return new RegularFile(move);
		}

		public RegularFile move(RegularFile file, CopyOption... option) throws IOException
		{
			Path move = Files.move(toPath(), file.toPath(), option);
			return new RegularFile(move);
		}
	}

	abstract class DirVisitor extends SimpleFileVisitor<Path>
	{
		private Path fromPath;
		private Path toPath;
		private CopyOption[] options;
		
		private boolean isDirRename = false;
		private boolean isFileRename = false;

		DirVisitor(Path fromPath, Path toPath, boolean isSelf, CopyOption... options) throws IOException
		{
			this.fromPath = fromPath;
			this.toPath = toPath;

			this.options = options;
//			CopyOption[] options = new CopyOption[copyOption.length]; 
			if(options.length > 0)
			{
    			int i = 0;
    			for (CopyOption option : options)
    			{
    				if(option instanceof ExtendCopyOption)
    				{
    					//ExtendCopyOption opt = (ExtendCopyOption) option;
    					switch((ExtendCopyOption) option)
    					{
    						case DIRECTORY_RENAME:
    							isDirRename = true;
    							break;
    						case FILE_RENAME:
    							isFileRename = true;
    							break;
    						default:
    							break;
    					}
    				}else
    				{
    					options[i] = option;
    					i++;
    				}
    			}
    			if(i < this.options.length)
    				this.options = Arrays.copyOf(options, i);
			}
			if(isSelf)
			{
				this.toPath = this.toPath.resolve(fromPath.getFileName());
			}
			if(Files.exists(this.toPath) && isDirRename)
			{
				String newName = _MakeDirReName(this.toPath);
				this.toPath = this.toPath.resolveSibling(newName);
			}
			Files.createDirectories(this.toPath);
		}

		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
		{
			Path targetPath = toPath.resolve(fromPath.relativize(dir));
			if(!Files.exists(targetPath))
			{
				Files.createDirectory(targetPath);
			}
				
			return FileVisitResult.CONTINUE;
		}

//		@Override
//		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
//		{
//			Path destfile = toPath.resolve(fromPath.relativize(file));
//			if(isFileRename && Files.exists(destfile))
//			{
//				String newName = _MakeFileReName(file);
//				destfile = destfile.resolveSibling(newName);
//			}
//			return FileVisitResult.CONTINUE;
//		}
	}
	
	class CopyDirVisitor extends DirVisitor
	{
		CopyDirVisitor(Path fromPath, Path toPath, boolean isSelf, CopyOption... options) throws IOException
		{
			super(fromPath, toPath, isSelf, options);
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
		{
			Path destfile = super.toPath.resolve(super.fromPath.relativize(file));
			if(super.isFileRename && Files.exists(destfile))
			{
				String newName = _MakeFileReName(file);
				destfile = destfile.resolveSibling(newName);
			}
			Files.copy(file, destfile, super.options);
			return FileVisitResult.CONTINUE;
		}
	}

	

	class MoveDirVisitor extends DirVisitor
	{
		public MoveDirVisitor(final Path fromPath, final Path toPath, boolean isSelf, CopyOption... options) throws IOException
		{
			super(fromPath, toPath, isSelf, options); 
		}

		@Override
		public final FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException
		{
			Objects.requireNonNull(file);
			Objects.requireNonNull(attrs);
			Path destfile = super.toPath.resolve(super.fromPath.relativize(file));
			if(super.isFileRename && Files.exists(destfile))
			{
				String newName = _MakeFileReName(file);
				destfile = destfile.resolveSibling(newName);
			}
			Files.move(file, destfile, super.options);
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult postVisitDirectory(final Path dir, IOException exc) throws IOException
		{
			Objects.requireNonNull(dir);
			if (exc != null)
				throw exc;
			Files.delete(dir);
			return FileVisitResult.CONTINUE;
		}
	}
	
	class DeleteDirVisitor extends SimpleFileVisitor<Path>
	{
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
		{
			Files.delete(file);
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
		{
			if (exc == null)
			{
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}
			throw exc;
		}
	}

	/*
	 * public class RegularFiles { public RegularFile create(String fileName) {
	 * Directory.this.path.resolve(fileName); return null; } }
	 */

	public static class Filter
	{
		public enum Depth
		{
			AllDirectories, TopDirectoryOnly;
		}
	
		public static Predicate<? extends Path> name(String name)
		{
			Predicate<Path> option = p -> p.getFileName().equals(name);
			return option;
		}
	
		public static Predicate<? extends Path> glob(String glob)
		{
			Predicate<Path> option = new Predicate<Path>()
			{
				private PathMatcher matcher;
	
				@Override
				public boolean test(Path p)
				{
					if (matcher == null)
						matcher = p.getFileSystem().getPathMatcher("glob:" + glob);
					Path name = p.getFileName();
					if (name != null && matcher.matches(name))
						return true;
					return false;
				}
			};
			return option;
		}
	
		public static Predicate<? extends Path> regex(String regex)
		{
			Predicate<Path> option = new Predicate<Path>()
			{
				private PathMatcher matcher;
	
				@Override
				public boolean test(Path p)
				{
					if (matcher == null)
						matcher = p.getFileSystem().getPathMatcher("regex:" + regex);
					Path name = p.getFileName();
					if (name != null && matcher.matches(name))
						return true;
					return false;
				}
			};
			return option;
		}
	
		public static Predicate<? extends Path> isDirectory(LinkOption... linkOption)
		{
			Predicate<Path> option = p -> Files.isDirectory(p, linkOption);
			return option;
		}
	
		public static Predicate<? extends Path> isFile(LinkOption... linkOption)
		{
			Predicate<Path> option = p -> Files.isRegularFile(p, linkOption);
			return option;
		}
	}

//	public static void main(String[] args) throws IOException
//	{
//		Path dirPath = Paths.get("C:\\Temp/보험청구");
//		for (int i = 0; i < 10000; i++)
//		{
//			Path generatePath = generatePath(dirPath, null, null);
//			System.out.println(i+ " "+ generatePath);
//		}
//		Path dirPath2 = Paths.get("C:\\Temp/보험청구2/복사본");
//		URI uri = URI.create("momket://images/ads");
//		Directory dir = new Directory("momket://images/ad/");
//		Stream<RegularFile> list2 = dir.files().list();
//		list2
////		.sorted(new Comparator<RegularFile>()
////		{
////
////			@Override
////			public int compare(RegularFile o1, RegularFile o2)
////			{
////				// TODO Auto-generated method stub
////				return 0;
////			}
////		})
//		.forEach(f->  
//			{
//				try
//				{
//					Map<String, String> userAttributes = f.getUserAttributes();
//					System.out.println(userAttributes);
//					System.out.println(f);
//					System.out.println(f.fileKey());
//				} catch (Exception e)
//				{
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		);
//		Stream<Directory> list = dir.dirs().list();
//		list.forEach(System.out::println);
////		for (Path path : list.)
////		{
////			
////		}
////		dir.files().copy(new RegularFile("C://Temp/5.gif"), ExtendCopyOption.FILE_RENAME);
//		System.out.println(dir.size());
////		dir.copy(dirPath2, ExtendCopyOption.FILE_RENAME);
//		//Files.exists(path, options)
//		//dir.copy(dirPath2);
//		// dir.move(dir);
//		// FileInputStream stream = new FileInputStream(new
//		// File("C:\\Temp\\5.gif"));
//		// dir.copyFile("5.gif", stream, x->x.toString()+x.size());
//		// dir.createFile("5-000.gif", stream);
//		// dir.list(FileVisitOption.FOLLOW_LINKS).forEach(
//		// p->{
//		// System.out.println(p);
//		// if(p.toString().equals("\\$Recycle.Bin"))
//		// {
//		// return;
//		// };
//		// }
//		// );
//
//		System.out.println("------------------------");
////		Stream<FileSystem.Entry> list2 = dir.entries();
////		// Stream<FileSystemEntry> list2 =
////		// dir.list(FileVisitOption.FOLLOW_LINKS);
////		Iterator<FileSystem.Entry> iterator2 = list2.iterator();
////		System.out.println("------------------------");
////		while (iterator2.hasNext())
////		{
////			System.out.println(iterator2.next());
////		}
////		list2.close();
//		// for (FileSystemEntry f : list)
//		// {
//		//
//		// }
//	}

	private String _MakeFileReName(Path file)
	{
		String nowDate = RENAME_FORMAT.format(LocalDateTime.now());
		String fileName = file.getFileName().toString().trim();
		String name = "";
		String extension = "";
		int i = fileName.lastIndexOf('.');
		if (i > 0 && i < fileName.length() - 1)
		{
			name = fileName.substring(0, i);
			extension = fileName.substring(i + 1).trim();
		}
		String newName = name+"-"+nowDate+"."+extension;
		return newName;
	}

	private String _MakeDirReName(Path path)
	{
		String nowDate = RENAME_FORMAT.format(LocalDateTime.now());
		String newName = path.getFileName().toString()+"-"+nowDate;
		return newName;
	}
	
    private static Path generatePath(Path dir, String prefix, String suffix) 
    {
    	if (prefix == null)
            prefix = "";
        if (suffix == null)
            suffix = ".tmp";
        
        SecureRandom random = secureRandom.get();
        if(null == random)
        {
        	random = new SecureRandom();
        	secureRandom = new SoftReference<SecureRandom>(random);
        }
        long n = random.nextLong();
        n = (n == Long.MIN_VALUE) ? 0 : Math.abs(n);
        String hex = Long.toString(n, TEMP_MAX_RADIX);
		Path name = dir.getFileSystem().getPath(prefix + hex + suffix);
        if (name.getParent() != null)
            throw new IllegalArgumentException("Invalid prefix or suffix");
        return dir.resolve(name);
    }
}
