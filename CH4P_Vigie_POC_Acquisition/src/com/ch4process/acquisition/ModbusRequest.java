package com.ch4process.acquisition;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ch4process.events.SignalValueEvent;
import com.ch4process.utils.CH4P_Functions;
import com.yoctopuce.YoctoAPI.YAPI_Exception;
import com.yoctopuce.YoctoAPI.YSerialPort;

public class ModbusRequest
{
	// Class variables
	
	Integer requestType = REQUEST_NONE;
	Integer startAddress;
	Integer requestlength;
	Map<Signal, SignalValueEvent> elements = new HashMap<Signal, SignalValueEvent>();
	ArrayList<Integer> values = null;
	String byteOrder = null;
	
	public static final int REQUEST_NONE = 0;
	public static final int REQUEST_READ_HOLDING_REGISTERS = 1;
	public static final int REQUEST_READ_COILS = 0;
	
	// Getters and Setters
	
	public String getByteOrder()
	{
		return this.byteOrder;
	}
	
	public void setByteOrder(String _byteOrder)
	{
		this.byteOrder = _byteOrder;
	}
	
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
			elements.put(signal, new SignalValueEvent(signal.getIdSignal(), null, null, null, false, 0, signal.getSignalType()));			
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
			CH4P_Functions.Log(CH4P_Functions.LOG_inConsole, 100, "ModbusRequest : Signal " + signal.getShortName() + " added.");
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
		
		CH4P_Functions.Log(CH4P_Functions.LOG_inConsole, 100, "ModbusRequest : Request - Start = " + startAddress + " - Length = " + requestlength);
	}

	private void HandleByteOrder(Integer _A, Integer _B)
	{
		Integer _C;
		try
		{
			switch (this.byteOrder)
			{
				case "1234": break;
				
				case "2143": _A = Integer.reverseBytes(_A) >> 16; _B = Integer.reverseBytes(_B) >> 16; break;
				
				case "3421" : _C = _A; _A = _B; _B = _C; break;
				
				case "4321" : _C = _A; _A = _B; _B = _C; _A = Integer.reverseBytes(_A) >> 16; _B = Integer.reverseBytes(_B) >> 16; break;
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public void Execute(YSerialPort serialPort, int slaveNumber)
	{
		boolean isValid = true;
		
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
		}
		catch (YAPI_Exception ex)
		{
			isValid = false;
		}
		
		try
		{
				int index = 0;
						
				for(Map.Entry <Signal, SignalValueEvent> entry : elements.entrySet())
				{
					Integer myValue;
					Integer myValue2;
					
					
					Signal signal = entry.getKey();
					index = signal.getAddress() - startAddress;
					
					CH4P_Functions.Log(CH4P_Functions.LOG_inConsole, 100, "ModbusRequest : Signal " + signal.getShortName() + " value update.");
					
					if (this.values == null)
					{
						myValue = 0;
						myValue2 = 0;
					}
					else
					{
						myValue = values.get(index);
						myValue2 = values.get(index+1);
						
						HandleByteOrder(myValue, myValue2);
					}
					
					switch(signal.getSignalType().getComFormat())
					{
						case "FLOAT32":
							Float f = Float.intBitsToFloat(myValue << 16 | myValue2);
							double data = Double.parseDouble(f.toString());
							if (signal.getSignalType().getCoeff() != null && signal.getSignalType().getCoeff() != 0.0)
							{
								data = data / signal.getSignalType().getCoeff();
							}
							entry.setValue(new SignalValueEvent(signal.getIdSignal(), data, null, null, isValid, Calendar.getInstance().getTime().getTime(), signal.getSignalType()));
							break;
						
						case "INT":
							entry.setValue(new SignalValueEvent(signal.getIdSignal(), null, myValue, null, isValid, Calendar.getInstance().getTime().getTime(), signal.getSignalType()));
							break;
							
						case "BOOL":
							boolean value;
							if (myValue == 1)
							{
								value = true;
							}
							else
							{
								value = false;
							}
							entry.setValue(new SignalValueEvent(signal.getIdSignal(), null, null, value, isValid, Calendar.getInstance().getTime().getTime(), signal.getSignalType()));
							break;
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
				
				if (signal != null)
				{
					signal.fireValueChanged(element.getValue());
				}				
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
}
