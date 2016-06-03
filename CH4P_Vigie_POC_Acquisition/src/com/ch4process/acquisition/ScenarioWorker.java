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
		System.out.println("scenarioWorker start : " + Calendar.getInstance().getTime());
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
						
						if(checkScenario(scenario, isTriggered))
						{
							busy = true;
							doScenario(scenario.getParams());
							fireActionEvent(scenario.getScenario_id(), Calendar.getInstance().getTime().getTime());
						}
					}
				}
				deleteEvent();
			}
		}
	}
	
	private boolean checkScenario(Scenario scenario, boolean triggered)
	{
		try
		{
			if (! scenario.getIsPresent() && triggered)
			{
				scenario.setIsPresent(true);
				return true;
			}
			else if (scenario.getIsPresent() && ! triggered)
			{
				scenario.setIsPresent(false);
				return false;
			}
			
			return false;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
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
				doMAIL(params[1]);
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
	}
	
	private void doMAIL(String parameters)
	{
		
	}
	
	private void fireCommandEvent(int capteur_id, boolean value)
	{
		for (IScenarioCommandListener listener : getScenarioCommandListeners())
		{
			listener.boolCommand(capteur_id, value);
		}
	}
	
	private void fireActionEvent(int scenario_id, long datetime)
	{
		for (IActionEventListener listener : getActionEventListeners())
		{
			listener.onActionEvent(scenario_id, datetime);
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
	
	public void addActionEventListener(IActionEventListener listener)
	{
		listeners.add(IActionEventListener.class, listener);
	}
	
	public void removeActionEventListener(IActionEventListener listener)
	{
		listeners.remove(IActionEventListener.class, listener);
	}
	
	protected IActionEventListener[] getActionEventListeners()
	{
		return this.listeners.getListeners(IActionEventListener.class);
	}
	
	
}
