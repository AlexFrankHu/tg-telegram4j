package com.tg.telegram4j.utils;

import java.security.MessageDigest;
import java.util.Arrays;

@SuppressWarnings("unused")
public class MD5 implements java.io.Serializable {

	private final static String[] hexDigits = {"0", "1", "2", "3", "4", "5", "6", "7",
			"8", "9", "a", "b", "c", "d", "e", "f"};
	private static final long serialVersionUID = 1L;
//	private static Logger log = Logger.getLogger(MD5.class);

	private static String to32BitString(String plainText, boolean is32or16, String charset) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			if(hasText(charset))
				md.update(plainText.getBytes(charset));
			else{
				md.update(plainText.getBytes());
			}
			byte[] b = md.digest();
			String buf= toHexAscii(b,"");
			if(is32or16){
				return buf;
			}else{
				return  buf.substring(8, 24);
			}

		} catch (Exception e) {
//			log.error("", e);
		}
		return "";
	}
	/**
	 * 产生32位md5加密字符串
	 * @param s
	 * @return
	 */
	public final static String MD5generator(String s) {
		//String charset =System.getProperties()
		return MD5.to32BitString(s, true,"");
	}
	/**
	 * 产生32位md5加密字符串
	 * @param s
	 * @return
	 */
	public final static String MD5generator(String s, String charset) {
		return MD5.to32BitString(s, true,charset);
	}
	/**
	 * 产生16为md5加密字符串
	 * @param s
	 * @return
	 */
	public final static String MD5generator16Bit(String s, String charset) {
		return MD5.to32BitString(s, false,charset);
	}
	public final static String MD5generator16Bit(String s) {
		return MD5.to32BitString(s, false,"");
	}
	public final static String MD5generator16Bit(byte[] bs){
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(bs);
			byte[] b = md.digest();
			String buf= toHexAscii(b,"");
			return  buf.substring(8, 24);
		} catch (Exception e) {
//			log.error("", e);
		}
		return "";
	}

	private static String paddingToFixedString(String src, char c, int fixLen, boolean isHead) {
		String s = src;
		if (s.length() > fixLen) {
			int begin = s.length() - fixLen;
			s = s.substring(begin);
			return s;
		} else if (s.length() == fixLen) {
			return src;
		} else {
			return fill(src, c, fixLen - src.length(), isHead);
		}
	}

	private static String fill(String src, char c, int num, boolean ishead) {
		StringBuilder sb = new StringBuilder(src);
		char[] cs = new char[num];
		Arrays.fill(cs, c);
		if (ishead)
			sb.insert(0, cs);
		else {
			sb.append(cs);
		}
		return sb.toString();
	}

	private static String toHexAscii(byte b) {
		String s= Integer.toHexString(b);
		return paddingToFixedString(s, '0', 2, true);
	}

	private static String toHexAscii(byte[] bs, String c) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bs.length; i++) {
			sb.append(toHexAscii(bs[i]));
			if(i<bs.length-1)
				sb.append(c);
		}
		return sb.toString();
	}

	private static boolean hasLength(String str) {
		return (str != null && str.length() > 0);
	}

	private static boolean hasText(String str) {
		if (!hasLength(str)) {
			return false;
		}
		int strLen = str.length();
		for (int i = 0; i < strLen; i++) {
			if (!Character.isWhitespace(str.charAt(i))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 转换byte到16进制
	 * @param b 要转换的byte
	 * @return 16进制格式
	 */
	private static String byteToHexString(byte b) {
		int n = b;
		if (n < 0) {
			n = 256 + n;
		}
		int d1 = n / 16;
		int d2 = n % 16;
		return hexDigits[d1] + hexDigits[d2];
	}

	/**
	 * 转换字节数组为16进制字串
	 * @param b 字节数组
	 * @return 16进制字串
	 */
	public static String byteArrayToHexString(byte[] b) {
		StringBuilder resultSb = new StringBuilder();
		for (byte aB : b) {
			resultSb.append(byteToHexString(aB));
		}
		return resultSb.toString();
	}

	/**
	 * MD5编码
	 * @param origin 原始字符串
	 * @return 经过MD5加密之后的结果
	 */
	public static String MD5Encode(String origin) {
		String resultString = null;
		try {
			resultString = origin;
			MessageDigest md = MessageDigest.getInstance("MD5");
			resultString = byteArrayToHexString(md.digest(resultString.getBytes()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultString;
	}

	public static void main(String args[]) {
		MD5 m = new MD5();
		String ss ="46:17:0C:99:DC:92:90:BA:D5:F3:CD:F6:C1:30:D8:42:5D:93:6D:77";
		//String s1=MD5.toString(ss);
		String s2 =MD5.MD5generator(ss);
		System.out.println(s2);
	}
}
