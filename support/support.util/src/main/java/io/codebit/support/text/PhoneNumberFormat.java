package io.codebit.support.text;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
//import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.codebit.support.lang.extensions.StringExtension;

public class PhoneNumberFormat extends Format
{
	private static final long serialVersionUID = -3561525398038677547L;

	@Override
	public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos)
	{
		String source = (String) obj;
		source  = StringExtension.extractDigits(source);
		
		String pattern = "(^02.{0}|^01.{1}|[0-9]{3})([0-9]+)([0-9]{4})";
		Pattern regex = Pattern.compile(pattern);
		Matcher matcher = regex.matcher(source);
		if(matcher.find())
		{
			String group1 = matcher.group(1);
			toAppendTo.append(group1);
			String spliter = "-";
			toAppendTo.append(spliter);
			String group2 = matcher.group(2);
			toAppendTo.append(group2);
			toAppendTo.append(spliter);
			String group3 = matcher.group(3);
			toAppendTo.append(group3);
		}
		
		return toAppendTo;
	}

	@Override
	public String parseObject(String source, ParsePosition pos)
	{
		if(pos != null && pos.getIndex() > 0)
			source = source.substring(pos.getIndex());
		Pattern regex = Pattern.compile("\\D");
		Matcher matcher = regex.matcher(source);
		
		source = matcher.replaceAll("");
		return source;
	}
}
