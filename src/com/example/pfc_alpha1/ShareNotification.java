package com.example.pfc_alpha1;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ShareNotification extends Activity{

	public static String gcm_type;
	public static String gcm_time;
	public static String gcm_time_parsed;
	public static String gcm_lat;
	public static String gcm_lng;
	public static String gcm_extras;
	
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.gcm_menu, menu);
	    return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	  switch (item.getItemId()) {
	  case R.id.action_settings:
		  Intent intent = new Intent(this, Settings.class);
		  intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		  startActivity(intent);
		  break;

	  default:
	    break;
	  }

	  return true;
	} 
	
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    // Preferences
	    PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
	    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
	    
		setContentView(R.layout.share_layout);
	    onNewIntent(getIntent());
	    
	}
	
	@Override
	public void onNewIntent(Intent intent){
	    Bundle extras = intent.getExtras();
	    if(extras != null){
	    	
	    
	    	
	    	gcm_type = extras.getString("type");
	 		
	    	gcm_time = extras.getString("time");
	 		
	    	Timestamp tstmp = Timestamp.valueOf(gcm_time);
	    	
	    	
	    	//Calendar c = Calendar.getInstance();
	    	Date d = new Date();
	    	if (tstmp!=null){
	 	//	long timestampLong = Long.parseLong(gcm_time);
	 		
	 		d.setTime(tstmp.getTime());
	 	//	c.setTime(d);
	    	gcm_time_parsed  = d.toString();
			
	    	}else{
	    		gcm_time = "no time";
	    		gcm_time_parsed = d.toString() ;
	    		
	    	}
	 		
	 		gcm_lat= extras.getString("lat");
	 		double lat = Double.parseDouble(gcm_lat);
	 		gcm_lng = extras.getString("lng");
	 		double lng = Double.parseDouble(gcm_lng);
	 		
	 		Geocoder geocoder;
	 		List<Address> addresses = new ArrayList<Address>();
	 		geocoder = new Geocoder(this, Locale.getDefault());
	 		try {
				addresses = geocoder.getFromLocation(lat, lng, 1);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	 		String address = addresses.get(0).getAddressLine(0);
	 		String city = addresses.get(0).getAddressLine(1);
	 		String country = addresses.get(0).getAddressLine(2);
	 		String place = address+","+city;
	 		
	 		gcm_extras = extras.getString("extras");
	 		
	 		
	       
	            // extract the extra-data in the Notification
	            TextView p_type_desc = (TextView) findViewById(R.id.p_type_desc);
	            p_type_desc.setText(gcm_type);
	            TextView p_loc_desc = (TextView) findViewById(R.id.p_loc_desc);
	            p_loc_desc.setText(place);
	            TextView p_time_desc = (TextView) findViewById(R.id.p_time_desc);
	            p_time_desc.setText(gcm_time_parsed.toString());
	            TextView p_extras = (TextView) findViewById(R.id.p_extras);
	            p_extras.setText(gcm_extras);
	            
	            String  message_build="";
	            if (!gcm_extras.isEmpty()){
	            	 message_build = "Atención: "+gcm_type+" en "+place + ". Contenido extra: "+gcm_extras;
	            }
	            else{
	            	 message_build = "Atención: "+gcm_type+" en "+place;
	            }
	            final String message = message_build;
	           
	            // Share button
	            Button shareButton = (Button) findViewById(R.id.share);
	            shareButton.setOnClickListener(new View.OnClickListener() {
	                public void onClick(View view) {                 
	                    
	                	 Intent sendIntent = new Intent();
	     	            sendIntent.setAction(Intent.ACTION_SEND);
	     	            sendIntent.putExtra(Intent.EXTRA_TEXT, message);
	     	            sendIntent.setType("text/plain");
	     	            startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.send_to)));
	                }
	            });
	            
	            
	            // Call button
	            Button callButton = (Button) findViewById(R.id.call);
	            callButton.setOnClickListener(new View.OnClickListener() {
	            	public void onClick(View view) { 
	            		Intent callIntent = new Intent(Intent.ACTION_CALL);          
	    	            callIntent.setData(Uri.parse("tel:"+"112"));          
	    	            startActivity(callIntent);
	            	}
	            });
	            
	          
	    }
	   


	}
	
	
	
}
