package io.codebit.support.spring.mvc.file.handler;

import java.nio.file.Path;


public class StoredFileInfo
{
	public StoredFileInfo(Path subPath, String name, Path path, Path systemPath, long size, UploadFileInfo originalInfo)
	{
		this.subPath = subPath;
		this.name = name;
		this.path = path;
		this.size = size;
		this.systemPath = systemPath;
		this.originalInfo = originalInfo;
	}

	public Path getPath() {
		return path;
	}

	/**
	 * subPath +  fileName
	 *
	 * @return the path
	 */
	private Path path;

	public Path getSubPath() {
		return subPath;
	}

	private Path subPath;

	public Path getSystemPath() {
		return systemPath;
	}

	/**
	 * System full Path
	 *
	 * @return the system path
	 */
	private Path systemPath;

	public String getName() {
		return name;
	}

	private String name;

	public long getSize() {
		return size;
	}

	private long size;

	public UploadFileInfo getOriginalInfo() {
		return originalInfo;
	}

	private UploadFileInfo originalInfo;
}
