package at.vcity.androidim.interfaces;

import android.content.Context;


public interface ISocketOperator {
	
	public String sendHttpRequest(Context context, String params);
	public int startListening(int port);
	public void stopListening();
	public void exit();
	public int getListeningPort();

}
