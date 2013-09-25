package com.android.aim.interfaces;

import java.io.UnsupportedEncodingException;

import android.content.Context;


public interface IAppManager {
	
	public String getUsername();
	public String sendMessage(Context context, String username,String tousername, String message) throws UnsupportedEncodingException;
	public String authenticateUser(Context context, String usernameText, String passwordText) throws UnsupportedEncodingException; 
	public void messageReceived(String username, String message);
//	public void setUserKey(String value);
	public boolean isNetworkConnected();
	public boolean isUserAuthenticated();
	public String getLastRawFriendList();
	public void exit();
	public String signUpUser(Context context, String usernameText, String passwordText, String email);
	public String addNewFriendRequest(Context context, String friendUsername);
	public String sendFriendsReqsResponse(Context context, String approvedFriendNames,
			String discardedFriendNames);

	
}
