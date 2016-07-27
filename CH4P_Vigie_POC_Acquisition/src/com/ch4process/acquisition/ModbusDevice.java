package com.ch4process.acquisition;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.EventListenerList;

public class ModbusDevice
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
	
	
}
