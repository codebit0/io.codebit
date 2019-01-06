package io.codebit.support.util;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

import io.codebit.support.util.ResourceBundle.Control.InheritBaseResourceBundleControl;


public class MessageResource
{
	protected static HashMap<String, SoftReference<MessageResource>> CACHE = new HashMap<String, SoftReference<MessageResource>>();
	protected static InheritBaseResourceBundleControl CONTROL = new InheritBaseResourceBundleControl();
	
	protected ResourceBundle RESOURCE_BUNDLE;

	public static MessageResource of(String bundle)
	{
		MessageResource messages = null;
		if(CACHE.containsKey(bundle))
		{
			SoftReference<MessageResource> reference = CACHE.get(bundle);
			if(reference == null)
			{
				messages = new MessageResource(bundle);
				CACHE.put(bundle, new SoftReference<MessageResource>(messages));
			}
		}else
		{
			messages = new MessageResource(bundle);
			CACHE.put(bundle, new SoftReference<MessageResource>(messages));
		}
		return messages;
	}
	
	MessageResource(String bundle)
	{
//		this.BUNDLE_NAME = bundle;
		RESOURCE_BUNDLE = ResourceBundle.getBundle(bundle, Locale.getDefault(),
                Thread.currentThread().getContextClassLoader(), CONTROL);
	}

	public String get(String key)
	{
		return RESOURCE_BUNDLE.getString(key);
	}
	
	public String getOrDefault(String key, String defaultValue)
	{
		try
		{
			return this.get(key);
		}catch(Exception e)
		{
			return defaultValue;
		}
	}
}
