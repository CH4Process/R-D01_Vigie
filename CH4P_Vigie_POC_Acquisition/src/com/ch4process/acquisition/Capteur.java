package com.ch4process.acquisition;

import java.lang.reflect.Field;
import com.yoctopuce.YoctoAPI.YAPI;
import com.yoctopuce.YoctoAPI.YAPI_Exception;

public class Capteur
{
	Integer capteur_id = null;
	String libelle = null;
	String numeroserie = null;
	Integer periode = null;
	String adresse = null;
	Integer plage_min = null;
	Integer plage_max = null;
	Float coeff = null;
	String marque = null;
	String modele = null;
	
	int countdown = 0;
	Boolean isAnalogique = null;
	int entree;
	
	
	public Capteur()
	{
		this(null, null, null, null, null, null, null, null, null);
	}
	
	public Capteur(String numSerie, String adresse, String libelle, Integer periode, Integer plage_min, Integer plage_max, Float coeff, String marque, String modele)
	{
		this.numeroserie = numSerie;
		this.adresse = adresse;
		this.libelle = libelle;
		this.periode = periode;
		this.plage_min = plage_min;
		this.plage_max = plage_max;
		this.coeff = coeff;
		this.marque = marque;
		this.modele = modele;
		
		this.countdown = 0;
	}
	
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

	
	public Integer getCapteur_id()
	{
		return capteur_id;
	}

	public String getLibelle() 
	{
		return libelle;
	}

	public void setLibelle(String libelle) 
	{
		this.libelle = libelle;
	}
	
	public String getNumeroserie() 
	{
		return numeroserie;
	}
	
	public Integer getPeriode() 
	{
		return periode;
	}

	public void setPeriode(Integer periode) 
	{
		this.periode = periode;
	}

	public String getAdresse() 
	{
		return adresse;
	}

	public Integer getPlage_min() 
	{
		return plage_min;
	}
	

	public void setPlage_min(Integer plage_min) 
	{
		this.plage_min = plage_min;
	}
	

	public Integer getPlage_max() 
	{
		return plage_max;
	}
	

	public void setPlage_max(Integer plage_max) 
	{
		this.plage_max = plage_max;
	}
	

	public Float getCoeff() 
	{
		return coeff;
	}
	

	public void setCoeff(Float coeff) 
	{
		this.coeff = coeff;
	}
	

	public String getMarque() 
	{
		return marque;
	}

	public String getModele() 
	{
		return modele;
	}
	
	public Object getValue()
	{
		return null;
	}

	public boolean connect()
	{
		try
		{
			 YAPI.RegisterHub(adresse);
			 return true;
		} 
		catch (YAPI_Exception ex)
		{
			ex.printStackTrace();
			return false;
		}
	}
	
	public int tick()
	{
		try
		{
			return this.countdown;
		}
		finally
		{
			this.countdown -= 1;
		}
	}
	
	public boolean refresh()
	{
		return false;
	}
	
	public boolean init()
	{
		return false;
	}
	
	protected void parseParams()
	{
		String[] params = this.numeroserie.split("\\|");
		if (params.length > 1)
		{
			this.numeroserie = params[0];
			this.entree = (int) Math.pow(2,Integer.parseInt(params[1]) -1);
		}
	}
	
	
	
}