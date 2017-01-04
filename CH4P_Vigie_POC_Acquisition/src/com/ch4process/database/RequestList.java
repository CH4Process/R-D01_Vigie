package com.ch4process.database;

public class RequestList
{
	// REVAMPED
	// ACQUISITION
	public static final String REQUEST_SignalList =
			"SELECT * FROM `Signal`;";
	public static final String REQUEST_SignalTypeList =
			"SELECT * FROM `SignalType`;";
	public static final String REQUEST_SignalLevelList =
			"SELECT * FROM `SignalLevel`;";
	public static final String REQUEST_DeviceList =
			"SELECT * FROM `Device`;";
	public static final String REQUEST_DeviceTypeList =
			"SELECT * FROM `DeviceType`;";
	public static final String REQUEST_RecordDigitalMeasure = 
			"INSERT INTO `DigitalMeasure` (value, datetime, isValid, idSignal) VALUES (?,?,?,?);";
	public static final String REQUEST_RecordAnalogMeasure = 
			"INSERT INTO `AnalogMeasure` (value, datetime, isValid, idSignal) VALUES (?,?,?,?);";
	public static final String REQUEST_RecordTotalizer = 
			"UPDATE `Totalizer` SET value = ?, datetime = ?, isValid = ? WHERE idSignal = ?;";
	public static final String REQUEST_ScenarioList = 
			"SELECT * FROM `Scenario`;";
	public static final String REQUEST_ModbusDeviceList = 
			"SELECT * FROM `ModbusDevice`;";
	// EVENTS
	public static final String REQUEST_RecordEventLog = 
			"INSERT INTO `EventLog` (eventName, eventMessage, errorLevel, datetime) VALUES (?,?,?,?);";
	// REPORT
	public static final String REQUEST_UpdateTotalizer = 
			"UPDATE `Totalizer` SET lastvalue = value, value = ?, datetime = ?, isValid = ? WHERE idSignal = ?;";
	public static final String REQUEST_TotalizerValue = 
			"SELECT t.idSignal, t.value, t.datetime, t.isValid, t.lastValue, s.label FROM `Totalizer` t, `Signal` s WHERE t.idSignal = s.idSignal;";
	public static final String REQUEST_DigitalMeasure = 
			"SELECT dm.idSignal, dm.value, dm.datetime, s.label, st.isNormallyOpen FROM `DigitalMeasure` dm,`Signal` s,`SignalType` st WHERE dm.datetime >= ? AND dm.isValid = 1 AND s.idSignal = dm.idSignal AND st.idSignalType = s.idSignalType ORDER BY dm.datetime;";
	public static final String REQUEST_AnalogMeasure = 
			"SELECT am.idSignal, am.value, am.datetime, s.label, st.precision, st.unit FROM `AnalogMeasure` am,`Signal` s,`SignalType` st WHERE am.datetime >= ? AND am.isValid = 1 AND s.idSignal = am.idSignal AND st.idSignalType = s.idSignalType ORDER BY am.datetime;";
	public static final String REQUEST_Scenarios = 
			"SELECT * FROM `EventLog` WHERE errorLevel BETWEEN 100 AND 199 AND datetime >= ?;";
}
