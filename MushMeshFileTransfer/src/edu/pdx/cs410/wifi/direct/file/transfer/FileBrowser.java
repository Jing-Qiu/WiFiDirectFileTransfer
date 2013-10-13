/*
 WiFi Direct File Transfer is an open source application that will enable sharing 
 of data between Android devices running Android 4.0 or higher using a WiFi direct
 connection without the use of a separate WiFi access point.This will enable data 
 transfer between devices without relying on any existing network infrastructure. 
 This application is intended to provide a much higher speed alternative to Bluetooth
 file transfers. 

 */

package edu.pdx.cs410.wifi.direct.file.transfer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.app.NavUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;




public class FileBrowser extends Activity {

	private String root;
	private String currentPath;
	
	private ResultReceiver rr;
	
	private ArrayList<String> targets;
	private ArrayList<String> paths;
	
	private File targetFile;
	private InfoManager info = new InfoManager(); 
	public PopupWindow popupWindow;
	private String[] items; 
	private File a=null;
	
	private TextView v1;
	public String peerIP;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_browser);
        getActionBar().setDisplayHomeAsUpEnabled(true);
              
        root = "/storage/sdcard0/";
        currentPath = root;
        
        targets = null;
        paths = null;
        
        targetFile = null;

        showDir(currentPath);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_file_browser, menu);
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
    
    
    public void selectDirectory(View view) {
    	
	     File f = new File(currentPath);
	     targetFile = f;
	     
	     //Return target File to activity
		  returnTarget();


    }
    

    public void setCurrentPathText(String message)
    {
    	TextView fileTransferStatusText = (TextView) findViewById(R.id.current_path);
    	fileTransferStatusText.setText(message);	
    }


	private void showDir(String targetDirectory){
		
		setCurrentPathText("Current Directory: " + currentPath);
		
		targets = new ArrayList<String>();
		paths = new ArrayList<String>();
				
	     File f = new File(targetDirectory);
	     File[] directoryContents = f.listFiles();
	     
	     
		if (!targetDirectory.equals(root))

		{
			targets.add(root);
			paths.add(root);
			//targets.add("../");
			targets.add("Move up");
			paths.add(f.getParent());
		}
		
		for(File target: directoryContents)
		{
			paths.add(target.getPath());
			
			if(target.isDirectory())
			{
		        targets.add(target.getName() + "/");
			}
			else
			{
		        targets.add(target.getName());

			}

		}
		
		ListView fileBrowserListView = (ListView) findViewById(R.id.file_browser_listview);

	    ArrayAdapter<String> directoryData = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, targets);
	    fileBrowserListView.setAdapter(directoryData);
	    
	    
	    
	    
	    fileBrowserListView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View view, int pos,long id) {
				
				  
				  File f = new File(paths.get(pos));
				  
				  
				  if(f.isFile())
				  {
					  //targetFile = new File("/");
					  //items= targetFile.list();
					  
					  
					 // showPopUp(f,items[(int)id]);
					  Intent intent = new Intent(FileBrowser.this, FileUpload.class);
					  System.out.println("Intent path = " + f.toString());
					  System.out.println("Intent fileName = " + f.getName());
					  intent.putExtra("path",f.toString());
					  intent.putExtra("name", f.getName());
					  startActivity(intent);
				   
					  //Return target File to activity
				  }
				  else
				  {
					  //f must be a dir
					  if(f.canRead())
					  {
						  currentPath = paths.get(pos);
						  showDir(paths.get(pos));
					  }
					  
				  }

				
			}			
				// TODO Auto-generated method stub				
			});
	    
	
	    /*
		final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle("WiFi Direct File Transfer");
		*/
	   
		
	}
	
	
	 // Generates a popUp screen, parameters are String(file name) and ArrayList(file information)
	public void showPopUp(File f, String str){
	//	v1.findViewById(R.id.file_contents);
		final String forAdd = str;
		a=f;
		System.out.println(str);
		LayoutInflater layoutInflater = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);  
		View popupView = layoutInflater.inflate(R.layout.popup_file_info, null);
		
		popupWindow = new PopupWindow(popupView,LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);  
		
		TextView t1 = (TextView)popupWindow.getContentView().findViewById(R.id.name);
		try{
			t1.setText(f.toString() + "\n" + readFile(f.toString())  );
			peerIP = readFile(f.toString());
		}
		catch(Exception e){
			
		}
		
		//TextView t1 = (TextView)popupWindow.getContentView().findViewById(R.id.name);
		//t1.setText("File: " + str + "\n" + "Name:" + al.get(0) + "\nType: " + al.get(1) + "\nTags: " + al.get(2) + "\nDescription: " + al.get(3) + "\nRating: " + al.get(4));
		
		popupWindow.showAtLocation(findViewById(R.id.view1), Gravity.CENTER, 0, 0);       
        Button btnDismiss = (Button)popupView.findViewById(R.id.dismiss);        
		btnDismiss.setOnClickListener(new Button.OnClickListener(){
			
			 public void onClick(View v) {
				 popupWindow.dismiss();
				 targetFile=a;
				// returnTarget();
				 
			 }});
		
		Button btnDelete = (Button)popupView.findViewById(R.id.delete);
		btnDelete.setOnClickListener(new Button.OnClickListener(){

		     public void onClick(View v){
		    	 try{
		 		    File fileToAdd = new File("/" + forAdd);
		 		   
		    		 
		 			}
		 			catch(Exception e) {
		 			}
		    	 
		    	 
		    	 
		    	 //info.deleteEntry(forDelete);
			     popupWindow.dismiss();
		     }}
		     );
	
	}
	
	public void returnTarget()
	{
		
		Intent returnIntent = new Intent();
		returnIntent.putExtra("file", targetFile);
		setResult(RESULT_OK, returnIntent);
		finish();
		
	}
	
	private static String readFile(String path) throws IOException {
		  FileInputStream stream = new FileInputStream(new File(path));
		  try {
		    FileChannel fc = stream.getChannel();
		    MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
		    /* Instead of using default, pass in a decoder. */
		    return Charset.defaultCharset().decode(bb).toString();
		  }
		  finally {
		    stream.close();
		  }
		}
	
	public void showToast(String str) {
		// TODO Auto-generated method stub
		Toast.makeText(this,str, Toast.LENGTH_SHORT).show();
	}

}



