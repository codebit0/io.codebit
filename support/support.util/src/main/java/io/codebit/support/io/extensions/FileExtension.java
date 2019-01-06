package io.codebit.support.io.extensions;

import java.io.File;

public abstract class FileExtension
{
	/**
	 * 파일 객체에서 확장자를 추출함
	 *
	 * @param file the file
	 * @return the extension
	 */
	public static String getExtension(File file)
	{
		if(file == null)
		{
			throw new IllegalArgumentException("file");
		}
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
	 * @param file the file
	 * @param exts the exts
	 * @return true, if is extension
	 */
	public static boolean isExtension(File file, String ... exts)
	{
		String extension = FileExtension.getExtension(file);
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
