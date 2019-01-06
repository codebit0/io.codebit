package io.codebit.support.spring.mvc.file.handler;

import org.springframework.web.multipart.MultipartFile;


public class UploadFileInfo
{
	public UploadFileInfo(MultipartFile file, String fileName, String extension, long size, String contentType) {
		this.multipartFile = file;
		this.name = fileName;
		this.extension = extension;
		this.contentType = contentType;
	}

	public MultipartFile getMultipartFile() {
		return multipartFile;
	}

	private MultipartFile multipartFile;

	public String getExtension() {
		return extension;
	}

	public String getName() {
		return name;
	}

	private String name;
	
	private String extension;

	public long getSize() {
		return size;
	}

	private long size;

	public String getContentType() {
		return contentType;
	}

	void setContentType(String contentType) {
		this.contentType = contentType;
	}

	private String contentType;
}
