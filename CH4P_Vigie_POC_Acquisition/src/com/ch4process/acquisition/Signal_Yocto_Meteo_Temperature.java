package com.ch4process.acquisition;

import java.util.Calendar;

import com.ch4process.events.SignalValueEvent;
import com.ch4process.utils.CH4P_Exception;
import com.yoctopuce.YoctoAPI.YTemperature;

public class Signal_Yocto_Meteo_Temperature extends Signal
{
	YTemperature sensor;
	Double value;
	
	public Signal_Yocto_Meteo_Temperature(Signal model)
	{
		super(model);
	}

	@Override
	public boolean Init()
	{
		try
		{
			sensor = YTemperature.FindTemperature(this.device.serialNumber + ".temperature");
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
	
			this.countdown = this.refreshRate;
			this.value = value;
			this.isValid = !(value == sensor.CURRENTVALUE_INVALID);
			
			fireValueChanged(new SignalValueEvent(this.getIdSignal(), this.value, null, null, this.isValid(), Calendar.getInstance().getTime().getTime(), this.getSignalType()));
			return true;

		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return false;
		}
	}

	@Override
	public Integer call() throws Exception
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
		catch (Exception ex)
		{
			ex.printStackTrace();
			throw new CH4P_Exception(ex.getMessage(), ex.getCause());
		}
	}
}

