/*
 WiFi Direct File Transfer is an open source application that will enable sharin
 of data between Android devices running Android 4.0 or higher using a WiFi direct
 connection without the use of a separate WiFi access point.This will enable data 
 transfer between devices without relying on any existing network infrastructure. 
 This application is intended to provide a much higher speed alternative to Bluetooth
 file transfers. 

 Copyright (C) 2013  Jing Qui (Hillian), Filippo Alimonda, Hongyi Wang (Jack), Badilo Lee (Jin)
 Contact: sceneryofautumn@gmail.com, filippo.alimonda@gmail.com

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

/* Development Notes
 *  - 	Although shared files in the downloads section are not viewable from our application, they 
 * 		can be viewed using ASTRO File Manager, to access the Downloads directory go to ASTRO > My Files >
 * 		FileExchange > Downloads. Further development will allow you to open the files directly through our application, check Downloads class more info
 * 		
 * 	-	While debugging you may want to delete some of the file stored in FileExchange folder but note that
 * 		deleting info.txt without deleting all files contained in the LocalDirectory folder (or vice versa) may cause errors.
 * 		info.txt is managed by the InfoManager class when files are uploaded or deleted through the My File interface (Main Menu)
 * 
 * 	- 	The InfoFile folder will contain info.txt files of peers with which communication has been established, it will be referenced by their IP address ex. "198.234.123.txt"
 * 		and can be accessed from the Connected Peer File List show on the Scan Area interface (Main Menu), once this is available (and two devices are still connected)
 * 		the peer's info.txt file can be viewed and a file can be chosen for download
 * 
 * 	- 	Currently there is a limit on the file size which may be sent to a peer, this is presumed to be due to a limit on the Java byte array size
 * 		but a clever way of parsing a file into multiple packets and some notification to the peer that a parsed file will be sent can be implemented
 * 
 * 	- 	To disconnect two devices or reestablish connection you must go to wifi settings of the android device and reset wifi on/off button,
 * 		but there is surely a way to do this through the application by using the WifiP2p framework (it would also makes debugging faster)
 * 
 * 	-	GroupOwnerIntent values in the WifiP2pConfig can be set to determine which device will be the GO but it did not seem to be working,
 * 		the implementation may be incorrect but there are not many resources on how to get it to work, although it may be helpful when dealing
 * 		with multi-user networks, we did not test with more than two devices and some changes may have to be made
 * 
 */

package edu.pdx.cs410.wifi.direct.file.transfer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class ClientActivity extends Activity {
	
	public final int fileRequestID = 98; 	// Arbitrary value, checked when returning to ClientActivity after result
	public final int port = 7950;			// May or may not be arbitrary, verify this
	
	private WifiP2pManager wifiManager;		// Used to setup P2P connection 
	private Channel wifichannel;
	private BroadcastReceiver wifiClientReceiver;
	private IntentFilter wifiClientReceiverIntentFilter;
	
	private boolean filePathProvided;
	private File fileToSend;
	private boolean transferActive;
	
	private boolean iClicked = false;		// Planned to use this to determine which device initiated the connection so that only that device
											// makes a call to PeerDocuments activity to display the peer's directory information
											// Regardless of this, both devices have the other peer's directory information (info.txt) after P2P is established

	private InfoManager infoMng;			// Manages info.txt additions, deletions, and content viewing 
	
	private Intent clientServiceIntent;
	private Intent clientStartIntent;
	private WifiP2pDevice targetDevice;
	private WifiP2pInfo  wifiInfo = null;
	private String path;					// Specifies application's root directory as a string object
	private File downloadTarget;			// Specifies application's root directory as a File object
	private byte[] fileBytes;				// serialization of file to be sent to peer

	private File exportDirectory;			// Generic File object for different program files which need to be stored for use by another part of the program	
	
	private Intent serverServiceIntent; 	// Intent allocation for ServerService activity, used when receiving packets/messages
	
	private boolean serverThreadActive;
	
	private BroadcastReceiver wifiServerReceiver;
	private ListView InfoListView;			// Used to display info.txt of peers which are exchanged after communication is established, 
	private ArrayList<String> infoStringArrayList;
	
	private String IPstring;
	private ArrayList<File> infofiles;

		
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        infoMng= new InfoManager();
                     
        wifiManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        
        wifichannel = wifiManager.initialize(this, getMainLooper(), null);
        wifiClientReceiver = new WiFiClientBroadcastReceiver(wifiManager, wifichannel, this);
        
        wifiServerReceiver = new WiFiServerBroadcastReceiver(wifiManager, wifichannel, this);
        
        wifiClientReceiverIntentFilter = new IntentFilter();;
        wifiClientReceiverIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        wifiClientReceiverIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        wifiClientReceiverIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        wifiClientReceiverIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        
        filePathProvided = false;
        fileToSend = null;
        transferActive = false;
        clientServiceIntent = null;
        targetDevice = null;
        
        path = "/storage/sdcard0/FileExchange";
    	downloadTarget = new File(path);
    	
    	serverThreadActive = false;
    	
    	serverServiceIntent= null;
        
        registerReceiver(wifiClientReceiver, wifiClientReceiverIntentFilter);
        registerReceiver(wifiServerReceiver, wifiClientReceiverIntentFilter);
        InfoListView = (ListView) findViewById(R.id.listView1);
        setClientFileTransferStatus("Client is currently idle");	
        search();
        startServer();
        }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_client, menu);
        
        return true;
    }

    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    
    public void setNetworkToReadyState(boolean status, WifiP2pInfo info, WifiP2pDevice device)
    {
    	wifiInfo = info;
    	targetDevice = device;
    }
    
    private void stopClientReceiver()
    {        
       	try
    	{
            unregisterReceiver(wifiClientReceiver);
    	}
    	catch(IllegalArgumentException e)
    	{
    		//This will happen if the server was never running and the stop button was pressed.
    		//Do nothing in this case.
    	}
    }
        
    public void searchForPeers(View view) {
        
       search();
       System.out.println("search for peers");

    }
    public void search()
    {
    	//Discover peers, no call back method given
        wifiManager.discoverPeers(wifichannel, null);	
    	
    }
    
   public void browseForFile(View view) {
        clientStartIntent = new Intent(this, FileBrowser.class);
        startActivityForResult(clientStartIntent, fileRequestID);  
        
   }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
    	displayInfos();
    	//fileToSend

    	if (resultCode == Activity.RESULT_OK && requestCode == fileRequestID) {
    		//Fetch result
    		File targetDir = (File) data.getExtras().get("file");
    		
    		if(targetDir.isFile())
    		{
    			if(targetDir.canRead())
    			{
    				fileToSend = targetDir;
    				filePathProvided = true;
    				
    				setTargetFileStatus(targetDir.getName() + " selected for file transfer");
    					    			
    			}
    			else
    			{
    				filePathProvided = false;
    				setTargetFileStatus("You do not have permission to read the file " + targetDir.getName());
    			}

    		}
    		else
    		{
				filePathProvided = false;
    			setTargetFileStatus("You may not transfer a directory, please select a single file");
    		}

        }
    	else if (requestCode == 2) {
    		System.out.println("request returned, attempting to send request");
    	     if(resultCode == RESULT_OK){      
		        String result = data.getStringExtra("result");
		        String IP_str = data.getStringExtra("deviceIP");
		  
		        HashMap<String,Object> hmap = new HashMap<String,Object>();
	        	hmap.put("deviceIP", Utils2.getIPAddress(true));
	        	hmap.put("intent", 3); //Delivery of a file request, 3
	        	hmap.put("fileName", result);
	        	saveFile(hmap, "/storage/sdcard0/FileExchange/sending_hash.txt", IP_str);
    	     }
    	     if (resultCode == RESULT_CANCELED) {    
    	         System.out.println("no result, error");
    	     }
    	  }
    }
    
    public void sendFile(File file,String str){
   
    	//Only try to send file if there isn't already a transfer active
    	if(!transferActive)
    	{ 
	        if(true)
	        {	
	        	fileToSend = file;
	        	showToast("Trying to send: " + file.toString());
	        	//Launch client service
	        	clientServiceIntent = new Intent(this, ClientService.class);
	        	clientServiceIntent.putExtra("fileToSend", fileToSend);
	        	clientServiceIntent.putExtra("port", new Integer(port));
	        	//clientServiceIntent.putExtra("targetDevice", targetDevice);
	        	clientServiceIntent.putExtra("wifiInfo", wifiInfo);
	        	clientServiceIntent.putExtra("sendToIP", str);
	        	clientServiceIntent.putExtra("clientResult", new ResultReceiver(null) {
		    	    @Override
		    	    protected void onReceiveResult(int resultCode, final Bundle resultData) {
		    	    	
		    	    	if(resultCode == port )
		    	    	{
			    	        if (resultData == null) {
			    	           //Client service has shut down, the transfer may or may not have been successful. Refer to message 
			    	        	transferActive = false;				    	        				    	       			    	        				    	        			    	        	
			    	        }
			    	        else
			    	        {    	        	
			    	        	final TextView client_status_text = (TextView) findViewById(R.id.file_transfer_status);

			    	        	client_status_text.post(new Runnable() {
			    	                public void run() {
			    	                	client_status_text.setText((String)resultData.get("message"));
			    	                }
			    	        	});		    	   		    	        	
			    	        }
		    	    	}
		    	           	        
		    	    }
		    	});
	        	
	        	showToast("Starting clientService");
	        	transferActive = true;
		        startService(clientServiceIntent);
	        	//end
	        }
    	}
    	else
    		showToast("Transfer in progress");
    }
    
    
    public void startServer()
    {
    	//If server is already listening on port or transferring data, do not attempt to start server service 
    	if(!serverThreadActive)
    	{
	    	//Create new thread, open socket, wait for connection, and transfer file 
	
	    	serverServiceIntent = new Intent(this, ServerService.class);
	    	serverServiceIntent.putExtra("saveLocation", downloadTarget);
	    	serverServiceIntent.putExtra("port", new Integer(port));
	    	serverServiceIntent.putExtra("serverResult", new ResultReceiver(null) {
	    	    @Override
	    	    protected void onReceiveResult(int resultCode, final Bundle resultData) {
	    	    	
	    	    	if(resultCode == port )
	    	    	{
		    	        if (resultData == null) {
		    	           //Server service has shut down. Download may or may not have completed properly. 
		    	        	serverThreadActive = false;	
		    	        	
		    	        	/*final TextView server_status_text = (TextView) findViewById(R.id.server_status_text);
		    	        	server_status_text.post(new Runnable() {
		    	                public void run() {
				    	        	server_status_text.setText(R.string.server_stopped);
		    	                }
		    	        	});*/	
		    	    	        			    	        	
		    	        }
		    	        else	
		    	        {   
		    	        	// loadPreviousHash would normally be used to de-serialize the device's info.txt serialized HashMap
		    	        	// but it can also be used to de-serialize the packet it has received from a peer
		    	        	// because the packet is also a serialized HashMap
		    	        	HashMap<String,Object>receivedHash = infoMng.loadPreviousHash(resultData.getString("message"));
		    	        	HashMap<String,Object> hmap;	// hmap is the packet which is prepared and sent to the peer, it is serialized into a byte array and then sent over a socket
		    	        	String IP_str; // will contain the peer's IP address, which is read from the received HashMap
		    	        	
		    	        	// The intent key in the receivedHash contains an integer value which is checked for by the following switch statement
		    	        	// this allows the devices to continue communication steps efficiently
		    	        	switch((Integer)receivedHash.get("intent")){
					    	case 0:	// GroupOwner must send it's info.txt now that it received the client's IP address (ClientIP is sent as soon as P2P established, by clientSendsIP method)
					    		IP_str = (String)receivedHash.get("deviceIP");
					    		hmap = new HashMap<String,Object>();
					        	hmap.put("deviceIP", Utils2.getIPAddress(true));
					        	hmap.put("intent", 1); //Delivery of confirmation is case:1
					        	hmap.put("infoTxt", infoMng.loadPreviousHash("/storage/sdcard0/FileExchange/info.txt")); // Device's info.txt is de-serialiaed into a HashMap and placed into the hmap
					        	System.out.println("GO sending info.txt to : " + IP_str);
					        	saveFile(hmap, "/storage/sdcard0/FileExchange/sending_hash.txt", IP_str); // saveFile will call a sendFile method which calls the ClientService
					    		break;
					    		
					    	case 1:	// Received a message containing the GO's info.txt, send Client's info.txt
					    		IP_str = (String)receivedHash.get("deviceIP");
					    		hmap = new HashMap<String,Object>();
					        	hmap.put("deviceIP", Utils2.getIPAddress(true));
					        	hmap.put("intent", 2); //Delivery of confirmation is case:2
					        	hmap.put("infoTxt", infoMng.loadPreviousHash("/storage/sdcard0/FileExchange/info.txt"));
					        	System.out.println("Client sending info.txt to : " + IP_str);
					        	
					        	try{
					    			File exportDirectory = new File("/storage/sdcard0/FileExchange/received.txt");
					    				if(!exportDirectory.exists())	{
					    					FileOutputStream f = new FileOutputStream(exportDirectory);
					    					ObjectOutputStream s = new ObjectOutputStream(f);
					    					s.writeObject(receivedHash);
					    					s.flush();
					    				    s.close();
					    				}
					    				else	{
					    					exportDirectory.delete();
					    					exportDirectory = new File("/storage/sdcard0/FileExchange/received.txt");
					    					FileOutputStream f = new FileOutputStream(exportDirectory);
					    					ObjectOutputStream s = new ObjectOutputStream(f);
					    					s.writeObject(receivedHash);
					    					s.flush();
					    				    s.close();
					    				}
					    			}
					    			catch(Exception e) {
					    			}
					 
					        	HashMap<String,Object> savedHmap = infoMng.loadPreviousHash("/storage/sdcard0/FileExchange/received.txt");
								String peername = (String)savedHmap.get("deviceIP");
								try
					        	{
									copyFile(new File("/storage/sdcard0/FileExchange/received.txt"),peername);
					        	}
					        	catch(Exception e)
					        	{
					        		System.out.println("error! ");
					        	}
					        	
					        	saveFile(hmap, "/storage/sdcard0/FileExchange/sending_hash.txt", IP_str);
					        	System.out.println("iClicked is = " + iClicked);
					   //     	if(iClicked == true){	// There were bugs when implementing the iClicked functionality, although it's certainly possible
					   //     		iClicked = false;
					        							// But for now the PeerDocuments activity is started by both devices as soon as they receive
					        							// the peer's info.txt file
						        	Intent intent_1 = new Intent(ClientActivity.this, PeerDocuments.class);
						        	System.out.println("Device IP sending is : " + IP_str);
						        	intent_1.putExtra("deviceIP",IP_str);
						        	intent_1.putExtra("file", "/storage/sdcard0/FileExchange/received.txt"); // received.txt is a serialized packet (HashMap) which contains the peer's info.txt (also a HashMap)
						    		startActivityForResult(intent_1, 2); // 2 is the requestCode used by onActivityResult method in ClientActivity
						    	break;
						    	
					    	case 2: // GO received the Client's info.txt so it may now view the directory information by using PeerDocuments activity
					    		IP_str = (String)receivedHash.get("deviceIP");
					    		System.out.println("Case 2: Received info.txt");
					    		try{
					    			File exportDirectory = new File("/storage/sdcard0/FileExchange/received.txt");
					    				if(!exportDirectory.exists())	{
					    					FileOutputStream f = new FileOutputStream(exportDirectory);
					    					ObjectOutputStream s = new ObjectOutputStream(f);
					    					s.writeObject(receivedHash);
					    					s.flush();
					    				    s.close();
					    				}
					    				else	{
					    					exportDirectory.delete();
					    					exportDirectory = new File("/storage/sdcard0/FileExchange/received.txt");
					    					FileOutputStream f = new FileOutputStream(exportDirectory);
					    					ObjectOutputStream s = new ObjectOutputStream(f);
					    					s.writeObject(receivedHash);
					    					s.flush();
					    				    s.close();
					    				}
					    			}
					    			catch(Exception e) {
					    			}
					    		
					    		HashMap<String,Object> savedHmap2 = infoMng.loadPreviousHash("/storage/sdcard0/FileExchange/received.txt");
								String peername2 = (String)savedHmap2.get("deviceIP");
								try
					        	{
									copyFile(new File("/storage/sdcard0/FileExchange/received.txt"),peername2);
					        	}
					        	catch(Exception e)
					        	{
					        		System.out.println("error! ");
					        	}
					    	//	if(iClicked == true){
					    		//	iClicked = false;
						    		Intent intent_2 = new Intent(ClientActivity.this, PeerDocuments.class);
						    		//startActivityForResult(intent_2, requestCode, Bundle options);
						    		intent_2.putExtra("deviceIP",IP_str);
						    		intent_2.putExtra("file", "/storage/sdcard0/FileExchange/received.txt");
						    		startActivityForResult(intent_2,2);
					    		break;
					    		
					    	case 3:
					    		// A file has been requested by a peer, it's name is contained in the receivedHash, the file must be serialized and sent to peer
					    		String fileRequested = (String)receivedHash.get("fileName");
					    		File file = new File("/storage/sdcard0/FileExchange/LocalDirectory/" + fileRequested);
					    		try{
					    		fileBytes = readFile(file);}	// File is serialized in the readFile method
					    		catch(Exception e){
					    			System.out.println("could not read to byte[]");
					    		}
					    		IP_str = (String)receivedHash.get("deviceIP");
					    		hmap = new HashMap<String,Object>();
					        	hmap.put("deviceIP", Utils2.getIPAddress(true));
					        	hmap.put("intent", 4); //Delivery of file, 4
					        	hmap.put("file", fileBytes);
					        	hmap.put("fileName", fileRequested);
					        	saveFile(hmap, "/storage/sdcard0/FileExchange/sending_hash.txt", IP_str);
					    		break;
					    		
					    	case 4:	// File has been received and is contained as a byte array, referenced by the "file" key in receivedHash
					    		if(receivedHash.get("file") != null){
					    			File sharedFile = new File("/storage/sdcard0/FileExchange/Downloads/" + (String)receivedHash.get("fileName"));
					    			try{
					    				if(sharedFile.exists()){
							    			FileOutputStream fos = new FileOutputStream(sharedFile);
							    			fos.write((byte[])receivedHash.get("file"));
							    			fos.flush();
							    			fos.close();	}
					    				else {
					    					sharedFile.delete();
					    					sharedFile = new File("/storage/sdcard0/FileExchange/Downloads/" + (String)receivedHash.get("fileName"));
					    					FileOutputStream fos = new FileOutputStream(sharedFile);
							    			fos.write((byte[])receivedHash.get("file"));
							    			fos.flush();
							    			fos.close();
					    				}
					    			}
					    			catch(Exception e){
					    				System.out.println("Could not save the received file");
					    			}
					    		}
					    		
					    		break;
					    	}
		    	        }
	    	    	}
	    	           	        
	    	    }
	    	});
	    		    		
	    	serverThreadActive = true;
	        startService(serverServiceIntent); // ServerService is started whenever a package is sent out, this way the device is listening for an incoming packet immediately 
	    }
    }
    

    public static byte[] readFile (File file) throws IOException {
        // Open file
        RandomAccessFile f = new RandomAccessFile(file, "r");

        try {
            long longlength = f.length();
            int length = (int) longlength;
            System.out.println("length is :" + length);
            if (length != longlength) throw new IOException("File size >= 2 GB");

            // Read file and return data
            byte[] data = new byte[length];
            f.readFully(data);
            return data;
        }
        finally {
            f.close();
        }
    }
  
	private static void copyFile(File sourceFile, String peernames)
			throws IOException {
		if (!sourceFile.exists()) {
			return;
		}
		// Saves the peer's info.txt in the InfoFiles directory, peernames is an IP address
		File destFile= new File("/storage/sdcard0/FileExchange/InfoFiles/"+peernames+".txt");
			
		FileChannel source = null;
		FileChannel destination = null;
		source = new FileInputStream(sourceFile).getChannel();
		destination = new FileOutputStream(destFile).getChannel();
		if (destination != null && source != null) {
			destination.transferFrom(source, 0, source.size());
		}
		if (source != null) {
			source.close();
		}
		if (destination != null) {
			destination.close();
		}
		

	}
	
	private static void copyFile(File sourceFile, File destFile)
			throws IOException {
		if (!sourceFile.exists()) {
			return;
		}
		if (!destFile.exists()) {
			destFile.createNewFile();
		}
		FileChannel source = null;
		FileChannel destination = null;
		source = new FileInputStream(sourceFile).getChannel();
		destination = new FileOutputStream(destFile).getChannel();
		if (destination != null && source != null) {
			destination.transferFrom(source, 0, source.size());
		}
		if (source != null) {
			source.close();
		}
		if (destination != null) {
			destination.close();
		}

	}
	
    // Serializes a given HashMap for sending to IP_str device
    private void saveFile(HashMap<String,Object> hmap, String str, String IP_str){

		try{
			File exportDirectory = new File(str);
				if(!exportDirectory.exists())	{
					FileOutputStream f = new FileOutputStream(exportDirectory);
					ObjectOutputStream s = new ObjectOutputStream(f);
					s.writeObject(hmap);
					s.flush();
				    s.close();
				}
				else	{
					exportDirectory.delete();
					exportDirectory = new File(str);
					FileOutputStream f = new FileOutputStream(exportDirectory);
					ObjectOutputStream s = new ObjectOutputStream(f);
					s.writeObject(hmap);
					s.flush();
				    s.close();
				}
				sendFile(exportDirectory, IP_str);
			}
			catch(Exception e) {
			}
    }
    
    
    @Override
    protected void onResume() {
        super.onResume();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        //Continue to listen for wifi related system broadcasts even when paused (this might be useful for automatic, user-less file sharing
        //stopClientReceiver();
    }
    
    private void stopServerReceiver()
    {        
       	try
    	{
            unregisterReceiver(wifiServerReceiver);
    	}
    	catch(IllegalArgumentException e)
    	{
    		//This will happen if the server was never running and the stop button was pressed.
    		//Do nothing in this case.
    	}
    }
    
    @Override	// This should technically disconnect two devices but it doesn't seem to work
    protected void onDestroy() {
        super.onDestroy();
        //Kill thread that is transferring data
        //Unregister broadcast receiver
        stopClientReceiver();
        stopServerReceiver();
       
    }
    
    
    public void setClientWifiStatus(String message)
    {
    	TextView connectionStatusText = (TextView) findViewById(R.id.client_wifi_status_text);
    	connectionStatusText.setText(message);	
    }
    
    public void setClientStatus(String message)
    {
    	TextView clientStatusText = (TextView) findViewById(R.id.client_status_text);
    	clientStatusText.setText(message);	
    }
    
    public void setClientFileTransferStatus(String message)
    {
    	TextView fileTransferStatusText = (TextView) findViewById(R.id.file_transfer_status);
    	fileTransferStatusText.setText(message);	
    }
    
    public void setTargetFileStatus(String message)
    {
    	TextView targetFileStatus = (TextView) findViewById(R.id.selected_filename);
    	targetFileStatus.setText(message);	
    }
    
    private String[] loadDirectory() {
		String[] files;
			try {
		        exportDirectory = new File("/storage/sdcard0/FileExchange/InfoFiles");
		        if (exportDirectory.exists() == false) {
		            if (exportDirectory.mkdirs() == false) {
		            	showToast("Error: Could not access SDCARD");
		            	return null;
		            }
		        }
	        	files = exportDirectory.list();	// gets a list of the files contained in the InfoFile folder
	        	return files; 
		} catch(Exception e) {
			return null;
		}	
	}
    
    public void displayInfos()
    {
    	
    	//open infos directory
    	//read all file headers
    	//populate array list
    	//use the arraylist to show the listview
    	infoStringArrayList = new ArrayList<String>(Arrays.asList(this.loadDirectory()));
  
    	//Set list view as clickable
    	InfoListView.setClickable(true);
     
    	//Make adapter to connect peer data to list view
    	ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, infoStringArrayList.toArray());    			
    	
    	//Show peer data in listview
    	InfoListView.setAdapter(arrayAdapter);	
    	
    	InfoListView.setOnItemClickListener(new OnItemClickListener() {
    		
   public void onItemClick(AdapterView<?> parent, android.view.View view, int postion,long id) {
				
	   File f= new File("storage/sdcard0/FileExchange/InfoFiles/"+infoStringArrayList.get(postion));
   	
   		HashMap<String,Object> savedHmap = infoMng.loadPreviousHash("storage/sdcard0/FileExchange/InfoFiles/"+infoStringArrayList.get(postion));
   		String IPstring = (String)savedHmap.get("deviceIP");
		
	   Intent intent_1 = new Intent(ClientActivity.this, PeerDocuments.class);
   	   System.out.println("Device IP sending is : " + IPstring);
   	   intent_1.putExtra("deviceIP",IPstring);
   	   intent_1.putExtra("file", f.getAbsolutePath());  
	   startActivityForResult(intent_1, 2);
											
			}			
				// TODO Auto-generated method stub				
			});
  	
    	
    	
    }
    
    public void displayPeers(final WifiP2pDeviceList peers)
    {
    	//Dialog to show errors/status
		final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle("WiFi Direct File Transfer");
		
		//Get list view
    	ListView peerView = (ListView) findViewById(R.id.peers_listview);
    	
    	//Make array list
    	ArrayList<String> peersStringArrayList = new ArrayList<String>();
    	
    	//Fill array list with strings of peer names
    	for(WifiP2pDevice wd : peers.getDeviceList())
    	{
    		peersStringArrayList.add(wd.deviceName);
    	}
    	
    	//Set list view as clickable
    	peerView.setClickable(true);
    	
   
    	   
    	//Make adapter to connect peer data to list view
    	ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, peersStringArrayList.toArray());    			
    	
    	//Show peer data in listview
    	peerView.setAdapter(arrayAdapter);
    		
    	
		peerView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View view, int arg2,long arg3) {
				
				
				//Get string from textview
				TextView tv = (TextView) view;
				
				WifiP2pDevice device = null;
				
				//Search all known peers for matching name
		    	for(WifiP2pDevice wd : peers.getDeviceList())
		    	{
		    		if(wd.deviceName.equals(tv.getText()))
		    			device = wd;	
		    		   
		    	}
				
				if(device != null)
				{
					//iClicked = true;	// This seemed to be the best place to set iClicked to true since only the device which 
										// first selected a peer's name on the shown list will go through this method call but it is buggy
					//Connect to selected peer
					showToast(device.toString());
					connectToPeer(device);	
				}
				else
				{
					dialog.setMessage("Failed");
					dialog.show();			
				}							
			}			
				// TODO Auto-generated method stub				
			});
    }
        
    public void connectToPeer(final WifiP2pDevice wifiPeer)
    {
    	this.targetDevice = wifiPeer;
    	//iClicked = true;
    	WifiP2pConfig config = new WifiP2pConfig();
    	config.deviceAddress = wifiPeer.deviceAddress;
    	config.groupOwnerIntent = 15;	// Doesn't seem to make a difference in who is GO and who is Client
    	wifiManager.connect(wifichannel, config, new WifiP2pManager.ActionListener()  {
    	    public void onSuccess() {
    	    	showToast("Connection to " + targetDevice.deviceName + " sucessful");
    	    	
    	    	//setClientStatus("Connection to " + targetDevice.deviceName + " sucessful");
    	    }

    	    public void onFailure(int reason) {
    	    	//setClientStatus("Connection to " + targetDevice.deviceName + " failed");

    	    }
    	});    	
    
    }
    
    // This is called by the Client when a P2P connection is first established, since the GO does not know the Client's IP it is the Client's job to make the GO aware
    public File packageIP(){
    	String str = Utils2.getIPAddress(true);
    	HashMap<String,Object> hmap = new HashMap<String,Object>();
    	hmap.put("deviceIP", str);
    	hmap.put("intent", 0); //Delivery of IP address is case:0
		try{
		File exportDirectory = new File("/storage/sdcard0/FileExchange/ip.txt");
			if(!exportDirectory.exists())	{
				FileOutputStream f = new FileOutputStream(exportDirectory);
				ObjectOutputStream s = new ObjectOutputStream(f);
				s.writeObject(hmap);
				s.flush();
			    s.close();
			    return exportDirectory;
			}
			else	{
				exportDirectory.delete();
				exportDirectory = new File("/storage/sdcard0/FileExchange/ip.txt");
				FileOutputStream f = new FileOutputStream(exportDirectory);
				ObjectOutputStream s = new ObjectOutputStream(f);
				s.writeObject(hmap);
				s.flush();
			    s.close();
			    return exportDirectory;
			}
		}
		catch(Exception e) {
		}
		return null;
    }
    
    
	public void showToast(String str) {
		// TODO Auto-generated method stub
		Toast.makeText(this,str, Toast.LENGTH_SHORT).show();
	}

	// This method is called in the WiFiClientBroadcastReceiver class as soon as hardware connection is established 
    public void clientSendsIP(){
    	
    	// If a device is not the GO then it will prepare it's IP address and send it to the GO
    	if(!wifiInfo.isGroupOwner){
    		sendFile(packageIP(),wifiInfo.groupOwnerAddress.toString());
    	}  	
    	// The group owner will just wait and listen for an incoming package containing the client's IP address
    	else if(wifiInfo.isGroupOwner)	{
    	showToast("This is owner");    	
    	}
    }  
}
