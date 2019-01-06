package io.codebit.support.system;

//https://github.com/mosa/Mono-Class-Libraries/blob/master/mcs/class/corlib/System/ArgumentOutOfRangeException.cs
//http://referencesource.microsoft.com/#mscorlib/system/argumentoutofrangeexception.cs
public class ArgumentOutOfRangeException extends ArgumentException
{
	private static final long serialVersionUID = 8718969647532507719L;

	private static final String RangeMessage = "Argument is out of range.";

	private Object actualValue;

	public ArgumentOutOfRangeException()
	{
		super(RangeMessage);
	}

	/**
	 * @param paramName 파라미터 명
	 */
	public ArgumentOutOfRangeException(String paramName)
	{
		super(RangeMessage, paramName);
	}

	/**
	 * @param throwable 에러
	 */
	public ArgumentOutOfRangeException(Throwable throwable)
	{
		super(null, throwable);
	}

	public ArgumentOutOfRangeException(String paramName, String message)
	{
		super(message, paramName);
		// HResult = Result;
	}

	public ArgumentOutOfRangeException(String paramName, Object actualValue, String message)
	{
		super(message, paramName);
		// HResult = Result;
		this.actualValue = actualValue;
	}

	/**
	 * @param message
	 *            the detail message
	 * @param innerException
	 *            the cause
	 */
	public ArgumentOutOfRangeException(String message, Throwable innerException)
	{
		super(message, innerException);
	}

	public Object ActualValue()
	{
		return actualValue;
	}

	@Override
	public String getMessage()
	{
		String basemsg = super.getMessage();
		if (actualValue == null)
			return basemsg;
		return basemsg + System.getProperty("line.separator") + actualValue;
	}
}
