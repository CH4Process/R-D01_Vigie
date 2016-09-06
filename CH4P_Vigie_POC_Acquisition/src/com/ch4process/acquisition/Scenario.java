package com.ch4process.acquisition;

import java.lang.reflect.Field;

import com.ch4process.events.SignalValueEvent;
import com.ch4process.utils.CH4P_Exception;
import com.ch4process.utils.CH4P_Functions;

public class Scenario
{
	Integer idScenario;
	Integer idSignal;
	String test;
	String testValue;
	String action;
	String actionParams;
	String actionMessage;
	Integer priority;
	
	Boolean isPresent = false;
	
	// Constructors
	
	/**
	 * @param idScenario
	 * @param idSignal
	 * @param test
	 * @param testValue
	 * @param action
	 * @param actionParams
	 * @param priority
	 */
	public Scenario(Integer idScenario, Integer idSignal, String test, String testValue, String action,
			String actionParams, String actionMessage, Integer priority)
	{
		this.idScenario = idScenario;
		this.idSignal = idSignal;
		this.test = test;
		this.testValue = testValue;
		this.action = action;
		this.actionParams = actionParams;
		this.actionMessage = actionMessage;
		this.priority = priority;
	}

	/**
	 * 
	 */
	public Scenario()
	{
		this(null, null, null, null, null, null, null, null);
	}

	
	// Getters and Setters
	
	public String getActionMessage()
	{
		return this.actionMessage;
	}
	public void setActionMessage(String actionMessage)
	{
		this.actionMessage = actionMessage;
	}
	
	public Boolean getIsPresent()
	{
		return isPresent;
	}

	public void setIsPresent(Boolean isPresent)
	{
		this.isPresent = isPresent;
	}
	
	public Integer getIdScenario()
	{
		return idScenario;
	}

	public void setIdScenario(Integer idScenario)
	{
		this.idScenario = idScenario;
	}

	public Integer getIdSignal()
	{
		return idSignal;
	}

	public void setIdSignal(Integer idSignal)
	{
		this.idSignal = idSignal;
	}

	public String getTest()
	{
		return test;
	}

	public void setTest(String test)
	{
		this.test = test;
	}

	public String getTestValue()
	{
		return testValue;
	}

	public void setTestValue(String testValue)
	{
		this.testValue = testValue;
	}

	public String getAction()
	{
		return action;
	}

	public void setAction(String action)
	{
		this.action = action;
	}

	public String getActionParams()
	{
		return actionParams;
	}

	public void setActionParams(String actionParams)
	{
		this.actionParams = actionParams;
	}

	public Integer getPriority()
	{
		return priority;
	}

	public void setPriority(Integer priority)
	{
		this.priority = priority;
	}

	
	
	// Operational code
	
	public boolean testValue(SignalValueEvent event)
	{
		// We test the value only if the event comes from our Signal
		if (event.getIdSignal() == this.idSignal)
		{
			try
			{
				boolean result = false;
				
				if (event.getBoolValue() != null)
				{
					if (event.getBoolValue().compareTo(CH4P_Functions.StringToBool(testValue)) == 0)
					{
						result = true;
					}
				}
				
				if (event.getIntValue() != null)
				{
					if (test.equals(">"))
					{
						if (event.getIntValue() > Integer.valueOf(testValue))
						{
							result = true;
						}
					}
					else if (test.equals("<"))
					{
						if (event.getIntValue() < Integer.valueOf(testValue))
						{
							result = true;
						}
					}
					else if (test.equals("!="))
					{
						if (event.getIntValue() != Integer.valueOf(testValue))
						{
							result = true;
						}
					}
					else if (test.equals("="))
					{
						if (event.getIntValue().equals(Integer.valueOf(testValue)))
						{
							result = true;
						}
					}
				}
				
				if (event.getDoubleValue() != null)
				{
					if (test.equals(">"))
					{
						if (event.getDoubleValue() > Double.valueOf(testValue))
						{
							result = true;
						}
					}
					else if (test.equals("<"))
					{
						if (event.getDoubleValue() < Double.valueOf(testValue))
						{
							result = true;
						}
					}
					else if (test.equals("!="))
					{
						if (event.getDoubleValue() != Double.valueOf(testValue))
						{
							result = true;
						}
					}
					else if (test.equals("="))
					{
						if (event.getDoubleValue().equals(Double.valueOf(testValue)))
						{
							result = true;
						}
					}
				}
				
				return result;
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				return false;
			}
		}
		else
		{
			return false;
		}
	}
}
