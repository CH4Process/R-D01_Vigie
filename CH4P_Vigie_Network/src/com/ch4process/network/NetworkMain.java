package com.ch4process.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.Callable;

import com.ch4process.utils.CH4P_ConfigManager;
import com.ch4process.utils.CH4P_Exception;
import com.ch4process.utils.CH4P_Functions;
import com.ch4process.utils.CH4P_Multithreading;
import com.ch4process.utils.CH4P_System;

public class NetworkMain
{	
	static String EXTERNAL_SERVER = null;
	static Integer EXTERNAL_PORT = null;
	static String INTERNAL_SERVER = null;
	static Integer INTERNAL_PORT = null;
	static Integer CONNECTION_TIMEOUT = null;
	static String SOFTWARE_NAME = null;
	static String START_COMMAND = null;
	static Integer LOOP_TIME = null;
	static String SIDE = null;
	//final static String COMMAND_VPN_SOFTWARE = "sudo /etc/init.d/openvpn start";
	//final static int STANDARD_WAIT_TIME = 60000;
	//final static int ERROR_WAIT_TIME = 600000;
	//final static int ERROR_MAX = 3;
	static boolean internalDown = false;
	static boolean externalDown = false;
	static int restartCounter = 0;
	
	
	
	public static void main (String args[])
	{
		Init();
		
		switch (SIDE)
		{
			case "client": ClientLoop();;break;
			case "server": ServerLoop();break;
			default: break;
		}
	}
	
	private static void Init()
	{
		try
		{
			CH4P_ConfigManager.Init();	
			CH4P_Multithreading.Init();
		}
		catch (CH4P_Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			Properties prop = CH4P_ConfigManager.getNetworkConfig().GetProperties();
			
			if (prop != null)
			{
				SIDE = prop.getProperty("SIDE");
				EXTERNAL_SERVER = prop.getProperty("externalServerAddress");
				EXTERNAL_PORT = Integer.valueOf(prop.getProperty("externalConnectionPort"));
				INTERNAL_SERVER = prop.getProperty("internalServerAddress");
				INTERNAL_PORT = Integer.valueOf(prop.getProperty("internalConnectionPort"));
				CONNECTION_TIMEOUT = Integer.valueOf(prop.getProperty("connectionTimeout")) * 1000;
				SOFTWARE_NAME = prop.getProperty("softwareName");
				START_COMMAND = prop.getProperty("startCommand");
				LOOP_TIME = Integer.valueOf(prop.getProperty("loopTime")) * 1000;
				
			}
			else
			{
				SIDE = "client";
				EXTERNAL_SERVER = "www.google.com";
				EXTERNAL_PORT = 80;
				INTERNAL_SERVER = "10.4.0.1";
				INTERNAL_PORT = 50401;
				CONNECTION_TIMEOUT = 5000;
				SOFTWARE_NAME = "notepad";
				START_COMMAND = "sudo /etc/init.d/openvpn start";
				LOOP_TIME = 10000;
			}
		}
	}
	
	private static void ClientLoop()
	{
		while (true)
		{
			try
			{	
				// If we can join the server we continue, if not we have to check things ! 
				if(! TestServer(INTERNAL_SERVER))
				{
					if (! internalDown)
					{
						CH4P_Functions.Log("VigieNetwork", CH4P_Functions.LOG_inConsole, CH4P_Functions.LEVEL_WARNING, "Server " + INTERNAL_SERVER + " not reachable.");
						internalDown = true;
					}
					
					// Couldn't join the server... If we have acces to internet we have to reboot the VPN Tunnelling software, if not we have to wait.
					if (TestServer(EXTERNAL_SERVER))
					{
						if (externalDown)
						{
							CH4P_Functions.Log("VigieNetwork", CH4P_Functions.LOG_inConsole, CH4P_Functions.LEVEL_INFO, "Internet connection ok - restarting vpn.");
							externalDown = false;
						}
						
						// Killing the software

						ArrayList<String> processList = CH4P_System.GetProcessList();
						for (String processName:processList)
						{
							if (processName.toLowerCase().contains(SOFTWARE_NAME))
							{
								CH4P_System.KillProcess(SOFTWARE_NAME);
								//restartCounter ++;
								Thread.sleep(10000);
							}
						}
						
						// At this point the software is killed or not running, so we start it !
						CH4P_System.StartProcess(START_COMMAND);
						
					}
					else
					{
						if (! externalDown)
						{
							CH4P_Functions.Log("VigieNetwork", CH4P_Functions.LOG_inConsole, CH4P_Functions.LEVEL_WARNING, "No internet connection available.");
							externalDown = true;
						}
						// If we can't access internet it may be because of a 3G dongle problem. So we may have to reboot the dongle. TODO : Reboot 3G dongle
					}
				}
				else
				{
					System.out.println(" -- INTERNAL OK ");
					// Everything is ok
					if (internalDown)
					{
						CH4P_Functions.Log("VigieNetwork", CH4P_Functions.LOG_inConsole, CH4P_Functions.LEVEL_INFO, "Server " + INTERNAL_SERVER + " reachable.");
						internalDown = false;
					}
					
					restartCounter = 0;
				}
				
				// Whatever happens, we'll wait one minute before trying again.

				Thread.sleep(LOOP_TIME);
					
			}
			catch (Exception ex)
			{
				CH4P_Functions.LogException(CH4P_Functions.LOG_inConsole, ex);
			}
		}
	}
	
	private static boolean TestServer(String serverAddress) 
	{
		try
		{
			InetAddress addr = InetAddress.getByName(serverAddress);
			boolean ping = addr.isReachable(CONNECTION_TIMEOUT);
			return ping;
		}
		catch (Exception ex)
		{
			CH4P_Functions.LogException(CH4P_Functions.LOG_inConsole, ex);
			return false;
		}
	}
	
	private static void ServerLoop()
	{
		while(true)
		{
			try
			{
				// The server listens to his port and creates a socket for each connection
				ServerSocket waitingConnection = new ServerSocket(INTERNAL_PORT);
				Socket socket = waitingConnection.accept();
				
				// new ConnectionCheck(socket)
				// multithreading.submit(ConnectionCheck)
				
				ConnectionCheck client = new ConnectionCheck(socket);
				
				
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}
	
	private class ConnectionCheck implements Callable
	{
		Socket socket = null;
		String message;
		
		public ConnectionCheck(Socket _socket)
		{
			socket = _socket;
		}
		
		@Override
		public Object call() throws Exception
		{
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));	
			message = in.readLine();
			
			while (message != null)
			{
				System.out.println(in.readLine());
			}
			
			in.close();
			socket.close();
			return null;
		}
		
	}

}
