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
 * http://ko.wikipedia.org/wiki/HTTP_%EC%83%81%ED%83%9C_%EC%BD%94%EB%93%9C
 * http://www.coolcheck.co.kr/upload/http_scode.asp
 */
public class ApiException extends AbstractApiException
{
	private static final long serialVersionUID = -7823800670506884689L;

	public ApiException(String message)
	{
		super(message, HttpStatus.INTERNAL_SERVER_ERROR, null);
	}
	
	public ApiException(String message, Throwable cause)
	{
		super(message, HttpStatus.INTERNAL_SERVER_ERROR, cause);
	}
	
	public ApiException(String message, HttpStatus httpStatus)
	{
		super(message, httpStatus, null);
	}
	
	public ApiException(String message, HttpStatus httpStatus, int code)
	{
		super(message, httpStatus, null);
	}

	public ApiException(String message, HttpStatus httpStatus, Throwable cause)
	{
		super(message, httpStatus, cause);
	}

	public ApiException(String message, int code)
	{
		super(message, code, null);
	}
	
	public ApiException(String message, int code, Throwable cause)
	{
		super(message, code, cause);
	}
	
	public ApiException(String message, HttpStatus httpStatus, int code, Throwable cause)
	{
		super(message, httpStatus, code, cause);
	}
}
