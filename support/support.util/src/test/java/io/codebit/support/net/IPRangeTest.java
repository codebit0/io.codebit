package io.codebit.support.net;

import io.codebit.support.net.InetAddressRange;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class IPRangeTest
{
	public static void main(String[] args) throws UnknownHostException
	{
		InetAddress minmum = InetAddress.getByName("192.168.0.0");
		InetAddress maximum = InetAddress.getByName("192.168.0.255");
		InetAddress test = InetAddress.getByName("192.168.0.19");
		InetAddressRange inetAddressRange = new InetAddressRange(minmum, maximum);
		//inetAddressRange.co
		System.out.println(inetAddressRange.contains(test));
	}
}
