package com.ch4process.acquisition;

import java.lang.reflect.Field;

public class Scenario
{
	Integer scenario_id;
	Integer capteur_id;
	String test;
	String params;
	Double valueTested;
	Boolean isTriggered;
	
	public void setField(String fieldName, Object fieldValue)
	{
		try
		{
			Class thisClass = this.getClass();
			Class parentClass = thisClass.getSuperclass();
			Field field = parentClass.getDeclaredField(fieldName);
			boolean access = field.isAccessible();
			field.setAccessible(true);
			field.set(this, fieldValue);
			field.setAccessible(access);
		}
		catch(NoSuchFieldException | IllegalArgumentException | IllegalAccessException ex)
		{
			ex.printStackTrace();
		}
	}
	
	public void init()
	{
		this.isTriggered = false;
		
		String[] testing = this.test.split("\\|");
		if (testing.length > 1)
		{
			this.test = testing[0];
			this.valueTested = Double.valueOf(testing[1]);
		}
	}

	public boolean testValue(Double value)
	{
		try
		{
			boolean result = false;
			
			if (test.equals(">"))
			{
				if(value > valueTested) result = true;
			}
			else if (test.equals("<"))
			{
				if(value < valueTested) result = true;
			}
			else if (test.equals("="))
			{
				if(value == valueTested) result = true;
			}
			else if (test.equals("!="))
			{
				if(value != valueTested) result = true;
			}
			
			this.isTriggered = result;
			
			return true;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return false;
		}
	}
	
	public Integer getScenario_id()
	{
		return scenario_id;
	}

	public Integer getCapteur_id()
	{
		return capteur_id;
	}

	
	public String getTest()
	{
		return test;
	}
	

	public String getParams()
	{
		return params;
	}
	

	public Double getValueTested()
	{
		return valueTested;
	}
	
	
}
