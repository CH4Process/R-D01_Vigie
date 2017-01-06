package com.ch4process.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
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
import java.util.Properties;
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
import com.ch4process.utils.CH4P_ConfigManager;
import com.ch4process.utils.CH4P_Exception;
import com.ch4process.utils.CH4P_Functions;
import com.ch4process.utils.CH4P_Multithreading;
import com.ch4process.utils.CH4P_System;
import com.opencsv.CSVWriter;


public class VigieReport implements Callable<Integer>
{
	private String reportScheduleHour = "23:30";
	private Integer reportSpan = 7;
	private final Integer REPORT_SCHEDULE_DAILY = 1;
	private final Integer REPORT_SCHEDULE_WEEKLY = 2;
	private final Integer REPORT_SCHEDULE_MONTHLY = 3;
	private Integer reportScheduleFrequency = 0;
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
	
	int currentStep = 0;
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
		catch (Exception ex)
		{
			throw new CH4P_Exception("-VigieReport call error-" + ex.getMessage(), ex.getCause());
		}
		finally
		{
			return null;
		}
	}
	
	private void ScheduleInit()
	{
		reportTime = Calendar.getInstance();
		String[] time;
		Properties prop = CH4P_ConfigManager.getReportConfig().GetProperties();
		if (prop != null)
		{
			time = prop.getProperty("reporttime").split(":");

			switch (prop.getProperty("reportschedule"))
			{
				case "quotidien": 
					reportScheduleFrequency = REPORT_SCHEDULE_DAILY; 
					reportSpan = 1;
					break;
					
				case "hebdomadaire": 
					reportScheduleFrequency = REPORT_SCHEDULE_WEEKLY;

					switch (prop.getProperty("reportday"))
					{
						case "lundi": reportTime.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY); break;
						case "mardi": reportTime.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY); break;
						case "mercredi": reportTime.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY); break;
						case "jeudi": reportTime.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY); break;
						case "vendredi": reportTime.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY); break;
						case "samedi": reportTime.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY); break;
						case "dimanche": reportTime.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY); break;
					}
					
					reportSpan = 7;
					break;
					
				case "mensuel":
					reportScheduleFrequency = REPORT_SCHEDULE_DAILY;
					reportSpan = 1;
					break;
			}
		}
		else
		{
			time = reportScheduleHour.split(":");
			reportScheduleFrequency = REPORT_SCHEDULE_DAILY;
		}

		reportTime.set(Calendar.HOUR_OF_DAY, Integer.valueOf(time[0]));
		reportTime.set(Calendar.MINUTE, Integer.valueOf(time[1]));
		reportTime.set(Calendar.SECOND, 0);
		
		if (Calendar.getInstance().after(reportTime))
		{
			// If we are already marked as AFTER the reportTime then we shift of a week
			reportTime.add(Calendar.DAY_OF_MONTH, reportSpan);
		}
		
	}
	
	public void start() throws CH4P_Exception
	{
		try
		{
			CH4P_Functions.Log(this.getClass().getName(), CH4P_Functions.LOG_inConsole, 100, threadName + " start : " + Calendar.getInstance().getTime());

			initDatabaseRelations();
			ScheduleInit();				
			
			CH4P_Functions.Log(this.getClass().getName(), CH4P_Functions.LOG_inConsole, 100, threadName + " next report generation on : " + reportTime.getTime());
		}
		catch (Exception ex)
		{
			throw new CH4P_Exception("-VigieReport start error-" + ex.getMessage(), ex.getCause());
		}
	}
	
	public void run() throws CH4P_Exception
	{
		while (true)
		{
			try
			{
				if (isReportTime() && currentStep == 0)
				{
					getDatas();
					currentStep = 1;
				}
				
				// Report based on totalizers
				if (getDigitalMeasuresRequest_done && getTotalizersRequest_done && currentStep == 1 && yield)
				{
					yield = false;
					FaultsReport();
					getTotalizersRequest_done = false;
					currentStep = 2;
				}
				
				// Report based on measures
				if (getAnalogMeasuresRequest_done && getDigitalMeasuresRequest_done && currentStep == 2 && yield)
				{
					yield = false;
					MeasuresReport();
					getDigitalMeasuresRequest_done = false;
					getAnalogMeasuresRequest_done = false;
					currentStep = 3;
				}
				
				// Report based on the scenarios
				if (getScenariosRequest_done && currentStep == 3 && yield)
				{
					yield = false;
					ScenariosReport();
					getScenariosRequest_done = false;
					currentStep = 4;
				}
				
				if (currentStep == 4 && yield)
				{
					yield = false;
					currentStep = 0;
					// Update reportTime to set it to next week
					reportTime.add(Calendar.DAY_OF_MONTH, reportSpan);
					SendReports();
				}
				
				
				Thread.sleep(60 * 1000);
				yield = true;
			}
			catch (InterruptedException ex)
			{
				continue;
			}
			catch (Exception ex)
			{
				throw new CH4P_Exception("-VigieReport run error-" + ex.getMessage(), ex.getCause());
			}
		}
	}
	
	private void initDatabaseRelations() throws CH4P_Exception
	{
		try
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
			updateTotalizers = new DatabaseRequest(connectionHandler, RequestList.REQUEST_UpdateTotalizer, updateTotalizersRequestCallback);
			getScenariosRequest = new DatabaseRequest(connectionHandler, RequestList.REQUEST_Scenarios, getScenariosRequestCallback);

			getDigitalMeasures.start();
			getAnalogMeasures.start();
			getTotalizers.start();
			updateTotalizers.start();
			getScenariosRequest.start();
		}
		catch (Exception ex)
		{
			throw new CH4P_Exception("-VigieReport InitDatabaseRelations error-" + ex.getMessage(), ex.getCause());
		}
	}

	private boolean isReportTime() throws CH4P_Exception
	{
		try
		{
			Calendar now = Calendar.getInstance();
			
			if (now.after(reportTime))
			{
				return true;
			}
			return false;
		}
		catch (Exception ex)
		{
			throw new CH4P_Exception("-VigieReport isReportTime error-" + ex.getMessage(), ex.getCause());
		}
	}
	
	private String GetFormatedDate(Calendar date, String pattern) throws CH4P_Exception
	{
		try
		{
			SimpleDateFormat format = new SimpleDateFormat(pattern);
			return format.format(date.getTime());
		}
		catch (Exception ex)
		{
			throw new CH4P_Exception("-VigieReport GetFormatedDate error-" + ex.getMessage(), ex.getCause());
		}
	}
	
	private CSVWriter GetWriter(String reportName) throws CH4P_Exception
	{
		try
		{
			reportName = CH4P_System.PATH_Vigie_Reports + CH4P_System.GetSeparator() + reportName;
			return new CSVWriter( new OutputStreamWriter(new FileOutputStream(reportName), StandardCharsets.UTF_8));
		}
		catch (Exception ex)
		{
			throw new CH4P_Exception("-VigieReport GetWriter error-" + ex.getMessage(), ex.getCause());
		}
	}

	private void getDatas() throws CH4P_Exception
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
			getScenariosRequest.setStatementDateParameter(1, startDay.getTimeInMillis());
			getScenariosRequest.doQuery();
		}
		catch (Exception ex)
		{
			throw new CH4P_Exception("-VigieReport GetDatas error-" + ex.getMessage(), ex.getCause());
		}
	}
	
	private void UpdateTotalizers(CachedRowSet _indexes) throws CH4P_Exception
	{
		Callable<Integer> task = new Callable<Integer>()
		{

			@Override
			public Integer call() throws CH4P_Exception
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
						updateTotalizers.doUpdate();
						
						while (updateTotalizersRequest_done == false)
						{
							Thread.sleep(500);
						}
					}
				}
				catch (Exception ex)
				{
					throw new CH4P_Exception("-VigieReport UpdateTotalizer internal class error-" + ex.getMessage(), ex.getCause());
				}
				return null;
			}
		};
		
		try
		{
			CH4P_Multithreading.Submit(task);
		}
		catch (Exception ex)
		{
			throw new CH4P_Exception("-VigieReport UpdateTotalizer error-" + ex.getMessage(), ex.getCause());
		}
	}
	
	private void FaultsReport() throws CH4P_Exception
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
			
			String date = GetFormatedDate(Calendar.getInstance(), "yyyy_MM_dd_HH'h'mm");
			String reportName = "CH4Process_Rapport_Defauts_" + date + ".txt";
			CSVWriter writer = GetWriter(reportName);
			
			writer.writeNext(new String[] {"CH4Process"});
			writer.writeNext(new String[] {"Rapport des défauts du : ", date});
			writer.writeNext(new String[] {""});
			
			writer.writeNext(new String[] {"CAPTEUR", "TOTALISATEUR", "PERIODE"});
			
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
			CH4P_Functions.Log(this.getClass().getName(), CH4P_Functions.LOG_inConsole, 100, "VigieReport : Report generated : " + reportName);	
			
			UpdateTotalizers(indexes);
			
			indexes = null;
		}
		catch (Exception ex)
		{
			throw new CH4P_Exception("-VigieReport FaultsReport error-" + ex.getMessage(), ex.getCause());
		}
	}
	
	private void MeasuresReport() throws CH4P_Exception
	{
		try
		{
			Map<String, Integer> dataMap = new HashMap<>();
			Map<String, String> dataUnitMap = new HashMap<>();
			dataMap.put("DATE", 0);
			Integer reportColumn = 1;
			
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
				if (dataMap.putIfAbsent(digitalmeasures.getString("shortName"), reportColumn) == null)
				{
					dataUnitMap.put(digitalmeasures.getString("shortName"), "");
					reportColumn += 2;
				}
				measureList.add(new measure(digitalmeasures.getInt("idSignal"), (double) CH4P_Functions.boolToInt(digitalmeasures.getBoolean("value")),
						digitalmeasures.getTimestamp("datetime").getTime(), digitalmeasures.getString("shortName"), null));
			}
			
			digitalmeasures = null;
			
			
			analogmesures.beforeFirst();
			

			while (analogmesures.next())
			{
				if (dataMap.putIfAbsent(analogmesures.getString("shortName"), reportColumn) == null)
				{
					dataUnitMap.put(analogmesures.getString("shortName"), analogmesures.getString("unit"));
					reportColumn += 2;
				}
				
				Integer value = analogmesures.getInt("value");
				BigDecimal precision = analogmesures.getBigDecimal("precision");
				String val;
				
				if (precision != null && precision != BigDecimal.ZERO)
				{
					BigDecimal f = precision.multiply(BigDecimal.valueOf(value));
					val = f.toString();
				}
				else
				{
					val = value.toString();
				}
				
				Double dblVal = Double.parseDouble(val);
				
				measureList.add(new measure(analogmesures.getInt("idSignal"), dblVal,
						analogmesures.getTimestamp("datetime").getTime(), analogmesures.getString("shortName"), analogmesures.getString("unit")));
			}
			
			measureList.sort((d1, d2) -> d1.datetime.compareTo(d2.datetime));
			
			String date = GetFormatedDate(Calendar.getInstance(), "yyyy_MM_dd_HH'h'mm");
			String reportName = "CH4Process_Rapport_Mesures_" + date + ".txt";
			CSVWriter writer = GetWriter(reportName);
					
			writer.writeNext(new String[] {"CH4Process"});
			writer.writeNext(new String[] {"Rapport des mesures du : ", date});
			writer.writeNext(new String[] {""});
			
			//writer.writeNext(new String[] {"CAPTEUR", "HEURE" , "VALEUR", "UNITE"});
			String[] line = new String[reportColumn];
			
			for(Map.Entry<String, Integer> data:dataMap.entrySet())
			{
				line[data.getValue()] = data.getKey();
				line[data.getValue() +1] = dataUnitMap.get(data.getKey());
			}
			
			String currentDatetime = "";
			
			for(measure m : measureList)
			{
				//SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
				SimpleDateFormat format = new SimpleDateFormat("HH:mm dd/MM/yyyy");
				String datetime = format.format(new Date(m.datetime));
				
				// We construct the line to write
				
				if (! datetime.equals(currentDatetime))
				{
					// new datetime so new line
					writer.writeNext(line);
					currentDatetime = datetime;
				}
				
				line[0] = datetime;
				line[dataMap.get(m.label)] = m.value.toString().replace(".", ",");
				line[dataMap.get(m.label) + 1] = "";
				//writer.writeNext(new String[] {m.label, datetime, m.value.toString().replace(".", ","), m.unit });
			}
			
			writer.writeNext(line);
			writer.close();
			CH4P_Functions.Log(this.getClass().getName(), CH4P_Functions.LOG_inConsole, 100, "VigieReport : Report generated : " + reportName);	
			
			measureList = null;
			digitalmeasures = null;
			analogmesures = null;
		}
		catch (Exception ex)
		{
			throw new CH4P_Exception("-VigieReport MeasureReport error-" + ex.getMessage(), ex.getCause());
		}

	}
	
	private void ScenariosReport() throws CH4P_Exception
	{
		try
		{
			CachedRowSet scenarios = getScenariosRequest.getCachedRowSet();
			
			String date = GetFormatedDate(Calendar.getInstance(), "yyyy_MM_dd_HH'h'mm");
			String reportName = "CH4Process_Rapport_Scenarios_" + date + ".txt";
			CSVWriter writer = GetWriter(reportName);
			
			writer.writeNext(new String[] {"CH4Process"});
			writer.writeNext(new String[] {"Rapport des scenarios du : ", date});
			writer.writeNext(new String[] {""});
			
			writer.writeNext(new String[] {"MESSAGE", "HEURE"});
			
			while (scenarios.next())
			{
				SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
				String datetime = format.format(new Date(scenarios.getTimestamp("datetime").getTime()));
				writer.writeNext(new String[] {scenarios.getString("eventMessage"), datetime});
			}
			
			writer.close();
			CH4P_Functions.Log(this.getClass().getName(), CH4P_Functions.LOG_inConsole, 100, "VigieReport : Report generated : " + reportName);	
			
			scenarios = null;
			
		}
		catch (Exception ex)
		{
			throw new CH4P_Exception("-VigieReport ScenariosReport error-" + ex.getMessage(), ex.getCause());
		}
	}
	
	private void SendReports() throws CH4P_Exception
	{
		try
		{
			// Mail setup
			Mail mail = new Mail();

			String date = GetFormatedDate(Calendar.getInstance(), "dd/MM/yyyy HH'h'mm");

			String subject = "Rapports d'exploitation du " + date;

			mail.setAuthenticationType(Mail.AUTH_SSL);
			mail.setFrom(mail.getUsername());
			mail.setSubject(subject);
			mail.setTo(CH4P_ConfigManager.getReportConfig().GetProperties().getProperty("reportrecipients"));
			mail.setText("");

			// Attachments setup
			mail.addAttachmentsFolder(CH4P_System.PATH_Vigie_Reports);

			if (mail.sendMail())
			{
				CH4P_Functions.Log(this.getClass().getName(), CH4P_Functions.LOG_inConsole, 100, "VigieReport : REPORTS sent !");
				SaveReports();
			}
			else
			{
				CH4P_Functions.Log(this.getClass().getName(), CH4P_Functions.LOG_inConsole, 100, "VigieReport : Failed to send REPORTS !");
			}
		}
		catch (Exception ex)
		{
			throw new CH4P_Exception("-VigieReport SendReports error-" + ex.getMessage(), ex.getCause());
		}
	}
	
	private void SaveReports() throws CH4P_Exception
	{
		try
		{
			String source = CH4P_System.PATH_Vigie_Reports;
			
			File[] files = new File(source).listFiles();
			
			for (File file : files)
			{
				Path destination = CH4P_System.GetPath(CH4P_System.PATH_Vigie_Reports_Sent + CH4P_System.GetSeparator() + file.getName());
				Files.move(file.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
			}
		}
		catch (Exception ex)
		{
			throw new CH4P_Exception("-VigieReport SaveReports error-" + ex.getMessage(), ex.getCause());
		}
	}
	
}
