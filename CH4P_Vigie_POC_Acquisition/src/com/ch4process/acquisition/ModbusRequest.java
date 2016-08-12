package com.ch4process.acquisition;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ch4process.events.SignalValueEvent;
import com.yoctopuce.YoctoAPI.YSerialPort;

public class ModbusRequest
{
	// Class variables
	
	Integer requestType = REQUEST_NONE;
	Integer startAddress;
	Integer requestlength;
	Map<Signal, SignalValueEvent> elements = new HashMap<Signal, SignalValueEvent>();
	ArrayList<Integer> values = null;
	
	public static final int REQUEST_NONE = 0;
	public static final int REQUEST_READ_HOLDING_REGISTERS = 1;
	public static final int REQUEST_READ_COILS = 0;
	
	// Getters and Setters
	
	public Integer getRequestType()
	{
		return requestType;
	}
	
	public void setRequestType(Integer requestType)
	{
		this.requestType = requestType;
	}
	
	public Integer getStartAddress()
	{
		return startAddress;
	}

	public void setStartAddress(Integer startAddress)
	{
		this.startAddress = startAddress;
	}

	public Integer getRequestlength()
	{
		return requestlength;
	}

	public void setRequestlength(Integer requestlength)
	{
		this.requestlength = requestlength;
	}
	
	public void setSignals (List<Signal> signals)
	{
		for (Signal signal:signals)
		{
			elements.put(signal, null);			
		}
	}

	
	/**
	 * Constructor
	 * @param startAddress
	 * @param requestlength
	 */
	public ModbusRequest(List<Signal> signals)
	{
		for (Signal signal:signals)
		{
			elements.put(signal, null);			
		}
	}
	
	public void Init()
	{
		getParams();
	}
	
	private void getParams()
	{
		int lowAddress = 99999;
		int highAddress = 0;
		int size = 0;
		
		for(Map.Entry<Signal, SignalValueEvent> element: elements.entrySet())
		{
			Signal signal = element.getKey();
			
			if (signal.getAddress() < lowAddress)
			{
				lowAddress = signal.getAddress();
			}
			
			if (signal.getAddress() > highAddress)
			{
				highAddress = signal.getAddress();
				
				switch (signal.getSignalType().getComFormat())
				{
					case "FLOAT32": size = 2; this.requestType = this.REQUEST_READ_HOLDING_REGISTERS; break;
					case "INT": size = 1; this.requestType = this.REQUEST_READ_HOLDING_REGISTERS; break;
					case "BOOL": size = 1; this.requestType = this.REQUEST_READ_COILS; break;
					default: size = 1; break;
				}
			}
		}
		
		this.startAddress = lowAddress;
		this.requestlength = highAddress - lowAddress + size;
	}

	public void Execute(YSerialPort serialPort, int slaveNumber)
	{
		try
		{
			switch(this.requestType)
			{
				case REQUEST_READ_HOLDING_REGISTERS:
					this.values = serialPort.modbusReadRegisters(slaveNumber, startAddress, requestlength);
					break;
				
				case REQUEST_READ_COILS:
					this.values = serialPort.modbusReadBits(slaveNumber, startAddress, requestlength);
					break;
					
				default:
					break;
			}
			
			if (this.values != null)
			{
				int index = 0;
						
				for(Map.Entry <Signal, SignalValueEvent> entry : elements.entrySet())
				{
					Signal signal = entry.getKey();
					index = signal.getAddress() - startAddress;
					
					switch(signal.getSignalType().getComFormat())
					{
						case "FLOAT32":
							double data = (double) Float.intBitsToFloat(values.get(index) & values.get(index+1));
							entry.setValue(new SignalValueEvent(signal.getIdSignal(), data, null, null, serialPort.isOnline(), Calendar.getInstance().getTime().getTime(), signal.getSignalType()));
							break;
						
						case "INT":
							entry.setValue(new SignalValueEvent(signal.getIdSignal(), null, values.get(index), null, serialPort.isOnline(), Calendar.getInstance().getTime().getTime(), signal.getSignalType()));
							break;
							
						case "BOOL":
							boolean value;
							if (values.get(index) == 1)
							{
								value = true;
							}
							else
							{
								value = false;
							}
							entry.setValue(new SignalValueEvent(signal.getIdSignal(), null, null, value, true, Calendar.getInstance().getTime().getTime(), signal.getSignalType()));
							break;
					}
				}
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public void NotifyValueChanged()
	{
		try
		{
			for(Map.Entry<Signal, SignalValueEvent> element: elements.entrySet())
			{
				Signal signal = element.getKey();
				
				signal.fireValueChanged(element.getValue());
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
}
