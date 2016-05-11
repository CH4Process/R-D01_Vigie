package com.ch4process.database;

import java.sql.*;
import javax.sql.rowset.CachedRowSet;
import com.sun.rowset.CachedRowSetImpl;

/**
 * @author Alex
 *
 */

public class DatabaseRequest extends Thread
{

	private ResultSet resultSet = null;
	private PreparedStatement preparedStatement = null;
	private Integer rowsUpdated = null;
	
	private ConnectionHandler connectionHandler = null;
	private String request = null;
	
	private boolean error = false;
	
	private IDatabaseRequestCallback dbrc;
	
	
	
	public DatabaseRequest(ConnectionHandler cnh, String request, IDatabaseRequestCallback dbrc)
	{
		try
		{
			this.connectionHandler = cnh;
			this.request = request;
			this.dbrc = dbrc;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			error = true;
		}
	}
	
	public void start()
	{
		try
		{
			this.preparedStatement = connectionHandler.getConnection().prepareStatement(request);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void run()
	{
		try
		{
			while (true)
			{
				Thread.sleep(1000);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			error = true;
		}
	}
	
	public void doQuery()
	{
		try
		{
			this.resultSet = this.preparedStatement.executeQuery();
			saveResultSet();
			doCallback();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			error = true;
		}
		
	}
	
	public void doUpdate()
	{
		try
		{
			this.rowsUpdated = this.preparedStatement.executeUpdate();
			doCallback();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			error = true;
		}
	}
	
	private void doCallback()
	{
		this.dbrc.databaseRequestCallback();
	}
	
	public void setStatementIntParameter(int id_parameter, int value)
	{
		try
		{
			this.preparedStatement.setInt(id_parameter, value);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	public void setStatementDoubleParameter(int id_parameter, double value)
	{
		try
		{
			this.preparedStatement.setDouble(id_parameter, value);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public void setStatementDateParameter(int id_parameter, long value)
	{
		try
		{
			Timestamp timestamp = new Timestamp((long) value);
			this.preparedStatement.setTimestamp(id_parameter, timestamp);
			timestamp = null;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	private void saveResultSet()
	{
		try
		{
			if (! error)
			{
				connectionHandler.setCachedrowset(new CachedRowSetImpl());
				connectionHandler.getCachedrowset().populate(this.resultSet);
			}
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
			error = true;
		}
		finally
		{
			closeResultset();
		}
		
	}
	
	private void closeResultset()
	{
		try
		{
			if (resultSet != null)
			{
				resultSet.close();
			}
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			resultSet = null;
		}
	}
}