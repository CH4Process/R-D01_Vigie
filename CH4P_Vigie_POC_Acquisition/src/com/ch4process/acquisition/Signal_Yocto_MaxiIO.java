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
	Boolean lastValue;
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
			offset = (int) Math.pow(2, Integer.valueOf(this.address) - 1);
			//ioSensor = YDigitalIO.FindDigitalIO(this.device.serialNumber + ".digitalIO");
			ioSensor = YDigitalIO.FindDigitalIOInContext(yapiContext,this.device.serialNumber + ".digitalIO");
			portMapping = ioSensor.get_portDirection();
			portSize = ioSensor.get_portSize();
			countdown = logRate;
			lastValue = null;
			
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
			//CH4P_Functions.Log(this.getClass().getName(), CH4P_Functions.LOG_inConsole, 100, "Signal : " + this.shortName + " :: Refresh.");
			
			portState = ioSensor.get_portState();
			value = ((portState & offset) != 0);
			
			isValid = !(portState == ioSensor.PORTSTATE_INVALID);
			
			// We have to update the countdown
			countdown -= refreshRate;
			
			// If the current value differs from the stored value OR if the time elapsed exceeds the lograte
			if ((! value.equals(lastValue)) || (countdown <= 0))
			{
				// Either the value has changed or it's time we record it.
				fireValueChanged(new SignalValueEvent(this.getIdSignal(), null, null, this.value, this.isValid(), Calendar.getInstance().getTime().getTime(), this.getSignalType()));
				
				// The value has been updated so we have to reset the countdown
				countdown = logRate;
			}
			
			// In any case we take the current value and store it.
			lastValue = value;
			
			//fireValueChanged(new SignalValueEvent(this.getIdSignal(), null, null, this.value, this.isValid(), Calendar.getInstance().getTime().getTime(), this.getSignalType()));

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
