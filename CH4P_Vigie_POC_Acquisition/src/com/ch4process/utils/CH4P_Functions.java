package com.ch4process.utils;

public class CH4P_Functions
{
	public static int boolToInt(boolean value)
	{
		return value ? 1:0;
	}
	
	public static Boolean StringToBool(String s)
	{
		return s.equals("1") ? true : false;
	}
	
	public static final String getHomePath()
	{
		return System.getProperty("user.home");
	}
}
