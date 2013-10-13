package edu.pdx.cs410.wifi.direct.file.transfer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

public class PeerDocuments extends Activity {

	private ListView lv;
	private String[] items;
	private String result;
	
	private String deviceIP;
	private String fileName;
	
	private InfoManager infoMng;
	public PopupWindow popupWindow;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_peer_documents);
		infoMng = new InfoManager();
		updateListView();
		
		Bundle extras = getIntent().getExtras();
		if(extras !=null){
		     deviceIP = extras.getString("deviceIP");
		     fileName = extras.getString("file");
		     }
	}
	
	private void updateListView()	{
		items = loadEntries();
		lv = (ListView) findViewById(R.id.listView1);
		if (items != null)	{
		    ArrayAdapter<String> adapter =
		    new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
		    lv.setAdapter(adapter);  
		    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	
		        public void onItemClick(AdapterView<?> parent, android.view.View view,
		                int position, long id) {
		        	showPopUp(items[(int)id], infoMng.findMatch(items[(int)id],fileName));
				     
		        }
		    });   
		}
	}
	
	// Loads all peer info.txt file names and corresponding descriptions 
	private String[] loadEntries() {
			HashMap<String,Object> hmap = infoMng.loadPreviousHash("/storage/sdcard0/FileExchange/received.txt");
			HashMap infoHash = (HashMap)hmap.get("infoTxt");
			Integer integer = infoHash.size();
			Set<String> stringSet = infoHash.keySet();
			String[] stringArray = (String[])stringSet.toArray(new String[0]);
	   return stringArray; 
	
	}

	// Generates a popUp screen, parameters are String(file name) and ArrayList(file information)
	public void showPopUp(String str, ArrayList<String> al){
		final String forDelete = str;
		LayoutInflater layoutInflater = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);  
		View popupView = layoutInflater.inflate(R.layout.popup_downloadable, null);
		
		popupWindow = new PopupWindow(popupView,LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);  
		
		TextView t1 = (TextView)popupWindow.getContentView().findViewById(R.id.name);
		//t1.setText(al.toString());
		t1.setText("File Name: "+ al.get(0) + "\nTags: " + al.get(1) + "\nDescription: " + al.get(2) + "\nRating: " + al.get(3));
		result = al.get(0);
		popupWindow.showAtLocation(findViewById(R.id.view), Gravity.CENTER, 0, 0);       
        Button btnDismiss = (Button)popupView.findViewById(R.id.dismiss);        
		btnDismiss.setOnClickListener(new Button.OnClickListener(){

		     public void onClick(View v) {
		     popupWindow.dismiss();
		     }});
		
		Button btnDelete = (Button)popupView.findViewById(R.id.download);
		btnDelete.setOnClickListener(new Button.OnClickListener(){

		     public void onClick(View v) {
		     //show mushroom loading bar
		     	
		    	 
	    	 Intent returnIntent = new Intent();
	    	 returnIntent.putExtra("result",result);
	    	 returnIntent.putExtra("deviceIP", deviceIP);
	    	 System.out.println("result value is : " + result.toString());
	    	 System.out.println("device IP is : " + deviceIP);
	    	 
	    	 setResult(RESULT_OK,returnIntent);     
	    	 finish();
		     popupWindow.dismiss();
		     }});
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_peer_documents, menu);
		return true;
	}
	
	
	
	
	public void showToast(String str) {
		// TODO Auto-generated method stub
		Toast.makeText(this,str, Toast.LENGTH_SHORT).show();
	}
	

}
