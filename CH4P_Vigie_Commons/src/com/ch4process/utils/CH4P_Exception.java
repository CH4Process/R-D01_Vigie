package com.ch4process.utils;

public class CH4P_Exception extends Exception
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public CH4P_Exception(String message)
	{
		super(message);
	}
	
	public CH4P_Exception(Throwable cause)
	{
		super(cause);
	}
	
	public CH4P_Exception(String message, Throwable cause)
	{
		super(message, cause);
	}

}
