package com.ch4process.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;

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
	public static final String PATH_Config_Network = PATH_Config + "/network.properties";
	public static final String PATH_Config_RebootToken = PATH_Config + "/RebootToken";
	public static final String PATH_Vigie_Apps = PATH_Vigie + "/APPS";
	public static final String PATH_Vigie_Reports = PATH_Vigie + "/REPORTS";
	public static final String PATH_Vigie_Reports_Sent = PATH_Vigie + "/REPORTS_SENT";
	
	public static String GetOS()
	{
		return System.getProperty("os.name").toLowerCase();
	}
	
	public static String GetRootPath()
	{
		try
		{
			String OS = GetOS();
			
			if (isWindows(OS))
			{
				return "C:";
			}
			else if (isLinux(OS))
			{
				return System.getProperty("user.home");
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
	
	private static boolean isWindows(String os) 
	{
		return (os.indexOf("win") >= 0);
	}

	private static boolean isMac(String os) 
	{
		return (os.indexOf("mac") >= 0);
	}

	private static boolean isLinux(String os) 
	{
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
	
	public static ArrayList<String> GetProcessList()
	{
		try
		{
			String OS = GetOS();

			Process p = null;
			String processName;
			ArrayList<String> processes = new ArrayList<String>();

			if (isWindows(OS))
			{
				p = Runtime.getRuntime().exec(System.getenv("windir") +"\\system32\\"+"tasklist.exe /fo csv /nh");
			}
			else if (isLinux(OS))
			{
				p = Runtime.getRuntime().exec("ps -few");
			}
			else if (isMac(OS))
			{
				p = null;
			}

			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));

			while ((processName = input.readLine()) != null) 
			{
				processName = processName.replace("\\","");
				processes.add(processName.split(",")[0]);
			}
			
			input.close();
			
			return processes;
		}
		catch (Exception ex)
		{
			CH4P_Functions.LogException(CH4P_Functions.LOG_inConsole, ex);
			return null;
		}
	}
	
	public static void KillProcess(String processName)
	{
		try
		{
			String OS = GetOS();
			
			if (isWindows(OS))
			{
				Runtime.getRuntime().exec(System.getenv("windir") +"\\system32\\"+"taskkill.exe /IM " + processName + "*");
			}
			else if (isLinux(OS))
			{
				Runtime.getRuntime().exec("sudo pkill " + processName);
			}
		}
		catch (Exception ex)
		{
			CH4P_Functions.LogException(CH4P_Functions.LOG_inConsole, ex);
		}
	}
	
	public static void StartProcess(String command)
	{
		try
		{
			Runtime.getRuntime().exec(command);
		}
		catch (Exception ex)
		{
			CH4P_Functions.LogException(CH4P_Functions.LOG_inConsole, ex);
		}
	}
	
}
