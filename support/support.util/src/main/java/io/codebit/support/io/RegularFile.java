package io.codebit.support.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.DosFileAttributes;
import java.util.List;
import java.util.Objects;

import io.codebit.support.nio.extensions.PathExtension;

public class RegularFile extends FileSystem.Entry
{
	private String extension;

	public String getExtension(){
		return this.extension;
	}


//	private Path path;
	
	public RegularFile(Path filePath, LinkOption... linkOptions)
	{
		super(filePath, linkOptions);
//		this.path = filePath;
		Path fileName = filePath.getFileName();
		extension = PathExtension.getExtension(fileName);
	}
	
	public RegularFile(String filePath, LinkOption... linkOptions)
	{
		super(filePath, linkOptions);
		Path fileName = this.path.getFileName();
		extension = PathExtension.getExtension(fileName);
	}
	
	public RegularFile(URI filePath, LinkOption... linkOptions)
	{
		super(filePath, linkOptions);
		Path fileName = this.path.getFileName();
		extension = PathExtension.getExtension(fileName);
	}

	public RegularFile(Path path, boolean ifExistCheck, LinkOption... linkOptions) throws FileNotFoundException, FileAlreadyExistsException
	{
		super(path, ifExistCheck, linkOptions);
	}
	
	public RegularFile(String path, boolean ifExistCheck, LinkOption... linkOptions) throws FileNotFoundException, FileAlreadyExistsException
	{
		super(path, ifExistCheck, linkOptions);
	}
	
	public RegularFile(URI path, boolean ifExistCheck, LinkOption... linkOptions) throws FileNotFoundException, FileAlreadyExistsException
	{
		super(path, ifExistCheck, linkOptions);
	}
	
	public long size()
	{
		try
		{
			return Files.size(path);
		} catch (IOException e)
		{
			return 0;
		}
	}
	
	public Path toPath()
	{
		return this.path;
	}
	
	public boolean delete() throws IOException
	{
		if(Files.exists(path) && Files.isRegularFile(path))
		{
			Files.delete(this.path);
			return true;
		}
		return false;
	}
	
	public boolean exists()
	{
		return Files.exists(path);
	}
	
//	public Path getRoot()
//	{
//		return this.path.getRoot();
//	}

	public Path getName()
	{
		return this.path.getFileName();
	}

//	public Directory getDirectory()
//	{
//		return new Directory(this.path.getParent());
//	}

	public URI toUri()
	{
		return this.path.toUri();
	}

	public Path toRealPath() throws IOException
	{
		return this.path.toRealPath(getLinkOptions());
	}

//	public Path toRealPath(LinkOption... options) throws IOException
//	{
//		return this.path.toRealPath(options);
//	}

	public File toFile()
	{
		return this.path.toFile();
	}
	
	public boolean isArchive()  throws IOException
    {
		return Files.readAttributes(this.path, DosFileAttributes.class, RegularFile.this.getLinkOptions()).isArchive();
    }
	
//	public boolean isArchive(LinkOption... options)  throws IOException
//    {
//		return Files.readAttributes(this.path, DosFileAttributes.class, options).isArchive();
//    }

//	public WatchKey register(WatchService watcher, Kind<?>[] events, Modifier... modifiers) throws IOException
//	{
//		return this.path.register(watcher, events, modifiers);
//	}
//
//	public WatchKey register(WatchService watcher, Kind<?>... events) throws IOException
//	{
//		return this.path.register(watcher, events);
//	}

	
	public List<String> readAllLines() throws IOException
	{
		List<String> allLines = Files.readAllLines(path);
		return allLines;
	}
	
	public List<String> readAllLines(Charset charset) throws IOException
	{
		List<String> allLines = Files.readAllLines(path, charset);
		return allLines;
	}
	
	public byte[] readAllBytes() throws IOException
	{
		return Files.readAllBytes(path);
	}
	
	public void write(Iterable<? extends CharSequence> lines) throws IOException
	{
		Files.write(path, lines);
	}
	
	public void write(Iterable<? extends CharSequence> lines, Charset charset) throws IOException
	{
		Files.write(path, lines, charset);
	}
	
	public long write(byte[] bytes) throws IOException
	{
		Files.write(path, bytes);
		return bytes.length;
	}
	
	public long write(InputStream stream) throws IOException
	{
		return Files.copy(stream, path, StandardCopyOption.REPLACE_EXISTING);
	}
	
	public long append(InputStream stream) throws IOException
	{
		long readLen = 0;
		try(OutputStream out = Files.newOutputStream(path, StandardOpenOption.APPEND, StandardOpenOption.CREATE_NEW))
		{
			byte[] buf = new byte[1024 * 8];
			
			int len = -1;
			while((len = stream.read(buf)) > 0)
			{
				out.write(buf, 0, len);
				readLen += len;
			}
		}
		return readLen;
	}
	
	public long append(Iterable<? extends CharSequence> lines, Charset charset) throws IOException
	{
		Objects.requireNonNull(lines);
		Objects.requireNonNull(charset);
		long readLen = 0;
		CharsetEncoder encoder = charset.newEncoder();
		try(OutputStream out = Files.newOutputStream(path, StandardOpenOption.APPEND, StandardOpenOption.CREATE_NEW);
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, encoder)))
		{
            for (CharSequence line: lines) 
            {
                writer.append(line);
                writer.newLine();
                readLen += line.length(); 
            }
		}
		return readLen;
	}
	
//	public boolean reName(String name)
//	{
//		Path rename = path.resolveSibling(name);
//		return path.toFile().renameTo(rename.toFile());
//	}
	
//	public String toString()
//	{
//		return this.path.toString();
//	}
}
