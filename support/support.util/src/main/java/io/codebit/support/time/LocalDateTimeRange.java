package io.codebit.support.time;

import java.time.Duration;
import java.time.LocalDateTime;

import io.codebit.support.data.Range;

public class LocalDateTimeRange extends Range<LocalDateTime>
{
	
	public static LocalDateTimeRange of(LocalDateTime minimum, LocalDateTime maximum)
	{
		return new LocalDateTimeRange(minimum, maximum);
	}
	
	public LocalDateTimeRange(LocalDateTime minimum, LocalDateTime maximum)
	{
		super(minimum, maximum);
	}
	
	public Duration Duration()
	{
		return Duration.between(this.Minimum(), this.Maximum());
	}
}
