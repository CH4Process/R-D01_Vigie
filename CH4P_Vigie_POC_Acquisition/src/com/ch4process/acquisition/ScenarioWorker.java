package com.ch4process.acquisition;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import javax.sql.rowset.CachedRowSet;
import javax.swing.event.EventListenerList;

import com.ch4process.database.DatabaseRequest;
import com.ch4process.database.IDatabaseRequestCallback;
import com.ch4process.events.CapteurValueEvent;

public class ScenarioWorker extends Thread implements ICapteurValueListener
{
	DatabaseRequest scenarioListRequest;
	IDatabaseRequestCallback scenarioListRequestCallback;
	boolean scenarioListRequest_done = true;
	
	Calendar date;
	
	List<CapteurValueEvent> eventList = new LinkedList<>();
	List<Scenario> scenarios = new LinkedList<>();
	EventListenerList listeners = new EventListenerList();
	
	Integer DOUBLE_VALUE_EVENT = 1;
	Integer INTEGER_VALUE_EVENT = 2;
	Integer BOOLEAN_VALUE_EVENT = 3;
	
	boolean busy = false;
	boolean init_done = false;

	
	public ScenarioWorker(DatabaseRequest scenarioListRequest)
	{
			this.scenarioListRequest = scenarioListRequest;
	}
	
	private void init()
	{
		init_done = true;
		
		scenarioListRequestCallback = new IDatabaseRequestCallback()
		{
			@Override
			public void databaseRequestCallback()
			{
				ScenarioList(scenarioListRequest.getCachedRowSet());
				scenarioListRequest_done = true;
				scenarioListRequest.close();
				scenarioListRequest = null;
			}
		};
		
		scenarioListRequest.setCallback(scenarioListRequestCallback);
		scenarioListRequest.start();
		scenarioListRequest.doQuery();
		
	}
	
	public void start()
	{
		System.out.println("scenarioWorker start : " + date.getInstance().getTime());
		super.start();
	}
	
	public void run()
	{
		if (! init_done)
		{
			init();
		}

		try
		{
			while (true)
			{
				if (scenarioListRequest_done)
				{
					eventHandling();
				}
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
		if (! busy)
		{
			while (eventList.size() > 0)
			{		
				CapteurValueEvent event = eventList.get(0);
				
				for(Scenario scenario : scenarios)
				{
					if (scenario.capteur_id.equals(event.getCapteur_id()))
					{
						Boolean isTriggered = false;
						
						if (event.getType() == DOUBLE_VALUE_EVENT)
						{
							isTriggered = scenario.testValue(event.getDoubleValue());
						}
						else if (event.getType() == INTEGER_VALUE_EVENT)
						{
							isTriggered = scenario.testValue(event.getIntValue());
						}
						else if (event.getType() == BOOLEAN_VALUE_EVENT)
						{
							isTriggered = scenario.testValue(event.getBoolValue());
						}
						
						if (isTriggered)
						{
							doScenario(scenario.getParams());
							busy = true;
						}
					}
				}
				deleteEvent();
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
			System.out.println("scenarioWorker erreur de suppression d'evenement");
		}
		finally
		{
			busy = false;
		}
	}

	private void doScenario(String parameters)
	{
		try
		{
			String[] params = parameters.split("\\=");
			String action = params[0];
			
			if (action.equals("CMD"))
			{
				doCMD(params[1]);
			}
			else if (action.equals("MAIL"))
			{
				//doMAIL(params);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void doCMD(String parameters)
	{
		try
		{
			String params[] = parameters.split("\\|");
			Integer capteur_id = Integer.valueOf(params[0]);
			boolean value;
			
			if (Integer.valueOf(params[1]) == 1)
			{
				value = true;
			}
			else
			{
				value = false;
			}
			
			fireCommandEvent(capteur_id, value);
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			deleteEvent();
		}
	}
	
	private void fireCommandEvent(int capteur_id, boolean value)
	{
		for (IScenarioCommandListener listener : getScenarioCommandListeners())
		{
			listener.boolCommand(capteur_id, value);
		}
	}

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
	
	public void addScenarioCommandListener(IScenarioCommandListener listener)
	{
		listeners.add(IScenarioCommandListener.class, listener);
	}
	
	public void removeScenarioCommandListener(IScenarioCommandListener listener)
	{
		listeners.remove(IScenarioCommandListener.class, listener);
	}
	
	protected IScenarioCommandListener[] getScenarioCommandListeners()
	{
		return this.listeners.getListeners(IScenarioCommandListener.class);
	}
	
	private void ScenarioList(CachedRowSet listeScenarios)
	{
		try
		{
			ResultSetMetaData methadata = listeScenarios.getMetaData();
			Integer columnCount = methadata.getColumnCount();

			while(listeScenarios.next())
			{
				Scenario scenario = new Scenario();
				for(int i = 1; i <= columnCount; i++)
				{
					String arg0 = methadata.getColumnName(i);
					Object arg1 = listeScenarios.getObject(i);
					scenario.setField(arg0, arg1);
				}
				scenario.init();
				scenarios.add(scenario);
			}
		}
		catch(SQLException ex)
		{
			ex.printStackTrace();
		}
	}
}
