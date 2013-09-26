/* 
 * Copyright (C) 2007 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.aim.services;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import com.android.aim.Login;
import com.android.aim.Messaging;
import com.android.aim.R;
import com.android.aim.communication.SocketOperator;
import com.android.aim.interfaces.IAppManager;
import com.android.aim.interfaces.ISocketOperator;
import com.android.aim.interfaces.IUpdateData;
import com.android.aim.tools.FriendController;
import com.android.aim.tools.LocalStorageHandler;
import com.android.aim.tools.MessageController;
import com.android.aim.tools.XMLHandler;
import com.android.aim.types.FriendInfo;
import com.android.aim.types.MessageInfo;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class IMService extends Service implements IAppManager, IUpdateData {
	
	public static String USERNAME;
	public static final String TAKE_MESSAGE = "Take_Message";
	public static final String FRIEND_LIST_UPDATED = "Take Friend List";
	public static final String MESSAGE_LIST_UPDATED = "Take Message List";
	public ConnectivityManager conManager = null; 
	private final int UPDATE_TIME_PERIOD = 15000;
	private String rawFriendList = new String();
	private String rawMessageList = new String();

	ISocketOperator socketOperator = new SocketOperator(this);

	private final IBinder mBinder = new IMBinder();
	private String username;
	private String password;
	private boolean authenticatedUser = false;
	 // timer to take the updated data from server
	private Timer timer;
	

	private LocalStorageHandler localstoragehandler; 
	
	private NotificationManager mNM;

	public class IMBinder extends Binder {
		public IAppManager getService() {
			return IMService.this;
		}
		
	}
	   
    @Override
    public void onCreate() 
    {   	
         mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

         localstoragehandler = new LocalStorageHandler(this);
         // Display a notification about us starting.  We put an icon in the status bar.
         conManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
         new LocalStorageHandler(this);
    	
         // Timer is used to take the friendList info every UPDATE_TIME_PERIOD;
         timer = new Timer();   
		
         Thread thread = new Thread()
         {
        	 @Override
        	 public void run() {			
        		 waitForAPort();
        	 }
         };		
         thread.start();
    
    }
    
    public void waitForAPort(){
    	Random random = new Random();
		 int tryCount = 0;
		 while (socketOperator.startListening(10000 + random.nextInt(20000))  == 0 )
		 {		
			 tryCount++; 
			 if (tryCount > 10)
			 {
				 // if it can't listen a port after trying 10 times, give up...
				 break;
			 }
			
		 }
    }

	@Override
	public IBinder onBind(Intent intent) 
	{
		return mBinder;
	}


	/**
	 * Show a notification while this service is running.
	 * @param msg 
	 **/
    private void showNotification(String username, String msg) 
	{       
        // Set the icon, scrolling text and TIMESTAMP
    	String title = "AndroidIM: You got a new Message! (" + username + ")";
 				
    	String text = ((msg.length() < 5) ? msg : msg.substring(0, 5)+ "...");
    	
    	NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
    		.setSmallIcon(R.drawable.stat_sample)
    		.setContentTitle(title)
    		.setContentText(text); 
    	
        Intent i = new Intent(this, Messaging.class);
        i.putExtra(FriendInfo.USERNAME, username);
        i.putExtra(MessageInfo.MESSAGETEXT, msg);	
        
        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, 0);

        // Set the info for the views that show in the notification panel.
        mBuilder.setContentIntent(contentIntent); 
        
        mBuilder.setContentText("New message from " + username);
        
        // Send the notification.
        // We use a layout id because it is a unique number.  We use it later to cancel.
        mNM.notify((username+msg).hashCode(), mBuilder.build());
    }
	 

	public String getUsername() {
		return this.username;
	}

	
	public String sendMessage(Context context, String  username, String  tousername, String message) throws UnsupportedEncodingException 
	{			
		String params = "SEND_MSG:;:"+ URLEncoder.encode(this.username,"UTF-8") +
						":;:"+ URLEncoder.encode(this.password,"UTF-8") +
						":;:" + URLEncoder.encode(tousername,"UTF-8") +
						":;:"+ URLEncoder.encode(message,"UTF-8") +
						":;:"  + URLEncoder.encode("sendMessage","UTF-8")
						;		
		Log.i("PARAMS", params);
		return socketOperator.sendHttpRequest(context, params);		
	}

	
	private String getFriendList(Context context) throws UnsupportedEncodingException 	{		
		// after authentication, server replies with friendList xml
		
		 rawFriendList = socketOperator.sendHttpRequest(context, getAuthenticateUserParams(username, password));
		 if (rawFriendList != null) {
			 this.parseFriendInfo(rawFriendList);
		 }
		 return rawFriendList;
	}
	
	private String getMessageList(Context context) throws UnsupportedEncodingException 	{		
		// after authentication, server replies with friendList xml
		
		 rawMessageList = socketOperator.sendHttpRequest(context, getAuthenticateUserParams(username, password));
		 if (rawMessageList != null) {
			 this.parseMessageInfo(rawMessageList);
		 }
		 return rawMessageList;
	}
	/**
	 * authenticateUser: it authenticates the user and if succesful
	 * it returns the friend list or if authentication is failed 
	 * it returns the "0" in string type
	 * @throws UnsupportedEncodingException 
	 * */
	public String authenticateUser(final Context context, String usernameText, String passwordText) throws UnsupportedEncodingException 
	{
		this.username = usernameText;
		this.password = passwordText;	
		
		this.authenticatedUser = false;
		
		String result = this.getFriendList(context);
		if (!result.equals(Login.AUTHENTICATION_FAILED)) 
		{			
			// if user is authenticated then return string from server is not equal to AUTHENTICATION_FAILED
			this.authenticatedUser = true;
			
			if (result == null)
				rawFriendList = "";
			else
				rawFriendList = result;
			
			USERNAME = this.username;
			Intent i = new Intent(FRIEND_LIST_UPDATED);					
			i.putExtra(FriendInfo.FRIEND_LIST, rawFriendList);
			sendBroadcast(i);
			
			timer.schedule(new TimerTask()
			{			
				public void run() 
				{
					sendFriendsList(context);
				}			
			}, UPDATE_TIME_PERIOD, UPDATE_TIME_PERIOD);
		}
		
		return result;		
	}

	public void sendFriendsList(Context context){
		try {
			Intent i = new Intent(FRIEND_LIST_UPDATED);
			Intent i2 = new Intent(MESSAGE_LIST_UPDATED);
			String tmp = IMService.this.getFriendList(context);
			String tmp2 = IMService.this.getMessageList(context);
			if (tmp != null) {
				i.putExtra(FriendInfo.FRIEND_LIST, tmp);
				sendBroadcast(i);	
				Log.i("friend list broadcast sent ", "");
				
				if (tmp2 != null) {
					i2.putExtra(MessageInfo.MESSAGE_LIST, tmp2);
					sendBroadcast(i2);	
					Log.i("friend list broadcast sent ", "");
				}
			
			}
			else {
				Log.i("friend list returned null", "");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void messageReceived(String username, String message) 
	{				
		
		MessageInfo msg = MessageController.checkMessage(username);
		if ( msg != null)
		{			
			Intent i = new Intent(TAKE_MESSAGE);
		
			i.putExtra(MessageInfo.USERID, msg.userid);			
			i.putExtra(MessageInfo.MESSAGETEXT, msg.messagetext);			
			sendBroadcast(i);
			String activeFriend = FriendController.getActiveFriend();
			if (activeFriend == null || activeFriend.equals(username) == false) 
			{
				localstoragehandler.insert(username,this.getUsername(), message.toString());
				showNotification("*" + username, message);
			}
			
			Log.i("TAKE_MESSAGE broadcast sent by im service", "");
		}	
		
	}  
	
	private String getAuthenticateUserParams(String usernameText, String passwordText) throws UnsupportedEncodingException 
	{			
		String params = "LOGIN" + 
						":;:" + URLEncoder.encode(usernameText,"UTF-8") +
						":;:"+ URLEncoder.encode(passwordText,"UTF-8") +
						":;:"  + URLEncoder.encode("authenticateUser","UTF-8")+
						":;:"    + URLEncoder.encode(Integer.toString(socketOperator.getListeningPort()),"UTF-8")
						;		
		
		return params;		
	}

	public void setUserKey(String value) 
	{		
	}

	public boolean isNetworkConnected() {
		return conManager.getActiveNetworkInfo().isConnected();
	}
	
	public boolean isUserAuthenticated(){
		return authenticatedUser;
	}
	
	public String getLastRawFriendList() {		
		return this.rawFriendList;
	}
	
	@Override
	public void onDestroy() {
		Log.i("IMService is being destroyed", "...");
		super.onDestroy();
	}
	
	public void exit() 
	{
		socketOperator.exit(); 
		socketOperator = null;
		this.stopSelf();
	}
	
	public String signUpUser(Context context, String usernameText, String passwordText,
			String emailText) 
	{
		String params = "REGISTER:;:" + usernameText +
						":;:" + passwordText +
						":;:" + "signUpUser"+
						":;:" + emailText
						;
		
		String result = socketOperator.sendHttpRequest(context, params);		
		
		return result;
	}

	public String addNewFriendRequest(Context context, String friendUsername) 
	{
		String params = "ADD_FRIEND:;:" + this.username +
		":;:" + this.password +
		":;:" + "addNewFriend" +
		":;:" + friendUsername
		;

		String result = socketOperator.sendHttpRequest(context, params);		
		
		return result;
	}

	public String sendFriendsReqsResponse(Context context, String approvedFriendNames,
			String discardedFriendNames) 
	{
		String params = "FRIEND_RESPONSE:;:" + this.username +
		":;:" + this.password +
		":;:" + "responseOfFriendReqs"+
		":;:" + approvedFriendNames +
		":;:" +discardedFriendNames
		;

		String result = socketOperator.sendHttpRequest(context, params);		
		
		return result;
		
	} 
	
	private void parseFriendInfo(String xml)
	{			
		try 
		{
			SAXParser sp = SAXParserFactory.newInstance().newSAXParser();
			sp.parse(new ByteArrayInputStream(xml.getBytes()), new XMLHandler(IMService.this));		
		} 
		catch (ParserConfigurationException e) {			
			e.printStackTrace();
		}
		catch (SAXException e) {			
			e.printStackTrace();
		} 
		catch (IOException e) {			
			e.printStackTrace();
		}	
	}
	
	private void parseMessageInfo(String xml)
	{			
		try 
		{
			SAXParser sp = SAXParserFactory.newInstance().newSAXParser();
			sp.parse(new ByteArrayInputStream(xml.getBytes()), new XMLHandler(IMService.this));		
		} 
		catch (ParserConfigurationException e) {			
			e.printStackTrace();
		}
		catch (SAXException e) {			
			e.printStackTrace();
		} 
		catch (IOException e) {			
			e.printStackTrace();
		}	
	}

	public void updateData(MessageInfo[] messages,FriendInfo[] friends,
			FriendInfo[] unApprovedFriends, String userKey) 
	{
		this.setUserKey(userKey);
		//FriendController.	
		MessageController.setMessagesInfo(messages);
		
		int i = 1;
		while (i < messages.length){
			messageReceived(messages[i].userid,messages[i].messagetext);
			i++;
		}
		
		FriendController.setFriendsInfo(friends);
		FriendController.setUnapprovedFriendsInfo(unApprovedFriends);
		
	}
	
}