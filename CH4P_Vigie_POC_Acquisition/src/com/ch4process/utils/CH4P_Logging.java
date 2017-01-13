package com.ch4process.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;

public class CH4P_Logging
{
	private LoggerContext ctx;
	private Logger logger;
	
	private boolean init_done = false;
	
	public void Init()
	{
		ctx = Configurator.initialize("config", CH4P_System.PATH_Config_Logging);
		logger = LogManager.getLogger();
		
		init_done = true;
	}
	
	public Logger getLogger()
	{
		if (! init_done)
		{
			Init();
		}
		
		return this.logger;
	}
}
