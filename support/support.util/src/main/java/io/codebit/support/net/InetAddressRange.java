package io.codebit.support.net;

import java.net.InetAddress;

import io.codebit.support.data.Range;


public class InetAddressRange extends Range<InetAddressCompare>
{
	public static InetAddressRange of(InetAddress minimum, InetAddress maximum)
	{
		return new InetAddressRange(minimum, maximum);
	}
	
	public InetAddressRange(InetAddress minimum, InetAddress maximum)
	{
		super(new InetAddressCompare(minimum), new InetAddressCompare(maximum));
	}
	
	public InetAddressRange(InetAddressCompare minimum, InetAddressCompare maximum)
	{
		super(minimum, maximum);
	}
	
	public boolean contains(InetAddress value)
	{
		InetAddressCompare address = new InetAddressCompare(value);
		// this.minimum 이 value 보다 작으면 -1 같으면 0
		return this.Minimum().compareTo(address) <= 0 && this.Maximum().compareTo(address) >= 0;
	}
}
