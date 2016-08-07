package com.ch4process.acquisition;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import com.ch4process.utils.CH4P_Exception;
import com.yoctopuce.YoctoAPI.YAPI_Exception;
import com.yoctopuce.YoctoAPI.YDigitalIO;

public class Signal_Yocto_MaxiIO extends Signal
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
	public boolean Init()
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
	public boolean Refresh()
	{
		try
		{
			portState = ioSensor.get_portState();

				value = ((portState & offset) != 0);
				this.countdown = this.refreshRate;
				this.value = value;
				this.isValid = !(portState == ioSensor.PORTSTATE_INVALID);
				
				fireValueChanged(value, isValid);
				
				return true;

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
