package io.codebit.support.spring.mvc;

import java.util.ResourceBundle;

import io.codebit.support.util.ResourceBundle.Control.InheritBaseResourceBundleControl;


public abstract class Config
{
	private static String BUNDLE_NAME = "io.codebit.support.spring.mvc.config"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME, new InheritBaseResourceBundleControl());

	private Config()
	{
	}
	
	public static String get(String key)
	{
		return RESOURCE_BUNDLE.getString(key);
	}
	
	public static String getOrDefault(String key, String defaultValue)
	{
		try
		{
			return RESOURCE_BUNDLE.getString(key);
		}catch(Exception e)
		{
			return defaultValue;
		}
	}
}
