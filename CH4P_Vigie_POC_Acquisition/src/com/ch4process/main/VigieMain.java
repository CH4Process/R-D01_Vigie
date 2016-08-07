package com.ch4process.main;

import com.ch4process.utils.CH4P_Multithreading;

public class VigieMain extends Thread
{
	static Thread T_Acquisition;
	static Thread T_Report;
	
	public static void main(String[] args)
	{
		try
		{
			// TODO : Liste des �volutions

			//EVO DONE : 
				// Utiliser Executor pour les Threads et basculer les modules en Callable plut�t qu'en Threads.
				// Les workers doivent �tre ThreadSafes !! --> FAIT via l'utilisation de LinkedLists
			
			//EVO 5 : 
				// Mieux g�rer le modbus !
				// Travailler la mise � jour des valeurs de type Totalizer pour �viter une mise � jour trop fr�quente. Peut �tre en utilisant le countdown ? !!

			//EVO 10 : 
				// Logs en BDD
				// Vraie gestion des exceptions
				// Contr�leur qui relance les modules crash�s
				// Envoi de mail sur lancement / arr�t de module et sur exception
			
			//EVO 20 : 
				// InternalEmail en fichier de conf pour les mails internes
				// InternalName pour les variables en BDD pour les rapports
				// Il peut �tre sympa de suivre le nombre de d�fauts acquis et le nombre de sms envoy�s dans les rapports
				// Signal emmet un fireValueChanged pour signaler un changement de valeur. Les alertes et alarmes fonctionnent diff�remment : on notifie d�s que la valeur change ou au bout du refreshrate si la valeur n'a pas chang�
				// principe de lograte et refreshrate
				// G�rer de potentielles valeurs de retour des callable via des Futures dans la classe CH4P_Multithreading -> Executor
			
			//EVO 50 : 
				// Logs des erreurs (LOG4J ?)
				// Visu des applications (JavaFX ou Swing ?)
				// Fichiers XML
				// Mots de passe crypt�s via le JDK et pas par une biblioth�que externe : https://www.javacodegeeks.com/2012/05/secure-password-storage-donts-dos-and.html
			
			
						
			Init_Utils();
			Init_VigieAcquisition();
			Init_VigieReport();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	private static void Init_VigieAcquisition()
	{
		T_Acquisition = new VigieAcquisition("VigieAcquisition");
		T_Acquisition.start();
	}
	
	private static void Init_VigieReport()
	{
		T_Report = new VigieRapport("VigieRapport");
		T_Report.start();
	}
	
	private static void Init_Utils()
	{
		CH4P_Multithreading.Init();
	}
}
