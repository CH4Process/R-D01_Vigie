package com.ch4process.utils;

import java.util.Calendar;
import javax.swing.JOptionPane;

public class CH4P_Functions
{
	public static final int LOG_inConsole = 1;
	public static final int LOG_inFile = 2;
	public static final int LOG_inDatabase = 3;
	public static final int LOG_inMail = 4;
	public static final int LOG_inMsgBox = 5;
	
	public static final int LEVEL_INFO = 1;
	public static final int LEVEL_WARNING = 2;
	public static final int LEVEL_ERROR = 3;
	
	
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
	
	public static void Log(String source, Integer method, Integer level, String message)
	{
		String display = source + " :: " + level + " [ " + Calendar.getInstance().getTime().toString() + " ] -- " + message;
		
		switch (method)
		{
			case LOG_inConsole:
				System.out.println(display);
				break;
				
			case LOG_inFile:
				//Log(LOG_inConsole, level, message);
				break;
				
			case LOG_inDatabase:
				//Log(LOG_inConsole, level, message);
				break;
				
			case LOG_inMail:
				//Log(LOG_inConsole, level, message);
				break;
				
			case LOG_inMsgBox:
				JOptionPane.showMessageDialog(null, display, "CH4Process - Vigie", JOptionPane.WARNING_MESSAGE);
				break;
				
		}
	}
}
