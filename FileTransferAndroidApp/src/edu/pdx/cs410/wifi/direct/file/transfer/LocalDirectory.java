package edu.pdx.cs410.wifi.direct.file.transfer;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

public class LocalDirectory extends Activity {
	
	private Button b1;
	private ListView lv;
	private File exportDirectory;
	private String[] items;
	private InfoManager info = new InfoManager();
	
	public PopupWindow popupWindow;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_local_directory);
		b1 = (Button)findViewById(R.id.button1);
		b1.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				Intent intent = new Intent(LocalDirectory.this, FileBrowser.class);
				startActivity(intent);
			}
		});	
		updateListView();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		updateListView();
	}
	
	private void updateListView()	{
		items = loadDirectory();
		lv = (ListView) findViewById(R.id.list);
		
		if (items != null)	{
		    ArrayAdapter<String> adapter =
		    new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
		    lv.setAdapter(adapter);  
		
		    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	
		        public void onItemClick(AdapterView<?> parent, android.view.View view,
		                int position, long id) {
		        	System.out.println("Chose :" + items[(int)id]);
		        	showPopUp(items[(int)id], info.findMatch(items[(int)id],"/storage/sdcard0/FileExchange/info.txt"));
		        }
		    });   
		}
	}

	
	// Find /FileInfo directory and return a list of contained file names, create if /FileInfo doesn't exist
	private String[] loadDirectory() {
		String[] files;
			try {
		        exportDirectory = new File("/storage/sdcard0/FileExchange/LocalDirectory");
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
	
	
	// Generates a popUp screen, parameters are String(file name) and ArrayList(file information)
	public void showPopUp(String str, ArrayList<String> al){
		System.out.println("HEre! " + al.toString());
		final String forDelete = str;
		LayoutInflater layoutInflater = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);  
		View popupView = layoutInflater.inflate(R.layout.popup_file_info, null);
		
		popupWindow = new PopupWindow(popupView,LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);  
		
		TextView t1 = (TextView)popupWindow.getContentView().findViewById(R.id.name);
		t1.setText("File Name:" + al.get(0) + "\nTags: " + al.get(1) + "\nDescription: " + al.get(2) + "\nRating: " + al.get(3));
		
		popupWindow.showAtLocation(findViewById(R.id.view1), Gravity.CENTER, 0, 0);       
        Button btnDismiss = (Button)popupView.findViewById(R.id.dismiss);        
		btnDismiss.setOnClickListener(new Button.OnClickListener(){

		     public void onClick(View v) {
		     popupWindow.dismiss();
		     }});
		
		Button btnDelete = (Button)popupView.findViewById(R.id.delete);
		btnDelete.setOnClickListener(new Button.OnClickListener(){

		     public void onClick(View v) {
		     info.deleteEntry(forDelete);
		     System.out.println("flag2");
		     updateListView();
		     popupWindow.dismiss();
		     }});
		
	}
	
	public void showToast(String str) {
		// TODO Auto-generated method stub
		Toast.makeText(this,str, Toast.LENGTH_SHORT).show();
	}
	
}
