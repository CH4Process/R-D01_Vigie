package com.ch4process.acquisition;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Callable;
import javax.swing.event.EventListenerList;

import com.ch4process.events.SignalValueEvent;
import com.ch4process.utils.CH4P_Exception;
import com.yoctopuce.YoctoAPI.YAPI;
import com.yoctopuce.YoctoAPI.YAPI_Exception;
import com.yoctopuce.YoctoAPI.YSerialPort;

public class ModbusDevice extends Device implements Callable
{
	// Database variables
	Integer idModbusDevice = null;
	Integer idDevice = null;
	Integer slaveNumber = null;
	String byteOrder = null;
	Integer speed = null;
	
	// Internal variables
	List<Signal> HoldingRegisterSignals = new ArrayList<Signal>();
	List<Signal> CoilSignals = new ArrayList<Signal>();
	List<ModbusRequest> requests = new ArrayList<ModbusRequest>();
	Integer baseRefreshRate = 1;
	YSerialPort serialPort = null;
	boolean init_done = false;
	boolean isValid = true;
	
	// Event handling
	EventListenerList listeners = new EventListenerList();
	
	
	// Getters and Setters
	public Integer getIdModbusDevice()
	{
		return idModbusDevice;
	}
	public void setIdModbusDevice(Integer idModbusDevice)
	{
		this.idModbusDevice = idModbusDevice;
	}
	public Integer getIdDevice()
	{
		return idDevice;
	}
	public void setIdDevice(Integer idDevice)
	{
		this.idDevice = idDevice;
	}
	public Integer getSlaveNumber()
	{
		return slaveNumber;
	}
	public void setSlaveNumber(Integer slaveNumber)
	{
		this.slaveNumber = slaveNumber;
	}
	public String getByteOrder()
	{
		return byteOrder;
	}
	public void setByteOrder(String byteOrder)
	{
		this.byteOrder = byteOrder;
	}
	public Integer getSpeed()
	{
		return speed;
	}
	public void setSpeed(Integer speed)
	{
		this.speed = speed;
	}
	
	
	// Operationnal code
	
	public void addSignal(Signal signal)
	{
		if(signal.getSignalType().getIsTor())
		{
			// This is a boolean signal
			CoilSignals.add(signal);
		}
		else 
		{
			// It's not a boolean so it's a holding register
			HoldingRegisterSignals.add(signal);
		}
	}	
	
	private void InitRequest(List<Signal> SignalList)
	{
		try
		{
			
			// Here we create a new modbus request based on the list of Signals
			ModbusRequest request = new ModbusRequest(SignalList);
			
			request.Init();
			
			requests.add(request);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public boolean Init()
	{
		try
		{
			serialPort = YSerialPort.FindSerialPort(this.serialNumber);
			
			if (serialPort.isOnline())
			{
				InitRequest(HoldingRegisterSignals);
				InitRequest(CoilSignals);
				
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
	
	public boolean Connect()
	{
		try
		{
			 YAPI.RegisterHub(this.address);
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
			while (true)
			{
				if (! init_done)
				{
					init_done = Connect() && Init();
				}
				
				if (init_done)
				{
					// First we read the values in the Modbus device
					for(ModbusRequest request:requests)
					{
						request.Execute(this.serialPort, this.slaveNumber);
					}
					
					// Then we can notify the values
					for(ModbusRequest request:requests)
					{
						request.NotifyValueChanged();;
					}
				}
				
				Thread.sleep(baseRefreshRate * 1000);
			}
		}
		catch(Exception ex)
		{
			throw new CH4P_Exception(ex.getMessage(), ex.getCause());
		}
		finally
		{
			return errorCode;
		}
	}
	
	
	
}
