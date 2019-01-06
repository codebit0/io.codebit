package io.codebit.support.api.spring.exceptions;

import org.springframework.http.HttpStatus;


/**
 * The Class AbstractApiException. 
 * 200 성공 
 * 400 Bad Request - field validation 실패시
 * 401 Unauthorized - API 인증,인가 실패 
 * 404 Not found 
 * 401 Gone 은 요청된 리소스를 더 이상 사용할 수 없는 경우에 표시됩니다.
 * 503 ServiceUnavailable 은 일반적으로 로드가 많거나 유지 관리 문제 때문에 일시적으로 서버를 사용할 수 없는 경우에 표시됩니다.
 * 500 Internal Server Error - 서버 에러
 */
public abstract class AbstractApiException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	private int code;
	
	private HttpStatus httpStatus;

	public AbstractApiException(HttpStatus httpStatus)
	{
		this("", httpStatus);
	}

	public AbstractApiException(int code)
	{
		this("", code);
	}
	
	public AbstractApiException(String message, HttpStatus httpStatus)
	{
		super(message);
		this.httpStatus = httpStatus;
	}
	
	public AbstractApiException(String message, HttpStatus httpStatus, int code)
	{
		this(message, httpStatus);
		this.code = code;
	}
	
	public AbstractApiException(String message, int code)
	{
		this(message, HttpStatus.INTERNAL_SERVER_ERROR, code);
	}
	
	public AbstractApiException(String message, int code, Throwable cause)
	{
		this(message, HttpStatus.INTERNAL_SERVER_ERROR, code, cause);
	}
	
	public AbstractApiException(String message, HttpStatus httpStatus, Throwable cause)
	{
		super(message, cause, false, false);
		this.httpStatus = httpStatus;
	}
	
	public AbstractApiException(String message, HttpStatus httpStatus, int code, Throwable cause)
	{
		this(message, httpStatus, cause);
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public HttpStatus getHttpStatus() {
		return httpStatus;
	}

	public void setHttpStatus(HttpStatus httpStatus) {
		this.httpStatus = httpStatus;
	}
}
