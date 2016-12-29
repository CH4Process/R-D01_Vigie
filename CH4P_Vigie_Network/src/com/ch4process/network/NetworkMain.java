package com.ch4process.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
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
	static String NAME = null;
	static boolean internalDown = false;
	static boolean externalDown = false;
	static int restartCounter = 0;
	
	
	
	public static void main (String args[])
	{
		Init();
		
		VigieNetworkView view = new VigieNetworkView(800, 600);
		
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
			CH4P_ConfigManager.LoadNetworkConfig();	
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
				SIDE = prop.getProperty("side");
				NAME = prop.getProperty("name");
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
				NAME = "no name in config";
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
		CH4P_Functions.Log("VigieNetwork", CH4P_Functions.LOG_inConsole, CH4P_Functions.LEVEL_WARNING, "Initialized - " + NAME);
		
		while (true)
		{
			try
			{	
				// If we can join the server we continue, if not we have to check things ! 
				if(! TestServer(INTERNAL_SERVER, INTERNAL_PORT))
				{
					if (! internalDown)
					{
						CH4P_Functions.Log("VigieNetwork", CH4P_Functions.LOG_inConsole, CH4P_Functions.LEVEL_WARNING, "Server " + INTERNAL_SERVER + " not reachable.");
						internalDown = true;
					}
					
					// Couldn't join the server... If we have acces to internet we have to reboot the VPN Tunnelling software, if not we have to wait.
					if (TestServer(EXTERNAL_SERVER, EXTERNAL_PORT))
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
								Thread.sleep(10000);
								break;
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
	
	private static boolean TestServer(String serverAddress, Integer serverPort) 
	{
		try
		{
			Socket testSocket = new Socket();
			testSocket.connect(new InetSocketAddress(serverAddress, serverPort), CONNECTION_TIMEOUT);
			PrintWriter out = new PrintWriter(testSocket.getOutputStream());
			out.write(NAME);
			out.flush();
			out.close();
			testSocket.close();
			
			return true;
		}
		catch(UnknownHostException|SocketTimeoutException|ConnectException timex)
		{
			CH4P_Functions.Log("VigieNetwork", CH4P_Functions.LOG_inConsole, CH4P_Functions.LEVEL_INFO, timex.getMessage() + " " + serverAddress);
			return false;
		}
		catch (Exception ex)
		{
			CH4P_Functions.LogException(CH4P_Functions.LOG_inConsole, ex);
			return false;
		}
	}
	
	private static void ServerLoop()
	{
		try
		{
			CH4P_Functions.Log("VigieNetwork", CH4P_Functions.LOG_inConsole, CH4P_Functions.LEVEL_INFO, "ServerLoop started.");
			
			ConnectionHandlerModule incomingConnections = new ConnectionHandlerModule(INTERNAL_PORT);
			CH4P_Multithreading.Submit(incomingConnections);

			while(true)
			{
				Thread.sleep(500);
			}

		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	private static class ConnectionHandlerModule implements Callable
	{
		ServerSocket socket;
		Integer port;
		
		public ConnectionHandlerModule(Integer _port)
		{
			port = _port;
		}

		@Override
		public Object call() throws Exception
		{
			try
			{
				ServerSocket socket = new ServerSocket(port);
				while (true)
				{
					Socket clientsocket = socket.accept();

					ConnectionCheckModule client = new ConnectionCheckModule(clientsocket);
					CH4P_Multithreading.Submit(client);
				}
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
			
			return null;
		}
	}
	
	private static class ConnectionCheckModule implements Callable
	{
		Socket socket = null;
		String message;
		
		public ConnectionCheckModule(Socket _socket)
		{
			socket = _socket;
		}
		
		@Override
		public Object call() throws Exception
		{
			socket.setSoTimeout(CONNECTION_TIMEOUT);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));	
			
			try
			{
				message = in.readLine();
			}
			catch (SocketException|SocketTimeoutException sockex)
			{
				message = "FAILED " + sockex.getMessage();
			}
			
			CH4P_Functions.Log("VigieNetwork", CH4P_Functions.LOG_inConsole, CH4P_Functions.LEVEL_INFO, "Connection received : " + message);

			in.close();
			socket.close();
			return null;
		}
	}
	
}
