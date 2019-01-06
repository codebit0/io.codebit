package io.codebit.support.util.extensions;

import io.codebit.support.system.TimeSpan;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public abstract class DateExtension
{

	public static String toString(Date date, String format)
	{
		SimpleDateFormat df = new SimpleDateFormat(format);
		return df.format(date);
	}
	
	/**
	 * Format to date.
	 * 
	 * @param dateString
	 *            the date string
	 * @param format
	 *            the format
	 * @return the date
	 * @throws ParseException
	 *             the parse exception
	 */
	public static Date toDate(String dateString, String format)
			throws ParseException
	{
		SimpleDateFormat dt = new SimpleDateFormat(format);
		return dt.parse(dateString);
	}

	/**
	 * "yyyy-MM-dd HH:mm:ss.SSS" format To date.
	 * 
	 * @param dateString
	 *            the date string
	 * @return the date
	 * @throws ParseException
	 *             the parse exception
	 */
	public static Date toDate(String dateString) throws ParseException
	{
		SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		return dt.parse(dateString);
	}

	public static TimeSpan duration(Date lapDate)
	{
		Calendar temp = Calendar.getInstance();
		// temp.add(Calendar.DAY_OF_MONTH, lapDate);
		int year = temp.get(Calendar.YEAR);
		int month = temp.get(Calendar.MONTH) + 1;
		int day = temp.get(Calendar.DAY_OF_MONTH);
		return new TimeSpan(year, month, day).duration();
	}
}
