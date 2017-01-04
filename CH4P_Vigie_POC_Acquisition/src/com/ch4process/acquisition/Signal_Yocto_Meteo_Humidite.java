package com.ch4process.acquisition;

import java.util.Calendar;

import com.ch4process.events.SignalValueEvent;
import com.ch4process.utils.CH4P_Exception;
import com.ch4process.utils.CH4P_Functions;
import com.yoctopuce.YoctoAPI.YHumidity;



public class Signal_Yocto_Meteo_Humidite extends Signal
{
	YHumidity sensor;
	Double value;

	public Signal_Yocto_Meteo_Humidite(Signal model)
	{
		super(model);
	}
	
	@Override
	public boolean Init()
	{
		try
		{
			//sensor = YHumidity.FindHumidity(this.device.serialNumber + ".humidity");
			sensor = YHumidity.FindHumidityInContext(yapiContext,this.device.serialNumber + ".humidity");
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
