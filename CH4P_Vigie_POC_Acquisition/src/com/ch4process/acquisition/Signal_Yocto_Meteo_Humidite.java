package com.ch4process.acquisition;

import java.util.Calendar;

import com.ch4process.utils.CH4P_Exception;
import com.yoctopuce.YoctoAPI.YHumidity;



public class Signal_Yocto_Meteo_Humidite extends Signal
{
	YHumidity sensor;
	Double value;

	
	@Override
	public boolean Init()
	{
		try
		{
			sensor = YHumidity.FindHumidity(this.device.serialNumber + ".humidity");
			return sensor.isOnline(); 
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean Refresh()
	{
		try
		{
			value = sensor.getCurrentValue();
			if(value != sensor.CURRENTVALUE_INVALID)
			{
				this.countdown = this.refreshRate;
				this.value = value;
				fireValueChanged(value);
				return true;
			}
			else
			{
				return false;
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return false;
		}
	}
	

	@Override
	public Integer call() throws CH4P_Exception
	{
		try
		{
			Connect();
			Init();
			Refresh();
			
			while(true)
			{
				Refresh();
				Thread.sleep(this.refreshRate * 1000);
			}
			
		}
		catch (Exception e)
		{
			throw new CH4P_Exception(e.getMessage(), e.getCause());
		}
	}
}
