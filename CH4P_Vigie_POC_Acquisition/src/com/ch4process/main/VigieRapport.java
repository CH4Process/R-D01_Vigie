package com.ch4process.main;

import java.io.FileWriter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import javax.sql.rowset.CachedRowSet;
import java.sql.Date;
import java.text.SimpleDateFormat;

import com.ch4process.database.ConnectionHandler;
import com.ch4process.database.DatabaseController;
import com.ch4process.database.DatabaseRequest;
import com.ch4process.database.IDatabaseRequestCallback;
import com.ch4process.database.RequestList;
import com.opencsv.CSVWriter;




public class VigieRapport extends Thread
{
	String reportTimeParam = "23:30";
	Calendar reportTime;
	
	Thread thisThread;
	String threadName;
	
	ConnectionHandler connectionHandler;
	DatabaseRequest getWeekMeasuresRequest;
	DatabaseRequest getMeasuresIndexesRequest;
	DatabaseRequest updateMeasuresIndexesRequest;
	DatabaseRequest getEventsRequest;
	
	IDatabaseRequestCallback getWeekMeasuresRequestCallback;
	IDatabaseRequestCallback getMeasuresIndexesRequestCallback;
	IDatabaseRequestCallback updateMeasuresIndexesRequestCallback;
	IDatabaseRequestCallback getEventsRequestCallback;
	
	boolean getWeekMeasuresRequest_done;
	boolean getMeasuresIndexesRequest_done;
	boolean updateMeasuresIndexesRequest_done;
	boolean getEventsRequest_done;
	
	public VigieRapport(String name)
	{
		this.threadName = name;
	}
	
	public void start()
	{
		System.out.println(threadName + " start : " + Calendar.getInstance().getTime());
		
		initDatabaseRelations();
		
		String[] time = reportTimeParam.split(":");
		reportTime = Calendar.getInstance();
		reportTime.set(Calendar.HOUR_OF_DAY, Integer.valueOf(time[0]));
		reportTime.set(Calendar.MINUTE, Integer.valueOf(time[1]));
		reportTime.set(Calendar.SECOND, 0);
		
		// TODO : DEBUG !!
		Calendar now = Calendar.getInstance();
		reportTime.setTime(now.getTime());
		reportTime.add(Calendar.MINUTE, -1);
		
		if (thisThread == null)
		{
			thisThread = new Thread (this, threadName);
			System.out.println("Thread " + threadName + " lancé !");
			thisThread.start();
		}
	}
	
	public void run()
	{
		while (true)
		{
			try
			{
				if (isReportTime())
				{
					getDatas();
				}
				
				if (getWeekMeasuresRequest_done && getMeasuresIndexesRequest_done)
				{
					System.out.println("DEBUG : FAULTSREPORT CALLED ! ");
					FaultsReport();
					getWeekMeasuresRequest_done = false;
					getMeasuresIndexesRequest_done = false;
				}
				
				Thread.sleep(60 * 1000);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private void initDatabaseRelations()
	{
		DatabaseController.init();
		connectionHandler = DatabaseController.getConnection();
		
		getWeekMeasuresRequestCallback = new IDatabaseRequestCallback()
		{
			
			@Override
			public void databaseRequestCallback()
			{
				getWeekMeasuresRequest_done = true;
			}
		};
		
		getMeasuresIndexesRequestCallback = new IDatabaseRequestCallback()
		{
			
			@Override
			public void databaseRequestCallback()
			{
				getMeasuresIndexesRequest_done = true;
			}
		};
		
		updateMeasuresIndexesRequestCallback = new IDatabaseRequestCallback()
		{
			
			@Override
			public void databaseRequestCallback()
			{
				updateMeasuresIndexesRequest_done = true;
			}
		};
		
		getEventsRequestCallback = new IDatabaseRequestCallback()
		{
			
			@Override
			public void databaseRequestCallback()
			{
				getEventsRequest_done = true;
			}
		};
		
		getWeekMeasuresRequest = new DatabaseRequest(connectionHandler, RequestList.REQUEST_WeekMeasures, getWeekMeasuresRequestCallback);
		getMeasuresIndexesRequest = new DatabaseRequest(connectionHandler, RequestList.REQUEST_Indexes, getMeasuresIndexesRequestCallback);
		updateMeasuresIndexesRequest = new DatabaseRequest(connectionHandler, RequestList.REQUEST_UpdateIndexes, updateMeasuresIndexesRequestCallback);
		getEventsRequest = new DatabaseRequest(connectionHandler, RequestList.REQUEST_Scenarios, getEventsRequestCallback);
		
		getWeekMeasuresRequest.start();
		getMeasuresIndexesRequest.start();
		updateMeasuresIndexesRequest.start();
		getEventsRequest.start();
	}

	private boolean isReportTime()
	{
		try
		{
			Calendar now = Calendar.getInstance();
			
			if (now.after(reportTime))
			{
				System.out.println("DEBUG : REPORT TIME YAAAAAAAAAAAAAAAAY !");
				return true;
			}
			System.out.println("DEBUG : NOT REPORT TIME YET :)");
			return false;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	private void getDatas()
	{
		try
		{
			Calendar now = Calendar.getInstance();
			now.add(Calendar.DAY_OF_WEEK, -7);
			getWeekMeasuresRequest.setStatementDateParameter(1, now.getTimeInMillis());
			getWeekMeasuresRequest.doQuery();
			
			getMeasuresIndexesRequest.doQuery();
			getEventsRequest.doQuery();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void FaultsReport()
	{
		try
		{
			CachedRowSet weekMeasures = getWeekMeasuresRequest.getCachedRowSet();
			
			// A good start would be to count the occurrences in a map
			Map occurrences = new HashMap<Integer, Integer>();
			
			while (weekMeasures.next())
			{
				Integer value = weekMeasures.getInt("valeur");
				
				if (value.equals(1)) 
					// TODO : Ajouter une colonne "ActiveState" dans la BDD pour savoir si le contact est NO ou NF et compter les passage à cet état. Stocker ça dans une MAP pour faciliter le contrôle.
				{
					Integer id = weekMeasures.getInt("capteur_id");
					occurrences.putIfAbsent(id, 0);
					occurrences.replace(id, (int) occurrences.get(id) + 1);
				}
			}
			
			// We now know how many times the values changed during last week
			
			CachedRowSet indexes = getMeasuresIndexesRequest.getCachedRowSet();
			
			while(indexes.next())
			{
				Integer id = indexes.getInt("capteur_id");
				
				if (occurrences.containsKey(id))
				{
					Integer oldValue = indexes.getInt("valeur");
					indexes.updateInt("valeur", oldValue + (int)occurrences.get(id));
					indexes.updateDate("lastupdate", new java.sql.Date(Calendar.getInstance().getTimeInMillis()));
					indexes.updateRow();
				}
			}
			
			// TODO: RAPPORT : Normalement les indexes sont à jour en cachedrowset donc on peut produire le rapport des défauts avec le cachedrowset et la map occurrences.
			// On peut même supprimer la requête d'update et utiliser la méthode d'update qui est dans l'objet cachedrowset mais il faut modifier la classe DatabaseRequest pour le permettre.
			
			
			// TODO: Mettre OpenCSv en ANSI ou UTF-8 au lieu du format par défaut et mettre en place la mécanique de mise à jour en base du cachedrowset !!
			
			Calendar now = Calendar.getInstance();
			SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd_HH'h'mm");
			String date = format.format(now.getTime());
			String reportName = "CH4Process_Rapport_Defauts_" + date + ".txt";
			CSVWriter writer = new CSVWriter(new FileWriter(reportName));
			
			writer.writeNext(new String[] {"CH4Process"});
			writer.writeNext(new String[] {"Rapport des défauts du : ", date});
			writer.writeNext(new String[] {""});
			
			writer.writeNext(new String[] {"CAPTEUR", "TOTALISATEUR", "SEMAINE"});
			
			indexes.beforeFirst();
			
			while (indexes.next())
			{
				Integer id = indexes.getInt("capteur_id");
				System.out.println(indexes.getString("libelle"));
				System.out.println(String.valueOf(indexes.getInt("valeur")));
				writer.writeNext(new String[] {indexes.getString("libelle"), String.valueOf(indexes.getInt("valeur")), occurrences.get(id).toString() });
			}
			
			writer.close();
			System.out.println("DEBUG : OMG FIRST REPORT LOL LOL LOL :: " + reportName);
			
			// First report DONE \o/ YAAAAAAAAY.
			
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
}
