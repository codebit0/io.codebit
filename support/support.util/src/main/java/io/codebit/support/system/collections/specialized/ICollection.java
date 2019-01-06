package io.codebit.support.system.collections.specialized;

import java.util.Iterator;

public interface ICollection<T>
{
	int count ();
    String getKey (int index);
    T get (int index);
    T get (String key);
    Iterator<T> iter ();
}
