package edu.pdx.cs410.wifi.direct.file.transfer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}
	
	public void goToClient(View v) {
		Intent intent = new Intent(MainActivity.this, ClientActivity.class);
		startActivity(intent);	
	}
	public void goToMyDirectory(View v) {
		Intent intent = new Intent(MainActivity.this, LocalDirectory.class);
		startActivity(intent);	
	}
	public void goToDownloads(View v) {
		Intent intent = new Intent(MainActivity.this, Downloads.class);
		startActivity(intent);	
	}
	public void goToSettings(View v) {
		Intent intent = new Intent(MainActivity.this, Settings.class);
		startActivity(intent);	
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}
