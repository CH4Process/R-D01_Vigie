package com.ch4process.acquisition;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import com.ch4process.database.DatabaseRequest;
import com.ch4process.database.IDatabaseRequestCallback;
import com.ch4process.events.CapteurValueEvent;

public class RecordWorker extends Thread implements ICapteurValueListener
{
	DatabaseRequest recordValueRequest;
	IDatabaseRequestCallback recordValueRequestCallback;
	boolean recordValueRequest_done = true;
	
	Calendar date;
	
	List<CapteurValueEvent> eventList = new LinkedList<>();
	
	Integer DOUBLE_VALUE_EVENT = 1;
	Integer INTEGER_VALUE_EVENT = 2;
	Integer BOOLEAN_VUE_EVENT = 3;
	
	@Override
	public void doubleValueChanged(int capteur_id, double value, long datetime)
	{
		eventList.add(new CapteurValueEvent(capteur_id, value, datetime));
	}

	@Override
	public void intValueChanged(int capteur_id, int value, long datetime)
	{
		eventList.add(new CapteurValueEvent(capteur_id, value, datetime));
	}

	@Override
	public void boolValueChanged(int capteur_id, boolean value, long datetime)
	{
		eventList.add(new CapteurValueEvent(capteur_id, value, datetime));
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
		System.out.println("recordWorker start : " + date.getInstance().getTime());
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
				
				CapteurValueEvent event = eventList.get(0);
				int type = event.getType();

				recordValueRequest.setStatementIntParameter(1, event.getCapteur_id());
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
