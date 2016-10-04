package com.ch4process.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.swing.JOptionPane;

import com.ch4process.events.ILogEventListener;
import com.ch4process.events.ILogExceptionEventListener;

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
	
	public static List<ILogEventListener> logListeners = new ArrayList<ILogEventListener>();
	public static List<ILogExceptionEventListener> logExceptionListeners = new ArrayList<ILogExceptionEventListener>();
	
	
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
		
		notifyLogEventListeners(display);
	}
	
	public static void LogException(Integer method, Exception ex)
	{
		java.io.StringWriter sw = new java.io.StringWriter();
		java.io.PrintWriter pw = new java.io.PrintWriter(sw);
		ex.printStackTrace(pw);
		
		String display = sw.toString();
		
		try
		{
			pw.close();
			sw.close();
		}
		catch (IOException e)
		{
			pw = null;
			sw = null;
		}
		
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
				JOptionPane.showMessageDialog(null, display, "CH4Process - Vigie", JOptionPane.ERROR_MESSAGE);
				break;
				
		}
		
		notifyLogExceptionEventListeners(display);
	}
	
	public static void addLogEventListener(ILogEventListener listener)
	{
		if (!logListeners.contains(listener))
		{
			logListeners.add(listener);
		}
	}
	
	public static void removeLogEventListener(ILogEventListener listener)
	{
		if (logListeners.contains(listener))
		{
			logListeners.remove(listener);
		}
	}
	
	public static void notifyLogEventListeners(String message)
	{
		for(ILogEventListener listener:logListeners)
		{
			listener.onLogEvent(message);
		}
	}
	
	public static void addLogExceptionEventListener(ILogExceptionEventListener listener)
	{
		if (!logExceptionListeners.contains(listener))
		{
			logExceptionListeners.add(listener);
		}
	}
	
	public static void removeLogExceptionEventListener(ILogExceptionEventListener listener)
	{
		if (logExceptionListeners.contains(listener))
		{
			logExceptionListeners.remove(listener);
		}
	}
	
	public static void notifyLogExceptionEventListeners(String message)
	{
		for(ILogExceptionEventListener listener:logExceptionListeners)
		{
			listener.onLogExceptionEvent(message);
		}
	}
}
