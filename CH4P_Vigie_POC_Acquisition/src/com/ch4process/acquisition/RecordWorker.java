package com.ch4process.acquisition;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.xml.ws.RequestWrapper;

import com.ch4process.database.ConnectionHandler;
import com.ch4process.database.DatabaseRequest;
import com.ch4process.database.IDatabaseRequestCallback;
import com.ch4process.database.RequestList;
import com.ch4process.events.SignalValueEvent;
import com.ch4process.utils.CH4P_Exception;
import com.ch4process.utils.CH4P_Functions;

public class RecordWorker implements Callable<Integer>, ISignalValueListener
{
	DatabaseRequest recordDigitalValueRequest;
	DatabaseRequest recordAnalogValueRequest;
	DatabaseRequest recordTotalizerValueRequest;
	
	IDatabaseRequestCallback recordValueRequestCallback;
	boolean recordValueRequest_done = true;
	
	ConcurrentLinkedQueue<SignalValueEvent> eventList = new ConcurrentLinkedQueue<>();
	
	Boolean init_done = false;
	
	@Override
	public void SignalValueChanged(SignalValueEvent event)
	{
		eventList.add(event);
		//CH4P_Functions.Log(this.getClass().getName(), CH4P_Functions.LOG_inConsole, 100, "RecordWorker - Event received ID : " + event.getIdSignal());
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
		
		if (recordValueRequest_done == true)
		{
			if (! eventList.isEmpty())
			{
				SignalValueEvent event = eventList.peek();
				SignalType type = event.getType();
				DatabaseRequest request;
				
				// Putting the right values in the statement
				if (type.isTor)
				{
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
						request = recordTotalizerValueRequest;
					}
					else
					{
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
				
				
				recordValueRequest_done = false;
				request.doUpdate();
				
				//CH4P_Functions.Log(this.getClass().getName(), CH4P_Functions.LOG_inConsole, 100, "RecordWorker - EventHandling - Update done.");
			}

		}
	}
	
	private void deleteEvent()
	{
		try
		{
			eventList.remove();
		}
		catch (Exception ex)
		{
			CH4P_Functions.LogException(CH4P_Functions.LOG_inConsole, ex);
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
			CH4P_Functions.Log(this.getClass().getName(), CH4P_Functions.LOG_inConsole, 100, "recordWorker start : " + Calendar.getInstance().getTime());
			
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
			CH4P_Functions.LogException(CH4P_Functions.LOG_inConsole, ex);
			throw new CH4P_Exception(ex.getMessage(), ex.getCause());
		}
	}

	
}
