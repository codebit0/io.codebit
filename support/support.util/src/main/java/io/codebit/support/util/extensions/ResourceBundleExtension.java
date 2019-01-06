package io.codebit.support.util.extensions;

import java.util.ResourceBundle;

public class ResourceBundleExtension
{
	public static String getString(ResourceBundle resourceBundle, String key, String defaultValue)
	{
		try
		{
			return resourceBundle.getString(key);
		}catch(Exception e)
		{
			return defaultValue;
		}
	}
}
