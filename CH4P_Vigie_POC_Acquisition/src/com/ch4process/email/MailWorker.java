package com.ch4process.email;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.ch4process.utils.CH4P_Exception;
import com.ch4process.utils.CH4P_Functions;

public class MailWorker implements Callable<Integer>
{
	static ConcurrentLinkedQueue<Mail> mails = new ConcurrentLinkedQueue<Mail>();
	boolean busy = false;
	private IMailCallback callback;
	
	
	
	public MailWorker(IMailCallback _callback)
	{
		callback = _callback;
	}
	
	public void addMail(Mail mail)
	{
		mails.add(mail);
	}
	
	void removeFirst()
	{
		mails.remove();
	}
	
	void eventHandling()
	{
		try
		{
			if (!mails.isEmpty() && !busy)
			{
				busy = true;

				Mail mail = mails.peek();

				while((mail.getCurrentRetryCount() < mail.getNbRetry()))
				{
					mail.setCurrentRetryCount(mail.getCurrentRetryCount() + 1);
					
					if (mail.sendMail())
					{
						CH4P_Functions.Log(this.getClass().getName(), CH4P_Functions.LOG_inConsole, 100, "Email successfully sent.");
						this.callback.mailCallback(true, mail);
						break;
					}
					else
					{
						CH4P_Functions.Log(this.getClass().getName(), CH4P_Functions.LOG_inConsole, 100, "Error during email sending operation.");
						this.callback.mailCallback(false, mail);
						Thread.sleep(mail.getRetryWaitTime() * 1000);
					}
				}
				
				removeFirst();
				
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
