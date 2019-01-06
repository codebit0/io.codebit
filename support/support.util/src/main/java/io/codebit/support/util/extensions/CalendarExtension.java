package io.codebit.support.util.extensions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CalendarExtension
{

	/**
	 * To calendar.
	 * 
	 * @param dateString
	 *            the date string
	 * @param format
	 *            the format : simpledateformat http://entireboy.egloos.com/4152244
	 * @return the calendar
	 * @throws ParseException
	 *             the parse exception
	 */
	public static Calendar toCalendar(String dateString, String format) throws ParseException
	{
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat dt = new SimpleDateFormat(format);
		calendar.setTime(dt.parse(dateString));
		return calendar;
	}

	/**
	 * "yyyy-MM-dd HH:mm:ss.SSS" format To daCalendarte.
	 * 
	 * @param dateString
	 *            the date string
	 * @return the date
	 * @throws ParseException
	 *             the parse exception
	 */
	public static Calendar toCalendar(String dateString) throws ParseException
	{
		return CalendarExtension.toCalendar(dateString, "yyyy-MM-dd HH:mm:ss.SSS");
	}
}
