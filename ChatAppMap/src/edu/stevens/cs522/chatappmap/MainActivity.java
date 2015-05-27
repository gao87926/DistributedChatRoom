package edu.stevens.cs522.chatappmap;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import edu.stevens.cs522.chatappmap.entity.Peer;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.os.Build;

public class MainActivity extends FragmentActivity implements
		LoaderManager.LoaderCallbacks<Cursor>,
		 OnMarkerClickListener,
		 OnMarkerDragListener
	        {
	TextView resultView = null;
	CursorLoader cursorLoader;
	Vector<Peer> peers;
	public LatLng crrent;

	/** Demonstrates customizing the info window and/or its contents. */
	class CustomInfoWindowAdapter implements InfoWindowAdapter {
		//private final RadioGroup mOptions;

		// These a both viewgroups containing an ImageView with id "badge" and
		// two TextViews with id
		// "title" and "snippet".
		private final View mWindow;
		private final View mContents;

		CustomInfoWindowAdapter() {
			mWindow = getLayoutInflater().inflate(R.layout.custom_info_window,
					null);
			mContents = getLayoutInflater().inflate(
					R.layout.custom_info_contents, null);
		//	mOptions = (RadioGroup) findViewById(R.id.custom_info_window_options);
		}

		@Override
		public View getInfoWindow(Marker marker) {
//			if (mOptions.getCheckedRadioButtonId() != R.id.custom_info_window) {
//				// This means that getInfoContents will be called.
//				return null;
//			}
			render(marker, mWindow);
			return mWindow;
		}

		@Override
		public View getInfoContents(Marker marker) {
//			if (mOptions.getCheckedRadioButtonId() != R.id.custom_info_contents) {
//				// This means that the default info contents will be used.
//				return null;
//			}
			render(marker, mContents);
			return mContents;
		}

		private void render(Marker marker, View view) {
			int badge;
			// Use the equals() method on a Marker to check for equals. Do not
			// use ==.
			
				badge = R.drawable.badge_sa;
			
			((ImageView) view.findViewById(R.id.badge)).setImageResource(badge);

			String title = marker.getTitle();
			TextView titleUi = ((TextView) view.findViewById(R.id.title));
			if (title != null) {
				// Spannable string allows us to edit the formatting of the
				// text.
				SpannableString titleText = new SpannableString(title);
				titleText.setSpan(new ForegroundColorSpan(Color.RED), 0,
						titleText.length(), 0);
				titleUi.setText(titleText);
			} else {
				titleUi.setText("");
			}

			String snippet = marker.getSnippet();
			TextView snippetUi = ((TextView) view.findViewById(R.id.snippet));
			if (snippet != null && snippet.length() > 12) {
				SpannableString snippetText = new SpannableString(snippet);
				snippetText.setSpan(new ForegroundColorSpan(Color.MAGENTA), 0,
						10, 0);
				snippetText.setSpan(new ForegroundColorSpan(Color.BLUE), 12,
						snippet.length(), 0);
				snippetUi.setText(snippetText);
			} else {
				snippetUi.setText("");
			}
		}
	}
    private GoogleMap mMap;

    private Marker mPerth;
    private Marker mSydney;
    private Marker mBrisbane;
    private Marker mAdelaide;
    private Marker mMelbourne;
    private final List<Marker> mMarkerList = new ArrayList<Marker>();
    private final Random mRandom = new Random();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		 setContentView(R.layout.marker_demo);
		 Bundle bundle = this.getIntent().getExtras();
		 String lat=null;
		 String longt=null;
		 if(bundle!=null){
			 lat = bundle.getString("curLat");
		longt = bundle.getString("curLongt");
		 }
		
		 if (lat==null||longt==null){
			 crrent = new LatLng(40.7500, -74.0320);
		 }else{
		 crrent = new LatLng(Double.parseDouble(lat), Double.parseDouble(longt));
		 }
		peers = new Vector<>();
		getSupportLoaderManager().initLoader(1, null, this);
		
	}

	@Override
	    protected void onResume() {
	        super.onResume();
	        setUpMapIfNeeded();
	    }
	 private void setUpMapIfNeeded() {
	        // Do a null check to confirm that we have not already instantiated the map.
	        if (mMap == null) {
	               mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
	                    .getMap();
	           	            if (mMap != null) {
	                setUpMap();
	            }
	        }
	    }
	 private void setUpMap() {
	
	        addMarkersToMap();

	        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());

	        mMap.setOnMarkerClickListener(this);

	        final View mapView = getSupportFragmentManager().findFragmentById(R.id.map).getView();
	        if (mapView.getViewTreeObserver().isAlive()) {
	            mapView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
	                @SuppressWarnings("deprecation") // We use the new method when supported
	                @SuppressLint("NewApi") // We check which build version we are using.
	                @Override
	                public void onGlobalLayout() {
	                	LatLngBounds.Builder builder  = new LatLngBounds.Builder();
	                	for(Marker m : mMarkerList){
	                		builder.include(m.getPosition());
	                		Log.d("Building LatLngBounds", m.getPosition().toString());
	                	}
	                          
	                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
	                      mapView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
	                    } else {
	                      mapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
	                    }
	                    try{
	                    	LatLngBounds bounds = builder.build();
	                    	  mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
	                    }catch(Exception e){
	                    	Log.d("Error for creat bounds","===");
	                    	  mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(crrent , 14.0f));
	                    }
	                  
	                    mMap.setMyLocationEnabled(true);
	                   mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
	                   mMap.getUiSettings().setCompassEnabled(false);
	                   mMap.getUiSettings().setRotateGesturesEnabled(false);
	                  // mMap.getUiSettings().setScrollGesturesEnabled(false);
	                   mMap.getUiSettings().setZoomGesturesEnabled(false);
	                    mMap.getUiSettings().setZoomControlsEnabled(true);
	                }
	            });
	        }
	    }
	 private void addMarkersToMap() {
	        // Uses a colored icon.
		 for(Peer p : peers){
			 
			 mMarkerList.add(mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(p.latitude, p.longitude))
	                    .title(p.name)
	                    .snippet(p.street)
	                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
	                   ));
			 Log.d("Adding marker", p.name);
		 }
	    }
	 private boolean checkReady() {
	        if (mMap == null) {
	            Toast.makeText(this, "map_not_ready", Toast.LENGTH_SHORT).show();
	            return false;
	        }
	        return true;
	    }

	    /** Called when the Clear button is clicked. */
	    public void onClearMap(View view) {
	        if (!checkReady()) {
	            return;
	        }
	        mMap.clear();
	    }

	    /** Called when the Reset button is clicked. */
	    public void onResetMap(View view) {
	        if (!checkReady()) {
	            return;
	        }
	        // Clear the map because we don't want duplicates of the markers.
	        mMap.clear();
	        addMarkersToMap();
	    }
	    @Override
	    public boolean onMarkerClick(final Marker marker) {
	        if (marker.equals(mPerth)) {
	            // This causes the marker at Perth to bounce into position when it is clicked.
	            final Handler handler = new Handler();
	            final long start = SystemClock.uptimeMillis();
	            final long duration = 1500;

	            final Interpolator interpolator = new BounceInterpolator();

	            handler.post(new Runnable() {
	                @Override
	                public void run() {
	                    long elapsed = SystemClock.uptimeMillis() - start;
	                    float t = Math.max(1 - interpolator
	                            .getInterpolation((float) elapsed / duration), 0);
	                    marker.setAnchor(0.5f, 1.0f + 2 * t);

	                    if (t > 0.0) {
	                        // Post again 16ms later.
	                        handler.postDelayed(this, 16);
	                    }
	                }
	            });
	        } else if (marker.equals(mAdelaide)) {
	            marker.setIcon(BitmapDescriptorFactory.defaultMarker(mRandom.nextFloat() * 360));
	            marker.setAlpha(mRandom.nextFloat());
	        }
	        return false;
	    }


	    @Override
	    public void onMarkerDragStart(Marker marker) {
	        //mTopText.setText("onMarkerDragStart");
	    }

	    @Override
	    public void onMarkerDragEnd(Marker marker) {
	      //  mTopText.setText("onMarkerDragEnd");
	    }

	    @Override
	    public void onMarkerDrag(Marker marker) {
	       // mTopText.setText("onMarkerDrag.  Current Position: " + marker.getPosition());
	    }
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void onClickDisplayNames(View view) {
		getSupportLoaderManager().initLoader(1, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		cursorLoader = new CursorLoader(this,
				Uri.parse("content://multipanechatapp/peers"), new String[] {
						"_id", "name", "senderlatitude", "senderlongitude",
						"street" }, null, null, null);
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		cursor.moveToFirst();
		StringBuilder res = new StringBuilder();
		while (!cursor.isAfterLast()) {
			res.append("\n" + cursor.getString(cursor.getColumnIndex("street"))
					+ "-" + cursor.getString(cursor.getColumnIndex("name")));

			Peer newPeer = new Peer(cursor);

			peers.add(newPeer);
			Log.d("Peer adding",
					Long.toString(newPeer.id)
							+ Double.toString(newPeer.latitude));
			cursor.moveToNext();
		}
		setUpMap();
		// resultView.setText(res);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

}