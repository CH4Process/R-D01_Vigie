package com.ch4process.utils;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CH4P_Multithreading
{
	private static boolean init_done = false;
	private static ExecutorService executor;
	
	public static void Init()
	{
		if (! init_done)
		{
			executor = Executors.newFixedThreadPool(50);
			init_done = true;
		}
	}
	
	public static void Submit(Callable callable)
	{
		executor.submit(callable);
	}

}
