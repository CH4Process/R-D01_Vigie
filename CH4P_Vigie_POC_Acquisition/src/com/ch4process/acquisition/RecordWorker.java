package com.ch4process.acquisition;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.xml.ws.RequestWrapper;

import com.ch4process.database.ConnectionHandler;
import com.ch4process.database.DatabaseRequest;
import com.ch4process.database.IDatabaseRequestCallback;
import com.ch4process.database.RequestList;
import com.ch4process.events.SignalValueEvent;
import com.ch4process.utils.CH4P_Exception;

public class RecordWorker implements Callable<Integer>, ISignalValueListener
{
	DatabaseRequest recordDigitalValueRequest;
	DatabaseRequest recordAnalogValueRequest;
	DatabaseRequest recordTotalizerValueRequest;
	
	IDatabaseRequestCallback recordValueRequestCallback;
	boolean recordValueRequest_done = true;
	
	List<SignalValueEvent> eventList = new LinkedList<>();
	
	Boolean init_done = false;
	
	@Override
	public void SignalValueChanged(SignalValueEvent event)
	{
		eventList.add(event);
		System.out.println("RecordWorker - Event received ID : " + event.getIdSignal());
	}
	
	public RecordWorker(ConnectionHandler connectionHandler)
	{
		recordValueRequestCallback = new IDatabaseRequestCallback()
		{
			
			@Override
			public void databaseRequestCallback()
			{
				deleteEvent();
			}
		};
		
		recordDigitalValueRequest = new DatabaseRequest(connectionHandler, RequestList.REQUEST_RecordDigitalMeasure, recordValueRequestCallback);
		recordAnalogValueRequest = new DatabaseRequest(connectionHandler, RequestList.REQUEST_RecordAnalogMeasure, recordValueRequestCallback);
		recordTotalizerValueRequest = new DatabaseRequest(connectionHandler, RequestList.REQUEST_RecordTotalizer, recordValueRequestCallback);
	}

	
	private void eventHandling()
	{
		System.out.println("RecordWorker - EventHandling.");
		
		if (recordValueRequest_done == true)
		{
			if (eventList.size() > 0)
			{
				System.out.println("RecordWorker - EventHandling - Event .");
				
				SignalValueEvent event = eventList.get(0);
				SignalType type = event.getType();
				DatabaseRequest request;
				
				System.out.println("RecordWorker - EventHandling - Event idSignal = " + event.getIdSignal());
				
				// Putting the right values in the statement
				if (type.isTor)
				{
					System.out.println("RecordWorker - EventHandling - Tor Event.");
					
					request = recordDigitalValueRequest;
					
					if (event.getBoolValue() == true)
					{
						request.setStatementIntParameter(1, 1);
					}
					else
					{
						request.setStatementIntParameter(1, 0);
					}
				}
				else
				{
					if (type.isTotalizer)
					{
						System.out.println("RecordWorker - EventHandling - Totalizer Event.");
						request = recordTotalizerValueRequest;
					}
					else
					{
						System.out.println("RecordWorker - EventHandling - Analog Event.");
						request = recordAnalogValueRequest;
					}

					if (event.getDoubleValue() != null)
					{
						long value = Math.round(event.getDoubleValue());
						request.setStatementDoubleParameter(1, value);
					}
					else if (event.getIntValue() != null)
					{
						long value = (long) event.getIntValue();
						request.setStatementDoubleParameter(1, value);
					}
					else
					{
						// No value at all so we put zero
						request.setStatementDoubleParameter(1, 0);
					}
				}
				
				
				
				request.setStatementDateParameter(2, event.getDatetime());
				request.setStatementBoolParameter(3, event.isValid());
				request.setStatementIntParameter(4, event.getIdSignal());
				
				System.out.println("RecordWorker - EventHandling - Statements : idSignal : " + event.getIdSignal() );
				
				recordValueRequest_done = false;
				request.doUpdate();
				
				System.out.println("RecordWorker - EventHandling - Update done.");
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
		if (! this.init_done)
		{
			System.out.println("recordWorker start : " + Calendar.getInstance().getTime());
			
			recordDigitalValueRequest.start();
			recordAnalogValueRequest.start();
			recordTotalizerValueRequest.start();
			
			this.init_done = true;
			this.recordValueRequest_done = true;
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
			ex.printStackTrace();
			throw new CH4P_Exception(ex.getMessage(), ex.getCause());
		}
	}

	
}
