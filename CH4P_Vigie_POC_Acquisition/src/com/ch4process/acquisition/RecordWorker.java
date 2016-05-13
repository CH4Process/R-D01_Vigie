package com.ch4process.acquisition;

import java.util.LinkedList;
import java.util.List;
import com.ch4process.database.DatabaseRequest;
import com.ch4process.database.IDatabaseRequestCallback;

public class RecordWorker extends Thread implements ICapteurValueListener
{
	DatabaseRequest recordValueRequest;
	IDatabaseRequestCallback recordValueRequestCallback;
	boolean recordValueRequest_done = true;
	
	List<ValueEvent> eventList = new LinkedList<>();
	
	Integer DOUBLE_VALUE_EVENT = 1;
	Integer INTEGER_VALUE_EVENT = 2;
	Integer BOOLEAN_VUE_EVENT = 3;
	
	
	private class ValueEvent
	{
		int capteur_id;
		double doubleValue;
		int intValue;
		boolean boolValue;
		long datetime;
		int type = 0;
		
		public ValueEvent(int capteur_id, double value, long datetime)
		{
			this.capteur_id = capteur_id;
			this.doubleValue = value;
			this.datetime = datetime;
			this.type = DOUBLE_VALUE_EVENT;
		}
		
		public ValueEvent(int capteur_id, int value, long datetime)
		{
			this.capteur_id = capteur_id;
			this.intValue = value;
			this.datetime = datetime;
			this.type = INTEGER_VALUE_EVENT;
		}
		
		public ValueEvent(int capteur_id, boolean value, long datetime)
		{
			this.capteur_id = capteur_id;
			this.boolValue = value;
			this.datetime = datetime;
			this.type = BOOLEAN_VUE_EVENT;
		}

		public int getCapteur_id()
		{
			return capteur_id;
		}

		public double getDoubleValue()
		{
			return doubleValue;
		}

		public int getIntValue()
		{
			return intValue;
		}

		public boolean isBoolValue()
		{
			return boolValue;
		}

		public long getDatetime()
		{
			return datetime;
		}
		
		public int getType()
		{
			return type;
		}
		
		
	}

	@Override
	public void doubleValueChanged(int capteur_id, double value, long datetime)
	{
		eventList.add(new ValueEvent(capteur_id, value, datetime));
		System.out.println("recordWorker double event");
	}

	@Override
	public void intValueChanged(int capteur_id, int value, long datetime)
	{
		eventList.add(new ValueEvent(capteur_id, value, datetime));
		System.out.println("recordWorker int event");
	}

	@Override
	public void boolValueChanged(int capteur_id, boolean value, long datetime)
	{
		eventList.add(new ValueEvent(capteur_id, value, datetime));
		System.out.println("recordWorker bool event");
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
		System.out.println("recordWorker start !");
		this.recordValueRequest.setCallback(recordValueRequestCallback);
		this.recordValueRequest.start();
		super.start();
	}
	
	public void run()
	{
		try
		{
			System.out.println("recordWorker run method ..");
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
		System.out.println("recordWorker eventHandling : " + recordValueRequest_done);
		if (recordValueRequest_done == true)
		{
			if (eventList.size() > 0)
			{
				System.out.println("recordWorker evenement à gérer ! ");
				
				ValueEvent event = eventList.get(0);
				int type = event.getType();

				recordValueRequest.setStatementIntParameter(1, event.capteur_id);
				recordValueRequest.setStatementDateParameter(3, event.datetime);

				if (type == DOUBLE_VALUE_EVENT)
				{
					recordValueRequest.setStatementDoubleParameter(2, event.doubleValue);
				}
				else if (type == INTEGER_VALUE_EVENT)
				{
					recordValueRequest.setStatementIntParameter(2, event.intValue);
				}
				else if (type == BOOLEAN_VUE_EVENT)
				{
					if (event.boolValue == true)
					{
						recordValueRequest.setStatementIntParameter(2, 1);
					}
					else
					{
						recordValueRequest.setStatementIntParameter(2, 0);
					}
				}
				
				System.out.println("recordWorker event enregistré en base ! ");
				recordValueRequest_done = false;
				recordValueRequest.doUpdate();
			}

		}
	}
	
	private void deleteEvent()
	{
		try
		{
			System.out.println("recordWorker evenement supprimé");
			eventList.remove(0);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("recordWorker erreur de suppression d'evenement");
		}
		finally
		{
			recordValueRequest_done = true;
		}
	}
	
	

}
