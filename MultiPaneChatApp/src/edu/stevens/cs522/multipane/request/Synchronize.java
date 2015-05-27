package edu.stevens.cs522.multipane.request;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.Time;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
@SuppressLint("NewApi")
public class Synchronize {
	public String userId;
	public ArrayList<String> message;
	public long id = 0;
	public UUID regid;
	public String addr;
	public String chatroom;
	public double latitude;
	public double longitude;

	public int describeContents() {
		return hashCode();
	}
	public Synchronize(){};
	public Synchronize(long seqnum, UUID registrationID, String username,
			String addr,String chatroom, ArrayList<String> message, double latitude, double longitude) {
		this.id = seqnum;
		this.regid = registrationID;
		this.userId = username;
		this.addr = addr;
		this.chatroom = chatroom;
		this.message = message;
		this.latitude = latitude;
		this.longitude = longitude;
	}
	public Map<String, String> getRequestHeaders() {
		return null;
	}

	public Uri getRequestUri() {
		return Uri.parse(addr + "/chat/" + this.userId + "?regid="
				+ this.regid.toString() + "&seqnum=" + String.valueOf(id));

	}

	public String getRequestEntity() throws IOException {
		JSONObject obj = new JSONObject();
		try {
			obj.put("chatroom", chatroom);
			obj.put("timestamp", String.valueOf(new Date().getTime()));
			obj.put("latitude", Double.toString(this.latitude));
			obj.put("longitude", Double.toString(this.longitude));
			obj.put("text", this.message);
		} catch (JSONException e) {

			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return obj.toString();
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@SuppressLint("NewApi")
	public StreamingResponse getResponse(HttpURLConnection conn, JsonReader rd) {
		StreamingResponse response = new StreamingResponse();
		List<String[]> usersList = new ArrayList<String[]>();
		List<String[]> msgList = new Vector<String[]>();
		
		try {
			conn.setRequestMethod("POST");

			conn.setRequestProperty("latitude", Double.toString(this.latitude));
			conn.setRequestProperty("longitude", Double.toString(this.longitude));
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setChunkedStreamingMode(0);
		
			conn.connect();
			JsonWriter wr;

			wr = new JsonWriter(new BufferedWriter(new OutputStreamWriter(
					conn.getOutputStream(), "UTF-8")));

			wr.beginArray();
			
			for (String msg : message) {
				wr.beginObject();
				
				wr.name("chatroom");
				wr.value(chatroom);

				wr.name("timestamp");
				wr.value(Long.parseLong(String.valueOf( new Date().getTime())));
				
				wr.name("latitude");
				wr.value(latitude);
				
				wr.name("longitude");
				wr.value(longitude);
				
				wr.name("text");
				wr.value(msg);
				
				wr.endObject();
				Log.d("+++++Writing JSON++++++", chatroom + ":"+Long.parseLong(String.valueOf( new Date().getTime()))+msg+"|"+Double.toString(latitude)+"|"+Double.toString(longitude));
			}
			wr.endArray();
			wr.flush();
			wr.close();
			
			
			JsonReader jrd = new JsonReader(new BufferedReader(
					new InputStreamReader(conn.getInputStream(), "UTF-8")));
			
			jrd.beginObject();

			jrd.nextName();
			jrd.beginArray();
			
			while (jrd.hasNext()) {
				
				//usersList.add(jrd.nextString());
				jrd.beginObject();
				String[] clientStr = new String[3];
				jrd.nextName();
				clientStr[0] = jrd.nextString();
				jrd.nextName();
				clientStr[1] = jrd.nextString();
				jrd.nextName();
				clientStr[2] = jrd.nextString();
				//0: sender 1: latitude 2:longitude
				usersList.add(clientStr);
				jrd.endObject();
			}

			jrd.endArray();

			jrd.nextName();
			jrd.beginArray();
			while (jrd.hasNext()) {
				jrd.beginObject();
				String[] tmp = new String[7];
				jrd.nextName();
				tmp[0] = jrd.nextString();
				jrd.nextName();
				tmp[1] = jrd.nextString();
				jrd.nextName();
				tmp[2] = jrd.nextString();
				jrd.nextName();
				tmp[3] = jrd.nextString();
				jrd.nextName();
				tmp[4] = jrd.nextString();
				jrd.nextName();
				tmp[5] = jrd.nextString();
				jrd.nextName();
				tmp[6] = jrd.nextString();



				
				//0:room ,1:timestamp , 2:latitude, 3:longitude, 4:seqNumb, 5:sender, 6:text
				msgList.add(tmp);
				Log.d("+++Reading JSON++++", tmp[0]+":"+tmp[1]+":"+ tmp[2]+":"+tmp[3]+":"+tmp[4]+":"+tmp[5]+":"+tmp[6]);
				jrd.endObject();
			}
			jrd.endArray();
			jrd.endObject();
			response.usersList = usersList;
			response.msgList = msgList;

			jrd.close();
			// sync(jr);
			conn.disconnect();
			

		} catch (Exception e) {
			Log.e("Error on sync", e.toString());
		}
		return response;
	}


}
