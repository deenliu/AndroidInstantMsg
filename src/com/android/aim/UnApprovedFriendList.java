package com.android.aim;

import com.android.aim.interfaces.IAppManager;
import com.android.aim.services.IMService;
import com.android.aim.types.FriendInfo;

import android.app.ListActivity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;


public class UnApprovedFriendList extends ListActivity {
	private final Context context = this;
	private static final int APPROVE_SELECTED_FRIENDS_ID = 0;
	private String[] friendUsernames;
	private IAppManager imService;
	String approvedFrdNames = new String(); // comma separated
	String discardedFrdNames = new String(); // comma separated

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		Bundle extras = getIntent().getExtras();
		
		String names = extras.getString(FriendInfo.FRIEND_LIST);
		
		friendUsernames = names.split(",");
		
		setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, friendUsernames));			
		
		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);	
		
		// canceling friend request notification
		NotificationManager NM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		NM.cancel(R.string.new_friend_request_exist);					
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {		
		boolean result = super.onCreateOptionsMenu(menu);		

		menu.add(0, APPROVE_SELECTED_FRIENDS_ID, 0, R.string.approve_selected_friends);				
		
		return result;
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) 
	{		

		switch(item.getItemId()) 
		{	  
			case APPROVE_SELECTED_FRIENDS_ID:
			{
				getFriendsList();
				
				sendFriendRequest();

				Toast.makeText(UnApprovedFriendList.this, R.string.request_sent, Toast.LENGTH_SHORT).show();
				
				return true;
			}			
			
		}

		return super.onMenuItemSelected(featureId, item);		
	}
	
	public void getFriendsList(){
		int reqlength = getListAdapter().getCount();
		
		for (int i = 1; i < reqlength ; i++) 
		{
			if (getListView().isItemChecked(i)) {
				approvedFrdNames = approvedFrdNames.concat(friendUsernames[i]).concat(",");
			}
			else {
				discardedFrdNames = discardedFrdNames.concat(friendUsernames[i]).concat(",");						
			}					
		}
	}

	public void sendFriendRequest(){
		Thread thread = new Thread(){
			@Override
			public void run() {
				if ( approvedFrdNames.length() > 0 || 
					 discardedFrdNames.length() > 0 
					) 
				{
					imService.sendFriendsReqsResponse(context, approvedFrdNames, discardedFrdNames);
					
				}											
			}
		};
		thread.start();
	}
	
	@Override
	protected void onPause() 
	{
		unbindService(mConnection);
		super.onPause();
	}

	@Override
	protected void onResume() 
	{
		super.onResume();
		bindService(new Intent(UnApprovedFriendList.this, IMService.class), mConnection , Context.BIND_AUTO_CREATE);
	}
	
	private ServiceConnection mConnection = new ServiceConnection() {
		
		public void onServiceConnected(ComponentName className, IBinder service) {          
			imService = ((IMService.IMBinder)service).getService();      

			
		}
		public void onServiceDisconnected(ComponentName className) {          
			imService = null;
			Toast.makeText(UnApprovedFriendList.this, R.string.local_service_stopped, Toast.LENGTH_SHORT).show();
		}
	};
}
