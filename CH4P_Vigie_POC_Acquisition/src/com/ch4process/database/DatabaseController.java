package com.ch4process.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import com.ch4process.utils.PropertiesReader;


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
	static private String configFile = "resources/database.properties";
	
	static private Boolean initialized = false;
	
	static private List<ConnectionHandler> connectionPool = new ArrayList<ConnectionHandler>();

	private static boolean ReadConfigFile()
	{
		try
		{
			PropertiesReader propReader = new PropertiesReader();
			Properties prop = propReader.getPropValues(configFile);
			
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
			ex.printStackTrace();
			return false;
		}
	}
	public static boolean Init()
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
				ex.printStackTrace();
				return false;
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
			ex.printStackTrace();
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
	
	public static ConnectionHandler getConnection()
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
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public static void Free(ConnectionHandler cnh)
	{
		try
		{
			connectionPool.get(cnh.getId()).setIsAvailable(true);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}