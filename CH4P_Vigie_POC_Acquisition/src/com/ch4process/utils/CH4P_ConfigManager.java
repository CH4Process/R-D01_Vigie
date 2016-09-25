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
		
		
		public CH4P_ConfigHolder(String _fileName) throws CH4P_Exception
		{
			try
			{
				this.fileName = _fileName;
				propReader = new CH4P_PropertiesReader();
				RefreshConfig();
			}
			catch (Exception ex)
			{
				throw new CH4P_Exception("-ConfigManager ConfigHolder constructor error-" + ex.getMessage(), ex.getCause());
			}
		}
		
		public Boolean RefreshConfig() throws CH4P_Exception
		{
			try
			{
				if ((this.properties = propReader.getPropValues(this.fileName)) != null)
				{
					return true;
				}

				return null;
			}
			catch (Exception ex)
			{
				throw new CH4P_Exception("-ConfigManager RefreshConfig error-" + ex.getMessage(), ex.getCause());
			}
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


	public static void Init() throws CH4P_Exception
	{
		try
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
		catch (Exception ex)
		{
			throw new CH4P_Exception("-ConfigManager Init error-" + ex.getMessage(), ex.getCause());
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
	
	private void ConfigInit() throws CH4P_Exception
	{
		try
		{
			mailConfig = new CH4P_ConfigHolder(CH4P_System.PATH_Config_Mail);
			databaseConfig = new CH4P_ConfigHolder(CH4P_System.PATH_Config_Database);
			reportConfig = new CH4P_ConfigHolder(CH4P_System.PATH_Config_Report);
		}
		catch (Exception ex)
		{
			throw new CH4P_Exception("-ConfigManager ConfigInit error-" + ex.getMessage(), ex.getCause());
		}
	}
	
	
}
