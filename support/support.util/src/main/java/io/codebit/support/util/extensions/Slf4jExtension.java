package io.codebit.support.util.extensions;

import java.util.function.Supplier;

import org.slf4j.Marker;

public class Slf4jExtension
{
	public static void trace(org.slf4j.Logger logger, Supplier<String> message) 
	{
	    if (logger.isTraceEnabled())
	    	logger.trace(message.get());
	}
	
	public static void trace(org.slf4j.Logger logger, Supplier<String> message, Throwable throwable) 
	{
	    if (logger.isTraceEnabled())
	    	logger.trace(message.get(), throwable);
	}
	
	public static void trace(org.slf4j.Logger logger, Marker marker, Supplier<String> message) 
	{
	    if (logger.isTraceEnabled(marker))
	    	logger.trace(marker, message.get());
	}
	
	public static void trace(org.slf4j.Logger logger, Marker marker, Supplier<String> message, Throwable throwable) 
	{
	    if (logger.isTraceEnabled(marker))
	    	logger.trace(marker, message.get(), throwable);
	}
	
	public static void info(org.slf4j.Logger logger, String message, Object ... args ) 
	{
	    if (logger.isInfoEnabled())
	    	logger.info(message, args);
	}
	
	public static void info(org.slf4j.Logger logger, Supplier<String> message) 
	{
	    if (logger.isInfoEnabled())
	    	logger.info(message.get());
	}
	
	public static void info(org.slf4j.Logger logger, Supplier<String> message, Throwable throwable) 
	{
	    if (logger.isInfoEnabled())
	    	logger.info(message.get(), throwable);
	}
	
	public static void info(org.slf4j.Logger logger, Marker marker, Supplier<String> message) 
	{
	    if (logger.isInfoEnabled(marker))
	    	logger.info(marker, message.get());
	}
	
	public static void info(org.slf4j.Logger logger, Marker marker, Supplier<String> message, Throwable throwable) 
	{
	    if (logger.isInfoEnabled(marker))
	    	logger.info(marker, message.get(), throwable);
	}
	
	
	public static void warn(org.slf4j.Logger logger, Supplier<String> message) 
	{
	    if (logger.isWarnEnabled())
	    	logger.warn(message.get());
	}
	
	public static void warn(org.slf4j.Logger logger, Supplier<String> message, Throwable throwable) 
	{
	    if (logger.isWarnEnabled())
	    	logger.warn(message.get(), throwable);
	}
	
	public static void warn(org.slf4j.Logger logger, Marker marker, Supplier<String> message) 
	{
	    if (logger.isWarnEnabled(marker))
	    	logger.warn(marker, message.get());
	}
	
	public static void warn(org.slf4j.Logger logger, Marker marker, Supplier<String> message, Throwable throwable) 
	{
	    if (logger.isWarnEnabled(marker))
	    	logger.warn(marker, message.get(), throwable);
	}
	
	public static void debug(org.slf4j.Logger logger, String message, Object ... args ) 
	{
	    if (logger.isDebugEnabled())
	    	logger.debug(message, args);
	}
	
	public static void debug(org.slf4j.Logger logger, Supplier<String> message) 
	{
	    if (logger.isDebugEnabled())
	    	logger.debug(message.get());
	}
	
	public static void debug(org.slf4j.Logger logger, Supplier<String> message, Throwable throwable) 
	{
	    if (logger.isDebugEnabled())
	    	logger.debug(message.get(), throwable);
	}
	
	public static void debug(org.slf4j.Logger logger, Marker marker, Supplier<String> message) 
	{
	    if (logger.isDebugEnabled(marker))
	    	logger.debug(marker, message.get());
	}
	
	public static void debug(org.slf4j.Logger logger, Marker marker, Supplier<String> message, Throwable throwable) 
	{
	    if (logger.isDebugEnabled(marker))
	    	logger.debug(marker, message.get(), throwable);
	}
	
	public static void error(org.slf4j.Logger logger, String message, Object ... args ) 
	{
	    if (logger.isErrorEnabled())
	    	logger.error(message, args);
	}
	
	public static void error(org.slf4j.Logger logger, Supplier<String> message) 
	{
	    if (logger.isErrorEnabled())
	    	logger.error(message.get());
	}
	
	public static void error(org.slf4j.Logger logger, Supplier<String> message, Throwable throwable) 
	{
	    if (logger.isErrorEnabled())
	    	logger.error(message.get(), throwable);
	}
	
	public static void error(org.slf4j.Logger logger, Marker marker, Supplier<String> message) 
	{
	    if (logger.isErrorEnabled(marker))
	    	logger.error(marker, message.get());
	}
	
	public static void error(org.slf4j.Logger logger, Marker marker, Supplier<String> message, Throwable throwable) 
	{
	    if (logger.isErrorEnabled(marker))
	    	logger.error(marker, message.get(), throwable);
	}
}
