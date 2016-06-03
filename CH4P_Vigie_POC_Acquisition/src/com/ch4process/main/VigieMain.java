package com.ch4process.main;

public class VigieMain extends Thread
{
	static Thread T_Acquisition;
	static Thread T_Report;
	
	public static void main(String[] args)
	{
		try
		{
			// TODO : Liste des évolutions
			// + Logs en BDD
			// + Logs des erreurs
			// + Vraie gestion des exceptions
			// + Ccntrôleur qui relance les modules crashés
			// + Visu des applications
			// + Envoi de mail sur lancement / arrêt de module et sur exception
			// + Fichiers XML
			// + Mots de passe cryptés
			
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
