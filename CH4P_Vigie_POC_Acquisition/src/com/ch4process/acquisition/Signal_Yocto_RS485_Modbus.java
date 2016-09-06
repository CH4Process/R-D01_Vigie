package com.ch4process.acquisition;

import java.util.Calendar;
import java.util.Date;

import com.ch4process.utils.CH4P_Exception;
import com.yoctopuce.YoctoAPI.YGenericSensor;

public class Signal_Yocto_RS485_Modbus extends Signal
{
	
	public Signal_Yocto_RS485_Modbus(Signal model)
	{
		super(model);
	}
	
	// Operational code
	
	@Override
	public Integer call() throws Exception
	{
		try
		{
			while(true)
			{
				System.out.println("Signal : " + this.shortName + " routine called...");
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
