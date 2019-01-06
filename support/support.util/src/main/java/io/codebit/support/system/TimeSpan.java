package io.codebit.support.system;

/**
 * 
 * @author C#
 */
public class TimeSpan
{

	public static final long TicksPerMillisecond = 10000;
	private static final double MillisecondsPerTick = 1.0 / TicksPerMillisecond;

	public static final long TicksPerSecond = TicksPerMillisecond * 1000;
	private static final double SecondsPerTick = 1.0 / TicksPerSecond;

	public static final long TicksPerMinute = TicksPerSecond * 60;
	private static final double MinutesPerTick = 1.0 / TicksPerMinute;

	public static final long TicksPerHour = TicksPerMinute * 60;
	private static final double HoursPerTick = 1.0 / TicksPerHour;

	public static final long TicksPerDay = TicksPerHour * 24;
	private static final double DaysPerTick = 1.0 / TicksPerDay;

	private static final int MillisPerSecond = 1000;
	private static final int MillisPerMinute = MillisPerSecond * 60;
	private static final int MillisPerHour = MillisPerMinute * 60;
	private static final int MillisPerDay = MillisPerHour * 24;

	static final long MaxSeconds = Long.MAX_VALUE / TicksPerSecond;
	static long MinSeconds = Long.MIN_VALUE / TicksPerSecond;

	static long MaxMilliSeconds = Long.MAX_VALUE / TicksPerMillisecond;
	static long MinMilliSeconds = Long.MIN_VALUE / TicksPerMillisecond;

	final long TicksPerTenthSecond = TicksPerMillisecond * 100;

	public static final TimeSpan Zero = new TimeSpan(0);

	public static final TimeSpan MaxValue = new TimeSpan(Long.MAX_VALUE);
	public static final TimeSpan MinValue = new TimeSpan(Long.MIN_VALUE);

	long _ticks;

	// public TimeSpan() {
	// _ticks = 0;
	// }

	public TimeSpan(long ticks)
	{
		this._ticks = ticks;
	}

	public TimeSpan(int hours, int minutes, int seconds)
	{
		// _ticks = TimeToTicks(hours, minutes, seconds);
		this(0, hours, minutes, seconds, 0);
	}

	public TimeSpan(int days, int hours, int minutes, int seconds)
	{
		// this._days = d;
		// this._hours = h;
		// this._minutes = m;
		// this._seconds = s;
		this(days, hours, minutes, seconds, 0);
	}

	public TimeSpan(int days, int hours, int minutes, int seconds, int milliseconds)
	{
		long totalMilliSeconds = ((long) days * 3600 * 24 + (long) hours * 3600 + (long) minutes * 60 + seconds)
				* 1000 + milliseconds;
		if (totalMilliSeconds > MaxMilliSeconds || totalMilliSeconds < MinMilliSeconds)
			throw new ArgumentOutOfRangeException(null, "Overflow_TimeSpanTooLong");
		_ticks = totalMilliSeconds * TicksPerMillisecond;
	}

	static long TimeToTicks(int hour, int minute, int second)
	{
		// totalSeconds is bounded by 2^31 * 2^12 + 2^31 * 2^8 + 2^31,
		// which is less than 2^44, meaning we won't overflow totalSeconds.
		long totalSeconds = (long) hour * 3600 + (long) minute * 60 + second;
		if (totalSeconds > MaxSeconds || totalSeconds < MinSeconds)
			throw new ArgumentOutOfRangeException("Overflow_TimeSpanTooLong");
		return totalSeconds * TicksPerSecond;
	}

	public long Ticks()
	{
		return _ticks;
	}

	public int Days()
	{
		return (int) (_ticks / TicksPerDay);
	}

	public int Hours()
	{
		return (int) ((_ticks / TicksPerHour) % 24);
	}

	public int Minutes()
	{
		return (int) ((_ticks / TicksPerMinute) % 60);
	}

	public int Seconds()
	{
		return (int) ((_ticks / TicksPerSecond) % 60);
	}

	public int Milliseconds()
	{
		return (int) ((_ticks / TicksPerMillisecond) % 1000);
	}

	public double TotalDays()
	{
		return (_ticks) * DaysPerTick;
	}

	public double TotalHours()
	{
		return _ticks * HoursPerTick;
	}

	public double TotalMinutes()
	{
		return _ticks * MinutesPerTick;
	}

	public double TotalSeconds()
	{
		return _ticks * SecondsPerTick;
	}

	public double TotalMilliseconds()
	{
		double temp = _ticks * MillisecondsPerTick;
		if (temp > MaxMilliSeconds)
			return MaxMilliSeconds;

		if (temp < MinMilliSeconds)
			return MinMilliSeconds;

		return temp;
	}

	// public TimeSpan Duration()
	// {
	// if (Ticks == TimeSpan.MinValue.Ticks)
	// throw new OverflowException(Environment.GetResourceString("Overflow_Duration"));
	// Contract.EndContractBlock();
	// return new TimeSpan(_ticks >= 0 ? _ticks : -_ticks);
	// }

	public TimeSpan add(TimeSpan ts)
	{
		long result = _ticks + ts._ticks;
		// Overflow if signs of operands was identical and result's
		// sign was opposite.
		// >> 63 gives the sign bit (either 64 1's or 64 0's).
		// if ((_ticks >> 63 == ts._ticks >> 63) && (_ticks >> 63 != result >> 63))
		// throw new OverflowException("Overflow_TimeSpanTooLong");
		return new TimeSpan(result);

		/*
		 * int s = this.Seconds() + ts.Seconds();
		 * int m = this.Minutes() + ts.Minutes();
		 * int h = this.Hours() + ts.Hours();
		 * int d = this.Days() + ts.Days();
		 * 
		 * if (s > 59)
		 * {
		 * s -= 60;
		 * m += 1;
		 * }
		 * if (m > 59)
		 * {
		 * m -= 60;
		 * h += 1;
		 * }
		 * if (h > 23)
		 * {
		 * h -= 24;
		 * d += 1;
		 * }
		 * 
		 * return new TimeSpan2(d, h, m, s);
		 */

	}

	public TimeSpan subtract(TimeSpan ts)
	{
		long result = _ticks - ts._ticks;
		// Overflow if signs of operands was different and result's
		// sign was opposite from the first argument's sign.
		// >> 63 gives the sign bit (either 64 1's or 64 0's).
		// if ((_ticks >> 63 != ts._ticks >> 63) && (_ticks >> 63 != result >> 63))
		// throw new OverflowException(Environment.GetResourceString("Overflow_TimeSpanTooLong"));
		return new TimeSpan(result);

		/*
		 * int s1 = this.Seconds();
		 * int m1 = this.Minutes() * 60;
		 * int h1 = this.Hours() * 60 * 60;
		 * int d1 = this.Days() * 24 * 60 * 60;
		 * 
		 * int s2 = ts.Seconds();
		 * int m2 = ts.Minutes() * 60;
		 * int h2 = ts.Hours() * 60 * 60;
		 * int d2 = ts.Days() * 24 * 60 * 60;
		 * 
		 * int sd = (s1 + m1 + h1 + d1) - (s2 + m2 + h2 + d2);
		 * 
		 * int d = sd / (24 * 60 * 60);
		 * sd -= (d * (24 * 60 * 60));
		 * int h = sd / (60 * 60);
		 * sd -= (h * (60 * 60));
		 * int m = sd / 60;
		 * int s = sd - (m * 60);
		 * 
		 * return new TimeSpan2(d, h, m, s);
		 */
	}

	public int totalHours()
	{
		return (this.Days() * 24) + this.Hours();
	}

	public int totalMinutes()
	{
		return (((this.Days() * 24) + this.Hours()) * 60) + this.Minutes();
	}

	public int totalSeconds()
	{
		return (((((this.Days() * 24) + this.Hours()) * 60) + this.Minutes()) * 60) + this.Seconds();
	}

	public static TimeSpan fromTicks(long value)
	{
		return new TimeSpan(value);
	}

	public static TimeSpan fromDays(double value)
	{
		return interval(value, MillisPerDay);
	}

	public static TimeSpan fromHours(double value)
	{
		return interval(value, MillisPerHour);
	}

	public static TimeSpan fromMinutes(double value)
	{
		return interval(value, MillisPerMinute);
	}

	public static TimeSpan fromSeconds(double value)
	{
		return interval(value, MillisPerSecond);
	}

	public static TimeSpan fromMilliseconds(double value)
	{
		return interval(value, 1);
	}

	private static TimeSpan interval(double value, int scale)
	{
		// if (Double.IsNaN(value))
		// throw new ArgumentException("Arg_CannotBeNaN");
		// Contract.EndContractBlock();
		double tmp = value * scale;
		double millis = tmp + (value >= 0 ? 0.5 : -0.5);
		// if ((millis > long.MaxValue / TicksPerMillisecond) || (millis < Int64.MinValue / TicksPerMillisecond))
		// throw new OverflowException(Environment.GetResourceString("Overflow_TimeSpanTooLong"));
		return new TimeSpan((long) millis * TicksPerMillisecond);
	}

	/**
	 * 해당 값이 현재 TimeSpan 개체의 절대 값인 새 TimeSpan 개체를 반환합니다.
	 * 
	 * @return the time span
	 */
	public TimeSpan duration()
	{
		// if (Ticks == TimeSpan.MinValue.Ticks)
		// throw new OverflowException(Environment.GetResourceString("Overflow_Duration"));
		// Contract.EndContractBlock();
		return new TimeSpan(_ticks >= 0 ? _ticks : -_ticks);
	}

	/**
	 * 이 인스턴스의 부정 값을 값으로 가지는 새 TimeSpan 개체를 반환합니다.
	 * 
	 * @return the time span
	 */
	public TimeSpan negate()
	{
		// if (Ticks == TimeSpan.MinValue.Ticks)
		// throw new OverflowException(Environment.GetResourceString("Overflow_NegateTwosCompNum"));
		// Contract.EndContractBlock();
		return new TimeSpan(-_ticks);
	}

	// public static TimeSpan Parse(String s)
	// {
	// /* Constructs a TimeSpan from a string. Leading and trailing white space characters are allowed. */
	// return TimeSpanParse.Parse(s, null);
	// }
	//
	// public static TimeSpan Parse(String input, IFormatProvider formatProvider)
	// {
	// return TimeSpanParse.Parse(input, formatProvider);
	// }
	//
	// public static TimeSpan ParseExact(String input, String format, IFormatProvider formatProvider)
	// {
	// return TimeSpanParse.ParseExact(input, format, formatProvider, TimeSpanStyles.None);
	// }
	//
	// public static TimeSpan ParseExact(String input, String[] formats, IFormatProvider formatProvider)
	// {
	// return TimeSpanParse.ParseExactMultiple(input, formats, formatProvider, TimeSpanStyles.None);
	// }
	//
	// public static TimeSpan ParseExact(String input, String format, IFormatProvider formatProvider, TimeSpanStyles
	// styles)
	// {
	// TimeSpanParse.ValidateStyles(styles, "styles");
	// return TimeSpanParse.ParseExact(input, format, formatProvider, styles);
	// }
	//
	// public static TimeSpan ParseExact(String input, String[] formats, IFormatProvider formatProvider, TimeSpanStyles
	// styles)
	// {
	// TimeSpanParse.ValidateStyles(styles, "styles");
	// return TimeSpanParse.ParseExactMultiple(input, formats, formatProvider, styles);
	// }
	//
	// public static Boolean TryParse(String s, out TimeSpan result)
	// {
	// return TimeSpanParse.TryParse(s, null, out result);
	// }
	//
	// public static Boolean TryParse(String input, IFormatProvider formatProvider, out TimeSpan result)
	// {
	// return TimeSpanParse.TryParse(input, formatProvider, out result);
	// }
	//
	// public static Boolean TryParseExact(String input, String format, IFormatProvider formatProvider, out TimeSpan
	// result) {
	// return TimeSpanParse.TryParseExact(input, format, formatProvider, TimeSpanStyles.None, out result);
	// }
	// public static Boolean TryParseExact(String input, String[] formats, IFormatProvider formatProvider, out TimeSpan
	// result) {
	// return TimeSpanParse.TryParseExactMultiple(input, formats, formatProvider, TimeSpanStyles.None, out result);
	// }
	// public static Boolean TryParseExact(String input, String format, IFormatProvider formatProvider, TimeSpanStyles
	// styles, out TimeSpan result) {
	// TimeSpanParse.ValidateStyles(styles, "styles");
	// return TimeSpanParse.TryParseExact(input, format, formatProvider, styles, out result);
	// }
	// public static Boolean TryParseExact(String input, String[] formats, IFormatProvider formatProvider,
	// TimeSpanStyles styles, out TimeSpan result) {
	// TimeSpanParse.ValidateStyles(styles, "styles");
	// return TimeSpanParse.TryParseExactMultiple(input, formats, formatProvider, styles, out result);
	// }
	// public override String ToString() {
	// return TimeSpanFormat.Format(this, null, null);
	// }
	// public String ToString(String format) {
	// return TimeSpanFormat.Format(this, format, null);
	// }
	// public String ToString(String format, IFormatProvider formatProvider) {
	// if (LegacyMode) {
	// return TimeSpanFormat.Format(this, null, null);
	// }
	// else {
	// return TimeSpanFormat.Format(this, format, formatProvider);
	// }
	// }

	public boolean equals(TimeSpan value)
	{
		if (value instanceof TimeSpan)
		{
			return _ticks == value._ticks;
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return (int) _ticks ^ (int) (_ticks >> 32);
	}

	@Override
	public String toString()
	{
		if (this.Days() != 0)
		{
			return String.format("%d:%02d:%02d:%02d", this.Days(), this.Hours(), this.Minutes(), this.Seconds());
		}
		else
		{
			return String.format("%02d:%02d:%02d", this.Hours(), this.Minutes(), this.Seconds());
		}
	}
}