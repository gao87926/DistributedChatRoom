package edu.stevens.cs522.multipane.request;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import android.net.Uri;
import android.os.Parcelable;
import android.util.JsonReader;

public abstract class Request implements Parcelable {
	public long id=0;
	public UUID regid; 
	public String addr;
	public double latitude = 40.745700;
	public double longitude = -74.027300;
	// sanityCheck
//	public Map<String,String> header;
//	public Uri uri;
	
	// App-specific HTTP request headers.
	public Map<String, String> getRequestHeaders(){
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("latitude", Double.toString(latitude));
		headers.put("longitude", Double.toString(longitude));
		return headers;
	}
	// Chat service URI with parameters e.g. query string parameters.
	public abstract Uri getRequestUri();
	// JSON body (if not null) for request data not passed in headers.
	public abstract String getRequestEntity() throws IOException;
	// Define your own Response class, including HTTP response code.
	public abstract Response getResponse(HttpURLConnection connection, JsonReader rd /** Null for* streaming*/);
}
