package com.ch4process.utils;

import java.util.Properties;

import jdk.net.NetworkPermission;

public class CH4P_ConfigManager
{
	// Internal class
	public static class CH4P_ConfigHolder
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
	private static CH4P_ConfigHolder networkConfig = null;
	
	private static boolean initialized = false;
	private static CH4P_ConfigManager instance = null;
	
	
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
	
	public static CH4P_ConfigHolder getNetworkConfig()
	{
		return networkConfig;
	}
	
	public static boolean isInitialized()
	{
		return initialized;
	}
	
	public static void LoadAllConfigs() throws CH4P_Exception
	{
		try
		{
			LoadMailConfig();
			LoadDatabaseConfig();
			LoadReportConfig();
			LoadNetworkConfig();
		}
		catch (Exception ex)
		{
			throw new CH4P_Exception("-ConfigManager ConfigInit error-" + ex.getMessage(), ex.getCause());
		}
	}
	
	public static void LoadMailConfig() throws CH4P_Exception
	{
		try
		{
			mailConfig = new CH4P_ConfigHolder(CH4P_System.PATH_Config_Mail);
		}
		catch (Exception ex)
		{
			throw new CH4P_Exception("-ConfigManager ConfigInit error-" + ex.getMessage(), ex.getCause());
		}
	}
	
	public static void LoadDatabaseConfig() throws CH4P_Exception
	{
		try
		{
			databaseConfig = new CH4P_ConfigHolder(CH4P_System.PATH_Config_Database);
		}
		catch (Exception ex)
		{
			throw new CH4P_Exception("-ConfigManager ConfigInit error-" + ex.getMessage(), ex.getCause());
		}
	}
	
	public static void LoadReportConfig() throws CH4P_Exception
	{
		try
		{
			reportConfig = new CH4P_ConfigHolder(CH4P_System.PATH_Config_Report);
		}
		catch (Exception ex)
		{
			throw new CH4P_Exception("-ConfigManager ConfigInit error-" + ex.getMessage(), ex.getCause());
		}
	}
	
	public static void LoadNetworkConfig() throws CH4P_Exception
	{
		try
		{
			networkConfig = new CH4P_ConfigHolder(CH4P_System.PATH_Config_Network);
		}
		catch (Exception ex)
		{
			throw new CH4P_Exception("-ConfigManager ConfigInit error-" + ex.getMessage(), ex.getCause());
		}
	}
	
	
}
