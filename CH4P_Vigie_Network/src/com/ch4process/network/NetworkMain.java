package com.ch4process.network;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import com.ch4process.utils.CH4P_Functions;
import com.ch4process.utils.CH4P_System;

import sun.net.InetAddressCachePolicy;

public class NetworkMain
{	
	final static String EXTERNAL_SERVER = "www.google.com";
	final static int EXTERNAL_PORT = 80;
	//final static String INTERNAL_SERVER = "ch4pcsup.ddns.net";
	final static String INTERNAL_SERVER = "50.8.0.1";
	final static int INTERNAL_PORT = 1197;
	final static int CONNECTION_TIMEOUT = 10000;
	final static String VPN_SOFTWARE = "openvpn";
	final static String COMMAND_VPN_SOFTWARE = "sudo /etc/init.d/openvpn start";
	
	
	public static void main (String args[])
	{
		while (true)
		{
			try
			{	
				// If we can join the server we continue, if not we have to check things ! 
				if(! TestServer(INTERNAL_SERVER, INTERNAL_PORT))
				{
					// Couldn't join the server... If we have acces to internet we have to reboot the VPN Tunnelling software, if not we have to wait.
					if (TestServer(EXTERNAL_SERVER, EXTERNAL_PORT))
					{
						// Killing the software
						
						boolean isActive = true;
						
						while (isActive)
						{
							ArrayList<String> processList = CH4P_System.GetProcessList();
							for (String processName:processList)
							{
								isActive = false;
								
								if (processName.equals(VPN_SOFTWARE))
								{
									isActive = true;
									CH4P_System.KillProcess(VPN_SOFTWARE);
								}
							}
							
							Thread.sleep(500);
						}
						
						
						// At this point the software is killed, so we restart it !
						
						CH4P_System.StartProcess(COMMAND_VPN_SOFTWARE);
						
					}
					else
					{
						// If we can't access internet it may be because of a 3G dongle problem. So we may have to reboot the dongle. TODO : Reboot 3G dongle
					}
				}
				
				// Whatever happens, we'll wait one minute before trying again.
				Thread.sleep(60000);
					
			}
			catch (Exception ex)
			{
				CH4P_Functions.LogException(CH4P_Functions.LOG_inConsole, ex);
			}
		}
	}
	
	private static boolean TestServer(String serverAddress, int serverPort) 
	{
		InetSocketAddress address = new InetSocketAddress(serverAddress,serverPort); 
		try
		{
			InetAddress addr = InetAddress.getByName(serverAddress);
			boolean bool = addr.isReachable(2000);
			System.out.println(bool);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try (Socket socket = new Socket())
		{
			socket.connect(address,CONNECTION_TIMEOUT);
			return true;
		} 
		catch (Exception ex) 
		{
			CH4P_Functions.LogException(CH4P_Functions.LOG_inConsole, ex);
			return false;
		} 
	}
	
}
