package io.codebit.support.lang;

import java.util.EventListener;
import java.util.EventObject;

@FunctionalInterface
public interface IEventHandler<TEventArgs extends EventObject> extends EventListener
{
	void handler(Object sender, TEventArgs e);
}
