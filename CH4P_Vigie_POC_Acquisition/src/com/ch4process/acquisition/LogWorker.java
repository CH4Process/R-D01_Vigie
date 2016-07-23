package com.ch4process.acquisition;

import java.awt.event.ActionEvent;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import com.ch4process.database.DatabaseRequest;
import com.ch4process.database.IDatabaseRequestCallback;
import com.ch4process.events.SignalValueEvent;

public class LogWorker extends Thread implements IActionEventListener
{
	DatabaseRequest eventRecordRequest;
	IDatabaseRequestCallback eventRecordRequestCallback;
	boolean eventRecordRequest_done = true;
	
	List<scenarioEvent> eventList = new LinkedList<>();

	private class scenarioEvent
	{
		Integer scenario_id;
		Long date;
		
		public scenarioEvent(Integer id, Long date)
		{
			this.scenario_id = id;
			this.date = date;
		}

		public Integer getScenario_id()
		{
			return scenario_id;
		}

		public Long getDate()
		{
			return date;
		}
	}
	
	@Override
	public void onActionEvent(Integer scenario_id, Long datetime)
	{
		this.eventList.add(new scenarioEvent(scenario_id,datetime));
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
		System.out.println("LogWorker start : " + Calendar.getInstance().getTime());
		this.eventRecordRequest.setCallback(eventRecordRequestCallback);
		this.eventRecordRequest.start();
		super.start();
	}
	
	public void run()
	{
		try
		{
			while (true)
			{
				eventHandling();
				Thread.sleep(5 * 1000);
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
		
				eventRecordRequest.setStatementIntParameter(1, event.getScenario_id());
				eventRecordRequest.setStatementDateParameter(2, event.getDate());
				
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
	
	
}
