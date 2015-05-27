package edu.stevens.cs522.multipane.activity;

import java.util.UUID;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.SupportMapFragment;

import edu.stevens.cs522.multipane.R;
import edu.stevens.cs522.multipane.helper.ServiceHelper;
import edu.stevens.cs522.multipane.provider.MessageProviderCloud;
//import edu.stevens.cs522.helper.ServiceHelper;
import edu.stevens.cs522.multipane.request.Register;
import edu.stevens.cs522.multipane.request.RequestProcessor;
import edu.stevens.cs522.multipane.service.AlarmReceiver;
import edu.stevens.cs522.multipane.service.RequestService;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class SetHead extends Activity implements ConnectionCallbacks,
		OnConnectionFailedListener, LocationListener {
	public static final String NAME = "name";
	public static final String PORT = "port";
	public static final String HOST = "host";
	public static final String PREFS_NAME = "multipanechatapp";
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	// Milliseconds per second
	private static final int MILLISECONDS_PER_SECOND = 1000;
	// Update frequency in seconds
	public static final int UPDATE_INTERVAL_IN_SECONDS = 4;
	// Update frequency in milliseconds
	private static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND
			* UPDATE_INTERVAL_IN_SECONDS;
	// The fastest update frequency, in seconds
	private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
	// A fast frequency ceiling in milliseconds
	private static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND
			* FASTEST_INTERVAL_IN_SECONDS;
	// Define an object that holds accuracy and frequency parameters

	LocationRequest mLocationRequest;
	AckReceiver receiver;
	String clientName, portNo, hostStr, uuid;
	String latitude;
	String longitude;
	int ipt;
	Context mContext;
	AlarmManager alarmManager;
	SharedPreferences mPrefs;
	SharedPreferences.Editor mEditor;

	LocationClient mLocationClient;
	boolean mUpdatesRequested;

	final static public String TAG = SetHead.class.getCanonicalName();
	ContentResolver cr;
	

    private TextView mLatLng;
    private TextView mConnectionState;
    private TextView mConnectionStatus;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("onCreate","test");
		try {
			cr.delete(MessageProviderCloud.CONTENT_URI, null, null);
			cr.delete(MessageProviderCloud.CONTENT_URI_PEER, null, null);
			cr.delete(MessageProviderCloud.CONTENT_URI_CHATROOM, null, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		latitude="NA";
		longitude = "NA";
		setContentView(R.layout.activity_set_head);
		mLatLng = (TextView) findViewById(R.id.lat_lng);
		mLatLng.setText(latitude +"|"+longitude);
        mConnectionState = (TextView) findViewById(R.id.text_connection_state);
        mConnectionStatus = (TextView) findViewById(R.id.text_connection_status);
		receiver = new AckReceiver(new Handler());
		mContext = this.getApplicationContext();
		SharedPreferences prfs = getSharedPreferences(PREFS_NAME, 0);
		uuid = UUID.randomUUID().toString();
		SharedPreferences.Editor editor = prfs.edit();
		editor.putString("clientUUID", uuid);
		editor.commit();
		Intent intentAlarm = new Intent(this, AlarmReceiver.class);
		alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		PendingIntent sender = PendingIntent.getBroadcast(this, 12,
				intentAlarm, 0);
		try {
			alarmManager.cancel(sender);
		} catch (Exception e) {
			Log.e(TAG, "AlarmManager update was not canceled. " + e.toString());
		}
		// Create the LocationRequest object
		mLocationRequest = LocationRequest.create();
		// Use high accuracy
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		// Set the update interval to 5 seconds
		mLocationRequest.setInterval(UPDATE_INTERVAL);
		// Set the fastest update interval to 1 second
		mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

		// Open the shared preferences
		mPrefs = getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
		// Get a SharedPreferences editor
		mEditor = mPrefs.edit();
		/*
		 * Create a new location client, using the enclosing class to handle
		 * callbacks.
		 */
		mLocationClient = new LocationClient(this, this, this);
		// Start with updates turned off
		mUpdatesRequested = false;

	}

	@Override
	protected void onPause() {
		// Save the current setting for updates
		mEditor.putBoolean("KEY_UPDATES_ON", mUpdatesRequested);
		mEditor.commit();
		super.onPause();
	}

	@Override
	protected void onResume() {
		/*
		 * Get any previous setting for location updates Gets "false" if an
		 * error occurs
		 */
		if (mPrefs.contains("KEY_UPDATES_ON")) {
			mUpdatesRequested = mPrefs.getBoolean("KEY_UPDATES_ON", false);

			// Otherwise, turn off location updates
		} else {
			mEditor.putBoolean("KEY_UPDATES_ON", false);
			mEditor.commit();
		}
		super.onResume();
	}

	@Override
	protected void onStart() {
		super.onStart();
		mLocationClient.connect();
		
		//  mLocationClient.requestLocationUpdates(mLocationRequest, this);
		 // getLocation();
		// mLocationClient.requestLocationUpdates(request, listener)
	}

	@Override
	public void onStop() {
		 // If the client is connected
        if (mLocationClient.isConnected()) {
            /*
             * Remove location updates for a listener.
             * The current Activity is the listener, so
             * the argument is "this".
             */
        	 stopPeriodicUpdates();
        	   
        }
        /*
         * After disconnect() is called, the client is
         * considered "dead".
         */
        stopUpdates();
        mLocationClient.disconnect();
        
     
        super.onStop();
	}
//    public void getLocation() {
//
//        // If Google Play Services is available
//        if (servicesConnected()) {
//
//            // Get the current location
//            Location currentLocation = mLocationClient.getLastLocation();
//
//            // Display the current location in the UI
//            mLatLng.setText(LocationUtils.getLatLng(this, currentLocation));
//        }
//    }
	// private void setUpMapIfNeeded() {
	// // Do a null check to confirm that we have not already instantiated the
	// map.
	// if (mMap == null) {
	// // Try to obtain the map from the SupportMapFragment.
	// mMap = ((SupportMapFragment)
	// getSupportFragmentManager().findFragmentById(R.id.map))
	// .getMap();
	// // Check if we were successful in obtaining the map.
	// if (mMap != null) {
	// mMap.setMyLocationEnabled(true);
	// mMap.setOnMyLocationButtonClickListener(this);
	// }
	// }
	// }

	/*
	 * Handle results returned to the FragmentActivity by Google Play services
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Decide what to do based on the original request code
		switch (requestCode) {

		case CONNECTION_FAILURE_RESOLUTION_REQUEST:
			/*
			 * If the result code is Activity.RESULT_OK, try to connect again
			 */
			switch (resultCode) {
			case Activity.RESULT_OK:

				/*
				 * Try the request again
				 */

				break;
			}

		}

	}

	private boolean servicesConnected() {
		// Check that Google Play services is available
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		// If Google Play services is available
		if (ConnectionResult.SUCCESS == resultCode) {
			// In debug mode, log the status
			Log.d("Activity Recognition", "Google Play services is available.");
			// Continue
			return true;
			// Google Play services was not available for some reason
		} else {
			// Get the error dialog from Google Play services
			Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
					resultCode, this, CONNECTION_FAILURE_RESOLUTION_REQUEST);

			// If Google Play services can provide an error dialog
			if (errorDialog != null) {
				// Create a new DialogFragment for the error dialog
				ErrorDialogFragment errorFragment = new ErrorDialogFragment();
				// Set the dialog in the DialogFragment
				errorFragment.setDialog(errorDialog);
				// Show the error dialog in the DialogFragment
				errorFragment.show(getFragmentManager(),
						"Activity Recognition");
			}
			return false;
		}
	}

	public class AckReceiver extends ResultReceiver {
		public AckReceiver(Handler handler) {
			super(handler);
			// TODO Auto-generated constructor stub
		}

		protected void onReceiveResult(int resultCode, Bundle result) {
			switch (resultCode) {
			case 1:
				String client = result.getString("clientId");
				if (client != null) {
					Log.d("OnReceiveResult id:", client);
				}

				// Toast.makeText(context, "success register " +
				// result.getString("clientId"),Toast.LENGTH_SHORT).show();
				Intent i = new Intent(mContext, FragmentLayout.class);
				i.putExtra(NAME, clientName);
				i.putExtra(PORT, portNo);
				i.putExtra(HOST, hostStr);
				i.putExtra("clientId", client);
				startActivity(i);
				break;
			case 0:
				Toast.makeText(mContext, "Login Fail, Can't find server.",
						Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}

		}
	};

	// the function will be called when the start chat button is clicked
	public void start_chat(View view) {
		EditText username = (EditText) findViewById(R.id.name_field);
		EditText port = (EditText) findViewById(R.id.port_text);
		EditText host = (EditText) findViewById(R.id.dest_text);
		clientName = username.getText().toString();
		portNo = port.getText().toString();
		hostStr = host.getText().toString();

		if (username.getText().toString().matches("")
				|| port.getText().toString().matches("")
				|| host.getText().toString().matches("")) {
			Toast.makeText(this, "Please fill in every filed",
					Toast.LENGTH_SHORT).show();
		} else {

			Register register = new Register(0, UUID.fromString(uuid),
					clientName, "http://" + hostStr + ":" + portNo, Double.parseDouble(latitude), Double.parseDouble(longitude));
			SharedPreferences prefs = this.getSharedPreferences(
					"multipanechatapp", Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString("clientName", register.username);
			editor.putString("portNo", portNo);
			editor.putString("hostStr", hostStr);
			editor.putString("latitude", latitude);
			editor.putString("longitude", longitude);
			editor.commit();
			ServiceHelper.getInstance(this).register(register, receiver);
		}

	}

	// public static boolean checkPlayServices(Activity context) {
	// int resultCode =
	// GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
	// if (resultCode != ConnectionResult.SUCCESS) {
	// if (GooglePlayServicesUtil
	// .isUserRecoverableError(resultCode)) {
	// GooglePlayServicesUtil.getErrorDialog(resultCode,
	// context,PLAY_SERVICES_RESOLUTION_REQUEST).show();
	// } else {
	// Log.e(TAG, "This device is not	supported.");
	// context.finish();
	// }
	// return false;
	// }
	// return true;
	// }

	/**
	 * Button to get current Location. This demonstrates how to get the current
	 * Location as required without needing to register a LocationListener.
	 */
	public void showMyLocation(View view) {
		if (mLocationClient != null && mLocationClient.isConnected()) {
			String msg = "Location = " + mLocationClient.getLastLocation();
			Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT)
					.show();
		}
	}

	/*
	 * Called by Location Services when the request to connect the client
	 * finishes successfully. At this point, you can request the current
	 * location or start periodic updates
	 */
	@Override
	public void onConnected(Bundle dataBundle) {
		   // Display the connection status
		   mConnectionStatus.setText("connected");
	        Toast.makeText(this, "Google play service Connected", Toast.LENGTH_SHORT).show();
	        // If already requested, start periodic updates
	       // if (mUpdatesRequested) {
	        	startPeriodicUpdates();
	      //  }

//			latitude = Double.toString(mLocationClient.getLatitude());
//			longitude = Double.toString(location.getLongitude());
	        
	   
	}

	/*
	 * Called by Location Services if the connection to the location client
	 * drops because of an error.
	 */
	@Override
	public void onDisconnected() {
		// Display the connection status
		  mConnectionStatus.setText("disconnected");
		Toast.makeText(this,
				"Google play service Disconnected. Please re-connect.",
				Toast.LENGTH_SHORT).show();
	}

	/**
	 * Implementation of {@link LocationListener}.
	 */
	/*
	 * Called by Location Services if the attempt to Location Services fails.
	 */
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		/*
		 * Google Play services can resolve some errors it detects. If the error
		 * has a resolution, try sending an Intent to start a Google Play
		 * services activity that can resolve error.
		 */
		if (connectionResult.hasResolution()) {
			try {
				// Start an Activity that tries to resolve the error
				connectionResult.startResolutionForResult(this,
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
			/*
			 * If no resolution is available, display a dialog to the user with
			 * the error.
			 */

			  showErrorDialog(connectionResult.getErrorCode());
			
		}
	}

	// Define the callback method that receives location updates
	@Override
	public void onLocationChanged(Location location) {
		// mMessageView.setText("Location = " + location);
		// Report to the UI that the location was updated
		 mConnectionStatus.setText("location_updated");
		 Log.d("onLocationChanged","-----");
		String msg = "Updated Location: "
				+ Double.toString(location.getLatitude()) + ","
				+ Double.toString(location.getLongitude());
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
		this.latitude = Double.toString(location.getLatitude());
		this.longitude = Double.toString(location.getLongitude());
		mLatLng.setText(latitude+"|"+longitude);
	}
    public void startUpdates() {
        mUpdatesRequested = true;

        if (servicesConnected()) {
            startPeriodicUpdates();
        }
    }

    public void stopUpdates() {
        mUpdatesRequested = false;

        if (servicesConnected()) {
            stopPeriodicUpdates();
        }
    }
    /**
     * In response to a request to start updates, send a request
     * to Location Services
     */
    private void startPeriodicUpdates() {
    	Log.d("Start periodic updates", "------");
        mLocationClient.requestLocationUpdates(mLocationRequest, this);
        mConnectionState.setText("location_requested");
    }

    /**
     * In response to a request to stop updates, send a request to
     * Location Services
     */
    private void stopPeriodicUpdates() {
        mLocationClient.removeLocationUpdates(this);
        mConnectionState.setText("location_updates_stopped");
    }

	   /**
     * Show a dialog returned by Google Play services for the
     * connection error code
     *
     * @param errorCode An error code returned from onConnectionFailed
     */
    private void showErrorDialog(int errorCode) {

        // Get the error dialog from Google Play services
        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
            errorCode,
            this,
            CONNECTION_FAILURE_RESOLUTION_REQUEST);

        // If Google Play services can provide an error dialog
        if (errorDialog != null) {

            // Create a new DialogFragment in which to show the error dialog
            ErrorDialogFragment errorFragment = new ErrorDialogFragment();

            // Set the dialog in the DialogFragment
            errorFragment.setDialog(errorDialog);

            // Show the error dialog in the DialogFragment
            errorFragment.show(getFragmentManager(), LocationUtils.APPTAG);
        }
    }
    /**
     * Define a DialogFragment to display the error dialog generated in
     * showErrorDialog.
     */
    public static class ErrorDialogFragment extends DialogFragment {

        // Global field to contain the error dialog
        private Dialog mDialog;

        /**
         * Default constructor. Sets the dialog field to null
         */
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        /**
         * Set the dialog to display
         *
         * @param dialog An error dialog
         */
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        /*
         * This method must return a Dialog to the DialogFragment.
         */
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }
}
