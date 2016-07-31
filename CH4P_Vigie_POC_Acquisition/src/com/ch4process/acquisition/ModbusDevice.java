package com.ch4process.acquisition;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Callable;

import javax.swing.event.EventListenerList;

import com.ch4process.utils.CH4P_Exception;
import com.yoctopuce.YoctoAPI.YAPI;
import com.yoctopuce.YoctoAPI.YAPI_Exception;
import com.yoctopuce.YoctoAPI.YGenericSensor;
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
	List<Signal> signals = new ArrayList<Signal>();
	Integer baseAddress = 0;
	Integer baseRefreshRate = 1;
	Integer requestLength = 0;
	YSerialPort serialPort = null;
	boolean init_done = false;
	List<Integer> values = new ArrayList<Integer>();
	
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
		this.signals.add(signal);
	}
	
	public boolean Init()
	{
		try
		{
			serialPort = YSerialPort.FindSerialPort(this.serialNumber);
			
			if (serialPort.isOnline())
			{
				for(Signal signal: signals)
				{
					// Set up of the request length for the Modbus request
					requestLength += 2;
					
					// Set up of the base address for the Modbus request
					if (signal.getAddress() < baseAddress)
					{
						baseAddress = signal.getAddress();
					}
					
					// Set up of the base refreshrate for the Modbus request
					if (signal.getRefreshRate() < baseRefreshRate)
					{
						baseRefreshRate = signal.getRefreshRate();
					}
				}
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
					values = serialPort.modbusReadRegisters(slaveNumber, baseAddress, requestLength);
					
					// And now we give each signal his value ! 
					for(Signal signal:signals)
					{
						// The datas in the values ArrayList returned by the Yoctopuce Modbus card are ordered based on the Modbus request
						// So we have to determine which data to provide to which signal
						Integer index = baseAddress - signal.address;
						
						// TODO : THIS IS HARDCODED AND THIS IS SHIT !
						// The datas are Float32 so we have to merge two 16-digit integers into one and then transform it to a Float
						double data = (double) Float.intBitsToFloat(values.get(index) & values.get(index+1));
						
						signal.fireValueChanged(data);
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
