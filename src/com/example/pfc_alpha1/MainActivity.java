package com.example.pfc_alpha1;



import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;


import static com.example.pfc_alpha1.CommonUtilities.DISPLAY_MESSAGE_ACTION;
import static com.example.pfc_alpha1.CommonUtilities.EXTRA_MESSAGE;
import static com.example.pfc_alpha1.CommonUtilities.SENDER_ID;
import static com.example.pfc_alpha1.CommonUtilities.SERVER_URL;
import static  com.example.pfc_alpha1.CommonUtilities.EXTRA_MESSAGE;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class MainActivity extends FragmentActivity implements 
    GooglePlayServicesClient.ConnectionCallbacks, 
    GooglePlayServicesClient.OnConnectionFailedListener{

	
// General Variables
public static final String URL = "https://dl.dropboxusercontent.com/u/123539/parkingpositions.xml"; 	
public final static double EARTH_RADIUS = 6371000;
public  int RADIUS_DIST = 2000;
public boolean FILTERING_RADIUS = false;

//Map Variables
private SupportMapFragment mapFragment;
private GoogleMap mMap;
private double mLat, mLng;
private double aLat, aLng;
private LocationClient mLocationClient;
boolean retrieved_completed = false;
HashMap<Marker, ParkingMarker> eventMarkerMap = new HashMap<Marker, ParkingMarker>();  

//List Variables
List<ParkingMarker> parkings_data ;
ParkingAdapter adapter;
private ListView lstOptions;
private List<ParkingMarker> parkings;


// GCM
TextView mDisplay;
AsyncTask<Void, Void, Void> mRegisterTask;


/*
 * Define a request code to send to Google Play services
 * This code is returned in Activity.onActivityResult
 */
private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;


// Define a DialogFragment that displays the error dialog
public static class ErrorDialogFragment extends DialogFragment {

    // Global field to contain the error dialog
    private Dialog mDialog;

    // Default constructor. Sets the dialog field to null
    public ErrorDialogFragment() {
        super();
        mDialog = null;
    }

    // Set the dialog to display
    public void setDialog(Dialog dialog) {
        mDialog = dialog;
    }

    // Return a Dialog to the DialogFragment.
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return mDialog;
    }
}

public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu items for use in the action bar
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.main, menu);
    return super.onCreateOptionsMenu(menu);
}

@Override
public boolean onOptionsItemSelected(MenuItem item) {
  switch (item.getItemId()) {
  
  case R.id.action_share:
	  Intent share_intent = new Intent(this, ShareNotification.class);
	  share_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	  startActivity(share_intent);
	  break;
  case R.id.action_settings:
	  Intent settings_intent = new Intent(this, Settings.class);
	  settings_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	  startActivity(settings_intent);
	  break;
  case R.id.action_reload:
    Toast.makeText(this, "Reloading parkings...", Toast.LENGTH_LONG)
        .show();
    refresh();
    break;
    
  case R.id.options_register:
      GCMRegistrar.register(this, SENDER_ID);
      return true;
  case R.id.options_unregister:
      GCMRegistrar.unregister(this);
      return true;

  default:
    break;
  }

  return true;
} 

@SuppressLint("ShowToast")
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    
    // Preferences
    PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

    //The Location client
    mLocationClient = new LocationClient(this, this, this);

    //We create a map
    mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
    mMap = mapFragment.getMap();
    //and enable its location
    mMap.setMyLocationEnabled(true);
    
    // We activate GCM Notifications
    activateGCM(savedInstanceState);
    
    //We create a list of parkings
    parkings = new ArrayList<ParkingMarker>();
    
    //We call to an auxiliar Async method to retrieve all the information followin the Google criteria
    RetrieveFeed task = new RetrieveFeed();
    Toast.makeText(this, "Parsing the XML file", Toast.LENGTH_SHORT).show();
       Log.d("PARSER","Ejecuto el Async");
       task.execute(URL);
 
    try {
    	Log.d("PARSER","TaskGet");
		task.get(5,TimeUnit.SECONDS);
		Log.d("PARSER","Fin del TaskGet");
		/**
		 * Adding extra options
		 */			    
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (ExecutionException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (TimeoutException e){
		e.printStackTrace();
	}
	
}

//Adapter for the List
class ParkingAdapter extends ArrayAdapter<ParkingMarker> {
	
	 Activity context;
	 List<ParkingMarker> parkings_data;

	public ParkingAdapter(Activity context, List<ParkingMarker> parkings_data) {
		super(context, R.layout.list_details, parkings_data);
		this.context = context;
		this.parkings_data=parkings_data;	
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
		
		LayoutInflater inflater = context.getLayoutInflater();
		View item = inflater.inflate(R.layout.list_details, null);
		
		//We retrieve the name of the parking
		TextView lblTit = (TextView)item.findViewById(R.id.LblTitle);
		lblTit.setText(parkings_data.get(position).getName());
		
		//We retrieve the distance separing it from us
		TextView lblDist = (TextView)item.findViewById(R.id.LblDist);
		// ¿meters or km?
		double dist_num = Math.floor(parkings_data.get(position).getDist());
		String distance;
		if (dist_num<1000)
		distance = String.valueOf(dist_num)+" m";
		else{
			dist_num=dist_num/1000;
			dist_num= Math.round(dist_num*100)/100.00;
		distance = String.valueOf(dist_num)+" km";
		}
		lblDist.setText(distance);
		
		
		//We retrieve the status of the parking
		TextView lblStat = (TextView)item.findViewById(R.id.LblStatus);
		boolean free;
		free=parkings_data.get(position).getFree();
		//Depending on the status, we change the color
		if(free){
			lblStat.setText("Free");
			//Color.rgb(8, 77, 13)
			lblStat.setTextColor(Color.rgb(8, 77, 13));
		}
		else{
			lblStat.setText("Occupied");
			lblStat.setTextColor(Color.RED);
		}
		
		return(item);
	}
	
	private float retrieveColor (int index ){
    	
    	float marker_color;
    	
    	switch(index){
    	case 0:
    		marker_color = BitmapDescriptorFactory.HUE_AZURE;
    	case 1:
    		marker_color = BitmapDescriptorFactory.HUE_BLUE;
    	case 2:
    		marker_color = BitmapDescriptorFactory.HUE_CYAN;
    	case 3:
    		marker_color = BitmapDescriptorFactory.HUE_GREEN;
    	case 4:
    		marker_color = BitmapDescriptorFactory.HUE_MAGENTA;
    	case 5:
    		marker_color = BitmapDescriptorFactory.HUE_ORANGE;
    	case 6:
    		marker_color = BitmapDescriptorFactory.HUE_RED;
    	case 7:
    		marker_color = BitmapDescriptorFactory.HUE_ROSE;
    	case 8:
    		marker_color = BitmapDescriptorFactory.HUE_VIOLET;
    	case 9:
    		marker_color = BitmapDescriptorFactory.HUE_YELLOW;
    	default:
    		marker_color = BitmapDescriptorFactory.HUE_GREEN;

    	}
    	return marker_color;
    }
}

/*
 * Called when the Activity becomes visible.
 */

@Override
protected void onStart() {
    super.onStart();
    // Connect the client.
    if(isGooglePlayServicesAvailable()){
        mLocationClient.connect();
    }

}

/*
 * Called when the Activity is no longer visible.
 */
@Override
protected void onStop() {
    // Disconnecting the client invalidates it.
    mLocationClient.disconnect();
   
    super.onStop();
}
/*
 * GCM
 * @see android.support.v4.app.FragmentActivity#onDestroy()
 */
@Override
protected void onDestroy() {
    if (mRegisterTask != null) {
        mRegisterTask.cancel(true);
    }
    unregisterReceiver(mHandleMessageReceiver);
    GCMRegistrar.onDestroy(this);
    super.onDestroy();
}
private void checkNotNull(Object reference, String name) {
    if (reference == null) {
        throw new NullPointerException(
                getString(R.string.error_config, name));
    }
}
private final BroadcastReceiver mHandleMessageReceiver =
new BroadcastReceiver() {
@Override
public void onReceive(Context context, Intent intent) {
String newMessage = intent.getExtras().getString(EXTRA_MESSAGE);
//mDisplay.append(newMessage + "\n");
}
};
/*
 * Handle results returned to the FragmentActivity
 * by Google Play services
 */
@Override
protected void onActivityResult(
                int requestCode, int resultCode, Intent data) {
    // Decide what to do based on the original request code
    switch (requestCode) {

        case CONNECTION_FAILURE_RESOLUTION_REQUEST:
            /*
             * If the result code is Activity.RESULT_OK, try
             * to connect again
             */
            switch (resultCode) {
                case Activity.RESULT_OK:
                    mLocationClient.connect();
                    break;
            }

    }
}

public void refresh(){
	  //We call to an auxiliar Async method to retrieve all the information followin the Google criteria
    RetrieveFeed task = new RetrieveFeed();
    mMap.clear();
    Location location = mLocationClient.getLastLocation();
    mLat = location.getLatitude();
    mLng = location.getLongitude();
    task.execute(URL);
  
}

private boolean isGooglePlayServicesAvailable() {
    // Check that Google Play services is available
    int resultCode =  GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
    // If Google Play services is available
    if (ConnectionResult.SUCCESS == resultCode) {
        // In debug mode, log the status
        Log.d("Location Updates", "Google Play services is available.");
        return true;
    } else {
        // Get the error dialog from Google Play services
        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog( resultCode, this, CONNECTION_FAILURE_RESOLUTION_REQUEST);

        // If Google Play services can provide an error dialog
        if (errorDialog != null) {
            // Create a new DialogFragment for the error dialog
            ErrorDialogFragment errorFragment = new ErrorDialogFragment();
            errorFragment.setDialog(errorDialog);
            errorFragment.show(getSupportFragmentManager(), "Location Updates");
        }

        return false;
    }
}

/*
 * Called by Location Services when the request to connect the
 * client finishes successfully. At this point, you can
 * request the current location or start periodic updates
 */
@Override
public void onConnected(Bundle dataBundle) {
    // Display the connection status
    Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
    Location location = mLocationClient.getLastLocation();
    mLat = location.getLatitude();
    mLng = location.getLongitude();
    LatLng latLng = new LatLng(mLat, mLng);
    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 13);
    mMap.animateCamera(cameraUpdate);
}

/*
 * Called by Location Services if the connection to the
 * location client drops because of an error.
 */
@Override
public void onDisconnected() {
    // Display the connection status
    Toast.makeText(this, "Disconnected. Please re-connect.",
            Toast.LENGTH_SHORT).show();
}

/*
 * Called by Location Services if the attempt to
 * Location Services fails.
 */
@Override
public void onConnectionFailed(ConnectionResult connectionResult) {
    /*
     * Google Play services can resolve some errors it detects.
     * If the error has a resolution, try sending an Intent to
     * start a Google Play services activity that can resolve
     * error.
     */
    if (connectionResult.hasResolution()) {
        try {
            // Start an Activity that tries to resolve the error
            connectionResult.startResolutionForResult(
                    this,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);
            /*
            * Thrown if Google Play services canceled the original
            * PendingIntent
            */
        } catch (IntentSender.SendIntentException e) {
            // Log the error
            e.printStackTrace();
        }
    } else {
       Toast.makeText(getApplicationContext(), "Sorry. Location services not available to you", Toast.LENGTH_LONG).show();
    }
}


private void activateGCM (Bundle savedInstanceState){
	
     checkNotNull(SERVER_URL, "SERVER_URL");
     checkNotNull(SENDER_ID, "SENDER_ID");
     // Make sure the device has the proper dependencies.
     GCMRegistrar.checkDevice(this);
     // Make sure the manifest was properly set - comment out this line
     // while developing the app, then uncomment it when it's ready.
     GCMRegistrar.checkManifest(this);
     registerReceiver(mHandleMessageReceiver,
             new IntentFilter(DISPLAY_MESSAGE_ACTION));
     final String regId = GCMRegistrar.getRegistrationId(this);
     if (regId.equals("")) {
         // Automatically registers application on startup.
         GCMRegistrar.register(this, SENDER_ID);
         Toast.makeText(this, "GCM Registering.",
                 Toast.LENGTH_SHORT).show();
     } else {
         // Device is already registered on GCM, check server.
         if (GCMRegistrar.isRegisteredOnServer(this)) {
        	 Toast.makeText(this, "GCM Already registered.",
                     Toast.LENGTH_SHORT).show();
             // Skips registration.
        	 // already registered
         } else {
             // Try to register again, but not in the UI thread.
             // It's also necessary to cancel the thread onDestroy(),
             // hence the use of AsyncTask instead of a raw thread.
             final Context context = this;
             mRegisterTask = new AsyncTask<Void, Void, Void>() {

                 @Override
                 protected Void doInBackground(Void... params) {
                     boolean registered =
                             ServerUtilities.register(context, regId);
                     // At this point all attempts to register with the app
                     // server failed, so we need to unregister the device
                     // from GCM - the app will try to register again when
                     // it is restarted. Note that GCM will send an
                     // unregistered callback upon completion, but
                     // GCMIntentService.onUnregistered() will ignore it.
                     if (!registered) {
                         GCMRegistrar.unregister(context);
                     }
                     return null;
                 }

                 @Override
                 protected void onPostExecute(Void result) {
                     mRegisterTask = null;
                 }

             };
             mRegisterTask.execute(null, null, null);
         }
     }
}


	
/**
 * Called from onCreate to retrieve the list of parkings
 * @author Past
 *
 */

private class RetrieveFeed extends AsyncTask<String,Integer,Boolean> {
			
		// Getting the parkings	
		protected Boolean doInBackground(String... params) {
	     	 ParkingParser parkingparser = new ParkingParser(params[0]);
	    	 parkings = parkingparser.parse();	 
	    	 
	    	 
	    	Comparator<ParkingMarker> comparatorFree =  new Comparator<ParkingMarker>(){	    		 	
	    	        public int compare(ParkingMarker o1, ParkingMarker o2) {
	    	            String p1, p2;
	    	            
	    	            int result;
	    	            
	    	            if(o1.getFree()) p1="a";
	    	        	else p1 = "b";
	    	            if(o2.getFree()) p2="a";
	    	        	else p2 = "b";
	    	        	
	    	            result =p1.compareTo(p2);
	    	           return result;
	    	         }};
	    	         
	    	         
         Comparator<ParkingMarker> comparatorDist =  new Comparator<ParkingMarker>(){	    		 	
	        
        	 @Override
        	 public int compare(ParkingMarker o1, ParkingMarker o2) {
	            
	            int p1, p2;
	            int result;
	            p1= (int)o1.getDist();
	            p2= (int)o2.getDist();
	            
	           return p2-p1;
	         }};
	 	    	          
	    	 
	    	 ComparatorChain chain = new ComparatorChain();
	    	 chain.addComparator(comparatorFree);
	    	 chain.addComparator(comparatorDist);
	    	  	   
	    		 
	    	 Collections.sort(parkings,chain);
	    	 
	        return true;
	    }
	
		// Adding markers
	    protected void onPostExecute(Boolean result) {
	    	Log.d("PARSER","Estoy en el postExecute");
	    	SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
	    	
	    	// Retrieving Filtering options
	    	
	    	FILTERING_RADIUS = sh.getBoolean("pref_radiusenabled", false);
	    	RADIUS_DIST = Integer.parseInt(sh.getString("pref_radius_dist", getString(R.string.pref_radiusdistDefault)));
	    	
	    	//Retrieving Color Preferences  	
	    	int color_free_int = Integer.parseInt(sh.getString("pref_markercolorFREE",getString(R.string.pref_markercolorFREEdefault)));
	    	int color_occupied_int =Integer.parseInt(sh.getString("pref_markercolorOCCUPIED",getString(R.string.pref_markercolorOCCUPIEDdefault)));
	    	
	    	float markercolor_free, markercolor_occupied, markercolor;
	    	markercolor_free = retrieveColor(color_free_int);
	    	markercolor_occupied = retrieveColor(color_occupied_int);
	    	
	    	
	        //Go through each item on the list
	    	Iterator<ParkingMarker> i= parkings.iterator();
	    	//for (ParkingMarker parking : parkings){
	    	while(i.hasNext()){
	    		ParkingMarker parking = i.next();
	    		
	    		aLat = parking.getLat();
	    		aLng = parking.getLng();
	    		
	    		
	    		// Calculating the distance separing it from my position
	    		double dista = distance(aLat,aLng,mLat,mLng);
	    		parking.setDist(dista);
	    		// Filtering
	    		 if(dista>RADIUS_DIST && FILTERING_RADIUS){
	    		 i.remove();
	    		 }else{	
		    		//change the color depending on the status
		    		if (parking.getFree())
				    	markercolor = markercolor_free;
			    	else{
			    		markercolor = markercolor_occupied;
		    		}
		    		//adding a new marker on the map
		        	Marker marker = mMap.addMarker(new MarkerOptions()
		            .position(new LatLng(aLat,aLng ))
		            .title(parking.getName())
		    		.icon(BitmapDescriptorFactory.defaultMarker(markercolor)));
		        	
		        	//adding event to HashMap
		        	eventMarkerMap.put(marker, parking);
	    		}
	        }
	    	
	    	//Add the list
	    	parkings_data = new ArrayList<ParkingMarker>();
	    	parkings_data = parkings;
	        adapter = new ParkingAdapter(MainActivity.this,parkings_data);
	   
	        lstOptions = (ListView)findViewById(R.id.LstParkings);
	    	lstOptions.setAdapter(adapter); 
	    	Log.d("PARSER","Fin del PostExecute");
	    	Toast.makeText(MainActivity.this, "Parkings updated!", Toast.LENGTH_SHORT).show();
	    	
	    	addListeners();
	    	
	    	
	    }
	    
	    private float retrieveColor (int index ){
	    	
	    	float marker_color = BitmapDescriptorFactory.HUE_GREEN;
	    	
	    	switch(index){
	    	case 0:
	    		marker_color = BitmapDescriptorFactory.HUE_AZURE;
	    		break;
	    	case 1:
	    		marker_color = BitmapDescriptorFactory.HUE_BLUE;
	    		break;
	    	case 2:
	    		marker_color = BitmapDescriptorFactory.HUE_CYAN;
	    		break;
	    	case 3:
	    		marker_color = BitmapDescriptorFactory.HUE_GREEN;
	    		break;
	    	case 4:
	    		marker_color = BitmapDescriptorFactory.HUE_MAGENTA;
	    		break;
	    	case 5:
	    		marker_color = BitmapDescriptorFactory.HUE_ORANGE;
	    		break;
	    	case 6:
	    		marker_color = BitmapDescriptorFactory.HUE_RED;
	    		break;
	    	case 7:
	    		marker_color = BitmapDescriptorFactory.HUE_ROSE;
	    		break;
	    	case 8:
	    		marker_color = BitmapDescriptorFactory.HUE_VIOLET;
	    		break;
	    	case 9:
	    		marker_color = BitmapDescriptorFactory.HUE_YELLOW;
	    		break;
	    	}
	    	return marker_color;
	    }
	    
	    
	    public double distance(double lat1, double lng1, double lat2, double lng2){
	    	
	    	double distance=0;
	    	
	    	//Converting to radians
	    	lat1= Math.toRadians(lat1);
	    	lat2= Math.toRadians(lat2);
	    	lng1 = Math.toRadians(lng1);
	    	lng2 = Math.toRadians(lng2);
	    	
	    	double x = (lng2-lng1)*Math.cos((lat1+lat2)/2);
	    	double y = (lat2-lat1);
	    	
	    	distance = Math.sqrt(x*x + y*y)*EARTH_RADIUS;
	    	return distance;
	    }
	    
	    
	    
	    
	    private void addListeners(){
	    	// Preparing error message
			Context context = getApplicationContext();
			CharSequence text = "This parking is full!";
			int duration = Toast.LENGTH_SHORT;

			final Toast toast = Toast.makeText(context, text, duration);
	    	
	    	
	    	// OnclikListener for the List 
			lstOptions.setOnItemClickListener(new OnItemClickListener() {
			    @Override
			    public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
			    	
			    	//Obtaining the status
			    	boolean status_selected=((ParkingMarker)adapter.getAdapter().getItem(position)).getFree();
			    	
			    	if(status_selected){
			    	//Launch Navigation
			    	Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" +parkings.get(position).getLat()+","+parkings.get(position).getLng()));
		        	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		        	startActivity(intent);
			    	}else{
			    		toast.show();
			    	}
			    }
			});
		    
			//OnclickListener for the markers
		    mMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
		        @Override
		        public void onInfoWindowClick(Marker marker) {
		        	//Retrieve the parkingMarker object associated
		        	ParkingMarker parking_info=eventMarkerMap.get(marker);
		        	
		        	if(parking_info.getFree()){
		        	//Launch Navigation
		        	Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" +parking_info.getLat()+","+parking_info.getLng()));
		        	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		        	startActivity(intent);
		        	}
		        	else{toast.show();}
		        }
		    });
	    }
	    
}// End of retrieve

} // End of main
