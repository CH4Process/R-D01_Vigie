package com.ch4process.main;

public class VigieMain extends Thread
{
	static Thread T_Acquisition;
	static Thread T_Report;
	
	public static void main(String[] args)
	{
		try
		{
			// TODO : Liste des �volutions
			// + Logs en BDD
			// + Logs des erreurs
			// + Vraie gestion des exceptions
			// + Ccntr�leur qui relance les modules crash�s
			// + Visu des applications
			// + Envoi de mail sur lancement / arr�t de module et sur exception
			// + Fichiers XML
			// + Mots de passe crypt�s
			
			Acquisition_Init();
			Report_Init();
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
	
	private static void Report_Init()
	{
		T_Report = new VigieRapport("VigieRapport");
		T_Report.start();
	}
}
