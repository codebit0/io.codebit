package io.codebit.support.spring.mvc.file.handler;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import io.codebit.support.spring.mvc.Config;
import org.apache.commons.io.filefilter.WildcardFileFilter;

public class ImageDeleteHandler extends AbstractDeleteHandler
{

	public ImageDeleteHandler(String subPath, String fileName)
	{
		super(Paths.get(Config.get("image_root_path")), Paths.get(subPath), fileName);
	}

	public ImageDeleteHandler(String subPath)
	{
		super(Paths.get(Config.get("image_root_path")), Paths.get(subPath));
	}
	
	public ImageDeleteHandler(Path subPath, String fileName)
	{
		super(Paths.get(Config.get("image_root_path")), subPath, fileName);
	}
	
	public ImageDeleteHandler(Path subPath)
	{
		super(Paths.get(Config.get("image_root_path")), subPath);
	}
	
	/**
	 * Thumbnail 파일을 삭제함.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void thumbnailDelete() throws IOException
	{
		Path thumbDirPath = DEFAULT_SAVE_PATH.resolve(subPath).resolve("thumbnail");
		
		File dir = thumbDirPath.toFile();
		FileFilter fileFilter = new WildcardFileFilter("*"+fileName);
		File[] files = dir.listFiles(fileFilter);
		if(files != null)
		{
    		for(File file : files)
    		{
    			process(file.toPath());
    		}
		}
	}

	@Override
	public boolean delete() throws IOException , DeleteDirectoryIOException
	{
		boolean delete = super.delete();
		thumbnailDelete();
		return delete;
	}
}
