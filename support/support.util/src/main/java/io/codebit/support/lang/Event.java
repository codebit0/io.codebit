package io.codebit.support.lang;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

public final class Event<TEventArgs extends EventObject> 
	implements AutoCloseable 
{
	public static final EventObject EmptyArgs = new EventObject(Void.class);
	
	private List<IEventHandler<TEventArgs>> handlers =  new ArrayList<IEventHandler<TEventArgs>>();
	//protected EventListenerList list = new EventListenerList();
	
	public void onEvent(Object sender, TEventArgs args)
	{
		/*int size = handlers.size();
		for(int i=0; i < size; i++)
		{
			try
			{
				if(handlers.get(i) != null)
				{
					handlers.get(i).handlerEvent(sender, args);
				}
			}catch(Exception e)
			{
				
			}
		}*/
		for(IEventHandler<TEventArgs> handler: handlers)
		{
			if(handler != null)
				handler.handler(sender, args);
		}
	}
	
	//@SuppressWarnings({ "unchecked", "rawtypes" })
	public void add(IEventHandler<TEventArgs> handler)
	{
		this.handlers.add(handler);
	}
	
	public void remove(IEventHandler<TEventArgs> handler)
	{
		this.handlers.remove(handler);
	}
	
	public void clear()
	{
		this.handlers.clear();
	}
	
	public int size()
	{
		return this.handlers.size();
	}

	@Override
	public void close() throws Exception
	{
		close(true);
	}
	
	private boolean _disposed;
    private void close(boolean disposing)
    {
        if( _disposed ) return;
        _disposed = true;
        if (disposing)
        {
        	this.handlers.clear();
        }
    }
    
    public void finalize() 
    {
    	close(false);
    }
}
