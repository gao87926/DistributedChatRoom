package edu.stevens.cs522.multipane.entity;
import java.sql.Date;

import edu.stevens.cs522.multipane.contract.Contract;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

public class ChatMessage implements Parcelable{
	public long id;
	public String messageText;
	public String sender;
	public double latitude;
	public double longitude;
	public long date;
	public long messageId;
	public long chatroomId;
	public long senderId;
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	public ChatMessage() {
	}
	public ChatMessage(int id,String messageText,String sender,Double latitude, Double longitude, long messageid, long chatroomId, long date, long senderId) {
		this.id=id;
       this.messageText  = messageText;
       this.sender = sender;
       this.latitude = latitude;
       this.longitude = longitude;
       this.date=date;
       this.messageId=messageid;
       this.chatroomId=chatroomId;
       this.senderId=senderId;
	}
	
	public void writeToParcel(Parcel out, int arg1) {
		out.writeLong(id);
		out.writeLong(chatroomId);
		out.writeLong(senderId);
		out.writeDouble(latitude);
		out.writeDouble(longitude);
		out.writeStringArray(new String[] {this.sender,
                this.messageText});
	}
	
	public ChatMessage(Parcel in) {
		readFromParcel(in) ;
	}
	public void readFromParcel(Parcel in) { 
		this.id=in.readLong();
		this.chatroomId = in.readLong();
		this.senderId = in.readLong();
		this.latitude = in.readDouble();
		this.longitude = in.readDouble();
		 String[] data = new String[2];
        in.readStringArray(data);
        this.messageText  = data[0];
        this.sender = data[1];
	    }  
	 public static final Parcelable.Creator<ChatMessage> CREATOR = new Parcelable.Creator<ChatMessage>() {  
		    
	        public ChatMessage createFromParcel(Parcel in) {  
	            return new ChatMessage(in);  
	        }  
	   
	        public ChatMessage[] newArray(int size) {  
	            return new ChatMessage[size];  
	        }  
	          
	   };
	public void writeToProvider(ContentValues values) {
			Contract.putId(values,id);
			Contract.putSender(values, sender);
			Contract.putMsgLatitude(values, Double.toString(latitude));
			Contract.putMsgLongitude(values, Double.toString(longitude));
			Contract.putText(values, messageText);
			Contract.putDate(values, date);
			Contract.putMessageID(values, messageId);
			Contract.putSenderId(values, senderId);
			Contract.putChatroom(values, chatroomId);
		}
	public  ChatMessage(Cursor cursor){
		this.messageText = Contract.getText(cursor);
		this.sender = Contract.getSender(cursor);
		this.latitude = Double.parseDouble(Contract.getMsgLatitude(cursor));
		this.longitude = Double.parseDouble(Contract.getMsgLongitude(cursor));
		this.chatroomId = Contract.getChatroom(cursor);
	}

}
