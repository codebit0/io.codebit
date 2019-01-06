package io.codebit.support.spring.mvc.i18n;

import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.servlet.LocaleContextResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.support.RequestContextUtils;

public class TimeZoneChangeInterceptor extends HandlerInterceptorAdapter {

	/**
	 * Default name of the locale specification parameter: "timezone".
	 */
	public static final String DEFAULT_PARAM_NAME = "timezone";

	private String paramName = DEFAULT_PARAM_NAME;


	/**
	 * Set the name of the parameter that contains a locale specification
	 * in a locale change request. Default is "locale".
	 *
	 * @param paramName the new param name
	 */
	public void setParamName(String paramName) {
		this.paramName = paramName;
	}

	/**
	 * Return the name of the parameter that contains a locale specification
	 * in a locale change request.
	 *
	 * @return the param name
	 */
	public String getParamName() {
		return this.paramName;
	}


	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws ServletException {

		String newTimezone = request.getParameter(this.paramName);
		//assert newTimezone != null;
//		if (newTimezone != null) 
//		{
//			LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
//			if (localeResolver instanceof LocaleContextResolver) 
//			{
//				LocaleContext localeContext = ((LocaleContextResolver) localeResolver).resolveLocaleContext(request);
//				//((LocaleContextResolver) localeResolver).
////				if (localeContext instanceof TimeZoneAwareLocaleContext) {
////					return ((TimeZoneAwareLocaleContext) localeContext).getTimeZone();
////				}
//				if(localeContext instanceof LocaleContextHolder)
//				{
//					((LocaleContextHolder)localeContext).setTimeZone(TimeZone.getTimeZone(newTimezone));
//				}
//			}
//		}
		// Proceed in any case.
		return true;
	}
}