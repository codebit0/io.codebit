package io.codebit.support.net;

import java.net.InetAddress;

public class InetAddressCompare implements Comparable<InetAddressCompare>
{
	private final InetAddress address;

	public InetAddressCompare(InetAddress address)
	{
		this.address = address;
	}

	@Override
	public int compareTo(InetAddressCompare ip)
	{
		long right = toLong(this.address);
		long left = toLong(ip.toInetAddress());
		if(right == left) return 0;
		else if(right > left) return 1;
		return -1;
	}
	
	public InetAddress toInetAddress()
	{
		return address;
	}
	
	public static long toLong(InetAddress ip) 
	{
        byte[] octets = ip.getAddress();
        long result = 0;
        if(octets.length == 4)
        {
            for (byte octet : octets) {
                result <<= 8;
                result |= octet & 0xff;
            }
            return result;
        }
        //ip v6
        throw new UnsupportedOperationException("unsupport ipv6");
    }

	public int compareTo(InetAddress addr)
	{
		return this.compareTo(new InetAddressCompare(addr));
	}
}
