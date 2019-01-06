package io.codebit.support.spring.mvc.file.handler;


import java.nio.file.Path;
import java.nio.file.Paths;

import io.codebit.support.spring.mvc.Config;

public class FileDeleteHandler extends AbstractDeleteHandler
{
	public FileDeleteHandler(String subPath, String fileName)
	{
		super(Paths.get(Config.get("file_root_path")), Paths.get(subPath),fileName);
	}
	
	public FileDeleteHandler(String subPath)
	{
		super(Paths.get(Config.get("file_root_path")), Paths.get(subPath));
	}
	
	public FileDeleteHandler(Path subPath, String fileName)
	{
		super(Paths.get(Config.get("file_root_path")), subPath, fileName);
	}
	
	public FileDeleteHandler(Path subPath)
	{
		super(Paths.get(Config.get("file_root_path")), subPath);
	}
}
