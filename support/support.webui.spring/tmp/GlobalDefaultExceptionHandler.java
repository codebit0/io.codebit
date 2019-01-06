package in.java.support.spring.webui;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

//http://stackoverflow.com/questions/22157687/spring-mvc-rest-handing-bad-url-404-by-returning-json
//http://blog.codeleak.pl/2013/11/controlleradvice-improvements-in-spring.html
@ControllerAdvice
@Slf4j
public class GlobalDefaultExceptionHandler
{

	public static final String DEFAULT_ERROR_VIEW = "error";

	// @Inject
	// private MessageSource messageSource;

	// http://magicmonster.com/kb/prg/java/spring/webmvc/jackson_custom.html
	// http://stackoverflow.com/questions/3591291/spring-jackson-and-customization-e-g-customdeserializer

	// public static final String DEFAULT_ERROR_VIEW = "error";

	@ExceptionHandler(value = { Throwable.class, Exception.class })
	public String defaultErrorHandler(HttpServletRequest req, HttpServletResponse res, Exception ex) throws Exception
	{
		// AnnotationUtils.findAnnotation(ex.getClass(), ResponseStatus.class);
		ResponseStatus status = ex.getClass().getAnnotation(ResponseStatus.class);
		String message = null;
		if (status != null)
		{
			res.setStatus(status.value().value());
			message = status.reason();
		} else
		{
			// status code가 없으면 500 internal server error처리
			res.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		// status.reason();
		String hostName = _HostName();
		res.setHeader("Server", hostName);
		if (message == null)
		{
			message = ex.getLocalizedMessage();
		}
		res.addHeader("Message", message);
		String uuid = UUID.randomUUID().toString();
		res.addHeader("Key", uuid);
		res.addHeader("Exception", ex.getClass().getName());

		// Locale locale = LocaleContextHolder.getLocale();
		// String errorMessage = messageSource.getMessage("error.bad.url", null,
		// locale);

		String errorURL = req.getRequestURL().toString();
		// String helpUrl;

		// log.error("{} {}", uuid, hostName, hostName, hostName);
		log.error("{}, {}, {}, {}, {}", uuid, message, hostName, errorURL, ex);

		// ErrorInfo error = new ErrorInfo(message, 1000, errorURL, "");
		return DEFAULT_ERROR_VIEW;
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