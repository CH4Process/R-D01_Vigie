package com.ch4process.database;

import java.sql.*;
import javax.sql.rowset.CachedRowSet;

import com.ch4process.utils.CH4P_Exception;
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
	private CachedRowSet cachedRowSet = null;
	
	private ConnectionHandler connectionHandler = null;
	private String request = null;
	
	private boolean error = false;
	private boolean close = false;
	
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
	
	public void setCallback(IDatabaseRequestCallback dbrc)
	{
		this.dbrc = dbrc;
	}
	
	public void setRequest(String request)
	{
		this.request = request;
	}
	
	public void init()
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

	public void start()
	{
		init();
	}
	
	public void run()
	{
		try
		{
			while (!close)
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
	
	private void doCallback() throws CH4P_Exception
	{
		try
		{
			this.dbrc.databaseRequestCallback();
		}
		catch (Exception ex)
		{
			throw new CH4P_Exception("-DatabaseRequest doCallback error-" + ex.getMessage(), ex.getCause());
		}
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
	
	public void setStatementStringParameter(int id_parameter, String value)
	{
		try
		{
			this.preparedStatement.setString(id_parameter, value);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public void setStatementBoolParameter(int id_parameter, boolean value)
	{
		try
		{
			this.preparedStatement.setBoolean(id_parameter, value);
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
				this.setCachedRowSet(new CachedRowSetImpl());
				this.getCachedRowSet().populate(this.resultSet);
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

	public CachedRowSet getCachedRowSet()
	{
		return cachedRowSet;
	}

	public void setCachedRowSet(CachedRowSet cachedRowSet)
	{
		this.cachedRowSet = cachedRowSet;
	}
	
	public void close()
	{
		try
		{
			resultSet.close();
			preparedStatement.close();
		}
		catch (Exception e)
		{
		}
		finally 
		{
			resultSet = null;
			preparedStatement = null;
			rowsUpdated = null;
			cachedRowSet = null;
			connectionHandler = null;
			request = null;
			this.close = true;
		}
	}

}