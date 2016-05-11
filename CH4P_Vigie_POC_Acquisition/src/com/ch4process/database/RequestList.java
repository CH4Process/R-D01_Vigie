package com.ch4process.database;

public class RequestList
{
	public final String REQUEST_ListeCapteurs = "SELECT c.capteur_id, c.numeroserie, c.adresse, c.libelle, c.periode, tc.coeff, tc.marque, tc.modele, tc.plage_min, tc.plage_max FROM capteur c, type_capteur tc WHERE c.type_capteur_id = tc.type_capteur_id;";
	public final String REQUEST_RecordMesure = "INSERT INTO mesure (capteur_id, valeur, datetime) VALUES (?,?,?);";
	public final String REQUEST_ListeScenarios = "SELECT s.scenario_id, s.capteur_id, s.test, s.params FROM scenario;";

}
