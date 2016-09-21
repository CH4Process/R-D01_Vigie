package com.ch4process.utils;

import java.util.Properties;

public class CH4P_ConfigManager
{
	// Internal class
	public class CH4P_ConfigHolder
	{
		private String fileName;
		private CH4P_PropertiesReader propReader;
		private Properties properties;
		
		
		public CH4P_ConfigHolder(String _fileName)
		{
			this.fileName = _fileName;
			propReader = new CH4P_PropertiesReader();
			RefreshConfig();
		}
		
		public Boolean RefreshConfig()
		{
			if ((this.properties = propReader.getPropValues(this.fileName)) != null)
			{
				return true;
			}
			
			return null;
		}
		
		public Properties GetProperties()
		{
			return this.properties;
		}
	}

	private static CH4P_ConfigHolder mailConfig = null;
	private static CH4P_ConfigHolder databaseConfig = null;
	private static CH4P_ConfigHolder reportConfig = null;
	
	private static boolean init_done = false;
	private static CH4P_ConfigManager instance = null;


	public static void Init()
	{
		if (!init_done)
		{
			if (instance == null)
			{
				instance = new CH4P_ConfigManager();
			}
			
			instance.ConfigInit();
			
			init_done = true;
		}
	}
	
	public static CH4P_ConfigHolder getMailConfig()
	{
		return mailConfig;
	}
	
	public static CH4P_ConfigHolder getDatabaseConfig()
	{
		return databaseConfig;
	}
	
	public static CH4P_ConfigHolder getReportConfig()
	{
		return reportConfig;
	}
	
	private void ConfigInit()
	{
		mailConfig = new CH4P_ConfigHolder(CH4P_System.PATH_Config_Mail);
		databaseConfig = new CH4P_ConfigHolder(CH4P_System.PATH_Config_Database);
		reportConfig = new CH4P_ConfigHolder(CH4P_System.PATH_Config_Report);
	}
	
	
}
