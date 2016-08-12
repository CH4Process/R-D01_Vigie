package com.ch4process.main;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.rowset.CachedRowSet;

import com.ch4process.acquisition.Signal;
import com.ch4process.acquisition.SignalLevel;
import com.ch4process.acquisition.SignalType;
import com.ch4process.acquisition.Signal_Yocto_4_20mA;
import com.ch4process.acquisition.Signal_Yocto_MaxiIO;
import com.ch4process.acquisition.Signal_Yocto_Meteo_Humidite;
import com.ch4process.acquisition.Signal_Yocto_Meteo_Pression;
import com.ch4process.acquisition.Signal_Yocto_Meteo_Temperature;
import com.ch4process.acquisition.Commande;
import com.ch4process.acquisition.Device;
import com.ch4process.acquisition.DeviceType;
import com.ch4process.acquisition.LogWorker;
import com.ch4process.acquisition.ModbusDevice;
import com.ch4process.acquisition.Scenario;
import com.ch4process.acquisition.ScenarioWorker;
import com.ch4process.acquisition.RecordWorker;
import com.ch4process.database.ConnectionHandler;
import com.ch4process.database.DatabaseController;
import com.ch4process.database.DatabaseRequest;
import com.ch4process.database.IDatabaseRequestCallback;
import com.ch4process.database.RequestList;
import com.ch4process.utils.CH4P_Exception;
import com.ch4process.utils.CH4P_Multithreading;

import sun.security.jca.GetInstance;

public class VigieAcquisition extends Thread
{
	Thread thisThread;
	String threadName;
	
	Map<Integer, Signal> signals = new HashMap<Integer,Signal>();
	Map<Integer,Device> devices = new HashMap<Integer,Device>();
	Map<Integer,ModbusDevice> modbusDevices = new HashMap<Integer,ModbusDevice>();
	Map<Integer,DeviceType> deviceTypes = new HashMap<Integer,DeviceType>();
	Map<Integer,SignalLevel> signalLevels = new HashMap<Integer,SignalLevel>();
	Map<Integer,SignalType> signalTypes = new HashMap<Integer,SignalType>();
	Map<Integer,Scenario> scenarios = new HashMap<Integer,Scenario>();
	//List<Commande> commandes = new ArrayList<Commande>();
	
	ConnectionHandler connectionHandler;
	DatabaseRequest signalListRequest;
	DatabaseRequest deviceListRequest;
	DatabaseRequest modbusDeviceListRequest;
	DatabaseRequest deviceTypeListRequest;
	DatabaseRequest signalLevelListRequest;
	DatabaseRequest signalTypeListRequest;
	DatabaseRequest scenarioListRequest;
	DatabaseRequest recordValueRequest;
	//DatabaseRequest commandeListRequest;
	DatabaseRequest logEventRequest;
	IDatabaseRequestCallback signalListRequestCallback;
	IDatabaseRequestCallback deviceListRequestCallback;
	IDatabaseRequestCallback modbusDeviceListRequestCallback;
	IDatabaseRequestCallback deviceTypeListRequestCallback;
	IDatabaseRequestCallback signalLevelListRequestCallback;
	IDatabaseRequestCallback signalTypeListRequestCallback;
	IDatabaseRequestCallback scenarioListRequestCallback;
	//IDatabaseRequestCallback commandeListRequestCallback;
	boolean signalListRequest_done = false;
	boolean deviceListRequest_done = false;
	boolean modbusDeviceListRequest_done = false;
	boolean deviceTypeListRequest_done = false;
	boolean signalLevelListRequest_done = false;
	boolean signalTypeListRequest_done = false;
	boolean scenarioListRequest_done = false;
	boolean commandeListRequest_done = false;
	
	RecordWorker recordWorker;
	ScenarioWorker scenarioWorker;
	LogWorker logWorker;
	
	boolean firstRun = true;
	
	// Constructor
	
	/**
	 * Creates the VigieAcquisition main thread with the given name
	 * @param name
	 */
	public VigieAcquisition(String name)
	{
		this.threadName = name;
	}
	
	
	// Configuration objects creation
	
	/**
	 * Initialize the list of signals
	 * @param listSignals
	 * @throws CH4P_Exception
	 */
	private void SignalList(CachedRowSet listSignals) throws CH4P_Exception
	{
		try
		{
			while(listSignals.next())
			{
				//Signal signal = SignalInstance(listSignals.getString("brandName"),listSignals.getString("modelName"));
				Signal signal = new Signal();
				
				signal.setIdSignal(listSignals.getInt("idSignal"));
				signal.setIdDevice(listSignals.getInt("idDevice"));
				signal.setIdSignalType(listSignals.getInt("idSignalType"));
				signal.setIdSignalLevel(listSignals.getInt("idSignalLevel"));
				signal.setShortName(listSignals.getString("shortName"));
				signal.setAddress(listSignals.getInt("address"));
				signal.setLabel(listSignals.getString("label"));
				signal.setRefreshRate(listSignals.getInt("refreshrate"));
				signal.setLogRate(listSignals.getInt("logRate"));
				
				signals.put(signal.getIdSignal(),signal);
				signal.addValueListener(recordWorker);
				signal.addValueListener(scenarioWorker);
				signal = null;
			}
		}
		catch (Exception ex)
		{
			throw new CH4P_Exception(ex.getMessage(), ex.getCause());
		}
	}
	/**
	 * Initialize the list of devices
	 * @param listDevices
	 * @throws CH4P_Exception
	 */
	private void DeviceList(CachedRowSet listDevices) throws CH4P_Exception
	{
		try
		{
			while (listDevices.next())
			{
				Device device = new Device();
				
				device.setIdDevice(listDevices.getInt("idDevice"));
				device.setIdDeviceType(listDevices.getInt("idDeviceType"));
				device.setSerialNumber(listDevices.getString("serialNumber"));
				device.setAddress(listDevices.getString("address"));
				
				devices.put(device.getIdDevice(), device);
				device = null;
			}
		}
		catch(Exception ex)
		{
			throw new CH4P_Exception(ex.getMessage(), ex.getCause());
		}
	}
	
	private void ModbusDeviceList(CachedRowSet listModbusDevices) throws CH4P_Exception
	{
		try
		{
			while (listModbusDevices.next())
			{
				ModbusDevice modbusDevice = new ModbusDevice();
				
				modbusDevice.setIdModbusDevice(listModbusDevices.getInt("idModbusDevice"));
				modbusDevice.setIdDevice(listModbusDevices.getInt("idDevice"));
				modbusDevice.setSlaveNumber(listModbusDevices.getInt("slaveNumber"));
				modbusDevice.setByteOrder(listModbusDevices.getString("byteOrder"));
				modbusDevice.setSpeed(listModbusDevices.getInt("speed"));
				
				modbusDevices.put(modbusDevice.getIdDevice(), modbusDevice);
				modbusDevice = null;
			}
		}
		catch(Exception ex)
		{
			throw new CH4P_Exception(ex.getMessage(), ex.getCause());
		}
	}
	
	/**
	 * Initialize the list of deviceTypes
	 * @param listDeviceTypes
	 * @throws CH4P_Exception
	 */
	private void DeviceTypeList(CachedRowSet listDeviceTypes) throws CH4P_Exception
	{
		try
		{
			while (listDeviceTypes.next())
			{
				DeviceType deviceType = new DeviceType();
				
				deviceType.setIdDeviceType(listDeviceTypes.getInt("idDeviceType"));
				deviceType.setBrandName(listDeviceTypes.getString("brandName"));
				deviceType.setModelName(listDeviceTypes.getString("modelName"));
				
				deviceTypes.put(deviceType.getIdDeviceType(), deviceType);
				deviceType = null;
			}
		}
		catch(Exception ex)
		{
			throw new CH4P_Exception(ex.getMessage(), ex.getCause());
		}
	}
	/**
	 * Initialize the list of signalTypes
	 * @param listSignalTypes
	 * @throws CH4P_Exception
	 */
	private void SignalTypeList(CachedRowSet listSignalTypes) throws CH4P_Exception
	{
		try
		{
			while(listSignalTypes.next())
			{
				SignalType signalType = new SignalType();
				
				signalType.setIdSignalType(listSignalTypes.getInt("idSignalType"));
				signalType.setIsTor(listSignalTypes.getBoolean("isTor"));
				signalType.setIsOutput(listSignalTypes.getBoolean("isOutput"));
				signalType.setIsNormallyOpen(listSignalTypes.getBoolean("isNormallyOpen"));
				signalType.setIsCom(listSignalTypes.getBoolean("isCom"));
				signalType.setIsTotalizer(listSignalTypes.getBoolean("isTotalizer"));
				signalType.setCoeff(listSignalTypes.getFloat("coeff"));
				signalType.setUnit(listSignalTypes.getString("unit"));
				signalType.setMaxValue(listSignalTypes.getInt("maxValue"));
				signalType.setMinValue(listSignalTypes.getInt("minValue"));
				signalType.setComFormat(listSignalTypes.getString("comFormat"));
				
				signalTypes.put(signalType.getIdSignalType(), signalType);
				signalType = null;
			}
		}
		catch (Exception ex)
		{
			throw new CH4P_Exception(ex.getMessage(), ex.getCause());
		}
	}
	/**
	 * Initialize the list of signalLevels
	 * @param listSignalLevels
	 * @throws CH4P_Exception
	 */
	private void SignalLevelList(CachedRowSet listSignalLevels) throws CH4P_Exception
	{
		try
		{
			while(listSignalLevels.next())
			{
				SignalLevel signalLevel = new SignalLevel();
				
				signalLevel.setIdSignalLevel(listSignalLevels.getInt("idSignalLevel"));
				signalLevel.setLabel(listSignalLevels.getString("label"));
					
				signalLevels.put(signalLevel.getIdSignalLevel(), signalLevel);
				signalLevel = null;
			}
		}
		catch (Exception ex)
		{
			throw new CH4P_Exception(ex.getMessage(), ex.getCause());
		}
	}
	/**
	 * Determines the right type of signal class to create using it's model and brand names
	 * @param brand
	 * @param model
	 * @return
	 */
	private Signal SignalInstance(String brand, String model)
	{
		try
		{
			if (brand.equals("YOCTOPUCE"))
			{
				if (model.equals("YOCTO-4-20-MA-RX"))
				{
					return new Signal_Yocto_4_20mA();
				}
				else if (model.equals("YOCTO-METEO-HUMIDITE"))
				{
					return new Signal_Yocto_Meteo_Humidite();
				}
				else if (model.equals("YOCTO-METEO-PRESSION"))
				{
					return new Signal_Yocto_Meteo_Pression();
				}
				else if (model.equals("YOCTO-METEO-TEMPERATURE"))
				{
					return new Signal_Yocto_Meteo_Temperature();
				}
				else if (model.equals("YOCTO-MAXIIO") )
				{
					return new Signal_Yocto_MaxiIO();
				}
			}
			return null;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return null;
		}
	}
	
	private void SignalConfiguration()
	{
		if (signalListRequest_done && signalTypeListRequest_done && signalLevelListRequest_done && deviceListRequest_done && deviceTypeListRequest_done && modbusDeviceListRequest_done)
		{
			// We put the DeviceType variables values in each Device
			for (Device device:devices.values())
			{
				device.setDeviceType(deviceTypes.get(device.getIdDeviceType()));
			}
			
			// We put everything in the Signal object
			for(Signal signal:signals.values())
			{
				signal.setDevice(devices.get(signal.getIdDevice()));
				signal.setSignalType(signalTypes.get(signal.getIdSignalType()));
				signal.setSignalLevel(signalLevels.get(signal.getIdSignalLevel()));
				
				// If the Signal is from a Modbus Device we have to initialize this
				if(signal.getSignalType().getIsCom())
				{
					if (modbusDevices.containsKey(signal.getIdDevice()))
					{
						modbusDevices.get(signal.getIdDevice()).addSignal(signal);
					}
				}
				
				// We have to cast the Signal object into the correct children
				signals.replace(signal.getIdSignal(), SignalInstance(signal.getDevice().getDeviceType().getBrandName(), signal.getDevice().getDeviceType().getModelName()));
				
				// Now that everything is set, we can start the threads
				CH4P_Multithreading.Submit(signal);
			}
			
			// We have to clean up some memory space
			devices = null;
			deviceTypes = null;
			signalLevels = null;
			signalTypes = null;
		}
	}

//	private void CommandeList(CachedRowSet listeCommandes)
//	{
//		try
//		{
//			ResultSetMetaData metadata = listeCommandes.getMetaData();
//			Integer columnCount = metadata.getColumnCount();
//
//			while(listeCommandes.next())
//			{
//				Commande commande = new Commande();
//				for(int i = 1; i <= columnCount; i++)
//				{
//					String arg0 = metadata.getColumnName(i);
//					Object arg1 = listeCommandes.getObject(i);
//					commande.SetField(arg0, arg1);
//				}
//				commandes.add(commande);
//				scenarioWorker.addScenarioCommandListener(commande);
//				commande.start();
//			}
//		}
//		catch(SQLException ex)
//		{
//			ex.printStackTrace();
//		}
//	}
	
	
	// Thread code
	
	public void start()
	{
		System.out.println("VigieAcq start : " + Calendar.getInstance().getTime());
		
		DatabaseController.Init();
		connectionHandler = DatabaseController.getConnection();
		
		signalListRequestCallback = new IDatabaseRequestCallback()
		{
			
			@Override
			public void databaseRequestCallback()
			{
				try
				{
					SignalList(signalListRequest.getCachedRowSet());
				}
				catch (CH4P_Exception e)
				{
					e.printStackTrace();
				}
				signalListRequest_done = true;
				signalListRequest.close();
				signalListRequest = null;
				
				SignalConfiguration();
			}
		};
		deviceListRequestCallback = new IDatabaseRequestCallback()
		{
			
			@Override
			public void databaseRequestCallback()
			{
				try
				{
					DeviceList(deviceListRequest.getCachedRowSet());
				}
				catch (CH4P_Exception e)
				{
					e.printStackTrace();
				}
				deviceListRequest_done = true;
				deviceListRequest.close();
				deviceListRequest = null;
				
				SignalConfiguration();
			}
		};
		
		modbusDeviceListRequestCallback = new IDatabaseRequestCallback()
		{
			
			@Override
			public void databaseRequestCallback()
			{
				try
				{
					ModbusDeviceList(modbusDeviceListRequest.getCachedRowSet());
				}
				catch (CH4P_Exception e)
				{
					e.printStackTrace();
				}
				modbusDeviceListRequest_done = true;
				modbusDeviceListRequest.close();
				modbusDeviceListRequest = null;
				
				SignalConfiguration();
			}
		};
		
		deviceTypeListRequestCallback = new IDatabaseRequestCallback()
		{
			
			@Override
			public void databaseRequestCallback()
			{
				try
				{
					DeviceTypeList(deviceTypeListRequest.getCachedRowSet());
				}
				catch (CH4P_Exception e)
				{
					e.printStackTrace();
				}
				deviceTypeListRequest_done = true;
				deviceTypeListRequest.close();
				deviceTypeListRequest = null;
				
				SignalConfiguration();
			}
		};
		signalTypeListRequestCallback = new IDatabaseRequestCallback()
		{
			
			@Override
			public void databaseRequestCallback()
			{
				try
				{
					SignalTypeList(signalTypeListRequest.getCachedRowSet());
				}
				catch (CH4P_Exception e)
				{
					e.printStackTrace();
				}
				signalTypeListRequest_done = true;
				signalTypeListRequest.close();
				signalTypeListRequest = null;
				
				SignalConfiguration();
			}
		};
		signalLevelListRequestCallback = new IDatabaseRequestCallback()
		{
			
			@Override
			public void databaseRequestCallback()
			{
				try
				{
					SignalLevelList(signalLevelListRequest.getCachedRowSet());
				}
				catch (CH4P_Exception e)
				{
					e.printStackTrace();
				}
				signalLevelListRequest_done = true;
				signalLevelListRequest.close();
				signalLevelListRequest = null;
				
				SignalConfiguration();
			}
		};
		
//		commandeListRequestCallback = new IDatabaseRequestCallback()
//		{
//			
//			@Override
//			public void databaseRequestCallback()
//			{
//				CommandeList(commandeListRequest.getCachedRowSet());
//				commandeListRequest_done = true;
//				commandeListRequest.close();
//				commandeListRequest = null;
//			}
//		};
		
		
		signalListRequest = new DatabaseRequest(connectionHandler, RequestList.REQUEST_SignalList, signalListRequestCallback);
		signalTypeListRequest = new DatabaseRequest(connectionHandler, RequestList.REQUEST_SignalTypeList, signalTypeListRequestCallback);
		signalLevelListRequest = new DatabaseRequest(connectionHandler, RequestList.REQUEST_SignalLevelList, signalLevelListRequestCallback);
		deviceListRequest = new DatabaseRequest(connectionHandler, RequestList.REQUEST_DeviceList, deviceListRequestCallback);
		deviceTypeListRequest = new DatabaseRequest(connectionHandler, RequestList.REQUEST_DeviceTypeList, deviceTypeListRequestCallback);
		scenarioListRequest = new DatabaseRequest(connectionHandler, RequestList.REQUEST_ScenarioList, null);
		recordValueRequest = new DatabaseRequest(connectionHandler, null, null);
		//commandeListRequest = new DatabaseRequest(connectionHandler, RequestList.REQUEST_ListeCommandes, commandeListRequestCallback);
		logEventRequest = new DatabaseRequest(connectionHandler, RequestList.REQUEST_EventLog, null);
		
		logWorker = new LogWorker(logEventRequest);
		logWorker.start();
		
		recordWorker = new RecordWorker(recordValueRequest);
		recordWorker.start();
		
		scenarioWorker = new ScenarioWorker(scenarioListRequest);
		scenarioWorker.addActionEventListener(logWorker);
		scenarioWorker.start();
		
		//commandeListRequest.start();
		//commandeListRequest.doQuery();
		
		signalListRequest.start();
		signalListRequest.doQuery();
		signalTypeListRequest.start();
		signalTypeListRequest.doQuery();
		signalLevelListRequest.start();
		signalLevelListRequest.doQuery();
		deviceListRequest.start();
		deviceListRequest.doQuery();
		deviceTypeListRequest.start();
		deviceTypeListRequest.doQuery();
		
		super.start();
	}
	
	public void run()
	{
		
		while(true)
		{
			try
			{
				
				if (signalListRequest_done && signalTypeListRequest_done && signalLevelListRequest_done && deviceListRequest_done && deviceTypeListRequest_done &&  firstRun)
				{
					System.out.println("VigieAcq prête :) : " + Calendar.getInstance().getTime());
					firstRun = false;
				}
				Thread.sleep(1000);
			}
			catch( InterruptedException ex)
			{
				continue;
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}
}
