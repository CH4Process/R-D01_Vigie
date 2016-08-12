package com.ch4process.acquisition;

import java.util.Calendar;
import java.util.Date;

import com.ch4process.utils.CH4P_Exception;
import com.yoctopuce.YoctoAPI.YGenericSensor;

public class Signal_Yocto_RS485_Modbus extends Signal
{
	Integer offset;
	YGenericSensor sensor;
	
	// Operational code
	
	@Override
	public Integer call() throws CH4P_Exception
	{
		try
		{
			while(true)
			{
				Thread.sleep(this.refreshRate * 1000);
			}
		}
		catch (Exception e)
		{
			throw new CH4P_Exception(e.getMessage(), e.getCause());
		}
	}
}
