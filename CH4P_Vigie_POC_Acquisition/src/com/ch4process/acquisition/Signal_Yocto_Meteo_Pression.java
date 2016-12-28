package com.ch4process.acquisition;

import java.util.Calendar;

import com.ch4process.events.SignalValueEvent;
import com.ch4process.utils.CH4P_Exception;
import com.ch4process.utils.CH4P_Functions;
import com.yoctopuce.YoctoAPI.YPressure;

public class Signal_Yocto_Meteo_Pression extends Signal
{
	YPressure sensor;
	Double value;

	public Signal_Yocto_Meteo_Pression(Signal model)
	{
		super(model);
	}
	
	@Override
	public boolean Init()
	{
		try
		{
			//sensor = YPressure.FindPressure(this.device.serialNumber + ".pressure");
			sensor = YPressure.FindPressureInContext(yapiContext,this.device.serialNumber + ".pressure");
			return sensor.isOnline(); 
		}
		catch (Exception ex)
		{
			CH4P_Functions.LogException(CH4P_Functions.LOG_inConsole, ex);
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
			CH4P_Functions.LogException(CH4P_Functions.LOG_inConsole, ex);
			return false;
		}
	}
	
	public Double getDoubleValue()
	{
		return value;
	}

	@Override
	public Integer call() throws Exception
	{
		try
		{
			Connect();
			Init();
			
			while(true)
			{
				Refresh();
				Thread.sleep(this.refreshRate * 1000);
			}
			
		}
		catch (Exception ex)
		{
			CH4P_Functions.LogException(CH4P_Functions.LOG_inConsole, ex);
			throw new CH4P_Exception(ex.getMessage(), ex.getCause());
		}
	}
}
