package io.codebit.support.spring.mvc.file.handler;

import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import io.codebit.support.lang.extensions.StringExtension;
import io.codebit.support.spring.mvc.Messages;

//@ExtensionMethod({ StringExtension.class })
public abstract class AbstractDeleteHandler
{

	protected final Path DEFAULT_SAVE_PATH;

	protected Path subPath;

	protected String fileName;


	private final Path savePath;
	
//	protected AbstractDeleteHandler(String rootPath, String subPath, String fileName)
//	{
//		this.DEFAULT_SAVE_PATH = Paths.get(rootPath).toAbsolutePath();
//		this.subPath = Paths.get(subPath);
//		//this.fileName = fileName;
//		this.savePath = DEFAULT_SAVE_PATH.resolve(subPath).toAbsolutePath();
//	}
//
//	protected AbstractDeleteHandler(String rootPath, String subPath)
//	{
//		this.DEFAULT_SAVE_PATH = Paths.get(rootPath).toAbsolutePath();
//		this.subPath = Paths.get(subPath);
//		this.savePath = DEFAULT_SAVE_PATH.resolve(subPath).toAbsolutePath();
//	}
	
	protected AbstractDeleteHandler(Path rootPath, Path subPath)
	{
		this.DEFAULT_SAVE_PATH = rootPath.toAbsolutePath();
		this.subPath = subPath;
		this.savePath = DEFAULT_SAVE_PATH.resolve(subPath).toAbsolutePath();
	}
	
	protected AbstractDeleteHandler(Path rootPath, Path subPath, String fileName)
	{
		this.DEFAULT_SAVE_PATH = rootPath.toAbsolutePath();
		this.subPath = subPath;
		this.fileName = fileName;
		this.savePath = DEFAULT_SAVE_PATH.resolve(subPath).toAbsolutePath();
	}

	/**
	 * 파일이 다운로드 가능한 파일인지 여부를 체크.
	 *
	 * @param path the path
	 * @return true, if is allowed file
	 */
	protected boolean isAllowed(Path path)
	{
		if (path == null)
		{
			// throw new
			// IOException("파일 객체가 Null 혹은 존재하지 않거나 길이가 0, 혹은 파일이 아닌 디렉토리이다.");
			return false;
		}
		return true;
	}

	/**
	 * 파일 삭제.
	 *
	 * @return true, if successful
	 * @throws IOException             Signals that an I/O exception has occurred.
	 * @throws DeleteDirectoryIOException the delete directory io exception
	 */
	public boolean delete() throws IOException, DeleteDirectoryIOException
	{
		Path targetPath = savePath;
		if(this.fileName != null)
		{
			targetPath = targetPath.resolve(this.fileName);
		}
		if (DEFAULT_SAVE_PATH.getRoot() == null
				|| StringExtension.isNullOrEmpty(this.subPath.toString())
				|| this.subPath.getRoot() != null
				|| !savePath.normalize().toString().equals(savePath.toString())
				|| !targetPath.toAbsolutePath().startsWith(savePath))
		{
			throw new FileSystemException(Messages.get("error_invalid_path")); //$NON-NLS-1$
		}
		if (!isAllowed(targetPath))
		{
			throw new FileSystemException(Messages.get("error_denied_file"));
		}
		if(targetPath.toFile().isDirectory())
		{
			throw new DeleteDirectoryIOException(this);
		}
		return process(targetPath);
	}

	boolean dirForceDelete() throws IOException
	{
		Files.walkFileTree(this.savePath, new SimpleFileVisitor<Path>()
		{
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
			{
				process(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
			{
				process(dir);
				return FileVisitResult.CONTINUE;
			}
		});
		Files.deleteIfExists(this.savePath);
		return true;
	}

	/**
	 * 파일 출력 프로세스.
	 *
	 * @param path the path
	 * @return true, if successful
	 * @throws IOException             Signals that an I/O exception has occurred.
	 */
	protected boolean process(Path path) throws IOException
	{
		return Files.deleteIfExists(path);
	}
	
	public class DeleteDirectoryIOException extends Exception
	{
		private static final long serialVersionUID = 1L;
		private final AbstractDeleteHandler abstractDeleteHandler;
		
		public DeleteDirectoryIOException(AbstractDeleteHandler handler)
		{
			super(Messages.get("error_delete_dir") ,null, false, false);
			this.abstractDeleteHandler = handler;
		}

		public boolean force() throws IOException
		{
			this.abstractDeleteHandler.dirForceDelete();
			return true;
		}
	}
}
