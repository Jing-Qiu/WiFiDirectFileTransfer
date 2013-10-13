package edu.pdx.cs410.wifi.direct.file.transfer;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class Downloads extends Activity {
	
	private ListView lv;
	private File exportDirectory;
	private String[] items;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_downloads);
		updateListView();
	}
	
	private void updateListView()	{
		items = loadDirectory();
		lv = (ListView) findViewById(R.id.listView1);
		
		if (items != null)	{
		    ArrayAdapter<String> adapter =
		    new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
		    lv.setAdapter(adapter);  
		
		    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	
		        public void onItemClick(AdapterView<?> parent, android.view.View view,
		                int position, long id) {
		        	System.out.println("/storage/sdcard0/FileExchange/Downloads/" + items[(int)id]);
		        	// This intent for viewing content should be developed to accommodate for different file types, it is currently not working
		        	Intent intent = new Intent();
		        	intent.setAction(Intent.ACTION_VIEW);
		        	intent.setDataAndType(Uri.parse("/storage/sdcard0/FileExchange/Downloads/" + items[(int)id]), "image/jpeg");
		        	startActivity(intent);
		        }
		    });   
		}
	}

	// Find /FileInfo directory and return a list of contained file names, create if /FileInfo doesn't exist
	private String[] loadDirectory() {
		String[] files;
			try {
		        exportDirectory = new File("/storage/sdcard0/FileExchange/Downloads");
		        if (exportDirectory.exists() == false) {
		            if (exportDirectory.mkdirs() == false) {
		            	showToast("Error: Could not access SDCARD");
		            	return null;
		            }
		        }
	        	files = exportDirectory.list();
	        	return files; 
		} catch(Exception e) {
			return null;
		}	
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_downloads, menu);
		return true;
	}

	public void showToast(String str) {
		// TODO Auto-generated method stub
		Toast.makeText(this,str, Toast.LENGTH_SHORT).show();
	}
	
}
