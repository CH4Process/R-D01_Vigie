package com.ch4process.database;

public class RequestList
{
	public static final String REQUEST_SignalList =
			"SELECT * FROM `signal`;";
	public static final String REQUEST_SignalTypeList =
			"SELECT * FROM `signalType`;";
	public static final String REQUEST_SignalLevelList =
			"SELECT * FROM `signallevel`;";
	public static final String REQUEST_DeviceList =
			"SELECT * FROM `device`;";
	public static final String REQUEST_DeviceTypeList =
			"SELECT * FROM `devicetype`;";
	public static final String REQUEST_MeasureRecord = 
			"INSERT INTO mesure (capteur_id, valeur, datetime) VALUES (?,?,?);";
	public static final String REQUEST_ScenarioList = 
			"SELECT s.scenario_id, s.capteur_id, s.test, s.params FROM scenario s ORDER BY s.priorite;";
	public static final String REQUEST_ActionList = 
			"SELECT c.capteur_id, c.numeroserie, c.adresse, c.libelle, c.periode, tc.coeff, tc.marque, tc.modele, tc.plage_min, tc.plage_max FROM capteur c, type_capteur tc WHERE c.type_capteur_id = tc.type_capteur_id AND c.isOutput = true;";
	
	public static final String REQUEST_EventLog = 
			"INSERT INTO evenement (scenario_id, date) VALUES (?,?);";
	
	public static final String REQUEST_WeekMeasures = 
			"SELECT m.capteur_id, m.valeur, m.datetime, c.libelle, tc.unite, tc.coeff FROM mesure m, capteur c, type_capteur tc WHERE tc.type_capteur_id = c.type_capteur_id AND m.capteur_id = c.capteur_id AND m.datetime >= ?;";
	public static final String REQUEST_Indexes = 
			"SELECT t.capteur_id, c.libelle, t.valeur, t.lastupdate FROM totalizer t, capteur c WHERE c.capteur_id = t.capteur_id;";
	public static final String REQUEST_UpdateIndexes = 
			"UPDATE index i SET i.valeur = ?, i.lastupdate = NOW() WHERE i.capteur_id = ?;";
	public static final String REQUEST_Scenarios = 
			"SELECT s.libelle, s.test, s.params, e.date FROM scenario s, evenement e WHERE e.scenario_id = s.scenario_id;";
}
