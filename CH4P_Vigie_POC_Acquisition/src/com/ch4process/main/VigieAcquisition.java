package com.ch4process.main;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.sql.rowset.CachedRowSet;

import com.ch4process.acquisition.Signal;
import com.ch4process.acquisition.SignalLevel;
import com.ch4process.acquisition.SignalType;
import com.ch4process.acquisition.Signal_Yocto_4_20mA;
import com.ch4process.acquisition.Signal_Yocto_MaxiIO;
import com.ch4process.acquisition.Signal_Yocto_Meteo_Humidite;
import com.ch4process.acquisition.Signal_Yocto_Meteo_Pression;
import com.ch4process.acquisition.Signal_Yocto_Meteo_Temperature;
import com.ch4process.acquisition.Signal_Yocto_RS485_Modbus;
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
import com.ch4process.utils.CH4P_Functions;
import com.ch4process.utils.CH4P_Multithreading;

import sun.security.jca.GetInstance;

public class VigieAcquisition implements Callable<Integer>
{
	String threadName;
	
	Map<Integer, Signal> signals = new HashMap<Integer,Signal>();
	Map<Integer,Device> devices = new HashMap<Integer,Device>();
	Map<Integer,ModbusDevice> modbusDevices = new HashMap<Integer,ModbusDevice>();
	Map<Integer,DeviceType> deviceTypes = new HashMap<Integer,DeviceType>();
	Map<Integer,SignalLevel> signalLevels = new HashMap<Integer,SignalLevel>();
	Map<Integer,SignalType> signalTypes = new HashMap<Integer,SignalType>();
	Map<Integer,Scenario> scenarios = new HashMap<Integer,Scenario>();
	
	ConnectionHandler connectionHandler;
	DatabaseRequest signalListRequest;
	DatabaseRequest deviceListRequest;
	DatabaseRequest modbusDeviceListRequest;
	DatabaseRequest deviceTypeListRequest;
	DatabaseRequest signalLevelListRequest;
	DatabaseRequest signalTypeListRequest;
	DatabaseRequest scenarioListRequest;
	DatabaseRequest logEventRequest;
	
	IDatabaseRequestCallback signalListRequestCallback;
	IDatabaseRequestCallback deviceListRequestCallback;
	IDatabaseRequestCallback modbusDeviceListRequestCallback;
	IDatabaseRequestCallback deviceTypeListRequestCallback;
	IDatabaseRequestCallback signalLevelListRequestCallback;
	IDatabaseRequestCallback signalTypeListRequestCallback;
	IDatabaseRequestCallback scenarioListRequestCallback;
	
	boolean signalListRequest_done = false;
	boolean deviceListRequest_done = false;
	boolean modbusDeviceListRequest_done = false;
	boolean deviceTypeListRequest_done = false;
	boolean signalLevelListRequest_done = false;
	boolean signalTypeListRequest_done = false;
	boolean scenarioListRequest_done = false;
	
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
				
				signal.addValueListener(recordWorker);
				signal.addValueListener(scenarioWorker);
				
				signals.put(signal.getIdSignal(),signal);
				
				signal = null;
			}
		}
		catch (Exception ex)
		{
			throw new CH4P_Exception("-VigieAcquisition SignalList error-" + ex.getMessage(), ex.getCause());
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
			throw new CH4P_Exception("-VigieAcquisition DeviceList error-" + ex.getMessage(), ex.getCause());
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
			throw new CH4P_Exception("-VigieAcquisition ModbusDeviceList error-" + ex.getMessage(), ex.getCause());
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
			throw new CH4P_Exception("-VigieAcquisition DeviceTypeList error-" + ex.getMessage(), ex.getCause());
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
			throw new CH4P_Exception("-VigieAcquisition SignalTypeList error-" + ex.getMessage(), ex.getCause());
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
			throw new CH4P_Exception("-VigieAcquisition SignalLevelList error-" + ex.getMessage(), ex.getCause());
		}
	}
	/**
	 * Determines the right type of signal class to create using it's model and brand names
	 * @param brand
	 * @param model
	 * @return
	 */
	private Signal SignalInstance(Signal signal) throws CH4P_Exception
	{
		String brand = signal.getDevice().getDeviceType().getBrandName();
		String model = signal.getDevice().getDeviceType().getModelName();
		try
		{
			if (brand.equals("YOCTOPUCE"))
			{
				if (model.equals("YOCTO-4-20-MA-RX"))
				{
					return new Signal_Yocto_4_20mA(signal) ;
				}
				else if (model.equals("YOCTO-METEO-HUMIDITE"))
				{
					return new Signal_Yocto_Meteo_Humidite(signal);
				}
				else if (model.equals("YOCTO-METEO-PRESSION"))
				{
					return new Signal_Yocto_Meteo_Pression(signal);
				}
				else if (model.equals("YOCTO-METEO-TEMPERATURE"))
				{
					return new Signal_Yocto_Meteo_Temperature(signal);
				}
				else if (model.equals("YOCTO-MAXIIO") )
				{
					return new Signal_Yocto_MaxiIO(signal);
				}
				else if (model.equals("YOCTO-RS485"))
				{
					return new Signal_Yocto_RS485_Modbus(signal);
				}
			}
			return null;
		}
		catch (Exception ex)
		{
			throw new CH4P_Exception("-VigieAcquisition SignalInstance error-" + ex.getMessage(), ex.getCause());
		}
	}
	
	private void SignalConfiguration() throws CH4P_Exception
	{
		try
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
					Signal instance = SignalInstance(signal);
					signals.replace(signal.getIdSignal(), instance);

					// Now that everything is set, we can start the threads
					CH4P_Multithreading.Submit(instance);
				}

				// We have to start the ModbusDevices also
				for(ModbusDevice modbusDevice:modbusDevices.values())
				{
					if (devices.containsKey(modbusDevice.getIdDevice()))
					{
						modbusDevice.setDevice(devices.get(modbusDevice.getIdDevice()));
					}
					CH4P_Multithreading.Submit(modbusDevice);
				}

				// We have to clean up some memory space
				devices = null;
				deviceTypes = null;
				signalLevels = null;
				signalTypes = null;
			}
		}
		catch (Exception ex)
		{
			throw new CH4P_Exception("-VigieAcquisition SignalConfiguration error-" + ex.getMessage(), ex.getCause());
		}
	}

	
	// Thread code
	
	public void start() throws CH4P_Exception
	{
		try
		{
		CH4P_Functions.Log(this.getClass().getName(), CH4P_Functions.LOG_inConsole, 100, "VigieAcq start : " + Calendar.getInstance().getTime());
		
		DatabaseController.Init();
		connectionHandler = DatabaseController.getConnection();
		
		signalListRequestCallback = new IDatabaseRequestCallback()
		{
			
			@Override
			public void databaseRequestCallback() throws CH4P_Exception
			{
				try
				{
					SignalList(signalListRequest.getCachedRowSet());
					signalListRequest_done = true;
					signalListRequest.close();
					signalListRequest = null;
					
					SignalConfiguration();
				}
				catch (Exception ex)
				{
					throw new CH4P_Exception("-VigieAcquisition signalListRequestCallback error-" + ex.getMessage(), ex.getCause());
				}
				
			}
		};
		
		deviceListRequestCallback = new IDatabaseRequestCallback()
		{
			
			@Override
			public void databaseRequestCallback() throws CH4P_Exception
			{
				try
				{
					DeviceList(deviceListRequest.getCachedRowSet());
					deviceListRequest_done = true;
					deviceListRequest.close();
					deviceListRequest = null;

					SignalConfiguration();
				}
				catch (Exception ex)
				{
					throw new CH4P_Exception("-VigieAcquisition deviceListRequestCallback error-" + ex.getMessage(), ex.getCause());
				}

			}
		};
		
		modbusDeviceListRequestCallback = new IDatabaseRequestCallback()
		{
			
			@Override
			public void databaseRequestCallback() throws CH4P_Exception
			{
				try
				{
					ModbusDeviceList(modbusDeviceListRequest.getCachedRowSet());
					modbusDeviceListRequest_done = true;
					modbusDeviceListRequest.close();
					modbusDeviceListRequest = null;

					SignalConfiguration();
				}
				catch (Exception ex)
				{
					throw new CH4P_Exception("-VigieAcquisition modbusDeviceListRequestCallback error-" + ex.getMessage(), ex.getCause());
				}

			}
		};
		
		deviceTypeListRequestCallback = new IDatabaseRequestCallback()
		{
			
			@Override
			public void databaseRequestCallback() throws CH4P_Exception
			{
				try
				{
					DeviceTypeList(deviceTypeListRequest.getCachedRowSet());
					deviceTypeListRequest_done = true;
					deviceTypeListRequest.close();
					deviceTypeListRequest = null;

					SignalConfiguration();
				}
				catch (Exception ex)
				{
					throw new CH4P_Exception("-VigieAcquisition deviceTypeListRequestCallback error-" + ex.getMessage(), ex.getCause());
				}

			}
		};
		signalTypeListRequestCallback = new IDatabaseRequestCallback()
		{
			
			@Override
			public void databaseRequestCallback() throws CH4P_Exception
			{
				try
				{
					SignalTypeList(signalTypeListRequest.getCachedRowSet());
					signalTypeListRequest_done = true;
					signalTypeListRequest.close();
					signalTypeListRequest = null;

					SignalConfiguration();
				}
				catch (Exception ex)
				{
					throw new CH4P_Exception("-VigieAcquisition signalTypeListRequestCallback error-" + ex.getMessage(), ex.getCause());
				}

			}
		};
		signalLevelListRequestCallback = new IDatabaseRequestCallback()
		{
			
			@Override
			public void databaseRequestCallback() throws CH4P_Exception
			{
				try
				{
					SignalLevelList(signalLevelListRequest.getCachedRowSet());
					signalLevelListRequest_done = true;
					signalLevelListRequest.close();
					signalLevelListRequest = null;

					SignalConfiguration();
				}
				catch (Exception ex)
				{
					throw new CH4P_Exception("-VigieAcquisition signalLevelListRequestCallback error-" + ex.getMessage(), ex.getCause());
				}

			}
		};
		
		signalListRequest = new DatabaseRequest(connectionHandler, RequestList.REQUEST_SignalList, signalListRequestCallback);
		signalTypeListRequest = new DatabaseRequest(connectionHandler, RequestList.REQUEST_SignalTypeList, signalTypeListRequestCallback);
		signalLevelListRequest = new DatabaseRequest(connectionHandler, RequestList.REQUEST_SignalLevelList, signalLevelListRequestCallback);
		deviceListRequest = new DatabaseRequest(connectionHandler, RequestList.REQUEST_DeviceList, deviceListRequestCallback);
		deviceTypeListRequest = new DatabaseRequest(connectionHandler, RequestList.REQUEST_DeviceTypeList, deviceTypeListRequestCallback);
		modbusDeviceListRequest = new DatabaseRequest(connectionHandler, RequestList.REQUEST_ModbusDeviceList, modbusDeviceListRequestCallback);
	
		scenarioListRequest = new DatabaseRequest(connectionHandler, RequestList.REQUEST_ScenarioList, null);
		
		
		logEventRequest = new DatabaseRequest(connectionHandler, RequestList.REQUEST_RecordEventLog, null);
		logWorker = new LogWorker(logEventRequest);
		CH4P_Multithreading.Submit(logWorker);
		
		recordWorker = new RecordWorker(connectionHandler);
		CH4P_Multithreading.Submit(recordWorker);
		
		scenarioWorker = new ScenarioWorker(scenarioListRequest);
		scenarioWorker.addScenarioEventListener(logWorker);
		CH4P_Multithreading.Submit(scenarioWorker);
		
		
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
		modbusDeviceListRequest.start();
		modbusDeviceListRequest.doQuery();
		}
		catch (Exception ex)
		{
			throw new CH4P_Exception("-VigieAcquisition start error-" + ex.getMessage(), ex.getCause());
		}
	}
	
	public void run() throws CH4P_Exception
	{
		
		while(true)
		{
			try
			{
				
				if (signalListRequest_done && signalTypeListRequest_done && signalLevelListRequest_done && deviceListRequest_done && deviceTypeListRequest_done &&  firstRun)
				{
					CH4P_Functions.Log(this.getClass().getName(), CH4P_Functions.LOG_inConsole, 100, "VigieAcq prête :) : " + Calendar.getInstance().getTime());
					firstRun = false;
				}
				Thread.sleep(1000);
			}
			catch(InterruptedException ex)
			{
				continue;
			}
			catch (Exception ex)
			{
				throw new CH4P_Exception("-VigieAcquisition run error-" + ex.getMessage(), ex.getCause());
			}
		}
	}

	@Override
	public Integer call() throws CH4P_Exception
	{
		try
		{
			start();
			run();
		}
		catch(Exception ex)
		{
			throw new CH4P_Exception("-VigieAcquisition call error" + ex.getMessage(), ex.getCause());
		}
		return null;
	}
		
}
