package io.codebit.support.api.spring;

import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;

import io.codebit.support.api.spring.exceptions.AbstractApiException;

//http://stackoverflow.com/questions/22157687/spring-mvc-rest-handing-bad-url-404-by-returning-json
//http://blog.codeleak.pl/2013/11/controlleradvice-improvements-in-spring.html
@ControllerAdvice
public class GlobalDefaultExceptionHandler
{
	// @Inject
	// private MessageSource messageSource;ss

	// http://magicmonster.com/kb/prg/java/spring/webmvc/jackson_custom.html
	// http://stackoverflow.com/questions/3591291/spring-jackson-and-customization-e-g-customdeserializer

	// public static final String DEFAULT_ERROR_VIEW = "error";

	//@ExceptionHandler(value = { Throwable.class, Exception.class, RuntimeException.class, NullPointerException.class, Error.class})
	@ExceptionHandler(value = Throwable.class)
	public static @ResponseBody String defaultErrorHandler(HttpServletRequest req, HttpServletResponse res, Throwable ex)
			throws Exception
	{
		String message = "";
		//unknown code
		int code = Integer.MIN_VALUE;

		// AbstractApiException 상속받아 구현한 Exception인 경우
		if (ex instanceof AbstractApiException)
		{
			AbstractApiException apiException = (AbstractApiException) ex;
			res.setStatus(apiException.getHttpStatus().value());
			code = apiException.getCode();
		}else if(ex instanceof NoHandlerFoundException)
		{
			res.setStatus(HttpStatus.NOT_FOUND.value());
		}else
		{
			// AnnotationUtils.findAnnotation(ex.getClass(), ResponseStatus.class);
			ResponseStatus status = ex.getClass().getAnnotation(ResponseStatus.class);
			if (status != null)
			{
				res.setStatus(status.value().value());
				message = status.reason();
			} else
			{
				// status code가 없으면 500 internal server error처리
				res.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			}
		}
		if (message == null || message.isEmpty())
		{
			message = ex.getLocalizedMessage();
		}
		if (message == null || message.isEmpty())
		{
			message = ex.getMessage();
		}
		if(message != null && !message.isEmpty())
		{
			message = URLEncoder.encode(message, "UTF-8");
		}else
		{
			message = ex.getClass().getSimpleName();
		}
		
		res.setContentType("text/plain;charset=UTF-8");
		//res.setHeader("Server", hostName);
		String errorURL = req.getRequestURL().toString();
		String uuid = UUID.randomUUID().toString();
		res.addHeader("X-Error-Key", uuid);
		res.addHeader("X-Error-Message", message);
		res.addHeader("X-Error-Url", errorURL);
		res.addIntHeader("X-Error-Code", code);
		//res.addHeader("Exception", ex.getClass().getName());

		String hostName = _HostName();

		// log.error("{} {}", uuid, hostName, hostName, hostName);
		//FIXME LOG
//		log.error("[{}], {}, {}, {}", uuid, message, hostName, errorURL, ex);

		//ErrorInfo error = new ErrorInfo(message, code, errorURL);
		//return error;
		return null;
	}

	private static String _HostName()
	{
		try
		{
			String result = InetAddress.getLocalHost().getHostName();
			// To get the Canonical host name
			// String canonicalHostName =
			// InetAddress.getLocalHost().getCanonicalHostName();
			if (result != null && result.trim() == "")
				return result;
		} catch (UnknownHostException e)
		{
			// failed;
		}

		// try environment properties.
		String host = System.getenv("COMPUTERNAME");
		if (host != null)
			return host;
		host = System.getenv("HOSTNAME");
		if (host != null)
			return host;
		return null;
	}
}