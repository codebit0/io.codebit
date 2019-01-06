package io.codebit.support.spring.webui.exception;

import jdk.nashorn.internal.objects.annotations.Setter;
import org.springframework.http.HttpStatus;

/**
 * The Class AbstractApiException. 200 성공 400 Bad Request - field validation 실패시
 * 401 Unauthorized - API 인증,인가 실패 404 Not found HttpStatus.Gone : 401 Gone 은
 * 요청된 리소스를 더 이상 사용할 수 없는 경우에 표시됩니다. HttpStatus.SERVICE_UNAVAILABLE : 503
 * ServiceUnavailable 은 일반적으로 로드가 많거나 유지 관리 문제 때문에 일시적으로 서버를 사용할 수 없는 경우에 표시됩니다.
 * 500 Internal Server Error - 서버 에러
 * 
 * ResponseStatus annotaion 를 사용하시면 안됩니다.
 */
public abstract class AbstractWebUIException extends RuntimeException
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	/**
     * 
     */
	private int code;

	public HttpStatus getHttpStatus() {
		return httpStatus;
	}

	public void setHttpStatus(HttpStatus httpStatus) {
		this.httpStatus = httpStatus;
	}

	private HttpStatus httpStatus;

	public String getViewName() {
		return viewName;
	}

	public void setViewName(String viewName) {
		this.viewName = viewName;
	}

	private String viewName = null;

	public AbstractWebUIException(int code)
	{
		super();
		this.code = code;
	}

	public AbstractWebUIException(String message, int code)
	{
		this(message, code, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	/**
	 * @param message 사용자 정의 메시지
	 * @param code 사용자 정의 에러 코드
	 * @param httpStatus
	 *            200 성공 
	 *            400 Bad Request - field validation 실패시 
	 *            401 Unauthorized - API 인증,인가 실패 
	 *            404 Not found 
	 *            HttpStatus.Gone : 401 Gone 은 요청된 리소스를 더 이상 사용할 수 없는 경우에 표시됩니다. 
	 *            HttpStatus.SERVICE_UNAVAILABLE : 503 ServiceUnavailable 은 일반적으로 로드가 많거나 유지 관리 문제 때문에 일시적으로 서버를
	 *            사용할 수 없는 경우에 표시됩니다. 
	 *            500 Internal Server Error - 서버 에러
	 */
	public AbstractWebUIException(String message, int code, HttpStatus httpStatus)
	{
		super(message);
		this.code = code;
		this.httpStatus = httpStatus;
	}

	public AbstractWebUIException(String message, int code, Throwable cause)
	{
		this(message, code, HttpStatus.INTERNAL_SERVER_ERROR, cause);
	}

	/**
	 * @param message 사용자 정의 메시지
	 * @param code 사용자 정의 에러 코드
	 * @param httpStatus
	 *            200 성공 
	 *            400 Bad Request - field validation 실패시 
	 *            401 Unauthorized - API 인증,인가 실패 
	 *            404 Not found 
	 *            HttpStatus.Gone : 401 Gone 은 요청된 리소스를 더 이상 사용할 수 없는 경우에 표시됩니다. 
	 *            HttpStatus.SERVICE_UNAVAILABLE : 503 ServiceUnavailable 은 일반적으로 로드가 많거나 유지 관리 문제 때문에 일시적으로 서버를
	 *            사용할 수 없는 경우에 표시됩니다. 
	 *            500 Internal Server Error - 서버 에러
	 * @param cause 예외
	 */
	public AbstractWebUIException(String message, int code, HttpStatus httpStatus, Throwable cause)
	{
		super(message, cause);
		this.code = code;
		this.httpStatus = httpStatus;
	}
}
