package com.ch4process.main;

public class VigieMain extends Thread
{
	static Thread T_Acquisition;
	
	public static void main(String[] args)
	{
		try
		{
			Acquisition_Init();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	private static void Acquisition_Init()
	{
		T_Acquisition = new VigieAcquisition("VigieAcquisition");
		T_Acquisition.start();
	}
}
