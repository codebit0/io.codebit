package io.codebit.support.spring.webui;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.codebit.support.spring.webui.exception.AbstractWebUIException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
//@Slf4j
public class GlobalDefaultErrorController
{

	@RequestMapping("/error")
	public String error(HttpServletRequest request, HttpServletResponse response, Model model)
	{
		String errorView = "error";
		// retrieve some useful information from the request
		int code = Integer.MIN_VALUE;
		Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
		Throwable throwable = (Throwable) request.getAttribute("javax.servlet.error.exception");
		// String servletName = (String)
		// request.getAttribute("javax.servlet.error.servlet_name");
		String exceptionMessage = getExceptionMessage(throwable, statusCode);
		if(throwable instanceof AbstractWebUIException)
		{
			AbstractWebUIException webUIException = ((AbstractWebUIException)throwable);
			code = webUIException.getCode();
			HttpStatus httpStatus = webUIException.getHttpStatus();
			statusCode = httpStatus.value();
			response.setStatus(statusCode);
			if(webUIException.getViewName() !=  null)
			{
				errorView = webUIException.getViewName();  
			}
		}

		String requestUri = (String) request.getAttribute("javax.servlet.error.request_uri");
		if (requestUri == null)
		{
			requestUri = "Unknown";
		}
		//익스플로러 때문..
		if(statusCode == 404)
		{
			response.setStatus(200);
		}
		String hostName = _HostName();
		String message = MessageFormat.format("{0} returned for {1} with message {2}",	statusCode, requestUri, exceptionMessage);
		String uuid = UUID.randomUUID().toString();

		//FIXME LOG
//		log.error("[{}], {}, {}, {}, {}", uuid, message, hostName, requestUri, throwable);
		
		model.addAttribute("Key", uuid);
		model.addAttribute("Message", message);
		model.addAttribute("Url", requestUri);
		model.addAttribute("Code", code);
		
		return errorView;
	}

	private String getExceptionMessage(Throwable throwable, Integer statusCode)
	{
		if (throwable != null)
		{
			//return Throwable.getRootCause(throwable).getMessage();
			return throwable.getLocalizedMessage();
		}
		HttpStatus httpStatus = HttpStatus.valueOf(statusCode);
		return httpStatus.getReasonPhrase();
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
