package io.codebit.support.mybatis;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Config
{
	private static final String BUNDLE_NAME = "mybatishelper-config"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	private Config()
	{
	}

	public static String getString(String key)
	{
		try
		{
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e)
		{
			return '!' + key + '!';
		}
	}
}