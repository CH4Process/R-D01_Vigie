package com.ch4process.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.ch4process.utils.CH4P_ConfigManager;
import com.ch4process.utils.CH4P_Exception;
import com.ch4process.utils.CH4P_Functions;
import com.ch4process.utils.CH4P_PropertiesReader;


public class DatabaseController implements AutoCloseable
{	
	static private String JDBC_DRIVER = null;
	static private String URL = null;
	static private String DATABASEPORT = null;
	static private String databaseUser = null;
	static private String databasePassword = null;
	static private String databaseName = null;
	static private String connectionString = null;
	static private String databaseAddress = null;
	static private Integer nbConnection = null;
	
	static private Boolean initialized = false;
	
	static private List<ConnectionHandler> connectionPool = new ArrayList<ConnectionHandler>();

	private static boolean ReadConfigFile() throws CH4P_Exception
	{
		try
		{
			Properties prop = CH4P_ConfigManager.getDatabaseConfig().GetProperties();
			if (prop != null)
			{
				JDBC_DRIVER = prop.getProperty("JDBC_DRIVER");
				URL = prop.getProperty("URL");
				DATABASEPORT = prop.getProperty("DATABASEPORT");
				databaseUser = prop.getProperty("databaseUser");
				databasePassword = prop.getProperty("databasePassword");
				databaseName = prop.getProperty("databaseName");
				databaseAddress = prop.getProperty("databaseAddress");
				nbConnection = Integer.valueOf(prop.getProperty("nbConnections"));
			}
			else
			{
				// Default values
				JDBC_DRIVER = "com.mysql.jdbc.Driver";
				URL = "jdbc:mysql://";
				DATABASEPORT = "3306";
				databaseUser = "pi";
				databasePassword = "Crepitus";
				databaseName = "CH4Process_DB";
				databaseAddress = "127.0.0.1";
				nbConnection = 10;
			}
			
			connectionString = URL + databaseAddress + ":" + DATABASEPORT + "/" + databaseName;
			return true;
			
		}
		catch (Exception ex)
		{
			throw new CH4P_Exception(" -Database config file reading- " + ex.getMessage(), ex.getCause());
		}
	}
	public static boolean Init() throws CH4P_Exception
	{	
		if (! initialized)
		{
			try
			{
				ReadConfigFile();
				
				for (int i = 0; i < nbConnection; i++)
				{
					Connection conn = DriverManager.getConnection(connectionString, databaseUser, databasePassword);
					ConnectionHandler cnh = new ConnectionHandler(conn, i+1);

					connectionPool.add(cnh);
				}
				
				initialized = true;
				return true;
			}
			catch(Exception ex)
			{
				throw new CH4P_Exception("-DatabaseController init error-" + ex.getMessage(), ex.getCause());
			}
		}
		
		return true;
	}
	
	public void close()
	{
		try
		{
			for (int i = 0; i < nbConnection; i++)
			{
				connectionPool.get(i).getConnection().close();
			}
		}
		catch (Exception ex)
		{
			CH4P_Functions.LogException(CH4P_Functions.LOG_inConsole, ex);
		}
		finally
		{
			for (int i = 0; i < nbConnection; i++)
			{
				connectionPool.get(i).setConnection(null);
				connectionPool.remove(i);
				connectionPool = null;
			}
			
			initialized = false;
		}
	}
	
	public static ConnectionHandler getConnection() throws CH4P_Exception
	{
		try
		{
			for (int i = 0; i < nbConnection; i++)
			{
				if (connectionPool.get(i).getIsAvailable())
				{
					return connectionPool.get(i);
				}
			}
			
			return null;
		}
		catch (Exception ex)
		{
			throw new CH4P_Exception(ex.getMessage(), ex.getCause());
		}
	}

	public static void Free(ConnectionHandler cnh) throws CH4P_Exception
	{
		try
		{
			connectionPool.get(cnh.getId()).setIsAvailable(true);
		}
		catch (Exception ex)
		{
			throw new CH4P_Exception("-DatabaseController Free connection error-" + ex.getMessage(), ex.getCause());
		}
	}
}