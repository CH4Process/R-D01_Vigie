package com.ch4process.email;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import com.ch4process.utils.CH4P_Exception;

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
					System.out.println("Email successfully sent.");
					removeFirst();
				}
				else
				{
					System.out.println("Error during email sending operation.");
					removeFirst();
				}
				
				busy = false;
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
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
