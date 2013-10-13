package edu.pdx.cs410.wifi.direct.file.transfer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

// Will call SubCategoriesActivity when a general category is selected
public class PreferencesActivity extends Activity {

	private ListView lv;
	private String[] items;
	private int[] itemPosition = new int[9];
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preferences);
		updateListView();
	}
	
	private void updateListView()	{
		items = loadSuperCategories();
		lv = (ListView) findViewById(R.id.listView1);
		
		if (items != null)	{
		    ArrayAdapter<String> adapter =
		    new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
		    lv.setAdapter(adapter);  
		
		    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	
		        public void onItemClick(AdapterView<?> parent, android.view.View view,
		                int position, long id) {
		        	
		        	System.out.println("Item is: " + items[position]);
		        	Intent intent = new Intent(PreferencesActivity.this, SubCategoriesActivity.class);
		        	intent.putExtra("supercat",items[position]);
		        	startActivity(intent);
		        
		        	/*
		        	if(itemPosition[position] == 0)	{
		        		System.out.println("tag here");
		        		itemPosition[position] = 1;
		        		view.setBackgroundColor(-16776961);
		        	}
		        	else	{
		        		itemPosition[position] = 0;
		        		view.setBackgroundColor(-1);
		        	}
		        	*/
		        		
		        }
		    });   
		}
	}
	private String[] loadSuperCategories(){
		String[] superCategories = {"Art", "Dining", "Entertainment", "Health", "Hobbies", "Sports", "Science", "Technology","Media Type"};
		return superCategories;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_preferences, menu);
		return true;
	}
	public void showToast(String str) {
		// TODO Auto-generated method stub
		Toast.makeText(this,str, Toast.LENGTH_SHORT).show();
	}

}
