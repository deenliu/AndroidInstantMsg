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
		if(temp[0].equals("Register")) {
			String check[] = c.GetUser(temp[1]).split(":;:");
			if(!check[0].equals("")) {
				return_result = "Register fail1";
			} else {
				result = c.AddUser(temp[1], temp[2], temp[3], temp[4],temp[5]);
				if(result) {
					return_result = "Register success";
				} else {
					return_result = "Register fail2";
				}
			}
		} 

		else if(temp[0].equals("Login")) {
			result = c.isValidLogin(temp[1], temp[2]);
			if(result) {
				return_result = "Login success";
			} else {
			 	return_result = "Login fail";
			}
		} else if(temp[0].equals("ShowFriend")){
			String friendlist = c.ShowFriendList(temp[1]);
			return_result = c.ShowFriendInfo(friendlist);
			System.out.println(return_result);
		
		} else if(temp[0].equals("DeleteFriend")){
			result = c.DeleteFriendList(temp[1],temp[2]);
			if(result) {
				return_result = "DeleteFriend success";
			} else {
				return_result = "DeleteFriend fail";
			}
		}else if(temp[0].equals("AddFriend")){
			result = c.AddFriend(temp[1],temp[2]);
			if(result) {
				return_result = c.ShowSingleInFo(temp[2]);
			} else {
				return_result = "AddFriend fail";
			}
		}else if(temp[0].equals("ReceiveMessage")){
			return_result = c.ReceiveMessage(temp[1]);
			System.out.println(return_result);
	

		}else if(temp[0].equals("ShowTask")){
			return_result = c.ShowPersonalTask(temp[1]);
			System.out.println(return_result);
			if(return_result.length()>0){
				
			}else{
				return_result="Show Personal Task fail";
			}
		
		}else if(temp[0].equals("AddGroupTask")){
			result = c.AddGroupTask(temp[2]);
			if(result) {
				return_result = "Add group task success";
			} else {
				return_result = "Add group task fail";
			}
		} 
		else if(temp[0].equals("AddTask")){
			result = c.AddTask(temp[2]);
			if(result) {
				return_result = "Add task success";
			} else {
				return_result = "Add task fail";
			}
		} 
		else if(temp[0].equals("DeleteTask")){
			result = c.DeleteTask(temp[2]);
			if(result) {
				return_result = "Delete task success";
			} else {
				return_result = "Delete task fail";
			}
		} 
		else if(temp[0].equals("ModifyTask")){
			result = c.ModifyTask(temp[2]);
			if(result) {
				return_result = "Modify task success";
			} else {
				return_result = "Modify task fail";
			}
		} 
		else if(temp[0].equals("CreateGroup")){
			result = c.CreateGroup(temp[1], temp[2], temp[3]);
			if(result) {
				return_result = "Create group success";
			} else {
				return_result = "Create group fail";
			}
		}
		else if(temp[0].equals("DeleteGroup")){
			result = c.DeleteGroup(temp[1]);
			if(result) {
				return_result = "Delete group success";
			} else {
				return_result = "Delete group fail";
			}
		}else if(temp[0].equals("GetGroupID")){
			return_result = c.GetGroupID(temp[1]);
			System.out.println(return_result);
			if(return_result.length()>0){
				
			}else{
				return_result="Get Group ID fail";
			}
		
		} else if(temp[0].equals("GetGroupNames")){
			return_result = c.GetGroupNames(temp[1]);
			System.out.println(return_result);
			if(return_result.length()>0){
				
			}else{
				return_result="Get Group ID fail";
			}
		
		} else if(temp[0].equals("GetGroupLeaderAndMember")){
			return_result = c.GetGroupLeaderAndMember(temp[1]);
			System.out.println(return_result);
			if(return_result.length()>0){
				
			}else{
				return_result="Get Group Leader And Member fail";
			}
		
		} else if(temp[0].equals("GetUserName")){
			return_result = c.GetUserName(temp[1]);
			System.out.println(return_result);
			if(return_result.length()>0){
				
			}else{
				return_result="Get User Name fail";
			}
		
		} else if(temp[0].equals("GetGroupTaskIDs")){
			return_result = c.GetGroupTaskIDs(temp[1]);
			System.out.println(return_result);
			if(return_result.length()>0){
				
			}else{
				return_result="Get Group task id fail";
			}
		
		} else if(temp[0].equals("GetOneGroupTasksInfo")){
			return_result = c.GetOneGroupTasksInfo(temp[1]);
			System.out.println(return_result);
			if(return_result.length()>0){
				
			}else{
				return_result="Get Group task info fail";
			}
		
		} 
		//////////////////////Default/////////////////////////
		else {
			return_result = "Nothing is being requested.";
		}

		return return_result;
	}
}
