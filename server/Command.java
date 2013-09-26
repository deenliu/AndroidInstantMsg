import java.util.ArrayList;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Sender;

public class Command {
		
	String s;
	
	public Command(String command) {
		s = command;
	}
	
	public String getOutput(){
		String [] temp;
		String return_result = "";
		boolean result = true;
		Connector c = new Connector();
		System.out.println("connected Successfully");
		System.out.println("command: "+s);
		temp = s.split(":;:");
		//////////////////////User/////////////////////////


		if(temp[0].equals("LOGIN")) {

			String output;
			output = c.isValidLogin(temp[1], temp[2], temp[5]);
			if(output.length() >0) {
				return_result = output;
			} else {
			 	return_result = "0";
			}
		}else if(temp[0].equals("SEND_MSG")){
			//fromUser, password, message, toUser
			result = c.sendMessage(temp[1],temp[2],temp[3],temp[4], temp[6]);
			if(result){
				return_result = "1";	
			}else {
				return_result = "0";	
			}
		
		} else if(temp[0].equals("REGISTER")){
			//email, username, password
			result = c.SignUpUser(temp[4],temp[1],temp[2], temp[5]);
			if(result) {
				return_result = "1";
			}else{
				return_result = "0";
			}
		
		}  else if(temp[0].equals("ADD_FRIEND")){
		//userName, password, friendUserName
			result = c.addNewFriend(temp[1],temp[2],temp[4], temp[5]);
			if(result) {
				return_result = "1";
			}else{
				return_result = "0";
			}
		} else if(temp[0].equals("FRIEND_RESPONSE")){
			//username,password,approvedname,discardname
			result = c.reponseOfFriendReqs(temp[1],temp[2],temp[4],temp[5],temp[6]);
			if(result){	
				return_result = "1";
			}else{
				return_result = "0";
			}
		}
		//////////////////////Default/////////////////////////
		else {
			return_result = "Nothing is being requested.";
		}
		System.out.println(return_result);
		return return_result;
	}
}
