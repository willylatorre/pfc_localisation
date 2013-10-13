package com.example.pfc_alpha1;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

//Map Variables
private SupportMapFragment mapFragment;
private GoogleMap mMap;
private double mLat, mLng;
private LocationClient mLocationClient;
boolean finished_markers = false;

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

private ListView lstOptions;
private List<ParkingMarker> parkings;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    
    //The Location client
    mLocationClient = new LocationClient(this, this, this);

    //We create a map
    mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
    mMap = mapFragment.getMap();
    //and enable its location
    mMap.setMyLocationEnabled(true);
    
    //We create a list of parkings
    parkings = new ArrayList<ParkingMarker>();
    
    //We call to an auxiliar Async method to retrieve all the information followin the Google criteria
    RetrieveFeed task = new RetrieveFeed();
    task.execute("https://dl.dropboxusercontent.com/u/123539/parkingpositions.xml");
    
    //We have to wait some time in order that all the parkings in the list are retrieved
    try {
		task.get(2500, TimeUnit.MILLISECONDS);
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (ExecutionException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (TimeoutException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

  //Creating ListView
    List<ParkingMarker> parkings_data = new ArrayList<ParkingMarker>();
    parkings_data = parkings;
    ParkingAdapter adapter = new ParkingAdapter(this,parkings_data);
   
    lstOptions = (ListView)findViewById(R.id.LstParkings);
	lstOptions.setAdapter(adapter); 
	
	/**
	 * Adding extra options
	 */
	
	// OnclikListener for the List 
	lstOptions.setOnItemClickListener(new OnItemClickListener() {
	    @Override
	    public void onItemClick(AdapterView<?> a, View v, int position, long id) {
	    	//Launch Navigation
	    	Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" +parkings.get(position).getLat()+","+parkings.get(position).getLng()));
        	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        	startActivity(intent);
	    }
	});
    
    
	//OnclickListener for the markers
    mMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
        @Override
        public void onInfoWindowClick(Marker marker) {
        	//Launch Navigation
        	Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" +mLat+","+mLng));
        	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        	startActivity(intent);
        }
    });
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
		LayoutInflater inflater = context.getLayoutInflater();
		View item = inflater.inflate(R.layout.list_details, null);
		
		//We retrieve the name of the parking
		TextView lblTit = (TextView)item.findViewById(R.id.LblTitle);
		lblTit.setText(parkings_data.get(position).getName());
		//We retrieve the status of the parking
		TextView lblStat = (TextView)item.findViewById(R.id.LblStatus);
		boolean free;
		free=parkings_data.get(position).getFree();
		//Depending on the status, we change the color
		if(free){
			lblStat.setText("Free");
			//Color.rgb(8, 77, 13)
			lblStat.setTextColor(Color.GREEN);
		}
		else{
			lblStat.setText("Occupied");
			lblStat.setTextColor(Color.RED);
		}

		return(item);
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
    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 12);
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
	        return true;
	    }
	
		// Adding markers
	    protected void onPostExecute(Boolean result) {
	    	float markercolor;
	    	
	        //Go through each item on the list
	    	for (ParkingMarker parking : parkings){
	    		mLat = parking.getLat();
	    		mLng = parking.getLng();
	    		
	    		//change the color depending on the status
	    		if (parking.getFree())
	    		markercolor = BitmapDescriptorFactory.HUE_GREEN;
	    		else
	    			markercolor = BitmapDescriptorFactory.HUE_RED;
	        	
	    		//adding a new marker on the map
	        	mMap.addMarker(new MarkerOptions()
	            .position(new LatLng(mLat,mLng ))
	            .title(parking.getName())
	    		.icon(BitmapDescriptorFactory.defaultMarker(markercolor)));
	        }
	    }
}// End of retrieve

} // End of main
