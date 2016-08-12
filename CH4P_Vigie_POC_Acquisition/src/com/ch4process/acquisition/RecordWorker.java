package com.ch4process.acquisition;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.xml.ws.RequestWrapper;

import com.ch4process.database.DatabaseRequest;
import com.ch4process.database.IDatabaseRequestCallback;
import com.ch4process.database.RequestList;
import com.ch4process.events.SignalValueEvent;
import com.ch4process.utils.CH4P_Exception;

public class RecordWorker implements Callable<Integer>, ISignalValueListener
{
	DatabaseRequest recordValueRequest;
	IDatabaseRequestCallback recordValueRequestCallback;
	boolean recordValueRequest_done = true;
	
	List<SignalValueEvent> eventList = new LinkedList<>();
	
	Boolean init_done = false;
	
	@Override
	public void doubleValueChanged(int idSignal, double value, boolean quality, long datetime)
	{
		eventList.add(new SignalValueEvent(idSignal, value, quality, datetime));
	}

	@Override
	public void intValueChanged(int idSignal, int value, boolean quality, long datetime)
	{
		eventList.add(new SignalValueEvent(idSignal, value, quality, datetime));
	}

	@Override
	public void boolValueChanged(int idSignal, boolean value, boolean quality, long datetime)
	{
		eventList.add(new SignalValueEvent(idSignal, value, quality, datetime));
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

	
	private void eventHandling()
	{
		if (recordValueRequest_done == true)
		{
			if (eventList.size() > 0)
			{
				
				SignalValueEvent event = eventList.get(0);
				int type = event.getType();
				
				// Putting the right values in the statement
				switch (type)
				{
					case SignalValueEvent.DOUBLE_VALUE_EVENT: 
						recordValueRequest.setRequest(RequestList.REQUEST_RecordAnalogMeasure);
						recordValueRequest.setStatementDoubleParameter(1, event.getDoubleValue());
						break;
						
					case SignalValueEvent.INTEGER_VALUE_EVENT:
						recordValueRequest.setRequest(RequestList.REQUEST_RecordAnalogMeasure);
						recordValueRequest.setStatementIntParameter(1, event.getIntValue());
						break;
						
					case SignalValueEvent.BOOLEAN_VALUE_EVENT:
						recordValueRequest.setRequest(RequestList.REQUEST_RecordDigitalMeasure);
						if (event.getBoolValue() == true)
						{
							recordValueRequest.setStatementIntParameter(1, 1);
						}
						else
						{
							recordValueRequest.setStatementIntParameter(1, 0);
						}
						break;
						
					case SignalValueEvent.TOTALIZER_EVENT:
					{
						recordValueRequest.setRequest(RequestList.REQUEST_RecordTotalizer);
						recordValueRequest.setStatementDoubleParameter(1, event.getDoubleValue());
						break;
					}
				}
				recordValueRequest.setStatementDateParameter(2, event.getDatetime());
				recordValueRequest.setStatementBoolParameter(3, event.isValid());
				recordValueRequest.setStatementIntParameter(4, event.getIdSignal());
				
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

	@Override
	public Integer call() throws CH4P_Exception
	{
		if (! init_done)
		{
			System.out.println("recordWorker start : " + Calendar.getInstance().getTime());
			this.recordValueRequest.setCallback(recordValueRequestCallback);
			this.recordValueRequest.start();
			init_done = true;
		}
		
		try
		{
			while (true)
			{
				eventHandling();
				Thread.sleep(1000);
			}
		}
		catch (Exception ex)
		{
			throw new CH4P_Exception(ex.getMessage(), ex.getCause());
		}
	}
}
