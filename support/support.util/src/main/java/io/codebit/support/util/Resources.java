package io.codebit.support.util;


public class Resources extends MessageResource
{
	public static Resources of(String bundle)
	{
		return (Resources) MessageResource.of(bundle);
	}
	
	Resources(String bundle)
	{
		super(bundle);
	}
	
	public Object getObject(String key)
	{
		return RESOURCE_BUNDLE.getObject(key);
	}
}