package io.codebit.support.nio.extensions;

import java.nio.file.Path;

public abstract class PathExtension
{

	/**
	 * 파일 객체에서 확장자를 추출함
	 *
	 * @param path the path
	 * @return the extension
	 */
	public static String getExtension(Path path)
	{
		if(path == null)
		{
			throw new IllegalArgumentException("path");
		}
		Path file = path.getFileName();
		String fileName = file.toString();
		int i = fileName.lastIndexOf('.');
		if (i > 0 && i < fileName.length() - 1)
		{
			return fileName.substring(i + 1);
		}
		return "";
	}
	
	/**
	 * 일치하는 확장자의 존재 여부
	 *
	 * @param path the file
	 * @param exts the exts
	 * @return true, if is extension
	 */
	public static boolean isExtension(Path path, String ... exts)
	{
		String extension = PathExtension.getExtension(path);
		if (extension.equals(""))
			return false;
		for(String _ext: exts)
		{
			if(extension.equalsIgnoreCase(_ext))
				return true;
		}
		return false;
	}
}
