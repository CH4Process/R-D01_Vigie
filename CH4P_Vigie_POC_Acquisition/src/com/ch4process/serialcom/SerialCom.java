package com.ch4process.serialcom;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.ch4process.utils.CH4P_Functions;


public class SerialCom 
{
	InputStream inputStream;
	OutputStream outputStream;
	SerialPort serialPort;
	String portName;
	Integer speed;
	Integer dataBits;
	Integer stopBits;
	Integer parity;
	
	public SerialCom(String portName, int speed, int databits, int stopbits, int parity)
	{
		this.portName = portName;
		this.speed = speed;
		this.dataBits = databits;
		this.stopBits = stopbits;
		this.parity = parity;
	}
	
	public SerialCom(String portName)
	{
		this.portName = portName;
		this.speed = 57600;
		this.dataBits = SerialPort.DATABITS_8;
		this.stopBits = SerialPort.STOPBITS_1;
		this.parity = SerialPort.PARITY_NONE;
	}

	public boolean connect ()
	{
		try
		{
			CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
			if ( portIdentifier.isCurrentlyOwned() )
			{
				return false;
			}
			else
			{
				CommPort commPort = portIdentifier.open(this.getClass().getName(),2000);

				serialPort = (SerialPort) commPort;
				serialPort.setSerialPortParams(speed,dataBits,stopBits,parity);

				inputStream = serialPort.getInputStream();
				outputStream = serialPort.getOutputStream();

				return true;
			}  
		}
		catch (Exception ex)
		{
			CH4P_Functions.LogException(CH4P_Functions.LOG_inConsole, ex);
			return false;
		}
	}

	public boolean disconnect ()
	{
		try
		{
			serialPort.close();
			return true;
		}
		catch (Exception ex)
		{
			CH4P_Functions.LogException(CH4P_Functions.LOG_inConsole, ex);
			return false;
		}
	}
	
	public boolean send(String string)
	{
		try
		{
			outputStream.write(string.getBytes());
			return true;
		}
		catch (IOException ex)
		{
			CH4P_Functions.LogException(CH4P_Functions.LOG_inConsole, ex);
			return false;
		}
	}
}
