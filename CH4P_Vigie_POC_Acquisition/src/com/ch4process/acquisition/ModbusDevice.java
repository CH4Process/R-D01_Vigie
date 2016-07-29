package com.ch4process.acquisition;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Callable;

import javax.swing.event.EventListenerList;

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
	Integer baseAddress;
	Integer baseRefreshRate;
	Integer requestLength;
	YSerialPort serialPort;
	
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
			return serialPort.isOnline(); 
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
	public Object call() throws Exception
	{
		// TODO : Modbus routine
		return null;
	}
	
	
	// Event handling code
	
		public void addValueListener(ISignalValueListener listener)
		{
			listeners.add(ISignalValueListener.class, listener);
		}
		
		public void removeValueListener(ISignalValueListener listener)
		{
			listeners.remove(ISignalValueListener.class, listener);
		}
		
		protected ISignalValueListener[] getValueListeners()
		{
			return this.listeners.getListeners(ISignalValueListener.class);
		}
		
		protected void fireValueChanged(double value)
		{
			for (ISignalValueListener listener : getValueListeners())
			{
				// TODO : Notifier le signal que sa valeur a été mise à jour
				//listener.doubleValueChanged(this.idSignal, this.value, Calendar.getInstance().getTime().getTime());
			}
		}
	
}
