package io.codebit.support.servlet;

import java.lang.reflect.Field;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

//import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;


public class ServletContainerInitializer implements javax.servlet.ServletContainerInitializer
{
    @Override
    public void onStartup(Set<Class<?>> c, ServletContext servletContext)
            throws ServletException
    {
    	try
    	{
    		//Custom FileSystemProvider를 Web-App에서 추가 지원하기 위해서  
    		List<FileSystemProvider> list = new ArrayList<FileSystemProvider>();
    		for (FileSystemProvider fileSystemProvider : FileSystemProvider.installedProviders())
    		{
    			list.add(fileSystemProvider);
    		}
    		Field field = FileSystemProvider.class.getDeclaredField("installedProviders");
    		field.setAccessible(true);
    		ServiceLoader<FileSystemProvider> sl = ServiceLoader.load(FileSystemProvider.class, servletContext.getClassLoader());
    		Iterator<FileSystemProvider> iterator = sl.iterator();
    		while(iterator.hasNext())
    		{
    			FileSystemProvider fileSystemProvider = iterator.next();
    			if(!list.contains(fileSystemProvider))
    			{
    				list.add(fileSystemProvider);
    			}
    		}
    		field.set(null, Collections.unmodifiableList(list));
    		field.setAccessible(false);
    	}catch(Exception e)
    	{
    		throw new ServletException("FileSystemProvider 갱신 오류", e);
    	}
    }
}