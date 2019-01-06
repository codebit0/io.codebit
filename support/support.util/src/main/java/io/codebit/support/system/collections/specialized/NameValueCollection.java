package io.codebit.support.system.collections.specialized;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.codebit.support.system.collections.generic.KeyValuePair;

public class NameValueCollection /* implements Collection<String> */
{
	private static final long serialVersionUID = 1L;
	private static final String COMMA = ",";
	private Map<String, List< String>> value;
	private List<KeyValuePair<String, String>> values;

	public static class NameValue extends KeyValuePair<String, String> implements Serializable
	{
		private static final long serialVersionUID = -1183495821549080000L;

		public NameValue(String key, String value)
		{
			super(key, value);
		}
	}

	public NameValueCollection()
	{
		super();
	}

	public NameValueCollection(int cap)
	{
	}

	public NameValueCollection(Map<? extends String, ? extends List<String>> col)
	{
		values = new ArrayList<KeyValuePair<String, String>>();
	}

	public NameValueCollection(NameValueCollection col)
	{

	}

	
	
	/*
	 * public String get(int index) { List<String> oValues = super.get(index);
	 * if (oValues == null) { oValues = new ArrayList<String>();
	 * super.put(index, oValues); } oValues.add(index); }
	 * 
	 * public String get(String key) { List<String> oValues = getValues(key); if
	 * (oValues == null) return null; StringBuffer oBuf = new StringBuffer();
	 * for (int i = 0; i < oValues.size(); i++) { oBuf.append(oValues.get(i));
	 * if ((i+1) < oValues.size()) oBuf.append(COMMA); } return oBuf.toString();
	 * }
	 */

	/*
	 * public void add(String key, String value) { List<String> oValues =
	 * super.get(key); if (oValues == null) { oValues = new ArrayList<String>();
	 * super.put(key, oValues); } oValues.add(value); }
	 */

	private static String GetAsOneString(List<String> list)
	{
		int n = (list != null) ? list.size() : 0;

		if (n == 1)
		{
			return (String) list.get(0);
		} else if (n > 1)
		{
			StringBuilder s = new StringBuilder((String) list.get(0));

			for (int i = 1; i < n; i++)
			{
				s.append(',');
				s.append((String) list.get(i));
			}

			return s.toString();
		} else
		{
			return null;
		}
	}
	
	private static String[] GetAsStringArray(List<String> list)
    {
        int n = (list != null) ? list.size() : 0;
        if (n == 0)
            return null;

        String[] array = new String[n];
        list.toArray(array);
        return array;
    }
}
