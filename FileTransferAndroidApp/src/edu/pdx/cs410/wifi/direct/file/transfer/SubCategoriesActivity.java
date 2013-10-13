package edu.pdx.cs410.wifi.direct.file.transfer;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

// Pressing a subcategory will highlight it and should store this as a preference of the user
// Not implemented yet: When a user uploads a new file into their Local Directory the same list of categories and subcategories appears and 
// the user may select categories relevant to the file, these descriptors can be stored in the info.txt with each file. Once a user
// receives a peer's info.txt they can compare their own preferences with the file descriptors (useful when a peer has a many entries in their info.txt)
public class SubCategoriesActivity extends Activity {

	private ListView lv;
	private String[] items;
	private int[] itemPosition = new int[10];
	private String supercat;
	private View view;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sub_categories);
		
		Bundle extras = getIntent().getExtras();
		if(extras !=null){
		    supercat = extras.getString("supercat");
		    System.out.println("Updating");
			updateListView(supercat);
		   }
		else{
			showToast("Error loading list");
		}
	}
	
	private void updateListView(String str)	{
		items = loadSubCategories(str);
		lv = (ListView) findViewById(R.id.listView1);
		
		if (items != null)	{
			System.out.println("not null");
		    ArrayAdapter<String> adapter =
		    new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
		    lv.setAdapter(adapter); 
		    
		    for(int i = 0;i<lv.getCount();i++)	{
		    	view = (View)lv.getItemAtPosition(i);
			    if(itemPosition[i] == 0)	{
	        		view.setBackgroundColor(-16776961);
	        	}
	        	else	{
	        		view.setBackgroundColor(-1);
	        	}	
		    }
		
		    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	
		    	// There is a problem when highlighting list entries, entries in the same list but not yet in the view (until scrolled to) will also
		    	// be highlighted for some reason
		        public void onItemClick(AdapterView<?> parent, android.view.View view,
		                int position, long id) {
		        	
		        	if(itemPosition[position] == 0)	{
		        		itemPosition[position] = 1;
		        		view.setBackgroundColor(-16776961);
		        	}
		        	else	{
		        		itemPosition[position] = 0;
		        		view.setBackgroundColor(-1);
		        	}	
		        }
		    });   
		}
	}
	private String[] loadSubCategories(String str){
		System.out.println(str);
		if(str.equals("Art"))	{
			String[] subCategories = {"All","American Lit","Animation","Anime","Ballet","Beauty","Biographies",
					"Cartoons","Childrens Books","Clothing","Dancing","Drawing","Fashion","Filmmaking",
					"Fine Arts","Graphic Design","Interior Design","Jewelry","Landscaping","Literature",
					"Music Composition","Music Instruments","Opera","Painting","Percussion","Photography",
					"Photoshop","Poetry","Sculpting","Songwriting","Tattoos Piercing", "Writing"};
			return subCategories;
		}
		else if(str.equals("Dining"))	{
			String[] subCategories = {"All","Alcoholic Drinks","Bargains/Coupons","Beer","Beverages",
					"Cocktails","Coffee","Cooking","Homebrewing","International","Restaurants","Tea","Vegetarian","Wine"};
			return subCategories;
		}
		else if(str.equals("Entertainment"))	{
			String[] subCategories = {"All","Acting","Action Movies","Advertising","Alternative Rock","Ambient Music"
					,"Blues Music","Books","British Literature","Celebrities","Celtic Music","Christian Music","Classic Films"
					,"Classic Rock","Classical Music","Country Music","Cult Films","Dance Music","Disco","Drama Movies"
					,"Drum 'n Bass","Electronica","Ethnic Music","Fantasy Books","Folk Music","For Kids","Foreign Films"
					,"Funk","Gambling","Gospel Music","Heavy Metal","Hip-Hop/Rap","Horror Movies","Hotels","House Music","Humor"
					,"Independent Films","Indie Rock Pop","Industrial Music","Instant Messaging","Jazz","Karaoke","Latin Music"
					,"Live Theatre","Lounge Music","Luxury","Magic/Illusions","Movies","Multimedia","Music","Musicals","Mystery Novels"
					,"Nightlife","Oldies Music","Paranormal","Performing Arts","Pop Music","Punk Rock","Quizzes","Quotes"
					,"Radio Broadcasts","Reggae","Rock Music","Romance Novels","Satire","Science Fiction","Shakespeare","Soap Operas","Soul/R&B"
					,"Soundtracks","Spas","Techno","Television","Toys","Trance","UFOs","Vocal Music"};
			return subCategories;
		}
		else if(str.equals("Health"))	{
			String[] subCategories = {"All","Aids","Aging","Alternative Health","Anti-aging","Arthritis","Asthma","Babies",
					"Brain Disorders","Cancer","Dentistry","Diabetes","Disabilities","Doctors/Surgeons","Drugs",
					"Eating Disorders","Fitness","Glaucoma","Health","Heart Conditions","Kinesiology","Learning Disorders",
					"Medical Science","Mens Issues","Mental Health","Nursing","Nutrition","Parenting","Physical Therapy",
					"Pregnancy/Birth","Psychiatry","Psychology","Self Improvement","Sexual Health","Substance Abuse",
					"Weight Loss","Women's Issues","Yoga"};
			return subCategories;
		}
		else if(str.equals("Hobbies"))	{
			String[] subCategories = {"All", "Antiques","Audio Equipment","Car Parts","Cars","Collecting","Crafts",
					"Crochet","DJing/Mixing","Dolls Puppets","Gadgets,","Gardening","Guitar","Guns","Hacking","Home Improvement"
					,"Hunting","Knitting","MagicIllusions","Memorabilia","Music Theory","Musician Resources","Pets","Photo Gear"
					,"Poetry","Quilting","Restoration","Scouting","Scrapbooking","Sewing","Survivalist","Vintage Cars","Woodworking"};
			return subCategories;
		}
		else if(str.equals("Sports"))	{
			String[] subCategories = {"All","American","Football","Badminton","Baseball","Basketball","Bicycling","Boating",
			"Bodybuilding","Bowling","Boxin","Camping","Canoeing/Kayaking","Cheerleading","Climbing","Cricket",
			"Extreme Sports","Figure Skating","Fishing","Fly-fishing","Golf","Gymnastics","Hockey",
			"Martial Arts","Motor Sports","Motorcycles","Racquetball","Rodeo","Rugby","Running",
			"Sailing","Scuba Diving","Skateboarding","Skiing","Skydiving","Snowboarding",
			"Soccer","Squash","Surfing","Swimming","Tennis","Track and Field",
			"Volleyball","Water Sports","Windsurfing","Wrestling"};
			return subCategories;
		}
		else if(str.equals("Science/Education"))	{
			String[] subCategories = {"All", "American History","Anatomy","Ancient History","Anthropology","Anti-aging",
				"Archaeology","Art History","Botany","Capitalism","Chemistry","Classical Studies","Cognitive Science",
				"Cold War","Counterculture","Crime","Ethnicity","Eastern Studies",
				"Ecology","Environment","Ergonomics","Ethics","Evolution","Feminism","Forensics","Geography","Geoscience",
				"Government","History","Humanitarianism","Humanities","Journalism","Kids","Linguistics","Marine Biology",
				"Mathematics","Medieval History","Meteorology","Microbiology","Mythology","NativeAmericans","Neuroscience",
				"NuclearScience","Paleontolog","Petroleum","Pharmacology","Philosophy","Physics","Physiology",
				"Political Science","Sociology","Statistics","Zoology"};
			return subCategories;
		}
		else if(str.equals("Technology"))	{
			String[] subCategories = {"All","Artificial Intelligence","Alternative Energy","Amateur Radio","Architecture",
					"Astronomy","Aviation/Aerospace","Biology","Biomechanics","Biotech","CAD","Cell Phones","Chemical Eng",
					"Civil Engineering","Databases","Electrical Eng  ","Electronic Devices","Electronic Parts","Embedded Systems",
					"Encryption","Energy Industry","Genealogy","Genetics","IT","Internet","Linux Unix","MAC OS","Machinery"
					,"Manufacturing","Mechanical Eng  ","Military","Mobile Computing","Nanotech","Network Security","Open Source",
					"Operating Systems","P2P","PHP","Perl","Programming","Research","Robotics","Semiconductors","Shareware",
					"Software","Space Exploration","Supercomputing","Terrorism","Virtual Reality","Web Development","Webhosting",
					"Windows","Windows Dev"};
			return subCategories;
		}
		else if(str.equals("Media Type"))	{
			String[] subCategories = {"All", "News", "Pictures", "Videos", "Audio", "Text"};
			return subCategories;
		}
		return null;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_sub_categories, menu);
		return true;
	}
	public void showToast(String str) {
		// TODO Auto-generated method stub
		Toast.makeText(this,str, Toast.LENGTH_SHORT).show();
	}

}
