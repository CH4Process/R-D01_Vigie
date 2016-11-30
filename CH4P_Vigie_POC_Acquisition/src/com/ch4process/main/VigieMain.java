package com.ch4process.main;

import java.util.concurrent.Callable;
import com.ch4process.database.DatabaseController;
import com.ch4process.utils.*;
import com.ch4process.windows.*;

public class VigieMain extends Thread
{
	static Callable T_Acquisition = null;
	static Callable T_Report = null;
	static VigieMainView MainView = null;
	
	static VigieAcquisition vigieAcquisition = null;
	
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
				// Fonction de log custom (console, fichier, BDD)
			
			//EVO 10 : 
				// Mecanique d'injection / diffusion d'info pour rafraichir les Properties lues (pour permettre l'annulation des SMS ou le changement de la date des rapports
				// Remplacer tous les PRINTSTACKTRACE par des Log ! 
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
			
			
			try
			{
				Init_Utils();
				Init_View();
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
			CH4P_Functions.LogException(CH4P_Functions.LOG_inConsole, ex);
		}
	}
	
	private static void Init_VigieAcquisition() throws CH4P_Exception
	{
		try
		{
			vigieAcquisition = new VigieAcquisition("VigieAcquisition");
			T_Acquisition = vigieAcquisition;
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
			CH4P_ConfigManager.Init();
			
			if (CH4P_ConfigManager.isInitialized())
			{
				DatabaseController.Init();
				CH4P_Multithreading.Init();
			}
		}
		catch (CH4P_Exception ex)
		{
			throw new CH4P_Exception(ex.getMessage(), ex.getCause());
		}
	}
	
	private static void Init_View()
	{
		MainView = new VigieMainView();
		
		try
		{
			CH4P_Multithreading.Submit(MainView);
			CH4P_Functions.addLogEventListener(MainView);
			CH4P_Functions.addLogExceptionEventListener(MainView);
			
			while(! MainView.isInitialized())
			{
				Thread.sleep(200);
			}
		}
		catch (CH4P_Exception ex)
		{
			CH4P_Functions.LogException(CH4P_Functions.LOG_inConsole, ex);
		}
		catch (InterruptedException e)
		{
			CH4P_Functions.LogException(CH4P_Functions.LOG_inConsole, e);
		}
	}
	
	public static VigieMainView getMainView()
	{
		return MainView;
	}
	
	public static VigieAcquisition getVigieAcquisition()
	{
		return vigieAcquisition;
	}
}
