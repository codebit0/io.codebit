package io.codebit.support.system.collections.specialized;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.codebit.support.system.collections.generic.KeyValuePair;


public abstract class NameObjectCollectionBase<V> 
{
	
	class NameObjectEntry<V> extends KeyValuePair<String, V> implements Serializable
	{
		private static final long serialVersionUID = -1183495821549080000L;

		public NameObjectEntry(String key, V value)
		{
			super(key, value);
		}
	}
	
	
	private List<String> _entriesArray = new ArrayList();
	private List<NameObjectEntry<V>> values;
	//private Map<String, ? extends List<V>> values;
	
	public NameObjectCollectionBase()
	{
		super();
		values = new ArrayList<NameObjectEntry<V>>();
		_entriesArray = new ArrayList<String>();
	}

	public NameObjectCollectionBase(int cap)
	{
		values = new ArrayList<NameObjectEntry<V>>(cap);
		_entriesArray = new ArrayList<String>(cap);
	}

	/*public NameObjectCollectionBase(Map<String, ? extends List<V>> col)
	{
		values = col;
	}*/

	public String getKey(int index)
	{
		return _entriesArray.get(index);
	}
	
	public void add(String name, V value) 
	{
		if (name != null) 
		{
//            if (values.g[name] == null)
//                _entriesTable.Add(name, entry);
        }
        // add entry to the list
        //_entriesArray.Add(entry);
	}
	
	
	
	public void remove(String key) 
	{
	}
	
	public void set(String key, V value)
	{
		
	}
	
	public void HasKeys()
	{
		
	}
	
	private void reset() 
	{
        _entriesArray = new ArrayList<String>();
        values = new ArrayList<NameObjectEntry<V>>();
    }

    private void reset(int capacity) 
    {
    	_entriesArray = new ArrayList<String>(capacity);
        values = new ArrayList<NameObjectEntry<V>>(capacity);
    }
}
