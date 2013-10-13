

package edu.pdx.cs410.wifi.direct.file.transfer;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

public class ServerService extends IntentService {

	private boolean serviceEnabled;
	
	private int port;
	private File saveLocation;
	private ResultReceiver serverResult;
	private File exportDirectory;
	
	public HashMap<String, Object> receivedHash;
	
	public ServerService() {
		super("ServerService");
		serviceEnabled = true;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
		port = ((Integer) intent.getExtras().get("port")).intValue();	
		saveLocation = (File) intent.getExtras().get("saveLocation");
		serverResult = (ResultReceiver) intent.getExtras().get("serverResult");	
		
		//signalActivity("Starting to download");
		 
		
		String fileName = "";
		
        ServerSocket welcomeSocket = null;
        Socket socket = null;
                      
		try {
			
				welcomeSocket = new ServerSocket(port);
				
				while(true && serviceEnabled)
				{
				
				//Listen for incoming connections on specified port
				//Block thread until someone connects 
				socket = welcomeSocket.accept();
				
				//signalActivity("TCP Connection Established: " + socket.toString() + " Starting file transfer");

				InputStream is = socket.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);			
				
				OutputStream os = socket.getOutputStream();
				PrintWriter pw = new PrintWriter(os);
				
				
				String inputData = "";
				
				
				
				//signalActivity("About to start handshake");
				//Client-Server handshake
				
				/*
				String test = "Y";
				test = test + br.readLine() + test;
		
				
				signalActivity(test);
				 */
				
				/*
				inputData = br.readLine();
				
				if(!inputData.equals("wdft_client_hello"))
				{
					throw new IOException("Invalid WDFT protocol message");
					
				}
				
				pw.println("wdft_server_hello");
				
				
				inputData = br.readLine();
				
				
				if(inputData == null)
				{
					throw new IOException("File name was null");
					
				}
				
				
				fileName = inputData;
				
				pw.println("wdft_server_ready");
	
				*/
				
				//signalActivity("Handshake complete, getting file: " + fileName);
	
				String savedAs = "WifiDirectTransfer_" + System.currentTimeMillis();
			    File file = new File(saveLocation, savedAs);
			    
			    byte[] buffer = new byte[4096];
			    int bytesRead;
			    
			    FileOutputStream fos = new FileOutputStream(file);
			    BufferedOutputStream bos = new BufferedOutputStream(fos);
			    
			    while(true)
			    {
				    bytesRead = is.read(buffer, 0, buffer.length);
				    if(bytesRead == -1)
				    {
				    	break;
				    }			    
				    bos.write(buffer, 0, bytesRead);
				    bos.flush();
	
			    }
			    		    
	
			    /*
			    fos.close();
			    bos.close();
			    
			    br.close();
			    isr.close();
			    is.close();
			    
			    pw.close();
			    os.close();
			    		    
			    socket.close();
			    */
			    
			    bos.close();
			    socket.close();
	
			    
			    receivedHash = getHash(file);
			    file.delete();
			    
			    if(receivedHash != null){
			    	try{
			    		File exportDirectory = new File("/storage/sdcard0/FileExchange/hashMap.txt");
			    			if(!exportDirectory.exists())	{
			    				FileOutputStream f = new FileOutputStream(exportDirectory);
			    				ObjectOutputStream s = new ObjectOutputStream(f);
			    				s.writeObject(receivedHash);
			    				s.flush();
			    			    s.close();
			    			}
			    			else	{
			    				exportDirectory.delete();
			    				exportDirectory = new File("/storage/sdcard0/FileExchange/hashMap.txt");
			    				FileOutputStream f = new FileOutputStream(exportDirectory);
			    				ObjectOutputStream s = new ObjectOutputStream(f);
			    				s.writeObject(receivedHash);
			    				s.flush();
			    			    s.close();
			    			}
			    		}
			    		catch(Exception e) {
			    		}
			    	signalActivity("/storage/sdcard0/FileExchange/hashMap.txt");
			    	
			    }
			    else {
			    	signalActivity(null);
			    //	signalActivity("Error with getHash");
			    }

			}
			  
			
	    
		} catch (IOException e) {
			signalActivity(e.getMessage());
			
			
		}
		catch(Exception e)
		{
			signalActivity(e.getMessage());

		}
			
		//Signal that operation is complete
		serverResult.send(port, null);
	
	}
	
	public HashMap<String,Object> getHash(File savedFile)	{
		try{
		    FileInputStream f = new FileInputStream(savedFile);
		    ObjectInputStream s = new ObjectInputStream(f);
		    
		    @SuppressWarnings("unchecked")
			HashMap<String, Object> fileObj = (HashMap<String,Object>) s.readObject();
		    s.close();
		    return fileObj;
		}
		// Will catch when the first file is uploaded since info.txt doesn't exist, but this is not a problem
		catch(ClassNotFoundException cnfe) { 
			return null;
		}
		catch(IOException ioe)	{
			return null;
		}
	}
	
	public void signalActivity(String message)
	{
		Bundle b = new Bundle();
		b.putString("message", message);
		serverResult.send(port, b);
	}
	
	
	public void onDestroy()
	{
		serviceEnabled = false;
		
		//Signal that the service was stopped 
		//serverResult.send(port, new Bundle());
		
		stopSelf();
	}

}
