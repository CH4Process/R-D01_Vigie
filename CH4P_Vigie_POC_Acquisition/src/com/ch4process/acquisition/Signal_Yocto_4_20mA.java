package com.ch4process.acquisition;

import java.util.Calendar;
import java.util.EventListener;

import com.ch4process.utils.CH4P_Exception;
import com.yoctopuce.YoctoAPI.YGenericSensor;

public class Signal_Yocto_4_20mA extends Signal implements ISignal
{
	Integer offset;
	YGenericSensor sensor;
	Double value;
	
	@Override
	public boolean init()
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
	public boolean refresh()
	{
		try
		{
			value = sensor.getCurrentRawValue();
			if (value != sensor.CURRENTRAWVALUE_INVALID)
			{
				return scaleValue();
			}
			return false;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return false;
		}
	}
	
	private boolean scaleValue()
	{
		if (this.signalType.minValue != null && this.signalType.maxValue != null)
		{
			this.isValid = true;
			
			if (value == -29999.0 || value == 29999.0)
			{
				this.isValid = false;
			}
			
			int range = this.signalType.maxValue - this.signalType.minValue;
			value = (range / 16) * (value - 4);
			this.countdown = this.refreshRate;
			
			fireValueChanged(value);
			
			return true;			
		}
		return false;
	}

	public Double getDoubleValue()
	{
		return value;
	}
	
	@Override
	public Integer call() throws CH4P_Exception
	{
		try
		{
			connect();
			init();
			refresh();
			
			while(true)
			{
				refresh();
				Thread.sleep(this.refreshRate * 1000);
			}
			
		}
		catch (Exception e)
		{
			throw new CH4P_Exception(e.getMessage(), e.getCause());
		}
	}

	protected void fireValueChanged(double value)
	{
		for (ISignalValueListener listener : getValueListeners())
		{
			// TODO : Implémenter la validité sur la mesure jusqu'en BDD
			listener.doubleValueChanged(this.idSignal, this.value, Calendar.getInstance().getTime().getTime());
		}
	}
	
}
