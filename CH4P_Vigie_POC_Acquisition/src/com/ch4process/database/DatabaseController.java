package com.ch4process.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class DatabaseController implements AutoCloseable
{
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	static final String URL = "jdbc:mysql://";
	static final String DATABASEPORT = "3306";
	static private String databaseUser = "pi";
	static private String databasePassword = "Crepitus";
	static private String databaseName = "CH4Process_DB";
	static private String connectionString = "";
	static private String databaseAddress = "127.0.0.1";
	
	static private Integer nbConnection = 10;
	static private Boolean initialized = false;
	
	static private List<ConnectionHandler> connectionPool = new ArrayList<ConnectionHandler>();

		
	public static boolean init()
	{	
		if (! initialized)
		{
			connectionString = URL + databaseAddress + ":" + DATABASEPORT + "/" + databaseName;

			try
			{
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

	public static void free(ConnectionHandler cnh)
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