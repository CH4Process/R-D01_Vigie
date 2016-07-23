package com.ch4process.acquisition;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;

import javax.swing.event.EventListenerList;
import com.yoctopuce.YoctoAPI.YAPI;
import com.yoctopuce.YoctoAPI.YAPI_Exception;

public class Signal extends Thread
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
	String address = null;
	String label = null;
	String refreshRate = null;
	String logRate = null;
	
	// Other variables
	int countdown = 0;
	int entree;
	boolean isValid;
	
	// Event handling
	EventListenerList listeners = new EventListenerList();
	
	
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
			String address, String label, String refreshRate, String logRate)
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

	/**
	 * 
	 * @param fieldName
	 * @param fieldValue
	 */
	public void setField(String fieldName, Object fieldValue)
	{
		try
		{
			Class thisClass = this.getClass();
			Class parentClass = thisClass.getSuperclass();
			Field field = parentClass.getDeclaredField(fieldName);
			boolean access = field.isAccessible();
			field.setAccessible(true);
			field.set(this, fieldValue);
			field.setAccessible(access);
		}
		catch(NoSuchFieldException | IllegalArgumentException | IllegalAccessException ex)
		{
			ex.printStackTrace();
		}
		
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

	public String getAddress()
	{
		return address;
	}

	public void setAddress(String address)
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

	public String getRefreshRate()
	{
		return refreshRate;
	}

	public void setRefreshRate(String refreshRate)
	{
		this.refreshRate = refreshRate;
	}

	public String getLogRate()
	{
		return logRate;
	}

	public void setLogRate(String logRate)
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

	public int getEntree()
	{
		return entree;
	}

	public void setEntree(int entree)
	{
		this.entree = entree;
	}

	public boolean isValid()
	{
		return isValid;
	}

	public void setValid(boolean isValid)
	{
		this.isValid = isValid;
	}
	
	
	// Operationnal code
	
	public void start()
	{
		super.start();
	}
	
	public void run()
	{
	}

	protected boolean connect()
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
	
	protected boolean refresh()
	{
		return false;
	}
	
	protected boolean init()
	{
		return false;
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