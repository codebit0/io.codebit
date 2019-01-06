package io.codebit.support.spring.mvc.file.handler;


import java.nio.file.Path;
import java.nio.file.Paths;

import io.codebit.support.spring.mvc.Config;

public class FileDownloadHandler extends AbstractDownloadHandler<FileDownloadHandler>
{

	public FileDownloadHandler(String subPath, String fileName)
	{
		this(Paths.get(subPath), fileName);
	}
	
	public FileDownloadHandler(Path subPath, String fileName)
	{
		super(Paths.get(Config.get("file_root_path")), subPath, fileName);
	}
	
//	public FileDownloadHandler(HttpServletRequest request, HttpServletResponse response, String rootPath)
//	{
//		super(request, response, rootPath);
//	}
}
