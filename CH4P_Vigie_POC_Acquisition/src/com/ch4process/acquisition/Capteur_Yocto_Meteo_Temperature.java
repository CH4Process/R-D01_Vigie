package com.ch4process.acquisition;

import com.yoctopuce.YoctoAPI.YTemperature;

public class Capteur_Yocto_Meteo_Temperature extends Capteur
{
	YTemperature sensor;
	Double value;

	
	
	public Capteur_Yocto_Meteo_Temperature()
	{
		super();
	}

	public Capteur_Yocto_Meteo_Temperature(String numSerie, String adresse, String libelle, Integer periode,Integer plage_min, Integer plage_max, Float coeff, String marque, String modele)
	{
		super(numSerie, adresse, libelle, periode, plage_min, plage_max, coeff, marque, modele);
	}

	@Override
	public boolean init()
	{
		try
		{
			this.isAnalogique = true;
			sensor = YTemperature.FindTemperature(this.numeroserie);
			return sensor.isOnline(); 
		}
		catch (Exception ex)
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
			value = sensor.getCurrentValue();
			if(value != sensor.CURRENTVALUE_INVALID)
			{
				this.countdown = this.periode;
				fireValueChanged(value);
				return true;
			}
			else
			{
				return false;
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return false;
		}
	}
	
	
	@Override
	public Double getDoubleValue()
	{
		return value;
	}
	
	protected void fireValueChanged(double value)
	{
		for (ICapteurValueListener listener : getValueListeners())
		{
			listener.doubleValueChanged(this.capteur_id, this.value, date.getInstance().getTime().getTime());
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

