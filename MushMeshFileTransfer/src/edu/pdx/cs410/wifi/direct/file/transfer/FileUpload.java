package edu.pdx.cs410.wifi.direct.file.transfer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

public class FileUpload extends Activity {
	
	
	// ListView or Picker where file type can be chosen, sentds a specific intent
	
	private Button b2;
	private Button b3;	// Dismiss
	private Button b4; // Copy file
	private Button b5; // Categories
	private EditText et1;
	private EditText et2;
	private EditText et3;
	private EditText et4;
	private RatingBar rb;
	
	private File exportDirectory;
	private FileWriter exportFile;
	
	private String pathStr;
	private String fileName;
	
	private InfoManager info = new InfoManager();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_file_upload);
		et1 = (EditText)findViewById(R.id.editText1);
		et2 = (EditText)findViewById(R.id.editText2);
		et3 = (EditText)findViewById(R.id.editText3);
		et4 = (EditText)findViewById(R.id.editText4);
		rb = (RatingBar)findViewById(R.id.ratingBar1);
		b3 = (Button)findViewById(R.id.button3);
		b4 = (Button)findViewById(R.id.button4);
		b5 = (Button)findViewById(R.id.button5);
		
		Bundle extras = getIntent().getExtras();
		if(extras !=null){
		    pathStr = extras.getString("path");
		    fileName = extras.getString("name"); }
		
		
	
		// This was used for debugging purposes, file name and type can be entered into the GUI and a file is created in the
		// Local Directory, not necessary since the FileBrowser class was implemented
		/*b2 = (Button)findViewById(R.id.button2);
		b2.setOnClickListener(new View.OnClickListener() {
		
			public void onClick(View v) {
				String tags = et1.getText().toString();
				String description = et2.getText().toString();
				String name = et3.getText().toString();
				String type = et4.getText().toString();
				float rating = rb.getRating();
				
				// Creates a new file with corresponding name in /FileExchange directory
				try{
				 exportDirectory = new File("/storage/sdcard0/FileExchange/LocalDirectory");
				 if (exportDirectory.exists() == false) {
			            if (exportDirectory.mkdirs() == false) {
			            	showToast("Error: Could not access SDCARD");
			       
			            }
			        }
				 exportFile = new FileWriter(exportDirectory.getAbsolutePath() + "/" + name + type);
			     exportFile.write(description);
			     exportFile.flush();
			     exportFile.close();
				}
				catch(Exception e)	{
				}
		
				info.sendData(name, type, tags, description, rating);
				showToast("Upload Successful");
				if(true)
					showToast("All clear");
			}
		});
		*/
		
		b3.setOnClickListener(new View.OnClickListener() {
	
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
				
			}
		});
		b4.setOnClickListener(new View.OnClickListener() {
	
			public void onClick(View v) {
				// TODO Auto-generated method stub
				File sourceFile = new File(pathStr);
				File destFile = new File("/storage/sdcard0/FileExchange/LocalDirectory/" + fileName);
				System.out.println("File name is!!! : " + fileName);
			
				try {copyFile(sourceFile,destFile);
						info.sendData(destFile.getName().toString(), et1.getText().toString(), et2.getText().toString(), rb.getRating());
						finish();
				}
				catch(Exception e){
					System.out.println("Failed to copy :(");
				}
				
			}
		});
		b5.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
		
		
		
		
		
		
		
		
		
		
		
		
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
	
	public void showToast(String str) {
		// TODO Auto-generated method stub
		Toast.makeText(this,str, Toast.LENGTH_SHORT).show();
	}
	
	
}


