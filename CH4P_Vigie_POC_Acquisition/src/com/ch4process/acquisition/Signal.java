package com.ch4process.acquisition;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Callable;

import javax.swing.event.EventListenerList;

import com.ch4process.events.SignalValueEvent;
import com.ch4process.utils.CH4P_Exception;
import com.yoctopuce.YoctoAPI.YAPI;
import com.yoctopuce.YoctoAPI.YAPI_Exception;

public class Signal implements ISignal
{
	// Classes used as containers for configuration values
	SignalType signalType;
	SignalLevel signalLevel;
	Device device;
	
	// Class definition mapped one-to-one with database table
	Integer idSignal = null;
	Integer idDevice = null;
	Integer idSignalType = null;
	Integer idSignalLevel = null;
	String shortName = null;
	Integer address = null;
	String label = null;
	Integer refreshRate = null;
	Integer logRate = null;
	
	// Other variables
	int countdown = 0;
	boolean isValid;
	Calendar lastUpdate = null;
	
	// Event handling
	EventListenerList listeners = new EventListenerList();
	
	// Error handling
	Integer errorCode = 0;
	
	
	// Constructors
	
	/**
	 * This constructor sets everything to null.
	 * This constructor needs to be used with the setField method to autogenerate
	 * the class fields from database request.
	 */
	public Signal()
	{
		this(null, null, null, null, null, null, null, null, null);
	}
	
	/**
	 * Basic constructor of the class
	 * @param idSignal
	 * @param idDevice
	 * @param idSignalType
	 * @param idSignalLevel
	 * @param shortName
	 * @param address
	 * @param label
	 * @param refreshRate
	 * @param logRate
	 */
	public Signal(Integer idSignal, Integer idDevice, Integer idSignalType, Integer idSignalLevel, String shortName,
			Integer address, String label, Integer refreshRate, Integer logRate)
	{
		this.idSignal = idSignal;
		this.idDevice = idDevice;
		this.idSignalType = idSignalType;
		this.idSignalLevel = idSignalLevel;
		this.shortName = shortName;
		this.address = address;
		this.label = label;
		this.refreshRate = refreshRate;
		this.logRate = logRate;
	}
	
	public Signal(Signal model)
	{
		this(model.getIdSignal(), model.getIdDevice(), model.getIdSignalType(), model.getIdSignalLevel(), model.getShortName(), model.getAddress(), model.getLabel(),
				model.getRefreshRate(), model.getLogRate());
		
		this.device = model.getDevice();
		this.signalType = model.getSignalType();
		this.signalLevel = model.getSignalLevel();
		
		this.listeners = model.getListeners();
	}

	
	// Getters and Setters
	
	public SignalType getSignalType()
	{
		return signalType;
	}

	public void setSignalType(SignalType signalType)
	{
		this.signalType = signalType;
	}

	public SignalLevel getSignalLevel()
	{
		return signalLevel;
	}

	public void setSignalLevel(SignalLevel signalLevel)
	{
		this.signalLevel = signalLevel;
	}

	public Device getDevice()
	{
		return device;
	}

	public void setDevice(Device device)
	{
		this.device = device;
	}

	public Integer getIdSignal()
	{
		return idSignal;
	}

	public void setIdSignal(Integer idSignal)
	{
		this.idSignal = idSignal;
	}

	public Integer getIdDevice()
	{
		return idDevice;
	}

	public void setIdDevice(Integer idDevice)
	{
		this.idDevice = idDevice;
	}

	public Integer getIdSignalType()
	{
		return idSignalType;
	}

	public void setIdSignalType(Integer idSignalType)
	{
		this.idSignalType = idSignalType;
	}

	public Integer getIdSignalLevel()
	{
		return idSignalLevel;
	}

	public void setIdSignalLevel(Integer idSignalLevel)
	{
		this.idSignalLevel = idSignalLevel;
	}

	public String getShortName()
	{
		return shortName;
	}

	public void setShortName(String shortName)
	{
		this.shortName = shortName;
	}

	public Integer getAddress()
	{
		return address;
	}

	public void setAddress(Integer address)
	{
		this.address = address;
	}

	public String getLabel()
	{
		return label;
	}

	public void setLabel(String label)
	{
		this.label = label;
	}

	public Integer getRefreshRate()
	{
		return refreshRate;
	}

	public void setRefreshRate(Integer refreshRate)
	{
		this.refreshRate = refreshRate;
	}

	public Integer getLogRate()
	{
		return logRate;
	}

	public void setLogRate(Integer logRate)
	{
		this.logRate = logRate;
	}

	public int getCountdown()
	{
		return countdown;
	}

	public void setCountdown(int countdown)
	{
		this.countdown = countdown;
	}

	public boolean isValid()
	{
		return isValid;
	}

	public void setValid(boolean isValid)
	{
		this.isValid = isValid;
	}
	
	public EventListenerList getListeners()
	{
		return this.listeners;
	}
	
	
	// Operationnal code

	protected boolean Connect()
	{
		try
		{
			 YAPI.RegisterHub(this.device.getAddress());
			 return true;
		} 
		catch (YAPI_Exception ex)
		{
			ex.printStackTrace();
			return false;
		}
	}
	
	// Empty methods for polymorphism purposes
	@Override
	public Integer call() throws Exception
	{
		return null;
	}
	
	@Override
	public boolean Refresh()
	{
		return false;
	}

	@Override
	public boolean Init()
	{
		return false;
	}
	
	private boolean checkDate()
	{
		// init
		if (lastUpdate == null)
		{
			lastUpdate = Calendar.getInstance();
			lastUpdate.set(Calendar.DAY_OF_MONTH, lastUpdate.get(Calendar.DAY_OF_MONTH) - 1);
			
			System.out.println("Signal : " + this.shortName + " - Calendar init to : " + lastUpdate.getTime());
		}
		
		try
		{
			System.out.println("Signal : " + this.shortName + " - checkDate.");
			if (Calendar.getInstance().get(Calendar.DAY_OF_MONTH) != lastUpdate.get(Calendar.DAY_OF_MONTH))
			{
				System.out.println("Signal : " + this.shortName + " - checkDate = true.");
				lastUpdate = Calendar.getInstance();
				return true;
			}
			else
			{
				System.out.println("Signal : " + this.shortName + " - checkDate = false.");
				return false;
			}
		}
		
		catch(Exception ex)
		{
			ex.printStackTrace();
			return false;
		}
	}
	
	protected void FormatValue(Double value)
	{
		if (signalType.coeff != null && signalType.coeff != 0.0)
		{
			value = value / signalType.coeff;
		}
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
	
	public ISignalValueListener[] getValueListeners()
	{
		return this.listeners.getListeners(ISignalValueListener.class);
	}
	
	protected void fireValueChanged(SignalValueEvent event)
	{
		// A bit of work to advertise totalizers ONCE A DAY
		if (!(getSignalType().isTotalizer) || (getSignalType().isTotalizer && checkDate()))
		{
			System.out.println("Signal : " + this.shortName + " :: fireValueChanged ID - " + event.getIdSignal() + " - DoubleValue : " + event.getDoubleValue() + " - IntValue : " + event.getIntValue() + " - BoolValue : " + event.getBoolValue() + " - Datetime : " + new Date(event.getDatetime()).toString());
		
			for (ISignalValueListener listener : getValueListeners())
			{
				listener.SignalValueChanged(event);
			}
		}
		
	}

}