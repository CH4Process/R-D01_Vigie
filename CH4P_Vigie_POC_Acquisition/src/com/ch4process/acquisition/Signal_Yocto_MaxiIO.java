package com.ch4process.acquisition;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import com.ch4process.events.SignalValueEvent;
import com.ch4process.utils.CH4P_Exception;
import com.ch4process.utils.CH4P_Functions;
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

	
	public Signal_Yocto_MaxiIO(Signal model)
	{
		super(model);
	}
	
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
			CH4P_Functions.LogException(CH4P_Functions.LOG_inConsole, ex);
			return false;
		}
	}

	@Override
	public boolean Refresh()
	{
		try
		{
			CH4P_Functions.Log(this.getClass().getName(), CH4P_Functions.LOG_inConsole, 100, "Signal : " + this.shortName + " :: Refresh.");
			
			portState = ioSensor.get_portState();
			value = ((portState & offset) != 0);
			this.countdown = this.refreshRate;
			this.value = value;
			this.isValid = !(portState == ioSensor.PORTSTATE_INVALID);
			
			CH4P_Functions.Log(this.getClass().getName(), CH4P_Functions.LOG_inConsole, 100, "Signal : " + this.shortName + " :: Refresh - Value = " + value + " - Quality = " + this.isValid);

			fireValueChanged(new SignalValueEvent(this.getIdSignal(), null, null, this.value, this.isValid(), Calendar.getInstance().getTime().getTime(), this.getSignalType()));

			return true;

		}
		catch (YAPI_Exception ex)
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
