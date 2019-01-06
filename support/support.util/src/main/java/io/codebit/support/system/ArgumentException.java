package io.codebit.support.system;

public class ArgumentException extends IllegalArgumentException
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -9041563309371237897L;

	private String param_name;

	// Constructors
	public ArgumentException()
	{
		this("Value does not fall within the expected range.");
		// HResult = Result;
	}

	public ArgumentException(String message)
	{
		super(message);
		// HResult = Result;
	}

	public ArgumentException(String message, Throwable innerException)
	{
		super(message, innerException);
		// HResult = Result;
	}

	public ArgumentException(String message, String paramName)
	{
		super(message);
		this.param_name = paramName;
		// HResult = Result;
	}

	public ArgumentException(String message, String paramName, Throwable innerException)
	{
		super(message, innerException);
		this.param_name = paramName;
		// HResult = Result;
	}

	/*
	 * protected ArgumentException(SerializationInfo info, StreamingContext context)
	 * {
	 * 
	 * param_name = info.GetString("ParamName");
	 * }
	 */

	// Properties
	public String ParamName()
	{
		return param_name;
	}

	@Override
	public String getMessage()
	{
		if (ParamName() != null && ParamName().length() != 0)
			return super.getMessage() + System.getProperty("line.separator")
					+ "Parameter name: "
					+ ParamName();
		return super.getMessage();
	}

	/*
	 * public void getObjectData(SerializationInfo info, StreamingContext context)
	 * {
	 * base.GetObjectData(info, context);
	 * info.AddValue("ParamName", ParamName);
	 * }
	 */

}
