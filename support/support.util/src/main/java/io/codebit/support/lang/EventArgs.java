package io.codebit.support.lang;

import java.util.EventObject;

public class EventArgs extends EventObject
{
	private static final long serialVersionUID = 8507459719181219142L;
	
	public static final EventArgs Empty = new EventArgs();  

	public EventArgs()
	{
		super(new Object());
	}
	
	public EventArgs(Object source)
	{
		super(source);
	}
}
