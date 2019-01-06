package io.codebit.support.lang.extensions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import static java.util.Objects.*;

/**
 * The Class String class를 확장하는 확장메서드 입니다.
 * 
 * 초성중성종성 처리 참고
 * http://blog.kangwoo.kr/28
 */
public abstract class StringExtension 
{
	// public static final String Empty = "";

	private final static Pattern LTRIM = Pattern.compile("^\\s+");
	private final static Pattern RTRIM = Pattern.compile("\\s+$");
	private final static Pattern NON_DIGITS = Pattern.compile("\\D");

	/**
	 * Right trim.
	 * 
	 * @param src
	 *            원본문자열
	 * @param trim
	 *            오른쪽에서 제거할 문자열
	 * @return 오른쪽에서 trim문자를 제거한 문자열
	 */
	public static String rtrim(String src, CharSequence trim)
	{
		if (src == null)
			return null;
		if(trim == null)
			return src;
		String quote = Pattern.quote(trim.toString());
		Pattern LTRIM = Pattern.compile("("+quote+")+$");
		return LTRIM.matcher(src).replaceAll("");
	}
	
	/**
	 * Right trim.
	 *
	 * @param src 원본 문자열
	 * @param trim 제거할 문자
	 * @return the 왼쪽에서 trim문자를 제거한 문자열
	 */
	public static String ltrim(String src, CharSequence trim)
	{
		if (src == null)
			return null;
		if(trim == null)
			return src;
		String quote = Pattern.quote(trim.toString());
		Pattern LTRIM = Pattern.compile("^("+quote+")+");
		return LTRIM.matcher(src).replaceAll("");
	}

	/**
	 * Right trim
	 * 
	 * @param src
	 *            원본 문자열
	 * @return 오른쪽 공백을 제거한 문자열
	 */
	public static String rtrim(String src)
	{
		return RTRIM.matcher(src).replaceAll("");
	}

	public static String ltrim(String src)
	{
		return LTRIM.matcher(src).replaceAll("");
	}
	
	/**
	 * src에서 숫자문자만을 반환합니다.
	 *
	 * @param src the src
	 * @return the string
	 */
	public static String extractDigits(String src)
	{
		Matcher matcher = NON_DIGITS.matcher(src);
		src = matcher.replaceAll("");
		return src;
	}
	
	
	/**
	 * 매개변수 src의 첫문자를 대문자로 변환
	 * 
	 * @param src
	 *            the src
	 * @return the string
	 */
	public static String toCapitalize(String src)
	{
		if (src == null || src.length() == 0)
			return src;
		return Character.toUpperCase(src.charAt(0)) + src.substring(1);
	}

//	/**
//	 * @param src
//	 *            원본 문자열
//	 * @return 해당 문자열이 null값인지 여부 반환
//	 */
//	public static boolean isNull(String src)
//	{
//		return (src == null);
//	}

	/**
	 * @param src
	 *            원본 문자열
	 * @return 원본 문자열이 null 이거나 공백문자열이면 true 그렇지 않으면 false
	 */
	public static boolean isNullOrEmpty(String src)
	{
		return isNull(src) || src.isEmpty();
	}
	
	public static boolean isBlank(String src)
	{
		return src.replaceAll("\\p{Space}", "").length() == 0;
	}

	public static boolean isNullOrBlank(String src)
	{
		return isNull(src) || isBlank(src);
	}
	
	/**
	 * 지정된 문자열에 있는 하나 이상의 형식 항목을 지정된 개체의 문자열 표현으로 바꿉니다. String.format(src,
	 * objects) 함수를 호출합니다. 추가적으로 {} 문자를 문자 형식으로 인식하여 문자열로 대체합니다.
	 * 
	 * @param src
	 *            형식 패턴이 포함된 문자열
	 * @param objects
	 *            형식 패턴을 대체할 개체입니다.
	 * @return 포멧팅된 문자열
	 */
	public static String formats(String src, Object... objects)
	{
		src = src.replaceAll("\\{\\}", "%s");
		return String.format(src, objects);
	}

	public static boolean isNumeric(String str)
	{
		return str.matches("-?\\d+(\\.\\d+)?"); // match a number with optional
												// '-' and decimal.
	}

	public static String ellipsis(String text, int length)
	{
		String ellipsisString = "...";
		String outputString = text;

		if (text.length() > 0 && length > 0)
		{
			if (text.length() > length)
			{
				outputString = text.substring(0, length);
				outputString += ellipsisString;
			}
		}
		return outputString;
	}
	
	/**
	 * 문자열 압축
	 *
	 * @param src the src
	 * @return the byte[]
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static byte[] compress(String src) throws IOException 
	{
		byte[] dataByte = src.getBytes("UTF-8");
		Deflater deflater = new Deflater();
		deflater.setLevel(Deflater.BEST_COMPRESSION);
		deflater.setInput(dataByte);
		deflater.finish();
		
		try(ByteArrayOutputStream bao = new ByteArrayOutputStream(dataByte.length))
		{
			byte[] buf = new byte[1024];
			while(!deflater.finished()) 
			{
				int compByte = deflater.deflate(buf);
				bao.write(buf, 0, compByte);
			}
			deflater.end();
			byte[] bs = bao.toByteArray();
			return bs;
		}
	}
	
	/**
	 * 문자열 압축 해제
	 *
	 * @param data the data
	 * @return the byte[]
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws DataFormatException the data format exception
	 */
	public static byte[] decompress(byte[] data) throws IOException, DataFormatException 
	{
 		Inflater inflater = new Inflater();
		inflater.setInput(data);
		
		try(ByteArrayOutputStream bao = new ByteArrayOutputStream())
		{
			byte[] buf = new byte[1024];
			while(!inflater.finished()) 
			{
				int compByte = inflater.inflate(buf);			
				bao.write(buf, 0, compByte);
			}
			inflater.end();
			return bao.toByteArray();
		}
	}
	
	/**
	 * ByteExtension.toBase64String 으로 이동
	 *
	 * @param data the data
	 * @return the string
	 */
	@Deprecated
	public static String toBase64Encode(byte[] data)
	{
		Encoder encoder = Base64.getEncoder();
		return encoder.encodeToString(data);
	}
	
	public static String toBase64Encode(String data)
	{
		Encoder encoder = Base64.getEncoder();
		return encoder.encodeToString(data.getBytes());
	}
	
	public static byte[] toBase64Decode(String base64)
	{
		Decoder decoder = Base64.getDecoder();
		return decoder.decode(base64);
	}
	
	public static String repeat(String string, int times) 
	{
	    StringBuilder out = new StringBuilder();
	    while (times-- > 0) 
	    {
	        out.append(string);
	    }
	    return out.toString();
	}
	
	/**
	 * 주어진 파라미터 len 배열 만큼 글자를 각각 자른 후 List형태로 반환합니다.
	 *
	 * @param string 원본 문자열
	 * @param len 문자열을 자를 길이의 배열
	 * @return 생성된 문자열 리스트
	 */
	public static List<String> splitLength(String string, int... len) 
	{
		List<String> list = new ArrayList<String>(len.length);
		int length = string.length();
		int start = 0;
		for (int i = 0; i < len.length; i++)
		{
			int end = start + len[i];
			
			if(end > length)
				end  = length;
			list.add(string.substring(start, end));
			start += end;
			if(start > length)
				break;
		}
	    return list;
	}
	
	/**
	 * 주어진 파라미터 len 배열 마다 파라미터 glue를 삽입한 문자열을 생성합니다.
	 *
	 * @param string 원본문자열
	 * @param glue 구분문자
	 * @param len 길이 배열
	 * @return 생성된 문자열
	 */
	public static String splitJoin(String string, CharSequence glue, int... len)
	{
		return join(splitLength(string, len), glue);
	}
	
	public static String join(String[] strings, CharSequence glue)
	{
		return join(strings, glue, 0, strings.length -1);
	}
	
	public static String join(String[] strings, CharSequence glue, int start)
	{
		return join(strings, glue, start, strings.length -1);
	}
	
	public static String join(String[] strings, CharSequence glue, int start, int end)
	{
		StringBuffer rt = new StringBuffer();
		if(end > strings.length - 1)
			end = strings.length - 1;
		for (int i = start; i < end; i++)
		{
			rt.append(strings[i].toString());
			rt.append(glue);
		}
		rt.append(strings[end]);
		return rt.toString();
	}
	
	public static String join(List<String> strings, CharSequence glue)
	{
		return join(strings, glue, 0, strings.size() -1);
	}
	
	public static String join(List<String> strings, CharSequence glue, int start)
	{
		return join(strings, glue, start, strings.size() -1);
	}
	
	public static String join(List<String> strings, CharSequence glue, int start, int end)
	{
		StringBuffer rt = new StringBuffer();
		if(end > strings.size() - 1)
			end = strings.size() - 1;
		for (int i = start; i < end; i++)
		{
			rt.append(strings.get(i).toString());
			rt.append(glue);
		}
		rt.append(strings.get(end));
		return rt.toString();
	}
	
//	public static String josa(String str) 
//	{
//	    String josa = "이가은는을를과와";
//	    char[] cJosa = josa.toCharArray();
//	    String pattern = "(.)\\{([" + josa + "])\\}";
//	    Pattern regex = Pattern.compile(pattern, Pattern.UNICODE_CHARACTER_CLASS | Pattern.MULTILINE );
//	    Matcher matcher = regex.matcher(str);
//	    while(matcher.find())
//	    {
//	    	String pp1 = matcher.group(1);
//	    	String pp2 = matcher.group(2);
//	    	int locate = josa.indexOf(pp2);
//	    	char pp = ((locate % 2) > 0)?
//	    			cJosa[locate]: cJosa[locate + 1];
//	    	System.out.println(pp);
//	    }
//	    return str;
//	    return preg_replace_callback(
//	        "/(.)\\{([{$josa}])\\}/u",
//	        function($matches) use($josa) {
//	            list($_, $last, $pp) = $matches;
//
//	            $pp1 = $pp2 = $pp;
//	            $idx = mb_strpos($josa, $pp);
//	            ($idx % 2) ? ($pp1 = mb_substr($josa,--$idx,1)) : ($pp2 = mb_substr($josa,++$idx,1));
//
//	            if (strlen($last) > 1) {
//	                $code = (hexdec(bin2hex($last)) - 12) % 28;
//	            } else {
//	                $code = (strpos('2459', $last) > -1) ? 0 : 1;
//	            }
//
//	            return $last.($code ? $pp1 : $pp2);
//	        },
//	        $str
//	    );
//	}
	/*
	 * public class ParseResult<ParseType> { private ParseType value; private
	 * boolean result;
	 * 
	 * public ParseResult(boolean result) {
	 * 
	 * }
	 * 
	 * public boolean result() { return result; }
	 * 
	 * public ParseType value() { return value; } }
	 */
}
