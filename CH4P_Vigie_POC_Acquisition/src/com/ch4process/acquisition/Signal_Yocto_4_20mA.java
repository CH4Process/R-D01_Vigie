package com.ch4process.acquisition;

import java.util.Calendar;
import java.util.EventListener;

import com.ch4process.utils.CH4P_Exception;
import com.yoctopuce.YoctoAPI.YGenericSensor;

public class Signal_Yocto_4_20mA extends Signal
{
	Integer offset;
	YGenericSensor sensor;
	Double value;
	
	@Override
	public boolean Init()
	{
		try
		{
			offset = Integer.valueOf(this.address);
			sensor = YGenericSensor.FindGenericSensor(this.device.serialNumber + ".genericSensor" + offset);
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
			value = sensor.getCurrentRawValue();

			return ScaleValue();

		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return false;
		}
	}
	
	private boolean ScaleValue()
	{
		if (this.signalType.minValue != null && this.signalType.maxValue != null)
		{
			this.isValid = true;
			
			if (value == -29999.0 || value == 29999.0 || value == sensor.CURRENTVALUE_INVALID)
			{
				this.isValid = false;
			}
			
			int range = this.signalType.maxValue - this.signalType.minValue;
			value = (range / 16) * (value - 4);
			this.countdown = this.refreshRate;
			this.value = value;
			
			fireValueChanged(value, isValid);
			
			return true;			
		}
		return false;
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
