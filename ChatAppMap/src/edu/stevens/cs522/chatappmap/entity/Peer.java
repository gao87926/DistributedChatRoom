package edu.stevens.cs522.chatappmap.entity;


import edu.stevens.cs522.chatappmap.contract.Contract;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

public class Peer implements Parcelable {
	public long id;
	public String name;
	public double latitude;
	public double longitude;
	public String street;
	
	public Peer() {
	}
	public Peer(String n,long id, double latitude, double longitude) {
		this.id = id;
		this.name=n;
		this.latitude = latitude;
		this.longitude = longitude;
		this.street = "NA";
	
	}
	public Peer(String n,long id, double latitude, double longitude, String street ) {
		this.id = id;
		this.name=n;
		this.latitude = latitude;
		this.longitude = longitude;
		this.street = street;
	
	}
	public Peer(Parcel in) {
		readFromParcel(in);
	}
	public Peer(Cursor c) {
		this.id = Contract.getId(c);
		this.name = Contract.getName(c);
		this.latitude = Double.parseDouble(Contract.getSenderLatitude(c));
		this.longitude = Double.parseDouble(Contract.getSenderLongitude(c));
		this.street = Contract.getStreet(c);
		}
	
	public void readFromParcel(Parcel in) { 
		this.id=in.readLong();
        this.name  = in.readString();
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
        this.street = in.readString();
        
	 } 
	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeString(name);
		dest.writeDouble(latitude);
		dest.writeDouble(longitude);
		dest.writeString(street);
	}
	public static final Parcelable.Creator<Peer> CREATOR = new Parcelable.Creator<Peer>() {  
	    
        public Peer createFromParcel(Parcel in) {  
            return new Peer(in);  
        }  
   
        public Peer[] newArray(int size) {  
            return new Peer[size];  
        }  
          
    };
    public void writeToProvider(ContentValues values) {
		Contract.putId(values,id);
		Contract.putName(values, name);
		Contract.putSenderLatitude(values, Double.toString(latitude));
		Contract.putSenderLongitude(values, Double.toString(longitude));
		Contract.putStreet(values, street);
	
}

}
