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
						// +0 Logs en BDD
						// +1 Logs des erreurs (LOG4J ?)
						// +0 Vraie gestion des exceptions
						// +0 Contr�leur qui relance les modules crash�s
						// +1 Visu des applications (JavaFX ou Swing ?)
						// +1 Envoi de mail sur lancement / arr�t de module et sur exception
						// +1 Fichiers XML
						// +1 Mots de passe crypt�s via le JDK et pas par une biblioth�que externe : https://www.javacodegeeks.com/2012/05/secure-password-storage-donts-dos-and.html
						// +0 Utiliser Executor pour les Threads et basculer les modules en Callable plut�t qu'en Threads.
						// +0 InternalEmail en fichier de conf pour les mails internes
						// +0 InternalName pour les variables en BDD pour les rapports
						// +0 Il peut �tre sympa de suivre le nombre de d�fauts acquis et le nombre de sms envoy�s dans les rapports
						// +0 Le signal se charge de mettre sa valeur � jour et de me lancer un �v�nement � fireValueChanged � pour pr�venir mon logWorker qu�une valeur a chang�e. Le signal poss�de en interne sa derni�re valeur ainsi que sa qualit�.
						//Le logWorker travaille de son c�t� et poss�de une horloge pour chaque signal. Sur �v�nement ValueChanged il remet � z�ro le compteur de l�horloge et continue sa routine.
						//Si l�horloge atteint z�ro, il va demander la valeur au capteur et va l�enregistrer.
						//Est-ce qu�il est possible de faire cela avec des bases de temps fixes ou faut-il mettre en place un contr�le temps-r�el ? (avec quelque chose du genre Time.deltaTime ?)
						
						// +0 Signal emmet un fireValueChanged pour signaler un changement de valeur. Les alertes et alarmes fonctionnent diff�remment : on notifie d�s que la valeur change ou au bout du refreshrate si la valeur n'a pas chang�
						// +0 Les workers doivent �tre ThreadSafes !!

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
