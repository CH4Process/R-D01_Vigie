package com.ch4process.email;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import com.ch4process.utils.CH4P_Exception;
import com.ch4process.utils.CH4P_Functions;

public class MailWorker implements Callable<Integer>
{
	static List<Mail> mails = new LinkedList<Mail>();

	boolean busy = false;
	
	
	public static void addMail(Mail mail)
	{
		mails.add(mail);
	}
	
	void removeFirst()
	{
		mails.remove(0);
	}
	
	void eventHandling()
	{
		try
		{
			if (mails.size() > 0 && !busy)
			{
				busy = true;
				
				Mail mail = mails.get(0);
				
				if (mail.sendMail())
				{
					CH4P_Functions.Log(this.getClass().getName(), CH4P_Functions.LOG_inConsole, 100, "Email successfully sent.");
					removeFirst();
				}
				else
				{
					CH4P_Functions.Log(this.getClass().getName(), CH4P_Functions.LOG_inConsole, 100, "Error during email sending operation.");
					removeFirst();
				}
				
				busy = false;
			}
		}
		catch (Exception ex)
		{
			CH4P_Functions.LogException(CH4P_Functions.LOG_inConsole, ex);
		}
	}


	@Override
	public Integer call() throws CH4P_Exception
	{
		try
		{
			eventHandling();
			
			Thread.sleep(1000);
		}
		catch (Exception ex)
		{
			throw new CH4P_Exception(ex.getMessage(), ex.getCause());
		}
		finally
		{
			return null;
		}
		
	}
	
	
	
	
}
