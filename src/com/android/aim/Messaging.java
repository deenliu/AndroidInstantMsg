package com.android.aim;


import java.io.UnsupportedEncodingException;

import com.android.aim.interfaces.IAppManager;
import com.android.aim.services.IMService;
import com.android.aim.tools.FriendController;
import com.android.aim.tools.LocalStorageHandler;
import com.android.aim.types.FriendInfo;
import com.android.aim.types.MessageInfo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class Messaging extends Activity {
	private final Context context = this;
	private static final int MESSAGE_CANNOT_BE_SENT = 0;
	public String username;
	private EditText messageText;
	private EditText messageHistoryText;
	private Button sendMessageButton;
	private IAppManager imService;
	private FriendInfo friend = new FriendInfo();
	private LocalStorageHandler localstoragehandler; 
	private Cursor dbCursor;
	
	private ServiceConnection mConnection = new ServiceConnection() {
      
		
		
		public void onServiceConnected(ComponentName className, IBinder service) {          
            imService = ((IMService.IMBinder)service).getService();
        }
        public void onServiceDisconnected(ComponentName className) {
        	imService = null;
            Toast.makeText(Messaging.this, R.string.local_service_stopped,
                    Toast.LENGTH_SHORT).show();
        }
    };
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);	   
		
		setContentView(R.layout.messaging_screen); 
				
		messageHistoryText = (EditText) findViewById(R.id.messageHistory);
		
		messageText = (EditText) findViewById(R.id.message);
		
		messageText.requestFocus();			
		
		sendMessageButton = (Button) findViewById(R.id.sendMessageButton);
		
		Bundle extras = this.getIntent().getExtras();
		
		
		friend.userName = extras.getString(FriendInfo.USERNAME);
		friend.ip = extras.getString(FriendInfo.IP);
		friend.port = extras.getString(FriendInfo.PORT);
		String msg = extras.getString(MessageInfo.MESSAGETEXT);
		
		setTitle("Messaging with " );//+ friend.userName);
		
		localstoragehandler = new LocalStorageHandler(this);
		dbCursor = localstoragehandler.get(friend.userName, IMService.USERNAME );
		
		if (dbCursor.getCount() > 0){
		int noOfScorer = 0;
		dbCursor.moveToFirst();
		    while ((!dbCursor.isAfterLast())&&noOfScorer<dbCursor.getCount()) 
		    {
		        noOfScorer++;

				this.appendToMessageHistory(dbCursor.getString(2) , dbCursor.getString(3));
		        dbCursor.moveToNext();
		    }
		}
		localstoragehandler.close();
		
		this.appendToMessageHistory(friend.userName , msg);
		((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).cancel((friend.userName+msg).hashCode());

		
		sendMessageButton.setOnClickListener(new OnClickListener(){
			CharSequence message;
			Handler handler = new Handler();
			public void onClick(View arg0) {
				message = messageText.getText();
						
				appendToMessageHistory(imService.getUsername(), message.toString());
				
				localstoragehandler.insert(imService.getUsername(), friend.userName, message.toString());
							
				messageText.setText("lol");
				Thread thread = new Thread(){					
					public void run() {
						try {
							if (imService.sendMessage(context, imService.getUsername(), friend.userName, message.toString()) == null)
							{
								
								handler.post(new Runnable(){	

									public void run() {
										
								        Toast.makeText(getApplicationContext(),R.string.message_cannot_be_sent, Toast.LENGTH_LONG).show();
								
									}
									
								});
							}
						} catch (UnsupportedEncodingException e) {
							Toast.makeText(getApplicationContext(),R.string.message_cannot_be_sent, Toast.LENGTH_LONG).show();

							e.printStackTrace();
						}
					}						
				};
				thread.start();
				
			}});
				
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		int message = -1;
		switch (id)
		{
		case MESSAGE_CANNOT_BE_SENT:
			message = R.string.message_cannot_be_sent;
		break;
		}
		
		if (message == -1)
		{
			return null;
		}
		else
		{
			return new AlertDialog.Builder(Messaging.this)       
			.setMessage(message)
			.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					/* User clicked OK so do some stuff */
				}
			})        
			.create();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(messageReceiver);
		unbindService(mConnection);
		
		FriendController.setActiveFriend(null);
		
	}

	@Override
	protected void onResume() 
	{		
		super.onResume();
		bindService(new Intent(Messaging.this, IMService.class), mConnection , Context.BIND_AUTO_CREATE);
				
		IntentFilter i = new IntentFilter();
		i.addAction(IMService.TAKE_MESSAGE);
		
		registerReceiver(messageReceiver, i);
		
		FriendController.setActiveFriend(friend.userName);		
		
		
	}
	
	
	public class  MessageReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) 
		{		
			Bundle extra = intent.getExtras();
			String username = extra.getString(MessageInfo.USERID);			
			String message = extra.getString(MessageInfo.MESSAGETEXT);
			
			if (username != null && message != null)
			{
				if (friend.userName.equals(username)) {
					appendToMessageHistory(username, message);
					localstoragehandler.insert(username,imService.getUsername(), message);
					
				}
				else {
					if (message.length() > 15) {
						message = message.substring(0, 15);
					}
					Toast.makeText(Messaging.this,  /*username +*/ " says '"+

													message + "'",
													Toast.LENGTH_SHORT).show();		
				}
			}			
		}
		
	};
	private MessageReceiver messageReceiver = new MessageReceiver();
	
	public  void appendToMessageHistory(String username, String message) {
		if (username != null && message != null) {
			messageHistoryText.append(username + ":\n");								
			messageHistoryText.append(message + "\n");
		}
	}
	
	
	@Override
	protected void onDestroy() {
	    super.onDestroy();
	    if (localstoragehandler != null) {
	    	
	    }
	    if (dbCursor != null) {
	    	dbCursor.close();
	    }
	}
	

}
