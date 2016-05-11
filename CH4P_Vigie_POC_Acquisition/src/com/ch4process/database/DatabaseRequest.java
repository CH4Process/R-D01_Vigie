package com.ch4process.database;

import java.sql.*;
import javax.sql.rowset.CachedRowSet;
import com.sun.rowset.CachedRowSetImpl;

/**
 * @author Alex
 *
 */

public class DatabaseRequest implements AutoCloseable
{
	final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	final String URL = "jdbc:mysql://";
	final String DATABASEPORT = "3306";
	private String databaseUser = "pi";
	private String databasePassword = "Crepitus";
	private String databaseName = "CH4Process_DB";
	private String connectionString = "";
	private String databaseAddress = "127.0.0.1";

	private Connection connection = null;
	private ResultSet resultset = null;
	private CachedRowSet cachedrowset = null;
	private boolean error;
	private PreparedStatement STATEMENT_ListeCapteurs = null;
	private PreparedStatement STATEMENT_RecordMesure = null;
	private PreparedStatement STATEMENT_ListeScenarios = null;
	
	private final String REQUEST_ListeCapteurs = "SELECT c.capteur_id, c.numeroserie, c.adresse, c.libelle, c.periode, tc.coeff, tc.marque, tc.modele, tc.plage_min, tc.plage_max FROM capteur c, type_capteur tc WHERE c.type_capteur_id = tc.type_capteur_id;";
	private final String REQUEST_RecordMesure = "INSERT INTO mesure (capteur_id, valeur, datetime) VALUES (?,?,?);";
	private final String REQUEST_ListeScenarios = "SELECT s.scenario_id, s.capteur_id, s.test, s.params FROM scenario;";
	
	
	/**
	 * @param ip = IP Address of the database. Can be the string "localhost"
	 * @param database = Database name
	 * @param user = User with rights on the database
	 * @param password = Password of the User
	 * Constructor of the class
	 */
	public DatabaseRequest(String ip, String database, String user, String password)
	{
		this.databaseAddress = ip;
		this.databaseName = database;
		this.databaseUser = user;
		this.databasePassword = password;
		
		this.connectionString = URL + databaseAddress + ":" + DATABASEPORT + "/" + databaseName;
		connect();
	}
	
	public DatabaseRequest()
	{
		this.connectionString = URL + databaseAddress + ":" + DATABASEPORT + "/" + databaseName;
		connect();
	}
	
	private boolean connect()
	{
		try
		{
			// Trying to get the driver
			Class.forName(JDBC_DRIVER);
			// Creating the connection to the SQL server using the user password and connection string provided
			connection = DriverManager.getConnection(connectionString, databaseUser, databasePassword);
			
			STATEMENT_ListeCapteurs =  connection.prepareStatement(REQUEST_ListeCapteurs);
			STATEMENT_RecordMesure = connection.prepareStatement(REQUEST_RecordMesure);
			STATEMENT_ListeScenarios = connection.prepareStatement(REQUEST_ListeScenarios);
			
			return true;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			return false;
		}
	}

	public void listeCapteursRequest()
	{
		try
		{
			resultset = STATEMENT_ListeCapteurs.executeQuery();
			saveResultSet();
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			closeStatement();
			closeResultset();
		}
	}
	
	public CachedRowSet getListeCapteurs()
	{
		if (! error)
		{
			return cachedrowset;
		}
		else
		{
			return null;
		}	
	}
	
	public int recordMesureRequest(int id_capteur, int mesure, long date)
	{
		try
		{
			STATEMENT_RecordMesure.setInt(1, id_capteur);
			STATEMENT_RecordMesure.setInt(2, mesure);
			Timestamp timestamp = new Timestamp((long) date);
			STATEMENT_RecordMesure.setTimestamp(3, timestamp);
			
			return STATEMENT_RecordMesure.executeUpdate();
			
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
			return 0;
		}
		finally
		{
			closeStatement();
			closeResultset();
		}
	}

	public void listeScenariosRequest()
	{
		try
		{
			resultset = STATEMENT_ListeScenarios.executeQuery();
			saveResultSet();
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			closeStatement();
			closeResultset();
		}
	}
	
	public CachedRowSet getListeScenarios()
	{
		if (! error)
		{
			return cachedrowset;
		}
		else
		{
			return null;
		}	
	}
	
	private void saveResultSet()
	{
		try
		{
			if (! error)
			{
				cachedrowset = new CachedRowSetImpl();
				cachedrowset.populate(resultset);
			}
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
			error = true;
		}
		
	}
	
	private void closeResultset()
	{
		try
		{
			if (resultset != null)
			{
				resultset.close();
			}
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			resultset = null;
		}
	}
	
	private void closeStatement()
	{/*
		try
		{
			if (STATEMENT_ListeCapteurs != null)
			{
				STATEMENT_ListeCapteurs.close();
			}
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			statement = null;
		}
	*/}
	
	public void close()
	{
		try
		{
			closeResultset();
			closeStatement();
			if (connection != null)
			{
				connection.close();
			}
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			connection = null;
			error = false;
		}
	}

	
}
