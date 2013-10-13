/*


 WiFi Direct File Transfer is an open source application that will enable sharing 
 of data between Android devices running Android 4.0 or higher using a WiFi direct
 connection without the use of a separate WiFi access point.This will enable data 
 transfer between devices without relying on any existing network infrastructure. 
 This application is intended to provide a much higher speed alternative to Bluetooth
 file transfers. 

 Copyright (C) 2013 Scene Hillian
 Contact: sceneryofautumn@gmail.com

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>
 */




package edu.pdx.cs410.wifi.direct.file.transfer;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import android.app.IntentService;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.Bundle;
import android.os.ResultReceiver;

public class ClientService extends IntentService {

	private boolean serviceEnabled;
	
	private int port;
	private File fileToSend;
	private ResultReceiver clientResult;
	private WifiP2pDevice targetDevice;
	private WifiP2pInfo wifiInfo;
	private FileBrowser fileBrowserIP;
	private InetAddress targetIP;
	
	public static InetAddress respondToIP = null;
	
	public ClientService() {
		super("ClientService");
		serviceEnabled = true;
		
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		port = ((Integer) intent.getExtras().get("port")).intValue();	
		fileToSend = (File) intent.getExtras().get("fileToSend");
		clientResult = (ResultReceiver) intent.getExtras().get("clientResult");	
		//targetDevice = (WifiP2pDevice) intent.getExtras().get("targetDevice");	
		wifiInfo = (WifiP2pInfo) intent.getExtras().get("wifiInfo");	
		String IP_str = (String) intent.getExtras().get("sendToIP");
		
		try{ 
			respondToIP = InetAddress.getByName(IP_str);
		}
		catch(Exception e){
		}
		
		if(!wifiInfo.isGroupOwner)
		{	
				// Change this unless you want a crappy app
				targetIP = wifiInfo.groupOwnerAddress;
				//targetIP = respondToIP;
				//wifiInfo.
				Socket clientSocket = null;
				OutputStream os = null;
		
			try{
		
				clientSocket = new Socket(targetIP, port);
				os = clientSocket.getOutputStream();
				PrintWriter pw = new PrintWriter(os);

				
				InputStream is = clientSocket.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);			
				
				//signalActivity("About to start handshake");

				
				
				//Client-Server handshake
				/*
				pw.println(fileToSend.getName());

				
				
				String inputData = "";
				
				pw.println("wdft_client_hello");
				
				inputData = br.readLine();
				
				if(!inputData.equals("wdft_server_hello"))
				{
					throw new IOException("Invalid WDFT protocol message");
					
				}

				
				pw.println(fileToSend.getName());

				if(!inputData.equals("wdft_server_ready"))
				{
					throw new IOException("Invalid WDFT protocol message");
					
				}
				
				*/
				
				//Handshake complete, start file transfer
				
				
				
			    byte[] buffer = new byte[4096];
			    
			    FileInputStream fis = new FileInputStream(fileToSend);
			    BufferedInputStream bis = new BufferedInputStream(fis);
			   // long BytesToSend = fileToSend.length();
			    			   			    			  		    
			    while(true)
			    {
			    	
				    int bytesRead = bis.read(buffer, 0, buffer.length);
				    
				    if(bytesRead == -1)
				    {
				    	break;
				    }
				    
				    //BytesToSend = BytesToSend - bytesRead;
				    os.write(buffer,0, bytesRead);
				    os.flush();			    
			    }
			    
			    
			    
			    fis.close();
			    bis.close();
			    
			    br.close();
			    isr.close();
			    is.close();
			    
			    pw.close();		    
			    os.close();
			    			   			    
			    clientSocket.close();
			    			    
			    signalActivity("File Transfer Complete, sent file: " + fileToSend.getName());
			  
				
			} catch (IOException e) {
				signalActivity(e.getMessage());
			}
			catch(Exception e)
			{
				signalActivity(e.getMessage());

			}
			
		}
		else	// Case where the device is the GO
		{
				targetIP = respondToIP;
				signalActivity("Send to: " + respondToIP);
	
				Socket clientSocket = null;
				OutputStream os = null;
				 
				try {
					
					clientSocket = new Socket(targetIP, port);
					os = clientSocket.getOutputStream();
					PrintWriter pw = new PrintWriter(os);

					
					InputStream is = clientSocket.getInputStream();
					InputStreamReader isr = new InputStreamReader(is);
					BufferedReader br = new BufferedReader(isr);			
					
					//signalActivity("About to start handshake");

					
					
					//Client-Server handshake
					/*
					pw.println(fileToSend.getName());

					
					
					String inputData = "";
					
					pw.println("wdft_client_hello");
					
					inputData = br.readLine();
					
					if(!inputData.equals("wdft_server_hello"))
					{
						throw new IOException("Invalid WDFT protocol message");
						
					}

					
					pw.println(fileToSend.getName());

					if(!inputData.equals("wdft_server_ready"))
					{
						throw new IOException("Invalid WDFT protocol message");
						
					}
					
					*/
					
					//Handshake complete, start file transfer
					
					
					
				    byte[] buffer = new byte[4096];
				    
				    FileInputStream fis = new FileInputStream(fileToSend);
				    BufferedInputStream bis = new BufferedInputStream(fis);
				   // long BytesToSend = fileToSend.length();
				    			   			    			  		    
				    while(true)
				    {
				    	
					    int bytesRead = bis.read(buffer, 0, buffer.length);
					    
					    if(bytesRead == -1)
					    {
					    	break;
					    }
					    
					    //BytesToSend = BytesToSend - bytesRead;
					    os.write(buffer,0, bytesRead);
					    os.flush();			    
				    }
				    
				    
				    
				    fis.close();
				    bis.close();
				    
				    br.close();
				    isr.close();
				    is.close();
				    
				    pw.close();		    
				    os.close();
				    			   			    
				    clientSocket.close();
				    			    
				    signalActivity("File Transfer Complete, sent file: " + fileToSend.getName());
				    
					
				} catch (IOException e) {
					signalActivity(e.getMessage());
				}
				catch(Exception e)
				{
					signalActivity(e.getMessage());

				}
		}
		clientResult.send(port, null);
	}
	

public static int ipStringToInt(String str) {
     int result = 0;
     String[] array = str.split("\\.");
     if (array.length != 4) return 0;
     try {
         result = Integer.parseInt(array[3]);
         result = (result << 8) + Integer.parseInt(array[2]);
         result = (result << 8) + Integer.parseInt(array[1]);
         result = (result << 8) + Integer.parseInt(array[0]);
     } catch (NumberFormatException e) {
         return 0;
     }
     return result;
 }

public static InetAddress intToInetAddress(int hostAddress) {
    InetAddress inetAddress;
    byte[] addressBytes = { (byte)(0xff & hostAddress),
                            (byte)(0xff & (hostAddress >> 8)),
                            (byte)(0xff & (hostAddress >> 16)),
                            (byte)(0xff & (hostAddress >> 24)) };

    try {
       inetAddress = InetAddress.getByAddress(addressBytes);
    } catch(Exception e) {
       return null;
    }
    return inetAddress;
}

	public void signalActivity(String message)
	{
		Bundle b = new Bundle();
		b.putString("message", message);
		clientResult.send(port, b);
	}
	
	
	public void onDestroy()
	{
		serviceEnabled = false;
		
		//Signal that the service was stopped 
		//serverResult.send(port, new Bundle());
		
		stopSelf();
	}

}