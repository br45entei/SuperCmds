package com.gmail.br45entei.supercmds.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/** @author Brian_Entei */
public class CodeUtils {
	public static String limitStringToNumOfChars(String str, int limit) {
		return(str != null ? (str.length() >= 1 ? (str.substring(0, (str.length() >= limit ? limit : str.length()))) : "") : "");
	}
	
	public static String[] removeEmptyStrings(String[] data) {
		ArrayList<String> result = new ArrayList<>();
		for(int i = 0; i < data.length; i++) {
			if(!data[i].equals("")) {
				result.add(data[i]);
			}
		}
		String[] res = new String[result.size()];
		result.toArray(res);
		return res;
	}
	
	public static boolean isStrAValidDouble(String s) {
		boolean successful = true;
		try {
			Double.parseDouble(s);
		} catch(NumberFormatException | NullPointerException e) {
			successful = false;
		}
		return successful;
	}
	
	public static boolean isStrAValidFloat(String s) {
		boolean successful = true;
		try {
			Float.parseFloat(s);
		} catch(NumberFormatException | NullPointerException e) {
			successful = false;
		}
		return successful;
	}
	
	public static boolean isStrAValidLong(String s) {
		boolean successful = true;
		try {
			Long.parseLong(s);
		} catch(NumberFormatException | NullPointerException e) {
			successful = false;
		}
		return successful;
	}
	
	public static boolean isStrAValidInt(String s) {
		boolean successful = true;
		try {
			Integer.parseInt(s);
		} catch(NumberFormatException | NullPointerException e) {
			successful = false;
		}
		return successful;
	}
	
	public static boolean isStrAValidShort(String s) {
		boolean successful = true;
		try {
			Short.parseShort(s);
		} catch(NumberFormatException | NullPointerException e) {
			successful = false;
		}
		return successful;
	}
	
	public static boolean isStrAValidByte(String s) {
		boolean successful = true;
		try {
			Byte.parseByte(s);
		} catch(NumberFormatException | NullPointerException e) {
			successful = false;
		}
		return successful;
	}
	
	public static double getDoubleFromStr(String s, double fallBackValue) {
		return CodeUtils.isStrAValidDouble(s) ? Double.parseDouble(s) : fallBackValue;
	}
	
	public static float getFloatFromStr(String s, float fallBackValue) {
		return CodeUtils.isStrAValidFloat(s) ? Float.parseFloat(s) : fallBackValue;
	}
	
	public static long getLongFromStr(String s, long fallBackValue) {
		return CodeUtils.isStrAValidLong(s) ? Long.parseLong(s) : fallBackValue;
	}
	
	public static int getIntFromStr(String s, int fallBackValue) {
		return CodeUtils.isStrAValidInt(s) ? Integer.parseInt(s) : fallBackValue;
	}
	
	public static short getShortFromStr(String s, short fallBackValue) {
		return CodeUtils.isStrAValidShort(s) ? Short.parseShort(s) : fallBackValue;
	}
	
	public static int[] toIntArray(Integer[] data) {
		int[] result = new int[data.length];
		for(int i = 0; i < data.length; i++) {
			result[i] = data[i].intValue();
		}
		return result;
	}
	
	/** @param getTimeOnly Whether or not time should be included but not date as
	 *            well
	 * @param fileSystemSafe Whether or not the returned string will be used in
	 *            the making of a folder or file
	 * @return The resulting string */
	public static String getSystemTime(boolean getTimeOnly, boolean fileSystemSafe, boolean getDateOnly) {
		String timeAndDate = "";
		DateFormat dateFormat;
		if(!getTimeOnly && !getDateOnly) {
			dateFormat = new SimpleDateFormat(fileSystemSafe ? "MM-dd-yyyy_HH.mm.ss" : "MM/dd/yyyy_HH:mm:ss");
		} else if(getTimeOnly) {
			dateFormat = new SimpleDateFormat(fileSystemSafe ? "HH.mm.ss" : "HH:mm:ss");
		} else if(getDateOnly) {
			dateFormat = new SimpleDateFormat(fileSystemSafe ? "MM-dd-yyyy" : "MM/dd/yyyy");
		} else {
			dateFormat = new SimpleDateFormat(fileSystemSafe ? "HH.mm.ss" : "HH:mm:ss");
		}
		Date date = new Date();
		timeAndDate = dateFormat.format(date);
		return timeAndDate;
	}
	
	public static boolean isJvm64bit() {
		String[] astring = new String[] {"sun.arch.data.model", "com.ibm.vm.bitmode", "os.arch"};
		String[] astring1 = astring;
		int i = astring.length;
		for(int j = 0; j < i; ++j) {
			String s = astring1[j];
			String s1 = System.getProperty(s);
			if(s1 != null && s1.contains("64")) {
				return true;
			}
		}
		return false;
	}
	
	public static EnumOS getOSType() {
		String s = System.getProperty("os.name").toLowerCase();
		return s.contains("win") ? EnumOS.WINDOWS : (s.contains("mac") ? EnumOS.OSX : (s.contains("solaris") ? EnumOS.SOLARIS : (s.contains("sunos") ? EnumOS.SOLARIS : (s.contains("linux") ? EnumOS.LINUX : (s.contains("unix") ? EnumOS.LINUX : EnumOS.UNKNOWN)))));
	}
	
	public static enum EnumOS {
		LINUX, SOLARIS, WINDOWS, OSX, UNKNOWN;
	}
	
}
