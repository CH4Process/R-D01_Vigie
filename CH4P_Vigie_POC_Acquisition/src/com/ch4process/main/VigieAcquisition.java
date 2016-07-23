package com.ch4process.main;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.sql.rowset.CachedRowSet;

import com.ch4process.acquisition.Signal;
import com.ch4process.acquisition.Signal_Yocto_4_20mA;
import com.ch4process.acquisition.Signal_Yocto_MaxiIO;
import com.ch4process.acquisition.Signal_Yocto_Meteo_Humidite;
import com.ch4process.acquisition.Signal_Yocto_Meteo_Pression;
import com.ch4process.acquisition.Signal_Yocto_Meteo_Temperature;
import com.ch4process.acquisition.Commande;
import com.ch4process.acquisition.LogWorker;
import com.ch4process.acquisition.Scenario;
import com.ch4process.acquisition.ScenarioWorker;
import com.ch4process.acquisition.RecordWorker;
import com.ch4process.database.ConnectionHandler;
import com.ch4process.database.DatabaseController;
import com.ch4process.database.DatabaseRequest;
import com.ch4process.database.IDatabaseRequestCallback;
import com.ch4process.database.RequestList;

import sun.security.jca.GetInstance;

public class VigieAcquisition extends Thread
{
	Thread thisThread;
	String threadName;
	
	List<Signal> capteurs = new ArrayList<Signal>();
	List<Scenario> scenarios = new ArrayList<Scenario>();
	List<Commande> commandes = new ArrayList<Commande>();
	
	ConnectionHandler connectionHandler;
	DatabaseRequest capteurListRequest;
	DatabaseRequest scenarioListRequest;
	DatabaseRequest recordValueRequest;
	DatabaseRequest commandeListRequest;
	DatabaseRequest logEventRequest;
	IDatabaseRequestCallback capteurListRequestCallback;
	IDatabaseRequestCallback scenarioListRequestCallback;
	IDatabaseRequestCallback commandeListRequestCallback;
	boolean capteurListRequest_done = false;
	boolean scenarioListRequest_done = false;
	boolean commandeListRequest_done = false;
	
	RecordWorker recordWorker;
	ScenarioWorker scenarioWorker;
	LogWorker logWorker;
	
	boolean firstRun = true;
	
	
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
				Signal capteur = CapteurInstance(listeCapteurs.getString("marque"),listeCapteurs.getString("modele"));
				for(int i = 1; i <= columnCount; i++)
				{
					String arg0 = methadata.getColumnName(i);
					Object arg1 = listeCapteurs.getObject(i);
					capteur.setField(arg0, arg1);
				}
				capteurs.add(capteur);
				capteur.addValueListener(recordWorker);
				capteur.addValueListener(scenarioWorker);
				capteur.start();
			}
		}
		catch(SQLException ex)
		{
			ex.printStackTrace();
		}
	}
	
	private Signal CapteurInstance(String marque, String modele)
	{
		try
		{
			if (marque.equals("YOCTOPUCE"))
			{
				if (modele.equals("YOCTO-4-20-MA-RX"))
				{
					return new Signal_Yocto_4_20mA();
				}
				else if (modele.equals("YOCTO-METEO-HUMIDITE"))
				{
					return new Signal_Yocto_Meteo_Humidite();
				}
				else if (modele.equals("YOCTO-METEO-PRESSION"))
				{
					return new Signal_Yocto_Meteo_Pression();
				}
				else if (modele.equals("YOCTO-METEO-Temperature"))
				{
					return new Signal_Yocto_Meteo_Temperature();
				}
				else if (modele.equals("YOCTO-MAXIIO") )
				{
					return new Signal_Yocto_MaxiIO();
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
	

	private void CommandeList(CachedRowSet listeCommandes)
	{
		try
		{
			ResultSetMetaData methadata = listeCommandes.getMetaData();
			Integer columnCount = methadata.getColumnCount();

			while(listeCommandes.next())
			{
				Commande commande = new Commande();
				for(int i = 1; i <= columnCount; i++)
				{
					String arg0 = methadata.getColumnName(i);
					Object arg1 = listeCommandes.getObject(i);
					commande.setField(arg0, arg1);
				}
				commandes.add(commande);
				scenarioWorker.addScenarioCommandListener(commande);
				commande.start();
			}
		}
		catch(SQLException ex)
		{
			ex.printStackTrace();
		}
	}
	
	
	public void start()
	{
		System.out.println("VigieAcq start : " + Calendar.getInstance().getTime());
		
		DatabaseController.init();
		connectionHandler = DatabaseController.getConnection();
		
		capteurListRequestCallback = new IDatabaseRequestCallback()
		{
			
			@Override
			public void databaseRequestCallback()
			{
				CapteurList(capteurListRequest.getCachedRowSet());
				capteurListRequest_done = true;
				capteurListRequest.close();
				capteurListRequest = null;
			}
		};
		
		commandeListRequestCallback = new IDatabaseRequestCallback()
		{
			
			@Override
			public void databaseRequestCallback()
			{
				CommandeList(commandeListRequest.getCachedRowSet());
				commandeListRequest_done = true;
				commandeListRequest.close();
				commandeListRequest = null;
			}
		};
		
		
		capteurListRequest = new DatabaseRequest(connectionHandler, RequestList.REQUEST_ListeCapteurs, capteurListRequestCallback);
		scenarioListRequest = new DatabaseRequest(connectionHandler, RequestList.REQUEST_ListeScenarios, null);
		recordValueRequest = new DatabaseRequest(connectionHandler, RequestList.REQUEST_RecordMesure, null);
		commandeListRequest = new DatabaseRequest(connectionHandler, RequestList.REQUEST_ListeCommandes, commandeListRequestCallback);
		logEventRequest = new DatabaseRequest(connectionHandler, RequestList.REQUEST_LogEvent, null);
		
		logWorker = new LogWorker(logEventRequest);
		logWorker.start();
		
		recordWorker = new RecordWorker(recordValueRequest);
		recordWorker.start();
		
		scenarioWorker = new ScenarioWorker(scenarioListRequest);
		scenarioWorker.addActionEventListener(logWorker);
		scenarioWorker.start();
		
		commandeListRequest.start();
		commandeListRequest.doQuery();
		
		capteurListRequest.start();
		capteurListRequest.doQuery();
		
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
				
				if (capteurListRequest_done && firstRun)
				{
					System.out.println("VigieAcq prête :) : " + Calendar.getInstance().getTime());
					firstRun = false;
				}
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
}
