package com.ch4process.utils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;


public class CH4P_PropertiesReader
{
	
	public Properties getPropValues(String fileName) 
	{
		Properties prop = new Properties();
		
		try (InputStream inputStream = new FileInputStream(fileName))
		{

			if (inputStream != null) 
			{
				prop.load(inputStream);
				return prop;
			} 
			else 
			{
				return null;
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			return null;
		} 
	}
}
