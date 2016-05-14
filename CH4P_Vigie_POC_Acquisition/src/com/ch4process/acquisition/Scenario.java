package com.ch4process.acquisition;

import java.lang.reflect.Field;

public class Scenario
{
	Integer scenario_id;
	Integer capteur_id;
	String test;
	String params;
	Double doubleValueTested;
	Integer intValueTested;
	Boolean boolValueTested;
	
	public void setField(String fieldName, Object fieldValue)
	{
		try
		{
			Class thisClass = this.getClass();
			Field field = thisClass.getDeclaredField(fieldName);
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
		String[] testing = this.test.split("\\|");
		if (testing.length > 1)
		{
			this.test = testing[0];
			this.doubleValueTested = Double.valueOf(testing[1]);
			this.intValueTested = Integer.valueOf(testing[1]);
			if(Integer.valueOf(testing[1]) == 1)
			{
				this.boolValueTested = true;
			}
			else
			{
				this.boolValueTested = false;
			}
		}
	}

	public boolean testValue(Double value)
	{
		try
		{
			boolean result = false;
			
			if (test.equals(">"))
			{
				if(value > doubleValueTested) result = true;
			}
			else if (test.equals("<"))
			{
				if(value < doubleValueTested) result = true;
			}
			else if (test.equals("="))
			{
				if(value == doubleValueTested) result = true;
			}
			else if (test.equals("!="))
			{
				if(value != doubleValueTested) result = true;
			}
			
			return result;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return false;
		}
	}
	
	public boolean testValue(Integer value)
	{
		try
		{
			boolean result = false;
			
			if (test.equals(">"))
			{
				if(value > intValueTested) result = true;
			}
			else if (test.equals("<"))
			{
				if(value < intValueTested) result = true;
			}
			else if (test.equals("="))
			{
				if(value == intValueTested) result = true;
			}
			else if (test.equals("!="))
			{
				if(value != intValueTested) result = true;
			}
			
			return result;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return false;
		}
	}
	
	public boolean testValue(Boolean value)
	{
		try
		{
			boolean result = false;
			

			if (test.equals("="))
			{
				if(value == boolValueTested) result = true;
			}
			else if (test.equals("!="))
			{
				if(value != boolValueTested) result = true;
			}
			
			return result;
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
	

	public Double getDoubleValueTested()
	{
		return doubleValueTested;
	}
	
	public Integer getIntValueTested()
	{
		return intValueTested;
	}
	
	public Boolean getBoolValueTested()
	{
		return boolValueTested;
	}

}
