package com.ch4process.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.sql.rowset.CachedRowSet;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import com.ch4process.database.ConnectionHandler;
import com.ch4process.database.DatabaseController;
import com.ch4process.database.DatabaseRequest;
import com.ch4process.database.IDatabaseRequestCallback;
import com.ch4process.database.RequestList;
import com.ch4process.email.Mail;
import com.ch4process.utils.CH4P_Exception;
import com.ch4process.utils.CH4P_Functions;
import com.ch4process.utils.CH4P_Multithreading;
import com.ch4process.utils.CH4P_System;
import com.opencsv.CSVWriter;


public class VigieReport implements Callable<Integer>
{
	final String reportTimeParam = "23:30";
	final Integer reportSpan = 7;
	Calendar reportTime;
	
	Thread thisThread;
	String threadName;
	
	ConnectionHandler connectionHandler;
	DatabaseRequest getDigitalMeasures;
	DatabaseRequest getAnalogMeasures;
	DatabaseRequest getTotalizers;
	DatabaseRequest updateTotalizers;
	DatabaseRequest getScenariosRequest;
	
	IDatabaseRequestCallback getDigitalMeasuresRequestCallback;
	IDatabaseRequestCallback getAnalogMeasuresRequestCallback;
	IDatabaseRequestCallback getTotalizersRequestCallback;
	IDatabaseRequestCallback updateTotalizersRequestCallback;
	IDatabaseRequestCallback getScenariosRequestCallback;
	
	boolean getDigitalMeasuresRequest_done = false;
	boolean getAnalogMeasuresRequest_done = false;
	boolean getTotalizersRequest_done = false;
	boolean updateTotalizersRequest_done = false;
	boolean getScenariosRequest_done = false;
	
	int currentReport = 0;
	boolean yield = true;
	int reportSendLimit = 12;
	
	public VigieReport(String name)
	{
		this.threadName = name;
	}
	
	@Override
	public Integer call() throws CH4P_Exception
	{
		try
		{
			start();
			run();
		}
		catch(Exception ex)
		{
			throw new CH4P_Exception(ex.getMessage(), ex.getCause());
		}
		finally
		{
			return null;
		}
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
		
		// TODO : Remove this code (here for debug purpose)
		Calendar now = Calendar.getInstance();
		reportTime.setTime(now.getTime());
		reportTime.add(Calendar.MINUTE, -1);
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
				
				// Report based on totalizers
				if (getDigitalMeasuresRequest_done && getTotalizersRequest_done && currentReport == 0 && yield)
				{
					yield = false;
					FaultsReport();
					getTotalizersRequest_done = false;
					currentReport = 1;
				}
				
				// Report based on measures
				if (getAnalogMeasuresRequest_done && getDigitalMeasuresRequest_done && currentReport == 1 && yield)
				{
					yield = false;
					MeasuresReport();
					getDigitalMeasuresRequest_done = false;
					getAnalogMeasuresRequest_done = false;
					currentReport = 2;
				}
				
				// Report based on the scenarios
				if (getScenariosRequest_done && currentReport == 2 && yield)
				{
					yield = false;
					ScenariosReport();
					getScenariosRequest_done = false;
					currentReport = 3;
				}
				
				if (currentReport == 3 && yield)
				{
					yield = false;
					SendReports();
					currentReport = 0;
					
				}
				
				
				Thread.sleep(60 * 1000);
				yield = true;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private void initDatabaseRelations()
	{
		DatabaseController.Init();
		connectionHandler = DatabaseController.getConnection();
		
		getDigitalMeasuresRequestCallback = new IDatabaseRequestCallback()
		{
			
			@Override
			public void databaseRequestCallback()
			{
				getDigitalMeasuresRequest_done = true;
			}
		};
		
		getAnalogMeasuresRequestCallback = new IDatabaseRequestCallback()
		{
			
			@Override
			public void databaseRequestCallback()
			{
				getAnalogMeasuresRequest_done = true;
			}
		};
		
		getTotalizersRequestCallback = new IDatabaseRequestCallback()
		{
			
			@Override
			public void databaseRequestCallback()
			{
				getTotalizersRequest_done = true;
			}
		};
		
		updateTotalizersRequestCallback = new IDatabaseRequestCallback()
		{
			
			@Override
			public void databaseRequestCallback()
			{
				updateTotalizersRequest_done = true;
			}
		};
		
		getScenariosRequestCallback = new IDatabaseRequestCallback()
		{
			
			@Override
			public void databaseRequestCallback()
			{
				getScenariosRequest_done = true;
			}
		};
		
		getDigitalMeasures = new DatabaseRequest(connectionHandler, RequestList.REQUEST_DigitalMeasure, getDigitalMeasuresRequestCallback);
		getAnalogMeasures = new DatabaseRequest(connectionHandler, RequestList.REQUEST_AnalogMeasure, getAnalogMeasuresRequestCallback);
		getTotalizers = new DatabaseRequest(connectionHandler, RequestList.REQUEST_TotalizerValue, getTotalizersRequestCallback);
		updateTotalizers = new DatabaseRequest(connectionHandler, RequestList.REQUEST_RecordTotalizer, updateTotalizersRequestCallback);
		getScenariosRequest = new DatabaseRequest(connectionHandler, RequestList.REQUEST_Scenarios, getScenariosRequestCallback);
		
		getDigitalMeasures.start();
		getAnalogMeasures.start();
		getTotalizers.start();
		updateTotalizers.start();
		getScenariosRequest.start();
	}

	private boolean isReportTime()
	{
		try
		{
			Calendar now = Calendar.getInstance();
			
			if (now.after(reportTime))
			{
				System.out.println("VigieReport : isReportTime = true.");
				return true;
			}
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
			Calendar startDay = Calendar.getInstance();
			startDay.add(Calendar.DAY_OF_WEEK, -reportSpan);
			getDigitalMeasures.setStatementDateParameter(1, startDay.getTimeInMillis());
			getDigitalMeasures.doQuery();
			getAnalogMeasures.setStatementDateParameter(1, startDay.getTimeInMillis());
			getAnalogMeasures.doQuery();
			getTotalizers.doQuery();
			getScenariosRequest.doQuery();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void UpdateTotalizers(CachedRowSet _indexes)
	{
		Callable<Integer> task = new Callable<Integer>()
		{

			@Override
			public Integer call() throws Exception
			{
				try
				{
					_indexes.beforeFirst();
					
					while (_indexes.next())
					{
						updateTotalizers.setStatementDoubleParameter(1, _indexes.getDouble("value")); // value
						updateTotalizers.setStatementDateParameter(2, Calendar.getInstance().getTimeInMillis()); // datetime
						updateTotalizers.setStatementBoolParameter(3, true); // isValid
						updateTotalizers.setStatementIntParameter(4, _indexes.getInt("idSignal")); // id
						updateTotalizersRequest_done = false;
						System.out.println("VigieReport : UpdateTotalizers : A row has been updated :).");
						updateTotalizers.doUpdate();
						
						while (updateTotalizersRequest_done == false)
						{
							Thread.sleep(500);
						}
					}
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
				return null;
			}
		};
		
		CH4P_Multithreading.Submit(task);
	}

	private void FaultsReport()
	{
		try
		{
			CachedRowSet measures = getDigitalMeasures.getCachedRowSet();
			
			// A good start would be to count the occurrences in a map - KEY = id - VALUE = [0] = oldValue / [1] = count
			Map occurrences = new HashMap<Integer, Integer[]>();
			
			while (measures.next())
			{
				Integer value = CH4P_Functions.boolToInt(measures.getBoolean("value"));
				Integer isNO = CH4P_Functions.boolToInt(measures.getBoolean("isNormallyOpen"));
				Integer id = measures.getInt("idSignal");
				
				if (! occurrences.containsKey(id)) 
				{
					occurrences.putIfAbsent(id, new Integer[]{value, 0});
				}
				else
				{
					Integer[] data = (Integer[]) occurrences.get(id);
					
					// If the value is different from the previous one
					if (value != data[0])
					{
						// We update the oldValue
						data[0] = value;
						
						// If the value equals the variable isNO then the fault is active
						// Because if isNO = 1 then the default state of the digitalIO is 0
						// So if value = 1 and isNO = 1 it means that the digitalIO state is active
						// And vice versa
						if (value == isNO)
						{
							data[1] += 1;
						}
						
						// And now we update occurrences to reflect that
						occurrences.replace(id, data);
					}
				}
			}
			
			// We now know how many times the values changed during last week
			
			CachedRowSet indexes = getTotalizers.getCachedRowSet();
			
			while(indexes.next())
			{
				Integer id = indexes.getInt("idSignal");
				
				if (occurrences.containsKey(id))
				{				
					Integer[] data = (Integer[]) occurrences.get(id);
					Long count = (long) data[1];
					
					indexes.updateLong("lastValue", indexes.getLong("value"));
					indexes.updateLong("value", indexes.getLong("value") + count);
					indexes.updateTimestamp("datetime", new Timestamp(Calendar.getInstance().getTimeInMillis()));
					indexes.updateRow();
				}
			}
			
			Calendar now = Calendar.getInstance();
			SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd_HH'h'mm");
			String date = format.format(now.getTime());
			String reportName = "CH4Process_Rapport_Defauts_" + date + ".txt";
			CSVWriter writer = new CSVWriter( new OutputStreamWriter(new FileOutputStream(reportName), StandardCharsets.UTF_8));
			
			writer.writeNext(new String[] {"CH4Process"});
			writer.writeNext(new String[] {"Rapport des défauts du : ", date});
			writer.writeNext(new String[] {""});
			
			writer.writeNext(new String[] {"CAPTEUR", "TOTALISATEUR", "SEMAINE"});
			
			indexes.beforeFirst();
			
			while (indexes.next())
			{
				Integer id = indexes.getInt("idSignal");
				Long evolution;
				
				if (occurrences.containsKey(id))
				{
					Integer[] data = (Integer[]) occurrences.get(id);
					evolution = (long) data[1];
				}
				else
				{
					evolution = indexes.getLong("value") - indexes.getLong("lastvalue");
				}
				
				writer.writeNext(new String[] {indexes.getString("label"), String.valueOf(indexes.getLong("value")), evolution.toString() });
			}
			
			writer.close();
			System.out.println("VigieReport : Report generated : " + reportName);	
			
			// Warning ! This function is slow as it has to update every row in the Totalizer table ! Maybe it would be better to put that in the end...
			UpdateTotalizers(indexes);
			
			indexes = null;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void MeasuresReport()
	{
		try
		{
			CachedRowSet digitalmeasures = getDigitalMeasures.getCachedRowSet();
			CachedRowSet analogmesures = getAnalogMeasures.getCachedRowSet();

			class measure
			{
				Integer id = null;
				Double value = null;
				Long datetime = null;
				String label = null;
				String unit = "";

				public measure(Integer _id, Double _value, Long _datetime, String _label, String _unit)
				{
					this.id = _id;
					this.value = _value;
					this.datetime = _datetime;
					this.label = _label;
					
					if (_unit != null)
					{
						this.unit = _unit;
					}
				}
			}

			List<measure> measureList = new ArrayList<measure>();


			digitalmeasures.beforeFirst();

			while (digitalmeasures.next())
			{
				measureList.add(new measure(digitalmeasures.getInt("idSignal"), (double) CH4P_Functions.boolToInt(digitalmeasures.getBoolean("value")),
						digitalmeasures.getTimestamp("datetime").getTime(), digitalmeasures.getString("label"), null));
			}
			
			digitalmeasures = null;
			
			
			analogmesures.beforeFirst();
			
			while (analogmesures.next())
			{
				Integer value = analogmesures.getInt("value");
				Float coeff = analogmesures.getFloat("coeff");
				String val;
				
				if (coeff != null & coeff != 0.0f)
				{
					Float f = coeff * value;
					val = f.toString();
				}
				else
				{
					val = value.toString();
				}
				
				Double dblVal = Double.parseDouble(val);
				
				measureList.add(new measure(analogmesures.getInt("idSignal"), dblVal,
						analogmesures.getTimestamp("datetime").getTime(), analogmesures.getString("label"), analogmesures.getString("unit")));
			}
			
			measureList.sort((d1, d2) -> d1.datetime.compareTo(d2.datetime));
			
			Calendar now = Calendar.getInstance();
			SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd_HH'h'mm");
			String date = format.format(now.getTime());
			String reportName = "CH4Process_Rapport_Mesures_" + date + ".txt";
			CSVWriter writer = new CSVWriter( new OutputStreamWriter(new FileOutputStream(reportName), StandardCharsets.UTF_8));
			
			writer.writeNext(new String[] {"CH4Process"});
			writer.writeNext(new String[] {"Rapport des mesures du : ", date});
			writer.writeNext(new String[] {""});
			
			writer.writeNext(new String[] {"CAPTEUR", "HEURE" , "VALEUR", "UNITE"});
			
			for(measure m : measureList)
			{
				format.applyPattern("HH:mm:ss dd/MM/yyyy");
				String datetime = format.format(new Date(m.datetime));
				writer.writeNext(new String[] {m.label, datetime, m.value.toString(), m.unit });
			}
			
			writer.close();
			System.out.println("VigieReport : Report generated : " + reportName);	
			
			measureList = null;
			digitalmeasures = null;
			analogmesures = null;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

	}
	
	private void ScenariosReport()
	{
		try
		{
			CachedRowSet scenarios = getScenariosRequest.getCachedRowSet();
			
			Calendar now = Calendar.getInstance();
			SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd_HH'h'mm");
			String date = format.format(now.getTime());
			String reportName = "CH4Process_Rapport_Scenarios_" + date + ".txt";
			CSVWriter writer = new CSVWriter( new OutputStreamWriter(new FileOutputStream(reportName), StandardCharsets.UTF_8));
			
			writer.writeNext(new String[] {"CH4Process"});
			writer.writeNext(new String[] {"Rapport des scenarios du : ", date});
			writer.writeNext(new String[] {""});
			
			writer.writeNext(new String[] {"MESSAGE", "HEURE"});
			
			while (scenarios.next())
			{
				format.applyPattern("HH:mm:ss dd/MM/yyyy");
				String datetime = format.format(new Date(scenarios.getTimestamp("datetime").getTime()));
				writer.writeNext(new String[] {scenarios.getString("eventMessage"), datetime});
			}
			
			writer.close();
			System.out.println("VigieReport : Report generated : " + reportName);	
			
			scenarios = null;
			
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	private void SendReports()
	{
		// Mail setup
		Mail mail = new Mail();
		
		Calendar now = Calendar.getInstance();
		SimpleDateFormat format = new SimpleDateFormat("HH'h'mm dd/MM/yyyy");
		String date = format.format(now.getTime());
		
		String subject = "Rapports d'exploitation du " + date;
		
		mail.setAuthenticationType(Mail.AUTH_SSL);
		mail.setFrom(mail.getUsername());
		mail.setSubject(subject);
		mail.setTo(mail.getReportRecipients());
		mail.setText("");
		
		// Attachments setup
		mail.addAttachmentsFolder(CH4P_System.PATH_Vigie_Reports);
		
		if (mail.sendMail())
		{
			System.out.println("VigieReport : REPORTS sent !");
			SaveReports();
		}
		else
		{
			System.out.println("VigieReport : Failed to send REPORTS !");
		}
	}
	
	private void SaveReports()
	{
		try
		{
			String source = CH4P_System.PATH_Vigie_Reports;
			
			File[] files = new File(source).listFiles();
			
			for (File file : files)
			{
				Path destination = FileSystems.getDefault().getPath(CH4P_System.PATH_Vigie_Reports_Sent + FileSystems.getDefault().getSeparator() + file.getName());
				Files.move(file.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
}
