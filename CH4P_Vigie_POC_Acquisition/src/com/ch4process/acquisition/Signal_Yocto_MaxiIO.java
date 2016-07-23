package com.ch4process.acquisition;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import com.yoctopuce.YoctoAPI.YAPI_Exception;
import com.yoctopuce.YoctoAPI.YDigitalIO;

public class Signal_Yocto_MaxiIO extends Signal
{
	Boolean value;
	
	Integer portMapping;
	Integer portState;
	Integer portSize;
	YDigitalIO ioSensor;
	
	Map<Integer, String> channels = new HashMap<Integer, String>();
	Map<Integer, Integer> channelsState = new HashMap<Integer, Integer>();

	
	public Signal_Yocto_MaxiIO()
	{
		super();
	}

	public Signal_Yocto_MaxiIO(String numSerie, String adresse, String libelle, Integer periode,Integer plage_min, Integer plage_max, Float coeff, String marque, String modele)
	{
		super(numSerie, adresse, libelle, periode, plage_min, plage_max, coeff, marque, modele);
	}
	
	@Override
	public boolean init()
	{
		try
		{
			parseParams();
			this.numeroserie = this.numeroserie + ".digitalIO";
			this.isAnalogique = false;
			ioSensor = YDigitalIO.FindDigitalIO(this.numeroserie);
			portMapping = ioSensor.get_portDirection();
			portSize = ioSensor.get_portSize();
			
			if (portMapping != ioSensor.PORTDIRECTION_INVALID && portSize != ioSensor.PORTSIZE_INVALID)
			{
				return true;
			}
			return false;
			
		}
		catch (YAPI_Exception ex)
		{
			ex.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean refresh()
	{
		try
		{
			portState = ioSensor.get_portState();
			if (portState != ioSensor.PORTSTATE_INVALID)
			{
				value = ((portState & entree) != 0);
				this.countdown = this.periode;
				
				fireValueChanged(value);
				
				return true;
			}
			return false;
		}
		catch (YAPI_Exception ex)
		{
			ex.printStackTrace();
			return false;
		}
	}
	
	@Override
	public Boolean getBoolValue()
	{
		return value;
	}
	
	protected void fireValueChanged(boolean value)
	{
		for (ISignalValueListener listener : getValueListeners())
		{
			listener.boolValueChanged(this.capteur_id, this.value, Calendar.getInstance().getTime().getTime());
		}
	}
	
	@Override
	public void start()
	{
		try
		{
			connect();
			init();
			refresh();
			super.start();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public void run()
	{
		try
		{
			while(true)
			{
				refresh();
				Thread.sleep(this.periode * 1000);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
