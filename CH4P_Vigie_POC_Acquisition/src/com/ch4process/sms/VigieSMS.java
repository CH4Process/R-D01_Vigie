package com.ch4process.sms;

import com.ch4process.sms.SMS;
import com.ch4process.utils.CH4P_Functions;

public class VigieSMS extends Thread
{
	SMS sms;
	Object controller;
	Thread thisThread;
	String threadName;
	
	public void setController(Object obj)
	{
		this.controller = obj;
	}
	
	public void setThreadName(String name)
	{
		this.threadName = name;
	}
	
	public void start()
	{
		if (this.threadName == null || this.threadName == "")
		{
			this.threadName = "VigieSMS";
		}
		
		sms = SMS.getInstance();
		
		configureSMS();
		
		if (thisThread == null)
		{
			thisThread = new Thread (this, threadName);
			CH4P_Functions.Log(CH4P_Functions.LOG_inConsole, 100, "Thread " + threadName + " lancé !");
			thisThread.start();
		}
	}
	
	public void run()
	{
		while(true)
		{
			try
			{
				Thread.sleep(1000);
			}
			catch (InterruptedException ex)
			{
				ex.printStackTrace();
			}
		}
	}
	
	public void setSMSParameters(String message, String...destinataires)
	{
		sms.setMessage(message);
		sms.setNums(destinataires);
	}
	
	public void sendSMS()
	{
		sms.send();
	}
	
	private void configureSMS()
	{
		sms.setCodePin("0000");
		sms.setPortCom("dev/tty0");
	}
	

}
