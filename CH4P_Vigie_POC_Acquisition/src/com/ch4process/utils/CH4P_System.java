package com.ch4process.utils;

import java.nio.file.FileSystems;
import java.nio.file.Path;

public class CH4P_System
{	
	public static final String USER = "pi";
	public static final String PATH_Root = GetRootPath();
	public static final String PATH_Base = PATH_Root + "/CH4PROCESS";
	public static final String PATH_Vigie = PATH_Base + "/VIGIE";
	public static final String PATH_Config = PATH_Vigie + "/CONFIG";
	public static final String PATH_Config_Database = PATH_Config + "/database.properties";
	public static final String PATH_Config_Mail = PATH_Config + "/mail.properties";
	public static final String PATH_Config_Report = PATH_Config + "/report.properties";
	public static final String PATH_Vigie_Apps = PATH_Vigie + "/APPS";
	public static final String PATH_Vigie_Reports = PATH_Vigie + "/REPORTS";
	public static final String PATH_Vigie_Reports_Sent = PATH_Vigie + "/REPORTS_SENT";
	public static final String PATH_Libraries = PATH_Vigie + "/LIB";
	
	public static String GetRootPath()
	{
		try
		{
			String OS = System.getProperty("os.name").toLowerCase();
			
			if (isWindows(OS))
			{
				return "C:";
			}
			else if (isLinux(OS))
			{
				return "/home/" + USER;
			}
			else if (isMac(OS))
			{
				return "Users/" + USER;
			}
			
		}
		catch (Exception ex)
		{
			CH4P_Functions.LogException(CH4P_Functions.LOG_inConsole, ex);
		}
		return null;
	}
	
	private static boolean isWindows(String os) {

		return (os.indexOf("win") >= 0);

	}

	private static boolean isMac(String os) {

		return (os.indexOf("mac") >= 0);

	}

	private static boolean isLinux(String os) {

		return (os.indexOf("nux") >= 0);

	}
	
	public static String GetSeparator()
	{
		return FileSystems.getDefault().getSeparator();
	}
	
	public static Path GetPath(String destination)
	{
		return FileSystems.getDefault().getPath(destination);
	}
}
