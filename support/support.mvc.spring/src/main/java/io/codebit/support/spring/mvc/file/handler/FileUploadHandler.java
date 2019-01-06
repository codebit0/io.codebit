package io.codebit.support.spring.mvc.file.handler;

import java.nio.file.Path;
import java.nio.file.Paths;

import io.codebit.support.spring.mvc.Config;

public class FileUploadHandler extends AbstractUploadHandler<StoredFileInfo>
{
	public FileUploadHandler(String subPath)
	{
		//super(Paths.get(Config.get("file_root_path")), Paths.get(subPath));
		this(Paths.get(subPath));
	}

	public FileUploadHandler(Path subPath)
	{
		super(Paths.get(Config.get("file_root_path")), subPath);
	}
	
//	public FileUploadHandler(String rootPath, String subPath)
//	{
//		super(rootPath, subPath);
//	}
	
	protected boolean isAllowed(UploadFileInfo uploadFileInfo)
	{
		return true;
	}
}
