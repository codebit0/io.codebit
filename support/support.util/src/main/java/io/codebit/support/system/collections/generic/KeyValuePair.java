package io.codebit.support.system.collections.generic;

import java.util.Map;

public class KeyValuePair<TKey,TValue> implements Map.Entry<TKey, TValue>
{
	public static <TKey,TValue> KeyValuePair<TKey,TValue> of(TKey key, TValue value)
	{
		return new KeyValuePair<TKey,TValue>(key, value);
	}
	
	public KeyValuePair(TKey key, TValue value)
	{
		this.key = key;
		this.setValue(value);
	}
	
	private TKey key;
	
	private TValue value;

	@Override
	public TValue setValue(TValue value)
	{
		TValue old = this.value;
		this.value = value;
		return old;
	}

	@Override
	public TKey getKey() {
		return key;
	}

	@Override
	public TValue getValue() {
		return this.value;
	}
}
