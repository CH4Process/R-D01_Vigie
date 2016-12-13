package com.ch4process.network;

import java.net.InetSocketAddress;
import java.net.Socket;

import com.ch4process.utils.CH4P_Functions;

public class NetworkMain
{
	final static String EXTERNAL_SERVER = "www.google.com";
	final static int EXTERNAL_PORT = 80;
	final static String INTERNAL_SERVER = "ch4pcsup.ddns.net";
	final static int INTERNAL_PING = 1197;
	final static int CONNECTION_TIMEOUT = 10000;
	
	
	private void TestServer(String serverAddress, int serverPort) 
	{
		InetSocketAddress address = new InetSocketAddress(serverAddress,serverPort); 
		
		try (Socket socket = new Socket();)
		{
			socket.connect(address,CONNECTION_TIMEOUT);
		} 
		catch (Exception ex) 
		{
			CH4P_Functions.LogException(CH4P_Functions.LOG_inConsole, ex);
		} 
	}
	
}
