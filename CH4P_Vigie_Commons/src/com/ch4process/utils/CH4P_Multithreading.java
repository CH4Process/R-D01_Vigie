package com.ch4process.utils;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CH4P_Multithreading
{
	private static boolean initialized = false;
	private static ExecutorService executor;
	
	public static void Init() throws CH4P_Exception
	{
		try
		{
			if (! initialized)
			{
				executor = Executors.newFixedThreadPool(50);
				initialized = true;
			}
		}
		catch (Exception ex)
		{
			throw new CH4P_Exception("-CH4P_MultiThreading Init error-" + ex.getMessage(), ex.getCause());
		}
	}
	
	public static void Submit(Callable<Integer> callable) throws CH4P_Exception
	{
		try
		{
			executor.submit(callable);
		}
		catch (Exception ex)
		{
			throw new CH4P_Exception("-CH4P_Multithreading Submit error-" + ex.getMessage(), ex.getCause());
		}
	}
	
	public static boolean isInitialized()
	{
		return initialized;
	}

}
