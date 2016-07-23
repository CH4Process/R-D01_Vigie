package com.ch4process.acquisition;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import com.ch4process.utils.CH4P_Exception;
import com.yoctopuce.YoctoAPI.YAPI_Exception;
import com.yoctopuce.YoctoAPI.YDigitalIO;

public class Signal_Yocto_MaxiIO extends Signal implements ISignal
{
	// Variables
	Boolean value;
	Integer portMapping;
	Integer portState;
	Integer portSize;
	Integer offset;
	YDigitalIO ioSensor;
	Map<Integer, String> channels = new HashMap<Integer, String>();
	Map<Integer, Integer> channelsState = new HashMap<Integer, Integer>();

	
	// Operational code
	
	@Override
	public boolean init()
	{
		try
		{
			offset = Integer.valueOf(this.address);
			ioSensor = YDigitalIO.FindDigitalIO(this.device.serialNumber + ".digitalIO");
			portMapping = ioSensor.get_portDirection();
			portSize = ioSensor.get_portSize();
			
			if (portMapping != ioSensor.PORTDIRECTION_INVALID && portSize != ioSensor.PORTSIZE_INVALID)
			{
				return true;
			}
			return false;
			
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
			portState = ioSensor.get_portState();
			if (portState != ioSensor.PORTSTATE_INVALID)
			{
				value = ((portState & offset) != 0);
				this.countdown = this.refreshRate;
				
				fireValueChanged(value);
				
				return true;
			}
			return false;
		}
		catch (YAPI_Exception ex)
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

	public Boolean getBoolValue()
	{
		return value;
	}
	
	protected void fireValueChanged(boolean value)
	{
		for (ISignalValueListener listener : getValueListeners())
		{
			listener.boolValueChanged(this.idSignal, this.value, Calendar.getInstance().getTime().getTime());
		}
	}

}
