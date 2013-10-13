
package edu.pdx.cs410.wifi.direct.file.transfer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class InfoManager {
	
	private File exportDirectory;
	static public HashMap<String, ArrayList<String>> newHash = new HashMap<String, ArrayList<String>>();
	private ArrayList<String> list;	// Contains information about a file uploaded such as a description, tags, and rating
	
	// Adds new file information to info.txt, parameter is a HashMap containing new file information
	public void sendData(String fileName, String tags, String description, float rating)	{
		
		list = new ArrayList<String>();
		list.add(fileName);
		list.add(tags);
		list.add(description);
		list.add(Float.toString(rating));
		newHash.put(fileName,list);
		
		// Deserializes the info.txt file into oldHash if it already exists
		HashMap<String, Object> oldHash = loadPreviousHash("/storage/sdcard0/FileExchange/info.txt");
		HashMap<String, Object> bufferHash = new HashMap<String,Object>();

		// Combine the two HashMaps if an oldHash exists
		if (oldHash != null)	{
			bufferHash.putAll(oldHash);
			bufferHash.putAll(newHash);
		}
		// info.txt did not exist, just use newHash to generate info.txt
		else
			bufferHash.putAll(newHash);

		try{
			File exportDirectory = new File("/storage/sdcard0/FileExchange/info.txt");
				if(!exportDirectory.exists())	{
					FileOutputStream f = new FileOutputStream(exportDirectory);
					ObjectOutputStream s = new ObjectOutputStream(f);
					s.writeObject(bufferHash);
					s.flush();
				    s.close();
				}
				else	{
					exportDirectory.delete();
					exportDirectory = new File("/storage/sdcard0/FileExchange/info.txt");
					FileOutputStream f = new FileOutputStream(exportDirectory);
					ObjectOutputStream s = new ObjectOutputStream(f);
					s.writeObject(bufferHash);
					s.flush();
				    s.close();
				}
			}
			catch(Exception e) {
			}
	}
	
	// Searches for info.txt and deserializes and returns its contents as a HashMap
	public HashMap<String,Object> loadPreviousHash(String str)	{	
		File file = new File(str);
		try{
		    FileInputStream f = new FileInputStream(file);
		    ObjectInputStream s = new ObjectInputStream(f);
		    
		    @SuppressWarnings("unchecked")
		    HashMap<String, Object> fileObj = (HashMap<String, Object>) s.readObject();
		    //s.close();
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

	// Return HashMap value corresponding to key with same name as file name pressed on by user in the Local Directory (Main Menu) ListView
	public ArrayList<String> findMatch(String name, String str)	{

		// Find info.txt: a serialized version of the HashMap created in FileUpload.java
		File file = new File(str);
		try{
		    FileInputStream f = new FileInputStream(file);
		    ObjectInputStream s = new ObjectInputStream(f);
		    // Deserialize the text file into a HashMap object
		    @SuppressWarnings("unchecked")
		    
		    HashMap<String,Object> fileObj = (HashMap<String,Object>) s.readObject();
		   
		    if(fileObj.containsKey("infoTxt")){
		    	HashMap<String,ArrayList<String>> fileObj2 = (HashMap<String,ArrayList<String>>) fileObj.get("infoTxt");
		    	s.close();
		    	System.out.println("Name is : " + name);
			    System.out.println("Info txt is : " + fileObj2.toString());
			    list = (ArrayList<String>)fileObj2.get(name);
		    }
		    else if(fileObj.containsKey(name))	{
	    	s.close();
	    	System.out.println("Name is : " + name);
		    System.out.println("Info txt is !!!: " + fileObj.getClass().toString());
		    try{list = (ArrayList<String>)fileObj.get(name);}
		    catch(Exception e){
		    	System.out.println("error");
		    }
		    }
		    // Return the  string array data (tags, description, rating) for the corresponding file
		    return list;
		}
		catch(ClassNotFoundException cnfe) { 
		      return null; 
		}
		catch(IOException ioe)	{
		      return null; 
		}
	}
	
	public void deleteEntry(String str)	{
		HashMap<String, ArrayList<String>> hmap = new HashMap<String, ArrayList<String>>();
		System.out.println("attempting to delete :" + str);
		//hmap = loadPreviousHash();
		try{
			hmap.remove(str);
			exportDirectory = new File("/storage/sdcard0/FileExchange/info.txt");
				if(!exportDirectory.exists())	{
					FileOutputStream f = new FileOutputStream(exportDirectory);
					ObjectOutputStream s = new ObjectOutputStream(f);
					s.writeObject(hmap);
					s.flush();
				    s.close();
				    File fileToDelete = new File("/storage/sdcard0/FileExchange/LocalDirectory/" + str);
				    fileToDelete.delete();
				}
				else	{
					exportDirectory.delete();
					FileOutputStream f = new FileOutputStream(exportDirectory);
					ObjectOutputStream s = new ObjectOutputStream(f);
					s.writeObject(hmap);
					s.flush();
				    s.close();
				    File fileToDelete = new File("/storage/sdcard0/FileExchange/LocalDirectory/" + str);
				    fileToDelete.delete();
				}
			}
			catch(Exception e) {
			}
	}
}
