package com.ch4process.acquisition;

import java.util.concurrent.Callable;

import com.ch4process.utils.CH4P_Exception;
import com.yoctopuce.YoctoAPI.YRelay;

public class Commande extends Signal implements IScenarioCommandListener
{
	YRelay sensor;
	Double value; 
	Integer offset;
	
	
	public boolean Init()
	{
		try
		{
			offset = Integer.valueOf(this.address);
			sensor = YRelay.FindRelay(this.device.serialNumber + ".relay" + this.offset);
			
			this.refreshRate = 5;
			return sensor.isOnline(); 
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return false;
		}
	}
	
	public void setBoolValue(boolean value)
	{
		try
		{
			if (value == false)
			{
				sensor.setOutput(sensor.OUTPUT_OFF);
			}
			else
			{
				sensor.setOutput(sensor.OUTPUT_ON);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void boolCommand(int idSignal, boolean value)
	{
		if (this.idSignal.equals(idSignal))
		{
			this.setBoolValue(value);
		}
	}
	
	public void addActionEventListener(IScenarioEventListener listener)
	{
		listeners.add(IScenarioEventListener.class, listener);
	}
	
	public void removeActionEventListener(IScenarioEventListener listener)
	{
		listeners.remove(IScenarioEventListener.class, listener);
	}
	
	protected IScenarioEventListener[] getActionEventListeners()
	{
		return this.listeners.getListeners(IScenarioEventListener.class);
	}

	@Override
	public Integer call() throws CH4P_Exception
	{
		try
		{
			Connect();
			Init();
			
			while(true)
			{
				Thread.sleep(this.refreshRate * 1000);
			}
			
		}
		catch (Exception e)
		{
			throw new CH4P_Exception(e.getMessage(), e.getCause());
		}
	}
	

}
