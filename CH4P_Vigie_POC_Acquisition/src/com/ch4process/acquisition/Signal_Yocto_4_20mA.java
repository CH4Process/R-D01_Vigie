package com.ch4process.acquisition;

import java.util.Calendar;
import java.util.EventListener;

import com.ch4process.events.SignalValueEvent;
import com.ch4process.utils.CH4P_Exception;
import com.ch4process.utils.CH4P_Functions;
import com.yoctopuce.YoctoAPI.YGenericSensor;

public class Signal_Yocto_4_20mA extends Signal
{
	Integer offset;
	YGenericSensor sensor;
	Double value;
	
	public Signal_Yocto_4_20mA(Signal model)
	{
		super(model);
	}
	
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
			CH4P_Functions.LogException(CH4P_Functions.LOG_inConsole, ex);
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
			CH4P_Functions.LogException(CH4P_Functions.LOG_inConsole, ex);
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
			
			if (signalType.coeff != null && signalType.coeff != 0.0)
			{
				value = value / signalType.coeff;
			}
			
			fireValueChanged(new SignalValueEvent(this.getIdSignal(), this.value, null, null, this.isValid(), Calendar.getInstance().getTime().getTime(), this.getSignalType()));
			
			return true;			
		}
		return false;
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
