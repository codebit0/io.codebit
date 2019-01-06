package io.codebit.support.extensions;

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class DebugExtension
{
	public static void printDebug(Object src, PrintStream ps)
	{
		if (ps == null)
			ps = System.out;
		synchronized (src)
		{
			ps.println("debug print in");
			ps.println("time:"
					+ LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
			StackTraceElement stackTraceElement = new Throwable()
					.getStackTrace()[0];
			ps.println(stackTraceElement.getClassName() + "."
					+ stackTraceElement.getMethodName() + "() line "
					+ stackTraceElement.getLineNumber());
			ps.println("debug print out");
		}
	}
	
	public static void printDebug(Object src)
	{
		printDebug(src, null);
	}
	
	public static void showMemory(PrintStream ps) 
	{
		if (ps == null)
			ps = System.out;
		Runtime r = Runtime.getRuntime();
        DecimalFormat format = new DecimalFormat("###,###,###.##");

        //JVM이 현재 시스템에 요구 가능한 최대 메모리량, 이 값을 넘으면 OutOfMemory 오류가 발생 합니다.               
        long max = r.maxMemory();

        //JVM이 현재 시스템에 얻어 쓴 메모리의 총량
        long total = r.totalMemory();

        //JVM이 현재 시스템에 청구하여 사용중인 최대 메모리(total)중에서 사용 가능한 메모리
        long free = r.freeMemory();

        ps.println("Max:" + format.format(max) + ", Total:" + format.format(total) + ", Free:"+format.format(free));          
	}
}
