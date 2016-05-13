package com.ch4process.acquisition;

import java.util.EventListener;

import com.yoctopuce.YoctoAPI.YGenericSensor;

public class Capteur_Yocto_4_20mA extends Capteur
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
	public Double getDoubleValue()
	{
		return value;
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
				if(tick() <= 0)
				{
					refresh();
				}
				Thread.sleep(1000);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	protected void fireValueChanged(double value)
	{
		for (ICapteurValueListener listener : getValueListeners())
		{
			listener.doubleValueChanged(this.capteur_id, this.value, date.getInstance().getTime().getTime());
		}
	}
	
	private boolean scaleValue()
	{
		if (plage_min != null && plage_max != null)
		{
			// TODO : Filtrer la valeur si elle est à -29999 ou + 29999 (valeur invalide)
			// ajouter une colonne "isValid" dans la table mesure en BDD ?
			int plage = this.plage_max - this.plage_min;
			value = (plage / 16) * (value - 4);
			this.countdown = this.periode;
			
			fireValueChanged(value);
			
			return true;			
		}
		return false;
	}

}
