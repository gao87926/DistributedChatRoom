package edu.stevens.cs522.multipane.request;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;

import org.apache.http.HttpResponse;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.util.JsonReader;
import android.util.Log;

@SuppressLint("NewApi")
public class RestMethod {

	String destination = "http://10.0.0.2:8080";
	String data = "";

	public Response perform(Register request) {
		long clientId;
		Response response = new Response();
		/************ Make Post Call To Web Server ***********/
		// Send data
		try {
			// Defined URL where to send data
			URL url = new URL(request.getRequestUri().toString());
			// Send POST data request
			Log.d("RestMethod", url.toString());
			URLConnection connection = url.openConnection();
			HttpURLConnection conn = (HttpURLConnection) connection;
			 conn.setReadTimeout(1500);
			 conn.setConnectTimeout(2000);
//			connection.setDoOutput(true);
//		    connection.setDoInput(true);
			//Log.i("connecting..", conn.toString());
			
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Accept", "application/json");
			//location 
			Log.d("Register new user",request.latitude +"++" + request.longitude);
			conn.setRequestProperty("latitude", Double.toString(request.latitude));
			conn.setRequestProperty("longitude", Double.toString(request.longitude));
			conn.setRequestMethod("POST");
			response.status = conn.getResponseCode();
			Log.d("Response Code", String.valueOf(response.status));
			if (response.status == 201) {
				response.headers = conn.getHeaderFields();
//				DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
//				wr.writeBytes(urlParameters);
//				wr.flush();
//				wr.close();
				InputStream in = conn.getInputStream();
				JsonReader jr = new JsonReader(new InputStreamReader(in,"UTF-8"));
				jr.beginObject();
				if (jr != null) {
					jr.nextName();
					clientId = jr.nextLong();
					response.body = String.valueOf(clientId);
					Log.d("clientIdResponse",String.valueOf(response.body));
				}
				jr.close();
			}
		} catch (Exception ex) {
			Log.e("RestMethod", ex.getMessage());
		}

		return response;

	}

	public StreamingResponse perform(Synchronize request) {
		HttpURLConnection conn;
		URI uri;
		StreamingResponse response = null;
		URL url;
		try {
			uri = new URI(request.getRequestUri().toString());
		
			url = uri.toURL();
		conn =(HttpURLConnection)url.openConnection();
		JsonReader rd = null;
		response =  request.getResponse(conn, rd);
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		return response;
	}

	
	public Response perform(Unregister request) {
		long clientId;
		Response response = new Response();
		try {
			// Defined URL where to send data
			URL url = new URL(request.getRequestUri().toString());
			// Send POST data request
			Log.d("RestMethod", url.toString());
			URLConnection connection = url.openConnection();
			HttpURLConnection conn = (HttpURLConnection) connection;
			 conn.setReadTimeout(1500);
			 conn.setConnectTimeout(2000);
//			connection.setDoOutput(true);
//		    connection.setDoInput(true);
			Log.i("connecting..", conn.toString());
			//location 
			conn.setRequestProperty("latitude", Double.toString(request.latitude));
			conn.setRequestProperty("longitude", Double.toString(request.longitude));
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Accept", "application/json");
			conn.setRequestMethod("DELETE");
			response.status = conn.getResponseCode();
			Log.d("Response Code", String.valueOf(response.status));
//			if (response.status<400||response.status>600) {
//				response.headers = conn.getHeaderFields();
////				DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
////				wr.writeBytes(urlParameters);
////				wr.flush();
////				wr.close();
//				InputStream in = conn.getInputStream();
//				JsonReader jr = new JsonReader(new InputStreamReader(in,"UTF-8"));
//				jr.beginObject();
//				if (jr != null) {
//					jr.nextName();
//					clientId = jr.nextLong();
//					response.body = String.valueOf(clientId);
//					//Log.d("clientIdResponse",String.valueOf(response.body));
//				}
//				jr.close();
//			}
		} catch (Exception ex) {
			Log.e("RestMethod", ex.toString());
		}

		return response;


	}
}
