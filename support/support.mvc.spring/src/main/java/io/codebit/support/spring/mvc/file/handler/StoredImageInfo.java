package io.codebit.support.spring.mvc.file.handler;

import java.nio.file.Path;

public class StoredImageInfo extends StoredFileInfo
{
	public StoredImageInfo(Path subPath, String name, Path path, Path systemPath, long size, int width, int height, UploadFileInfo originalInfo)
	{
		super(subPath, name, path, systemPath, size, originalInfo);
		this.width = width;
		this.height = height;
	}

	public int getWidth() {
		return width;
	}

	private int width;

	public int getHeight() {
		return height;
	}

	private int height;
}
