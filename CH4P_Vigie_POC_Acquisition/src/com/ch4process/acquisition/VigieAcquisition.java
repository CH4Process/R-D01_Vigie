package com.ch4process.acquisition;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.rowset.CachedRowSet;
import com.ch4process.database.DatabaseRequest;

public class VigieAcquisition extends Thread
{
	Thread thisThread;
	String threadName;
	
	List<Capteur> capteurs = new ArrayList<Capteur>();
	CachedRowSet listeCapteurs;
	List<Scenario> scenarios = new ArrayList<Scenario>();
	CachedRowSet listeScenarios;
	
	boolean firstRun = true;
	Date date;
	
	//VigieSMS vigiesms;
	
	
	public VigieAcquisition(String name)
	{
		this.threadName = name;
	}
	
	private void CapteurList(CachedRowSet listeCapteurs)
	{
		try
		{
			ResultSetMetaData methadata = listeCapteurs.getMetaData();
			Integer columnCount = methadata.getColumnCount();

			while(listeCapteurs.next())
			{
				Capteur capteur = CapteurInstance(listeCapteurs.getString("marque"),listeCapteurs.getString("modele"));
				for(int i = 1; i <= columnCount; i++)
				{
					String arg0 = methadata.getColumnName(i);
					Object arg1 = listeCapteurs.getObject(i);
					capteur.setField(arg0, arg1);
				}
				capteurs.add(capteur);
			}
		}
		catch(SQLException ex)
		{
			ex.printStackTrace();
		}
	}
	
	private Capteur CapteurInstance(String marque, String modele)
	{
		try
		{
			if (marque.equals("YOCTOPUCE"))
			{
				if (modele.equals("YOCTO-4-20-MA-RX"))
				{
					return new Capteur_Yocto_4_20mA();
				}
				else if (modele.equals("YOCTO-METEO-HUMIDITE"))
				{
					return new Capteur_Yocto_Meteo_Humidite();
				}
				else if (modele.equals("YOCTO-METEO-PRESSION"))
				{
					return new Capteur_Yocto_Meteo_Pression();
				}
				else if (modele.equals("YOCTO-METEO-Temperature"))
				{
					return new Capteur_Yocto_Meteo_Temperature();
				}
				else if (modele.equals("YOCTO-MAXIIO") )
				{
					return new Capteur_Yocto_MaxiIO();
				}
			}
			return null;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return null;
		}
	}
	
	private void ScenarioList(CachedRowSet listeScenarios)
	{
		try
		{
			ResultSetMetaData methadata = listeScenarios.getMetaData();
			Integer columnCount = methadata.getColumnCount();

			while(listeScenarios.next())
			{
				Scenario scenario = new Scenario();
				for(int i = 1; i <= columnCount; i++)
				{
					String arg0 = methadata.getColumnName(i);
					Object arg1 = listeScenarios.getObject(i);
					scenario.setField(arg0, arg1);
				}
				scenario.init();
				scenarios.add(scenario);
			}
		}
		catch(SQLException ex)
		{
			ex.printStackTrace();
		}
	}
	
	public void start()
	{
		databaseHandling(0);
		CapteurList(listeCapteurs);
		
		databaseHandling(1);
		ScenarioList(listeScenarios);
		
		if (thisThread == null)
		{
			thisThread = new Thread (this, threadName);
			System.out.println("Thread " + threadName + " lancé !");
			thisThread.start();
		}
	}
	
	public void run()
	{
		
		while(true)
		{
			try
			{
				date = new Date();
				
				for(Capteur capteur : capteurs)
				{
					if (firstRun)
					{
						System.out.println("Thread " + threadName + " capteur : " + capteur.getLibelle() + " first run !");
						boolean error;
						error = capteur.connect();
						System.out.println("Thread " + threadName + " capteur : " + capteur.getLibelle() + " Connection : " + error);
						error = capteur.init();
						System.out.println("Thread " + threadName + " capteur : " + capteur.getLibelle() + " Init : " + error);
						error = capteur.refresh();
						System.out.println("Thread " + threadName + " capteur : " + capteur.getLibelle() + " Refresh : " + error);
					}
					
					if (! firstRun && checkRefresh(capteur))
					{
						boolean error;
						System.out.println("Thread " + threadName + " capteur : " + capteur.getLibelle() + " Check Scenario");
						error = checkScenario(capteur);
						System.out.println("Thread " + threadName + " capteur : " + capteur.getLibelle() + " Check Scenario = " + error);
						System.out.println("Thread " + threadName + " capteur : " + capteur.getLibelle() + " Record value");
						error = recordValue(capteur);
						System.out.println("Thread " + threadName + " capteur : " + capteur.getLibelle() + " Record value = " + error);
					}
				}
				
				firstRun = false;
				Thread.sleep(1000);
			}
			catch( InterruptedException ex)
			{
				continue;
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}
	
	private boolean checkRefresh(Capteur capteur)
	{
		try
		{
			int tick = capteur.tick();
			System.out.println("Thread " + threadName + " capteur " + capteur.libelle + " tick : " + tick);
			if (tick <= 0)
			{
				System.out.println("Thread " + threadName + " capteur " + capteur.libelle + " refresh needed ! ");
				capteur.refresh();
				return true;
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return false;
		}
		return false;
	}

	private boolean checkScenario(Capteur capteur)
	{
		try
		{
			for (Scenario scenario : scenarios)
			{
				if (scenario.getCapteur_id().equals(capteur.getCapteur_id()))
				{
					scenario.testValue((Double) capteur.getValue());
					if (scenario.isTriggered)
					{
						doScenario(scenario.params);
					}
				}
			}
			return false;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return false;
		}
	}

	private boolean doScenario(String parameters)
	{
		return false;
	}
	
	private boolean recordValue(Capteur capteur)
	{
		return (databaseHandling(10, capteur));
	}
	
	private boolean databaseHandling(Integer phase)
	{
		boolean result = false;
		switch(phase)
		{
			case 0: // initialisation during START phase
			{
				try (DatabaseRequest DBR = new DatabaseRequest())
				{
					DBR.listeCapteursRequest();
					this.listeCapteurs = DBR.getListeCapteurs();
					result = true;
				}
				break;
			}
			case 1: // initialisation during START phase
			{
				try (DatabaseRequest DBR = new DatabaseRequest())
				{
					DBR.listeScenariosRequest();
					this.listeScenarios = DBR.getListeScenarios();
					result = true;
				}
				break;
			}
		}
		return result;
	}
	
	private boolean databaseHandling(Integer phase, Capteur capteur)
	{
		boolean result = false;
		switch(phase)
		{
			case 10: // record analog value in database
			{
				try (DatabaseRequest DBR = new DatabaseRequest())
				{
					DBR.recordMesureRequest(capteur.getCapteur_id(), getSensorValue(capteur) , this.date.getTime());
					System.out.println("Thread " + threadName + " capteur " + capteur.libelle + "valeur mise à jour == " + capteur.getCapteur_id() + " -- " + getSensorValue(capteur) + " -- " + this.date.getTime() );
					result = true;
				}
				break;
			}
		}
		return result;
	}
	
	private Integer getSensorValue(Capteur capteur)
	{
		if (capteur.isAnalogique == true)
		{
			return (int) ((double) capteur.getValue() * 1000);
		}
		else
		{
			boolean value = (boolean) capteur.getValue();
			if (value == true)
			{
				return 0;
			}
			else
			{
				return 1;
			}
		}
	}
}
