package io.codebit.support.spring.mvc.bind.annotation;

import java.lang.annotation.Annotation;
import java.util.List;


import org.springframework.core.MethodParameter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.AbstractMessageConverterMethodArgumentResolver;

public class RequestPatternArgumentResolver extends AbstractMessageConverterMethodArgumentResolver /* implements WebArgumentResolver*/
{
	public RequestPatternArgumentResolver(List<HttpMessageConverter<?>> messageConverters)
	{
		super(messageConverters);
	}

	/*@Override
	public Object resolveArgument(MethodParameter methodParameter, NativeWebRequest webRequest) throws Exception
	{
		Annotation[] paramAnns = methodParameter.getParameterAnnotations();

		Class<?> paramType = methodParameter.getParameterType();

		for (Annotation paramAnn : paramAnns)
		{
			if (RequestPattern.class.isInstance(paramAnn))
			{
				RequestPattern reqAttr = (RequestPattern) paramAnn;
				HttpServletRequest httprequest = (HttpServletRequest) webRequest.getNativeRequest();
				Object result = httprequest.getAttribute(reqAttr.value());

				if (result == null)
					result = reqAttr.defaultValue();

				if (result == null && reqAttr.required())
					raiseMissingParameterException(reqAttr.value(), paramType);
				else
					return result;
			}
		}

		return WebArgumentResolver.UNRESOLVED;
	}*/

	protected void raiseMissingParameterException(String paramName, Class<?> paramType) throws Exception
	{
		throw new IllegalStateException("Missing parameter '" + paramName + "' of type [" + paramType.getName() + "]");
	}

	@Override
	public boolean supportsParameter(MethodParameter parameter)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
		NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception
	{
		// TODO Auto-generated method stub
		return null;
	}
}
