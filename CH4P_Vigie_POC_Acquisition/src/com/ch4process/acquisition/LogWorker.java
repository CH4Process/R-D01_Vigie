package com.ch4process.acquisition;

import java.awt.event.ActionEvent;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import com.ch4process.database.DatabaseRequest;
import com.ch4process.database.IDatabaseRequestCallback;
import com.ch4process.events.SignalValueEvent;
import com.ch4process.utils.CH4P_Functions;

public class LogWorker implements Callable<Integer>, IScenarioEventListener
{
	DatabaseRequest eventRecordRequest;
	IDatabaseRequestCallback eventRecordRequestCallback;
	boolean eventRecordRequest_done = true;
	
	List<scenarioEvent> eventList = new LinkedList<>();

	private class scenarioEvent
	{
		String name;
		String message;
		Long date;
		Integer errorLevel;
		
		public scenarioEvent(String _name, String _message, Long _date, Integer _errorLevel)
		{
			this.name = _name;
			this.message = _message;
			this.date = _date;
			this.errorLevel = _errorLevel;
		}
	}
	
	@Override
	public void onScenarioEvent(String _name, String _message, Long _datetime, Integer _code)
	{
		this.eventList.add(new scenarioEvent(_name, _message, _datetime, _code));
	}
	
	public LogWorker(DatabaseRequest databaseRequest)
	{
		this.eventRecordRequest = databaseRequest;
		
		eventRecordRequestCallback = new IDatabaseRequestCallback()
		{
			
			@Override
			public void databaseRequestCallback()
			{
				deleteEvent();
			}
		};
	}
	
	public void start()
	{
		CH4P_Functions.Log(this.getClass().getName(), CH4P_Functions.LOG_inConsole, 100, "LogWorker start : " + Calendar.getInstance().getTime());
		this.eventRecordRequest.setCallback(eventRecordRequestCallback);
		this.eventRecordRequest.start();
	}
	
	public void run()
	{
		try
		{
			while (true)
			{
				eventHandling();
				Thread.sleep(10 * 1000);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void eventHandling()
	{
		if (eventRecordRequest_done == true)
		{
			if (eventList.size() > 0)
			{
				
				scenarioEvent event = eventList.get(0);
		
				eventRecordRequest.setStatementStringParameter(1, event.name);
				eventRecordRequest.setStatementStringParameter(2, event.message);
				eventRecordRequest.setStatementIntParameter(3, event.errorLevel);
				eventRecordRequest.setStatementDateParameter(4, event.date);
				
				eventRecordRequest_done = false;
				eventRecordRequest.doUpdate();
			}

		}
	}
	
	private void deleteEvent()
	{
		try
		{
			eventList.remove(0);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			eventRecordRequest_done = true;
		}
	}

	@Override
	public Integer call() throws Exception
	{
		try
		{
			start();
			run();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return null;
		}
		return null;
	}
	
	
}
