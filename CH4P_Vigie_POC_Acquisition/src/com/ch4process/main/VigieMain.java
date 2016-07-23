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
						// +0 Logs en BDD
						// +1 Logs des erreurs (LOG4J ?)
						// +0 Vraie gestion des exceptions
						// +0 Contrôleur qui relance les modules crashés
						// +1 Visu des applications (JavaFX ou Swing ?)
						// +1 Envoi de mail sur lancement / arrêt de module et sur exception
						// +1 Fichiers XML
						// +1 Mots de passe cryptés via le JDK et pas par une bibliothèque externe : https://www.javacodegeeks.com/2012/05/secure-password-storage-donts-dos-and.html
						// +0 Utiliser Executor pour les Threads et basculer les modules en Callable plutôt qu'en Threads.
						// +0 InternalEmail en fichier de conf pour les mails internes
						// +0 InternalName pour les variables en BDD pour les rapports
						// +0 Il peut être sympa de suivre le nombre de défauts acquis et le nombre de sms envoyés dans les rapports
						// +0 Le signal se charge de mettre sa valeur à jour et de me lancer un évènement « fireValueChanged » pour prévenir mon logWorker qu’une valeur a changée. Le signal possède en interne sa dernière valeur ainsi que sa qualité.
						//Le logWorker travaille de son côté et possède une horloge pour chaque signal. Sur évènement ValueChanged il remet à zéro le compteur de l’horloge et continue sa routine.
						//Si l’horloge atteint zéro, il va demander la valeur au capteur et va l’enregistrer.
						//Est-ce qu’il est possible de faire cela avec des bases de temps fixes ou faut-il mettre en place un contrôle temps-réel ? (avec quelque chose du genre Time.deltaTime ?)
						
						// +0 Signal emmet un fireValueChanged pour signaler un changement de valeur. Les alertes et alarmes fonctionnent différemment : on notifie dés que la valeur change ou au bout du refreshrate si la valeur n'a pas changé
						// +0 Les workers doivent être ThreadSafes !!

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
