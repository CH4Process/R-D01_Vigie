package com.ch4process.sms;

import com.ch4process.serialcom.SerialCom;
import com.ch4process.utils.CH4P_Functions;

import java.util.List;

public class SMS extends Thread
{
	SerialCom portCom;
	String portName;
	List<String> numTel;
	String message;
	String codePIN;
	String command;
	
	static SMS instance = new SMS();
	Thread thisThread = null;
	boolean isAvailable = true;
	
	private SMS()
	{
		this.numTel.clear();
		this.message = "";
		this.portName = "";
		this.portCom = null;
		this.command = "";
		this.codePIN = "";
	}
	
	public static SMS getInstance()
	{
		return instance;
	}
	
	public void start()
	{
		if (thisThread == null)
		{
			thisThread = new Thread (this, "SMS");
			CH4P_Functions.Log(CH4P_Functions.LOG_inConsole, 100, "Thread SMS " + " lancé !");
			thisThread.start();
		}
	}
	
	public void run()
	{
		boolean result = this.send();
		
		if (! result)
		{
			CH4P_Functions.Log(CH4P_Functions.LOG_inConsole, 100, "Erreur lors de l'envoi du SMS !");
		}
	}
	
	public void setCodePin(String codepin)
	{
		this.codePIN = codepin;
	}
	
	public void setNums(String...numeros)
	{
		for (String num : numeros) 
		{
			numTel.add(num);
		}
	}
	
	public List<String> getNums()
	{
		return numTel;
	}
	
	public void setMessage(String message)
	{
		this.message = message;
	}
	
	public String getMessage()
	{
		return message;
	}

	public void setPortCom(String portname)
	{
		this.portName = portname;
	}
	
	public String getPortCom(String portname)
	{
		return this.portName;
	}
	
	public boolean send()
	{
		isAvailable = false;
		portCom = new SerialCom(this.portName);
		try
		{
			if(portCom.connect())
			{
				boolean result = true;
				
				command = "AT" + "\r\n";
				result &= portCom.send(command);
				wait(100);
				
				command = "AT+CMGF=1" + "\r\n";
				result &= portCom.send(command);
				wait(100);
				
				command = "AT+CPIN=\"" + codePIN + "\"" + "\r\n";
				result &= portCom.send(command);
				wait(100);
				
				for(String num : numTel)
				{
					command = "AT+CMGS=\"" + num + "\"" + "\r\n";
					result &= portCom.send(command);
					wait(100);
					
					command = message + "\u001A";
					result &= portCom.send(command);
					wait(500);
				}
				return result;
			}
			else
			{
				return false;
			}
		}
		catch (InterruptedException ex)
		{
			return false;
		}
		finally
		{
			portCom.disconnect();
			isAvailable = true;
		}
	}
}
