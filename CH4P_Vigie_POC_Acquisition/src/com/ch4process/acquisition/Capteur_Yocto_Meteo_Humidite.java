package com.ch4process.acquisition;

import com.yoctopuce.YoctoAPI.YHumidity;



public class Capteur_Yocto_Meteo_Humidite extends Capteur implements ICapteur
{
	YHumidity sensor;
	Double value;

	
	public Capteur_Yocto_Meteo_Humidite()
	{
		super();
	}

	public Capteur_Yocto_Meteo_Humidite(String numSerie, String adresse, String libelle, Integer periode,Integer plage_min, Integer plage_max, Float coeff, String marque, String modele)
	{
		super(numSerie, adresse, libelle, periode, plage_min, plage_max, coeff, marque, modele);
	}
	
	@Override
	public boolean init()
	{
		try
		{
			this.isAnalogique = true;
			sensor = YHumidity.FindHumidity(this.numeroserie);
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
	public Double getValue()
	{
		return value;
	}
}
