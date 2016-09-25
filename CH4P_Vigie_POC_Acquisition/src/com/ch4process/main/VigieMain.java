package com.ch4process.main;

import java.util.concurrent.Callable;

import com.ch4process.database.DatabaseController;
import com.ch4process.utils.CH4P_ConfigManager;
import com.ch4process.utils.CH4P_Exception;
import com.ch4process.utils.CH4P_Functions;
import com.ch4process.utils.CH4P_Multithreading;

public class VigieMain extends Thread
{
	static Callable T_Acquisition;
	static Callable T_Report;
	
	public static void main(String[] args)
	{
		try
		{
			// TODO : Liste des �volutions

			//EVO DONE : 
				// Utiliser Executor pour les Threads et basculer les modules en Callable plut�t qu'en Threads.
				// Les workers doivent �tre ThreadSafes !! --> FAIT via l'utilisation de LinkedLists	
				// Mieux g�rer le modbus !
				// Travailler la mise � jour des valeurs de type Totalizer pour �viter une mise � jour trop fr�quente. Peut �tre en utilisant le countdown ? !!
				// Fonction de log custom (console, fichier, BDD)
			
			//EVO 10 : 
				// Mecanique d'injection / diffusion d'info pour rafraichir les Properties lues (pour permettre l'annulation des SMS ou le changement de la date des rapports
				// Remplacer tous les PRINTSTACKTRACE par des Log ! 
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
				// G�rer de potentielles valeurs de retour des callable via des Futures dans la classe CH4P_Multithreading -> Executor // Ou g�rer �a dans l'exception custom...
			
			//EVO 50 : 
				// Logs des erreurs (LOG4J ?)
				// Visu des applications (JavaFX ou Swing ?)
				// Fichiers XML
				// Mots de passe crypt�s via le JDK et pas par une biblioth�que externe : https://www.javacodegeeks.com/2012/05/secure-password-storage-donts-dos-and.html
			
			
			try
			{
				Init_Utils();
			}
			catch(Exception ex)
			{
				CH4P_Functions.Log(VigieMain.class.getName(), CH4P_Functions.LOG_inMsgBox, CH4P_Functions.LEVEL_ERROR, "Erreur pendant l'initialisation de l'application : " + ex.getMessage());
				System.exit(0);
			}
			
			try
			{
				Init_VigieAcquisition();
				Init_VigieReport();
			}
			catch(Exception ex)
			{
				CH4P_Functions.Log(VigieMain.class.getName(), CH4P_Functions.LOG_inMsgBox, CH4P_Functions.LEVEL_ERROR, "Erreur pendant l'execution de l'application : " + ex.getMessage());
			}
			
			
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	private static void Init_VigieAcquisition() throws CH4P_Exception
	{
		try
		{
			T_Acquisition = new VigieAcquisition("VigieAcquisition");
			CH4P_Multithreading.Submit(T_Acquisition);
		}
		catch (CH4P_Exception ex)
		{
			throw new CH4P_Exception(ex.getMessage(), ex.getCause());
		}
	}
	
	private static void Init_VigieReport() throws CH4P_Exception
	{
		try
		{
			T_Report = new VigieReport("VigieReport");
			CH4P_Multithreading.Submit(T_Report);
		}
		catch (CH4P_Exception ex)
		{
			throw new CH4P_Exception(ex.getMessage(), ex.getCause());
		}
	}
	
	private static void Init_Utils() throws CH4P_Exception
	{
		try
		{
			DatabaseController.Init();
			CH4P_Multithreading.Init();
			CH4P_ConfigManager.Init();
		}
		catch (CH4P_Exception ex)
		{
			throw new CH4P_Exception(ex.getMessage(), ex.getCause());
		}
	}
}
