package com.ch4process.acquisition;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import javax.swing.event.EventListenerList;
import com.ch4process.utils.CH4P_Exception;
import com.yoctopuce.YoctoAPI.YAPI;
import com.yoctopuce.YoctoAPI.YAPI_Exception;
import com.yoctopuce.YoctoAPI.YSerialPort;

public class ModbusDevice extends Device implements Callable
{
	// Database variables
	Integer idModbusDevice = null;
	Integer idDevice = null;
	Integer slaveNumber = null;
	String byteOrder = null;
	Integer speed = null;
	
	// Internal variables
	List<Signal> HoldingRegisterSignals = new ArrayList<Signal>();
	List<Signal> CoilSignals = new ArrayList<Signal>();
	List<Integer> values = new ArrayList<Integer>();
	List<ModbusRequest> requests = new ArrayList<ModbusRequest>();
	Integer baseAddress = 0;
	Integer baseRefreshRate = 1;
	Integer requestLength = 0;
	YSerialPort serialPort = null;
	boolean init_done = false;
	boolean isValid = true;
	
	// Event handling
		EventListenerList listeners = new EventListenerList();
	
	
	// Getters and Setters
	public Integer getIdModbusDevice()
	{
		return idModbusDevice;
	}
	public void setIdModbusDevice(Integer idModbusDevice)
	{
		this.idModbusDevice = idModbusDevice;
	}
	public Integer getIdDevice()
	{
		return idDevice;
	}
	public void setIdDevice(Integer idDevice)
	{
		this.idDevice = idDevice;
	}
	public Integer getSlaveNumber()
	{
		return slaveNumber;
	}
	public void setSlaveNumber(Integer slaveNumber)
	{
		this.slaveNumber = slaveNumber;
	}
	public String getByteOrder()
	{
		return byteOrder;
	}
	public void setByteOrder(String byteOrder)
	{
		this.byteOrder = byteOrder;
	}
	public Integer getSpeed()
	{
		return speed;
	}
	public void setSpeed(Integer speed)
	{
		this.speed = speed;
	}
	
	
	// Operationnal code
	
	public void addSignal(Signal signal)
	{
		if(signal.getSignalType().getIsTor())
		{
			// This is a boolean signal
			CoilSignals.add(signal);
		}
		else 
		{
			// It's not a boolean so it's a holding register
			HoldingRegisterSignals.add(signal);
		}
	}	
	
	private void InitRequest(List<Signal> SignalList)
	{
		try
		{
			SignalList.sort((signal1,signal2) -> signal1.getAddress().compareTo(signal2.getAddress()));
			
			// Here we create a new modbus request starting at the address of the first element in the list and ending with the 
			ModbusRequest request = new ModbusRequest(SignalList.get(0).getAddress(), SignalList.get(SignalList.size()).getAddress() + 1);
			
			for(Signal signal:SignalList)
			{
				request.getSignals().add(signal.getAddress());
			}
			
			// We have to remember what type of request this is for a possible priority check later
			if (SignalList.get(0).getSignalType().getIsTor())
			{
				request.setRequestType(ModbusRequest.REQUEST_READ_COILS);
			}
			else
			{
				request.setRequestType(ModbusRequest.REQUEST_READ_HOLDING_REGISTERS);
			}
			
			requests.add(request);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public boolean Init()
	{
		try
		{
			serialPort = YSerialPort.FindSerialPort(this.serialNumber);
			
			if (serialPort.isOnline())
			{
				InitRequest(HoldingRegisterSignals);
				InitRequest(CoilSignals);
				
				return true;
			}
			return false;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return false;
		}
	}
	
	public boolean Connect()
	{
		try
		{
			 YAPI.RegisterHub(this.address);
			 return true;
		} 
		catch (YAPI_Exception ex)
		{
			ex.printStackTrace();
			return false;
		}
	}

	
	@Override
	public Integer call() throws CH4P_Exception
	{
		try
		{
			while (true)
			{
				if (! init_done)
				{
					init_done = Connect() && Init();
				}
				
				if (init_done)
				{
					// First we read the values in the Modbus device
					for(ModbusRequest request:requests)
					{
						if (request.getRequestType() == ModbusRequest.REQUEST_READ_HOLDING_REGISTERS)
						{
							request.setValues(serialPort.modbusReadRegisters(this.getSlaveNumber(), request.getStartAddress(), request.getRequestlength()));
						}
						else if (request.getRequestType() == ModbusRequest.REQUEST_READ_COILS)
						{
							request.setValues(serialPort.modbusReadBits(this.getSlaveNumber(), request.getStartAddress(), request.getRequestlength()));
						}
					}
					
					// And now we give each signal his value ! 
					for(Signal signal:signals)
					{
						// The datas in the values ArrayList returned by the Yoctopuce Modbus card are ordered based on the Modbus request
						// So we have to determine which data to provide to which signal
						Integer index = baseAddress - signal.address;
						
						// TODO : Put some smart code here to determine the correct size, index and format of the data to provide.
						// The datas are Float32 so we have to merge two 16-digit integers into one and then transform it to a Float
						double data = (double) Float.intBitsToFloat(values.get(index) & values.get(index+1));
						
						if (! signal.getSignalType().isTotalizer)
						{
							signal.fireValueChanged(data, isValid);
						}
					}
				}
				
				Thread.sleep(baseRefreshRate * 1000);
			}
		}
		catch(Exception ex)
		{
			throw new CH4P_Exception(ex.getMessage(), ex.getCause());
		}
		finally
		{
			return errorCode;
		}
	}
	
	
	
}
