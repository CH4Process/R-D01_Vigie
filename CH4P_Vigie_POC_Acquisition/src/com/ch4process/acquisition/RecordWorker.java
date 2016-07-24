package com.ch4process.acquisition;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import com.ch4process.database.DatabaseRequest;
import com.ch4process.database.IDatabaseRequestCallback;
import com.ch4process.events.SignalValueEvent;

public class RecordWorker extends Thread implements ISignalValueListener
{
	DatabaseRequest recordValueRequest;
	IDatabaseRequestCallback recordValueRequestCallback;
	boolean recordValueRequest_done = true;
	
	List<SignalValueEvent> eventList = new LinkedList<>();
	
	Integer DOUBLE_VALUE_EVENT = 1;
	Integer INTEGER_VALUE_EVENT = 2;
	Integer BOOLEAN_VUE_EVENT = 3;
	
	@Override
	public void doubleValueChanged(int idSignal, double value, long datetime)
	{
		eventList.add(new SignalValueEvent(idSignal, value, datetime));
	}

	@Override
	public void intValueChanged(int idSignal, int value, long datetime)
	{
		eventList.add(new SignalValueEvent(idSignal, value, datetime));
	}

	@Override
	public void boolValueChanged(int idSignal, boolean value, long datetime)
	{
		eventList.add(new SignalValueEvent(idSignal, value, datetime));
	}
	
	public RecordWorker(DatabaseRequest recordValueRequest)
	{
		
		this.recordValueRequest = recordValueRequest;
		
		recordValueRequestCallback = new IDatabaseRequestCallback()
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
		System.out.println("recordWorker start : " + Calendar.getInstance().getTime());
		this.recordValueRequest.setCallback(recordValueRequestCallback);
		this.recordValueRequest.start();
		super.start();
	}
	
	public void run()
	{
		try
		{
			while (true)
			{
				eventHandling();
				Thread.sleep(1000);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void eventHandling()
	{
		if (recordValueRequest_done == true)
		{
			if (eventList.size() > 0)
			{
				
				SignalValueEvent event = eventList.get(0);
				int type = event.getType();

				recordValueRequest.setStatementIntParameter(1, event.getIdSignal());
				recordValueRequest.setStatementDateParameter(3, event.getDatetime());

				if (type == DOUBLE_VALUE_EVENT)
				{
					recordValueRequest.setStatementDoubleParameter(2, event.getDoubleValue());
				}
				else if (type == INTEGER_VALUE_EVENT)
				{
					recordValueRequest.setStatementIntParameter(2, event.getIntValue());
				}
				else if (type == BOOLEAN_VUE_EVENT)
				{
					if (event.getBoolValue() == true)
					{
						recordValueRequest.setStatementIntParameter(2, 1);
					}
					else
					{
						recordValueRequest.setStatementIntParameter(2, 0);
					}
				}
				
				recordValueRequest_done = false;
				recordValueRequest.doUpdate();
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
			recordValueRequest_done = true;
		}
	}
}
