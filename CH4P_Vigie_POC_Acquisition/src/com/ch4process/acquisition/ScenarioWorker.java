package com.ch4process.acquisition;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.sql.rowset.CachedRowSet;
import javax.swing.event.EventListenerList;

import com.ch4process.database.DatabaseRequest;
import com.ch4process.database.IDatabaseRequestCallback;
import com.ch4process.email.Mail;
import com.ch4process.events.SignalValueEvent;

public class ScenarioWorker implements Callable<Integer>, ISignalValueListener
{
	DatabaseRequest scenarioListRequest;
	IDatabaseRequestCallback scenarioListRequestCallback;
	boolean scenarioListRequest_done = true;
	
	List<SignalValueEvent> eventList = new LinkedList<>();
	List<Scenario> scenarios = new LinkedList<>();
	EventListenerList listeners = new EventListenerList();
	
	boolean busy = false;
	boolean init_done = false;

	
	public ScenarioWorker(DatabaseRequest scenarioListRequest)
	{
		this.scenarioListRequest = scenarioListRequest;
	}
	
	private void init()
	{
		init_done = true;
		
		System.out.println("scenarioWorker start : " + Calendar.getInstance().getTime());
		
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
	@Override
	public Integer call() throws Exception
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
		catch (Exception ex)
		{
			ex.printStackTrace();
			return null;
		}
	}
	
	private void eventHandling()
	{
		if (! busy)
		{
			while (eventList.size() > 0)
			{		
				SignalValueEvent event = eventList.get(0);
				
				for(Scenario scenario : scenarios)
				{
					if (scenario.getIdSignal().equals(event.getIdSignal()))
					{
						if (scenario.getIsActive())
						{
							Boolean isTriggered = scenario.testValue(event);

							if(checkIsPresent(scenario, isTriggered))
							{
								busy = true;
								doScenario(scenario.getAction(), scenario.getActionParams(), scenario.getActionMessage());
							}
						}
					}
				}
				deleteEvent();
			}
		}
	}
	
	private boolean checkIsPresent(Scenario scenario, boolean triggered)
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
		catch (Exception ex)
		{
			ex.printStackTrace();
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

	private void doScenario(String action, String actionParams, String actionMessage)
	{
		try
		{	
			if (action.equals("CMD"))
			{
				doCMD(actionParams);
			}
			else if (action.equals("MAIL"))
			{
				doMAIL(actionParams);
			}
			else if (action.equals("MAILSMS"))
			{
				doMAILSMS(actionParams, actionMessage);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	private void doCMD(String parameters)
	{
		try
		{
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	private void doMAIL(String parameters)
	{
	}
	
	private void doMAILSMS(String recipients, String message)
	{
		Mail mail = new Mail();
		
		String subject = mail.getMailsmsaccount() + mail.getMailsmslogin() + mail.getMailsmspassword() + mail.getMailsmsfrom() + recipients + mail.getMailsmsparameters();
		
		Calendar now = Calendar.getInstance();
		SimpleDateFormat format = new SimpleDateFormat("HH'h'mm dd/MM/yyyy");
		String date = format.format(now.getTime());
		
		message = date + " - " + message;
		
		mail.setAuthenticationType(Mail.AUTH_SSL);
		mail.setFrom(mail.getUsername());
		mail.setSubject(subject);
		mail.setTo(mail.getMailsmsaddress());
		mail.setText(message);
		
		if (mail.sendMail())
		{
			System.out.println("ScenarioWorker : MAIL_SMS sent !");
			fireScenarioEvent("MAILSMS", "TO : " + recipients + " -- " + message , Calendar.getInstance().getTime().getTime(), 110);
		}
		else
		{
			System.out.println("ScenarioWorker : Failed to send a MAIL_SMS !");
		}
		
	}
	
	private void fireCommandEvent(int capteur_id, boolean value)
	{
		for (IScenarioCommandListener listener : getScenarioCommandListeners())
		{
			listener.boolCommand(capteur_id, value);
		}
	}
	
	private void fireScenarioEvent(String _name, String _message, Long _datetime, Integer _code)
	{
		for (IScenarioEventListener listener : getScenarioEventListeners())
		{
			listener.onScenarioEvent(_name, _message, _datetime, _code);
		}
	}
	
	@Override
	public void SignalValueChanged(SignalValueEvent event)
	{
		if (event.isValid())
		{
			eventList.add(event);
		}
	}
	
	
	private void ScenarioList(CachedRowSet listeScenarios)
	{
		try
		{
			while(listeScenarios.next())
			{
				Scenario scenario = new Scenario();
				scenario.setIdScenario(listeScenarios.getInt("idScenario"));
				scenario.setIdSignal(listeScenarios.getInt("idSignal"));
				scenario.setTest(listeScenarios.getString("test"));
				scenario.setTestValue(listeScenarios.getString("testValue"));
				scenario.setAction(listeScenarios.getString("action"));
				scenario.setActionParams(listeScenarios.getString("actionParams"));
				scenario.setPriority(listeScenarios.getInt("priority"));
				scenario.setActionMessage(listeScenarios.getString("actionMessage"));
				scenario.setIsActive(listeScenarios.getBoolean("isActive"));

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
	
	public void addScenarioEventListener(IScenarioEventListener listener)
	{
		listeners.add(IScenarioEventListener.class, listener);
	}
	
	public void removeScenarioEventListener(IScenarioEventListener listener)
	{
		listeners.remove(IScenarioEventListener.class, listener);
	}
	
	protected IScenarioEventListener[] getScenarioEventListeners()
	{
		return this.listeners.getListeners(IScenarioEventListener.class);
	}
}
