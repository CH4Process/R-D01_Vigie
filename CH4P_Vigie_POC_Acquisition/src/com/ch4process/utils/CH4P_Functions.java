package com.ch4process.utils;

import java.util.Calendar;

public class CH4P_Functions
{
	public static final int LOG_inConsole = 1;
	public static final int LOG_inFile = 2;
	public static final int LOG_inDatabase = 3;
	public static final int LOG_inMail = 4;
	
	
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
	
	public static void Log(Integer method, Integer level, String message)
	{
		String date = Calendar.getInstance().getTime().toString();
		
		switch (method)
		{
			case LOG_inConsole:
				System.out.println(level + " - " + date + " - " + message);
				break;
				
			case LOG_inFile:
				Log(LOG_inConsole, level, message);
				break;
				
			case LOG_inDatabase:
				Log(LOG_inConsole, level, message);
				break;
				
			case LOG_inMail:
				Log(LOG_inConsole, level, message);
				break;
		}
	}
}
