package io.codebit.support.spring.webui.exception;

import org.springframework.http.HttpStatus;

//@ResponseStatus annotaion 를 사용하시면 안됩니다. 
public class CheckServiceException extends AbstractWebUIException
{
	/**
     * 
     */
	private static final long serialVersionUID = 8986333546037085367L;

	private static int code = Integer.MAX_VALUE;

	public CheckServiceException(String message)
	{
		super(message, code, HttpStatus.SERVICE_UNAVAILABLE);
		this.setViewName("check-service");
	}
}
