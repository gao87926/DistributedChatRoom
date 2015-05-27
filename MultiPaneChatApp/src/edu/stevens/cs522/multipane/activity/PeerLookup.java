package edu.stevens.cs522.multipane.activity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import edu.stevens.cs522.multipane.R;
import edu.stevens.cs522.multipane.R.id;
import edu.stevens.cs522.multipane.R.layout;
import edu.stevens.cs522.multipane.R.menu;
import edu.stevens.cs522.multipane.database.DbAdapter;
import edu.stevens.cs522.multipane.entity.Peer;
import edu.stevens.cs522.multipane.helper.ServiceHelper;
import edu.stevens.cs522.multipane.provider.MessageProviderCloud;
import edu.stevens.cs522.multipane.request.Unregister;

import android.support.v4.app.Fragment;
import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.os.Build;

public class PeerLookup extends ListActivity {
	private SimpleCursorAdapter dbAdapter;
	private DbAdapter chatDbAdapter;
	private int position;
	public ArrayList<Peer> peersList;
	public String mAddress;
	public ArrayList<Location> mLocationArry;
	private static final String PROVIDER = "flp";
	private static final double LAT = 37.377166;
	private static final double LNG = -122.086966;
	private static final float ACCURACY = 3.0f;
	String lat;
	String longt;
	List<String> addressesBook;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		peersList = new ArrayList<Peer>();
		mLocationArry= new ArrayList<Location>();
		addressesBook = new ArrayList<String>();
		setContentView(R.layout.activity_peer_lookup);
		ListView myListView = (ListView) findViewById(android.R.id.list);
		registerForContextMenu(myListView);
		chatDbAdapter = new DbAdapter(this);
		chatDbAdapter.open();
		ContentResolver cr = getContentResolver();
		Cursor c = cr.query(MessageProviderCloud.CONTENT_URI_PEER, null,
				"peers.street=?", new String[] { "NA" }, null);

		Peer peer = new Peer();
		if (c.moveToFirst()) {
			do {
				peer = new Peer(c);
				
				peersList.add(peer);
				Log.d("need update a street","+++++++");

			} while (c.moveToNext());
		}
		c.close();
		getAddress(peersList);
		fillData();

	}

	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle("Peer Detail");
		menu.add(0, v.getId(), 0, "Show Messages");
		menu.add(0, v.getId(), 0, "Unregister");
		menu.add(0, v.getId(), 0, "Cancel");
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		if (item.getTitle() == "Show Messages") {
			try {
				// fillDataMsg(info.id);
				fillData();
			} catch (Exception e) {

			}
			// Log.d("Delelt been click:", String.valueOf(info.id));

			return true;
		} else if (item.getTitle().equals("Unregister")) {
			SharedPreferences prefs = this.getSharedPreferences(
					"multipanechatapp", Context.MODE_PRIVATE);
			String clientName = prefs.getString("clientName", "myself");
			String portNo = prefs.getString("portNo", "8080");
			String hostStr = prefs.getString("hostStr", "localhost");
			String uuid = prefs.getString("clientUUID", UUID.randomUUID()
					.toString());
			Unregister unrigister = new Unregister(info.id,
					UUID.fromString(uuid), "http://" + hostStr + ":" + portNo);
			 lat = prefs.getString("latitude", "40.7500");
			 longt = prefs.getString("longitude", "-74.0300");
					

			ServiceHelper.getInstance(this).unregister(unrigister);

			return true;
		} else {
			return super.onContextItemSelected(item);
		}

	}

	// private void fillDataMsg(long id) {
	// // TODO Auto-generated method stub
	// String name;
	// name = chatDbAdapter.getNameById(id);
	// Log.d("search peer", name);
	// Cursor c = chatDbAdapter.getMessgeByPeer(name);
	// startManagingCursor(c);
	//
	// // For the list adapter that will exchange data with the list view, we
	// // need
	// // to specify which layout object to bind to which data object.
	//
	// String[] from = new String[] {"text", "sender" };
	// int[] to = new int[] { android.R.id.text1, android.R.id.text2 };
	//
	// // Now create a list adaptor that encapsulates the result of a DB query
	// dbAdapter = new SimpleCursorAdapter(this, // Context.
	// android.R.layout.simple_list_item_2, // Specify the row template
	// // to use
	// c, // Cursor encapsulates the DB query result.
	// from, // Array of cursor columns to bind to.
	// to); // Parallel array of which layout objects to bind to those
	// // columns.
	//
	// // Bind to our new adapter.
	//
	// setListAdapter(dbAdapter);
	// setSelection(0);
	// }

	@SuppressWarnings("deprecation")
	private void fillData() {
		// TODO Auto-generated method stub
		Cursor c = chatDbAdapter.getAllPeer();
		startManagingCursor(c);

		// For the list adapter that will exchange data with the list view, we
		// need
		// to specify which layout object to bind to which data object.

		String[] from = new String[] { "name", "street" };
		int[] to = new int[] { android.R.id.text1, android.R.id.text2 };

		// Now create a list adaptor that encapsulates the result of a DB query
		dbAdapter = new SimpleCursorAdapter(this, // Context.
				android.R.layout.simple_list_item_2, // Specify the row template
														// to use
				c, // Cursor encapsulates the DB query result.
				from, // Array of cursor columns to bind to.
				to); // Parallel array of which layout objects to bind to those
						// columns.

		// Bind to our new adapter.

		setListAdapter(dbAdapter);
		setSelection(0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.peer_lookup, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case (R.id.open_map):
			Intent i = getPackageManager().getLaunchIntentForPackage("edu.stevens.cs522.chatappmap");
		i.putExtra("curLat", lat);
		i.putExtra("curLongt", longt);
		startActivity(i);
		}
		return false;
	}
	@SuppressLint("NewApi")
	public void getAddress(List<Peer> peers) {
		// Ensure that a Geocoder services is available
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD
				&& Geocoder.isPresent()) {
			// Show the activity indicator
			// mActivityIndicator.setVisibility(View.VISIBLE);
			/*
			 * Reverse geocoding is long-running and synchronous. Run it on a
			 * background thread. Pass the current location to the background
			 * task. When the task finishes,
			 * 
			 * onPostExecute() displays the address.
			 */
			
			for(Peer p: peers){
				
			Log.d("Getting address from peer", p.name+Double.toString(p.latitude)+Double.toString(p.longitude));
			Location mLocation = new Location(PROVIDER);
			mLocation.setLatitude(p.latitude);
			mLocation.setLongitude(p.longitude);
			mLocation.setAccuracy(ACCURACY);
			
			mLocationArry.add(mLocation);
			}
			(new GetAddressTask(this)).execute(mLocationArry);
		}
	}

	private class GetAddressTask extends
			AsyncTask<ArrayList<Location>, Void, List<String>> {
		Context mContext;

		public GetAddressTask(Context context) {
			super();
			mContext = context;
		}

		/**
		 * A method that's called once doInBackground() completes. Turn off the
		 * indeterminate activity indicator and set the text of the UI element
		 * that shows the address. If the lookup failed, display the error
		 * message.
		 */
		@Override
		protected void onPostExecute(List<String> adBook) {
			// Set activity indicator visibility to "gone"
			// mActivityIndicator.setVisibility(View.GONE);
			// Display the results of the lookup.
			
			ContentResolver cr = getContentResolver();
			ContentValues value;
			Peer p1;
			for(int i=0;i<peersList.size();i++){
				Log.d("Size of addressBook", Integer.toString(adBook.size()));
				Log.d("Size of Big addressBook", Integer.toString(addressesBook.size()));
				p1 = new Peer();
						p1.name = peersList.get(i).name;
						p1.id= peersList.get(i).id;
						p1.latitude = peersList.get(i).latitude;
						p1.longitude = peersList.get(i).longitude;
						//p1.street = peersList.get(i).street;
						p1.street = addressesBook.get(i);
				Log.d("looking for peersList",Long.toString(p1.id)+ p1.name +p1.street);
			
				value = new ContentValues();
				p1.writeToProvider(value);
				value.put("street", p1.street);
				value.put("senderlatitude", Double.toString(p1.latitude));
				value.put("senderlongitude", Double.toString(p1.longitude));
				value.put("name",p1.name);
				value.put("street", p1.street);
				value.put("_id", p1.id);				
				Log.d("Updated number of peer:", Integer.toString(cr.update(Uri.parse(MessageProviderCloud.CONTENT_URI_PEER.toString()+"/" + p1.id), value, null, null)));
			}
		
			fillData();
		}

		/**
		 * Get a Geocoder instance, get the latitude and longitude look up the
		 * address, and return it
		 * 
		 * @params params One or more Location objects
		 * @return A string containing the address of the current location, or
		 *         an empty string if no address can be found, or an error
		 *         message
		 */
		@Override
		protected List<String> doInBackground(ArrayList<Location>... params) {
			Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
			// Get the current location from the input parameter list
			addressesBook = new ArrayList<String>();
			for (Location L : params[0]) {

				Location loc = L;
				// Create a list to contain the result address
				List<Address> addresses = null;
				try {
					/*
					 * Return 1 address.
					 */
					addresses = geocoder.getFromLocation(loc.getLatitude(),
							loc.getLongitude(), 1);
				} catch (IOException e1) {
					Log.e("LocationSampleActivity","IO Exception in getFromLocation()");
					e1.printStackTrace();
					return null;
				} catch (IllegalArgumentException e2) {
					// Error message to post in the log
					String errorString = "Illegal arguments "
							+ Double.toString(loc.getLatitude()) + " , "
							+ Double.toString(loc.getLongitude())
							+ " passed to address service";
					Log.e("LocationSampleActivity", errorString);
					e2.printStackTrace();
					return null;
				}
				// If the reverse geocode returned an address
				if (addresses != null && addresses.size() > 0) {
					// Get the first address
					Log.d("Getting data from geocode", addresses.toString());
					Address address = addresses.get(0);
					/*
					 * Format the first line of address (if available), city,
					 * and country name.
					 */
					String addressText = String.format("%s, %s, %s",address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
							// Locality is usually a city
							address.getLocality(),
							// The country of the address
							address.getCountryName());
					Log.d("final address", addressText);
					addressesBook.add(addressText);
				} else {
					return null;
				}
			}
			return addressesBook;
		}

	}

}
