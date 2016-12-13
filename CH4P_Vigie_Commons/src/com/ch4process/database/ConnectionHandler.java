package com.ch4process.database;

import java.sql.Connection;
import java.sql.ResultSetMetaData;

import javax.sql.rowset.CachedRowSet;


public class ConnectionHandler
{
	Connection connection;
	Integer id;
	Boolean isAvailable;
	
	public ConnectionHandler(Connection conn, int id)
	{
		this.connection = conn;
		this.id = id;
		this.isAvailable = true;
	}

	public Connection getConnection()
	{
		return connection;
	}

	public void setConnection(Connection connection)
	{
		this.connection = connection;
	}

	public Integer getId()
	{
		return id;
	}

	public void setId(Integer id)
	{
		this.id = id;
	}

	public Boolean getIsAvailable()
	{
		return isAvailable;
	}

	public void setIsAvailable(Boolean isAvailable)
	{
		this.isAvailable = isAvailable;
	}
}