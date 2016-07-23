package com.ch4process.acquisition;

import com.yoctopuce.YoctoAPI.YRelay;

public class Commande extends Signal implements IScenarioCommandListener
{
	YRelay sensor;
	Double value; 
	
	public Commande()
	{
		super();
	}

	public Commande(String numSerie, String adresse, String libelle, Integer periode,Integer plage_min, Integer plage_max, Float coeff, String marque, String modele)
	{
		super(numSerie, adresse, libelle, periode, plage_min, plage_max, coeff, marque, modele);
	}
	
	@Override
	public boolean init()
	{
		try
		{
			parseParams();
			this.numeroserie = this.numeroserie + ".relay" + this.entree;
			this.isAnalogique = false;
			sensor = YRelay.FindRelay(this.numeroserie);
			this.periode = 5;
			return sensor.isOnline(); 
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return false;
		}
	}
	
	@Override
	public void start()
	{
		try
		{
			connect();
			init();
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
				Thread.sleep(this.periode * 1000);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void setBoolValue(boolean value)
	{
		try
		{
			if (value == false)
			{
				sensor.setOutput(sensor.OUTPUT_OFF);
			}
			else
			{
				sensor.setOutput(sensor.OUTPUT_ON);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void boolCommand(int capteur_id, boolean value)
	{
		if (this.capteur_id.equals(capteur_id))
		{
			this.setBoolValue(value);
		}
	}
	
	public void addActionEventListener(IActionEventListener listener)
	{
		listeners.add(IActionEventListener.class, listener);
	}
	
	public void removeActionEventListener(IActionEventListener listener)
	{
		listeners.remove(IActionEventListener.class, listener);
	}
	
	protected IActionEventListener[] getActionEventListeners()
	{
		return this.listeners.getListeners(IActionEventListener.class);
	}
	

}
