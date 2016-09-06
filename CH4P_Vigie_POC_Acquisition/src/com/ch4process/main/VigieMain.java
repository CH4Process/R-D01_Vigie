package com.ch4process.main;

import java.util.concurrent.Callable;

import com.ch4process.database.DatabaseController;
import com.ch4process.utils.CH4P_Multithreading;

public class VigieMain extends Thread
{
	static Callable T_Acquisition;
	static Callable T_Report;
	
	public static void main(String[] args)
	{
		try
		{
			// TODO : Liste des évolutions

			//EVO DONE : 
				// Utiliser Executor pour les Threads et basculer les modules en Callable plutôt qu'en Threads.
				// Les workers doivent être ThreadSafes !! --> FAIT via l'utilisation de LinkedLists	
				// Mieux gérer le modbus !
				// Travailler la mise à jour des valeurs de type Totalizer pour éviter une mise à jour trop fréquente. Peut être en utilisant le countdown ? !!
			
			//EVO 10 : 
				// Logs en BDD
				// Vraie gestion des exceptions
				// Contrôleur qui relance les modules crashés
				// Envoi de mail sur lancement / arrêt de module et sur exception
			
			//EVO 20 : 
				// InternalEmail en fichier de conf pour les mails internes
				// InternalName pour les variables en BDD pour les rapports
				// Il peut être sympa de suivre le nombre de défauts acquis et le nombre de sms envoyés dans les rapports
				// Signal emmet un fireValueChanged pour signaler un changement de valeur. Les alertes et alarmes fonctionnent différemment : on notifie dés que la valeur change ou au bout du refreshrate si la valeur n'a pas changé
				// principe de lograte et refreshrate
				// Gérer de potentielles valeurs de retour des callable via des Futures dans la classe CH4P_Multithreading -> Executor // Ou gérer ça dans l'exception custom...
			
			//EVO 50 : 
				// Logs des erreurs (LOG4J ?)
				// Visu des applications (JavaFX ou Swing ?)
				// Fichiers XML
				// Mots de passe cryptés via le JDK et pas par une bibliothèque externe : https://www.javacodegeeks.com/2012/05/secure-password-storage-donts-dos-and.html
			
			
						
			Init_Utils();
			//Init_VigieAcquisition();
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
		CH4P_Multithreading.Submit(T_Acquisition);
	}
	
	private static void Init_VigieReport()
	{
		T_Report = new VigieReport("VigieReport");
		CH4P_Multithreading.Submit(T_Report);
	}
	
	private static void Init_Utils()
	{
		DatabaseController.Init();
		CH4P_Multithreading.Init();
	}
}
