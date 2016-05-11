package com.ch4process.acquisition;

import com.yoctopuce.YoctoAPI.YGenericSensor;

public class Capteur_Yocto_4_20mA extends Capteur implements ICapteur
{
	YGenericSensor sensor;
	Double value;

	
	public Capteur_Yocto_4_20mA()
	{
		super();
	}

	public Capteur_Yocto_4_20mA(String numSerie, String adresse, String libelle, Integer periode,Integer plage_min, Integer plage_max, Float coeff, String marque, String modele)
	{
		super(numSerie, adresse, libelle, periode, plage_min, plage_max, coeff, marque, modele);
	}
	
	@Override
	public boolean init()
	{
		try
		{
			parseParams();
			this.numeroserie = this.numeroserie + ".genericSensor" + this.entree;
			this.isAnalogique = true;
			sensor = YGenericSensor.FindGenericSensor(this.numeroserie);
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
			value = sensor.getCurrentRawValue();
			if (value != sensor.CURRENTRAWVALUE_INVALID)
			{
				return scaleValue();
			}
			return false;
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
	
	private boolean scaleValue()
	{
		if (plage_min != null && plage_max != null)
		{
			int plage = this.plage_max - this.plage_min;
			System.out.println("Invalide : " + sensor.CURRENTRAWVALUE_INVALID);
			System.out.println(" CALCUL VALEUR : " + value + "plage min : " + plage_min + " plage max" + plage_max + " plage : " + plage);
			value = (plage / 16) * (value - 4);
			this.countdown = this.periode;
			return true;			
		}
		return false;
	}
}
