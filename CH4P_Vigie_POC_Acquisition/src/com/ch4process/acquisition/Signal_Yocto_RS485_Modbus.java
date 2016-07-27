package com.ch4process.acquisition;

import java.util.Calendar;
import java.util.EventListener;

import com.ch4process.utils.CH4P_Exception;
import com.yoctopuce.YoctoAPI.YGenericSensor;

public class Signal_Yocto_RS485_Modbus extends Signal implements ISignalValueListener
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
			if (value != sensor.CURRENTRAWVALUE_INVALID)
			{
				return ScaleValue();
			}
			return false;
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

	protected void fireValueChanged(double value)
	{
		for (ISignalValueListener listener : getValueListeners())
		{
			// TODO : Implémenter la validité sur la mesure jusqu'en BDD
			listener.doubleValueChanged(this.idSignal, this.value, Calendar.getInstance().getTime().getTime());
		}
	}

	@Override
	public void doubleValueChanged(int idSignal, double value, long datetime)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void intValueChanged(int idSignal, int value, long datetime)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void boolValueChanged(int idSignal, boolean value, long datetime)
	{
		// TODO Auto-generated method stub
		
	}
	
}
