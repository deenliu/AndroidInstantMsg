import java.sql.*;
import java.io.*;
import java.util.*;
import java.util.Calendar;

public class Connector {
	String dburl;

	// Connection to the database. Kept open between calls.
	Connection con;

	public boolean isConnected;

	// Constructors
	public Connector(String url) {
		dburl = url;
		isConnected = false;
	}

	public Connector() {
		this("jdbc:mysql://mydb.ics.purdue.edu:3306/chen932");
	}

	public boolean Connect() {
		if (isConnected)
			return true;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection(dburl, "chen932", "1234");
			isConnected = true;
		} catch (Exception e) {	
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public void Disconnect() {
		if (!isConnected)
			return;
		try {
			con.close();
			isConnected = false;
		} catch (Exception e) {
		}
	}

	private synchronized ResultSet executeQuery(String query) {
		Connect();
		ResultSet output = null;
		if (!isConnected)
			return null;
		try {
			Statement stmt = con.createStatement();
			output = stmt.executeQuery(query);
		} catch (Exception e) {
			output = null;
		}
		return output;
	}

	private synchronized boolean execute(String query) {
		Connect();
		boolean output = true;
		if (!isConnected)
			return false;
		try {
			Statement stmt = con.createStatement();
			stmt.execute(query);
		} catch (Exception e) {
			e.printStackTrace();
			output = false;
		}
		Disconnect();
		return output;
	}

	private String EscapeString(String input) {
		String output = "";
		for (int i = 0; i < input.length(); i++) {
			int charvalue = (int) input.charAt(i);
			if ((charvalue >= 48 && charvalue <= 57)
					|| (charvalue >= 65 && charvalue <= 90)
					|| (charvalue >= 97 && charvalue <= 122)) {
				output += input.charAt(i);
			} else {
				output += "#" + String.valueOf(charvalue) + "#";
			}
		}
		return output;
	}

	private String UnescapeString(String input) {
		if (input == null)
			return "";
		String output = "";
		boolean converting = false;
		int charvalue = 0;
		for (int i = 0; i < input.length(); i++) {
			char curchar = input.charAt(i);
			if (!converting) {
				// looking at normal
				if (curchar == '#') {
					converting = true;
					continue;
				}
				output += curchar;
			} else {
				if (curchar == '#') {
					output += (char) charvalue;
					charvalue = 0;
					converting = false;
					continue;
				}
				charvalue *= 10;
				charvalue += Character.getNumericValue(curchar);
			}
		}
		return output;
	}

	private String ResultSetToString(ResultSet rs) {
		String output = "";
		if (rs == null) {
			// TODO
			Disconnect();
			return "";
		}
		try {
			boolean first = true;
			while (rs.next()) {
				if (!first) {
					output = output + ":;:";
				}
				int cols = rs.getMetaData().getColumnCount();
				for (int i = 1; i <= cols; i++) {
					if (i != 1)
						output = output + ":;:";
					output = output + UnescapeString(rs.getString(i));
				}
				first = false;
			}
		} catch (SQLException e) {
			output = "";
			e.printStackTrace();
		}
		// TODO
		Disconnect();
		return output;
	}

	/* execute */
	public String GetUser(String user_ID){
		user_ID = EscapeString(user_ID);
		String query = "SELECT * FROM user WHERE user_ID='"+user_ID+"'";
		
		return ResultSetToString(executeQuery(query));
	} 


	public boolean AddUser(String user_ID, String password, String first_name, String last_name, String phone){
		user_ID = EscapeString(user_ID);
		password = EscapeString(password);
		first_name = EscapeString(first_name);
		last_name = EscapeString(last_name);
		phone = EscapeString(phone);
		String query = "INSERT INTO user (user_ID,password,first_name,last_name, phone, address) VALUES ('" + user_ID+ "','" + password + "','" + first_name + "','"+ last_name + "','" +phone +  "','fdfd');";
		//String query =  "INSERT INTO user (user_ID, password,first_name,last_name) VALUES ('LOGG','123','dd','sdfd');";
		//return true;
		System.out.println("query:"+query);
		return execute(query);
	}
	
	public boolean CreateGroup(String grouptitle, String leader_ID, String member_list){
		leader_ID = EscapeString(leader_ID);
		String query = "INSERT INTO groupN (group_ID, team_leader_ID, team_member_list, timestamp, group_title) VALUES (null,'" + leader_ID+ "','" + member_list + "', NOW() ,'" + grouptitle + "');";

		System.out.println("query:"+query);
		return execute(query);
	}

	public boolean isValidLogin(String user_ID, String password){
		user_ID = EscapeString(user_ID);
		password = EscapeString(password);
		String temp[] = null;
		temp = GetUser(user_ID).split(":;:");
		if(temp[0].equals("")) {
			return false;
		}
		String s = temp[0]+temp[1];
		return s.equals(user_ID + password);
	}
	public String GetUserName(String user_ID) {
		user_ID = EscapeString(user_ID);
		String query = "SELECT first_name FROM user WHERE user_ID='" + user_ID + "';";
		String result = ResultSetToString(executeQuery(query));
		query = "SELECT last_name FROM user WHERE user_ID='" + user_ID + "';";
		result = result +" "+ ResultSetToString(executeQuery(query));
		return result;
	}

	public String GetGroup(String name) {
		name = EscapeString(name);
		String query = "SELECT owner FROM task WHERE task_ID=" + name + ";";

		return ResultSetToString(executeQuery(query));
	}

	public String GetRegisterID(String user_ID) {
		user_ID = EscapeString(user_ID);
		String query = "SELECT register_ID FROM user WHERE user_ID='" + user_ID + "';";
		return ResultSetToString(executeQuery(query));
	}
	public String ShowFriendList(String user_ID) {
		user_ID = EscapeString(user_ID);
		String query = "SELECT contact_list FROM user WHERE user_ID='" + user_ID + "';";
		return ResultSetToString(executeQuery(query));
	}
	public String ShowSingleInFo(String target_ID){
		target_ID = EscapeString(target_ID);
		String query="SELECT user_ID, first_name, last_name, phone from user where user_ID = '" + target_ID + "';";
		return ResultSetToString(executeQuery(query));
	}
	public String GetGroupID(String user_ID){
		user_ID = EscapeString(user_ID);
		String query="SELECT group_ID from groupN where team_leader_ID='"+user_ID+"';";
		String result="";
		result=result+ResultSetToString(executeQuery(query))+":;:";
		query="select group_ID from groupN where team_member_list like '%:.:"+user_ID+":.:%';";
		result=result+ResultSetToString(executeQuery(query))+":;:";
		return result;
	}
	public String GetGroupNames(String id_array){
		String []temp=id_array.split(":.:");
		String query="";
		String result="";
		for(int i=0;i<temp.length;i++){
			query="SELECT group_title from groupN where group_ID = "+temp[i]+";";
			String output=ResultSetToString(executeQuery(query));
			if(i==0){
				result=result+output;
			}
			else{
				result=result+":;:"+output;
			}
		}
		System.out.println(result);
		return result;
	}
	public String GetGroupTaskIDs(String group_ID){
		String query="SELECT task_ID from task where group_ID = "+group_ID+";";
		return ResultSetToString(executeQuery(query));
	}
	public String GetOneGroupTasksInfo(String task_ID){
		String query="SELECT task_ID, parent_task_ID, owner, title, content, due_time, progress, type, weight, depth, time_stamp from task where task_ID = '"+task_ID+"' ;";
		System.out.println(query);
		return ResultSetToString(executeQuery(query));
	}
	public String GetGroupLeaderAndMember(String group_ID){
		String query="SELECT team_leader_ID from groupN where group_ID = "+group_ID+";";
		String result="";
		result=result+ResultSetToString(executeQuery(query));
		System.out.println(result);
		query="SELECT team_member_list from groupN where group_ID = "+group_ID+";";
		String output=ResultSetToString(executeQuery(query));
		String[] temp=output.split(":.:");
		for(int i=0;i<temp.length;i++){
			//System.out.println(temp[i]);
			if(i==0) continue;
			result=result+":;:"+temp[i];
		}
		System.out.println(result);
		return result;
	}
	public boolean DeleteFriendList(String user_ID, String target){
		user_ID = EscapeString(user_ID);
		String query = "SELECT contact_list from user where user_ID ='"+user_ID+"';";
		String output = ResultSetToString(executeQuery(query));
		String []temp;
		temp = output.split(":\\*:");
		int t=-1;
		for(int i=0;i<temp.length;i++){
			if(temp[i].equals(target)){
				t=i;
				break;
			}
		}
		if(t==-1){
			return true;
		}
		for(int i=t;i<temp.length-1;i++){
			temp[i]=temp[i+1];
		}
		boolean first=false;
		String newlist="";
		for(int i=0;i<temp.length-1;i++){
			if(first){
				newlist=newlist+":\\*:";
			}
			newlist=newlist+temp[i];
			first=true;
		}
	
		query = "UPDATE user set contact_list = '" + newlist + "' where user_ID = '" + user_ID + "';";
		System.out.println("query:"+query);
		return execute(query);
	}
	public boolean AddFriend(String user_ID, String target_ID){
		user_ID = EscapeString(user_ID);
		String query = "SELECT user_ID from user where user_ID ='"+target_ID+"';";
		if(ResultSetToString(executeQuery(query)).length()>1){
			query = "SELECT contact_list from user where user_ID ='"+user_ID+"';";
			String list=ResultSetToString(executeQuery(query));
			list=list+":\\*:"+target_ID;
			query = "UPDATE user set contact_list = '" + list + "' where user_ID = '" + user_ID + "';";
			return execute(query);
		}
		else{
			return false;
		}
	}
	public boolean SendMessage(String user_ID, String target, String content){
		user_ID = EscapeString(user_ID);
		target = EscapeString(target);
		String query = "INSERT INTO chat_history (FROM_User_ID, TO_User_ID, timestamp, content) VALUES ('"+user_ID+"','"+
					target + "',NOW(),'" + content + "');";
		System.out.println("query:"+query);

		return execute(query);
	}
	public String ReceiveMessage(String user_ID) {
		user_ID = EscapeString(user_ID);
		String query = "SELECT From_User_ID, To_User_ID, content, timestamp from chat_history where TO_User_ID = '" + user_ID + "' or From_User_ID= '" + user_ID + "' order by timestamp ;";
		System.out.println("query:"+query);
		String output = ResultSetToString(executeQuery(query));
		String temp[]=output.split(":;:");
		String result="";
		for(int i=0;i<temp.length;i+=4){
			result=result+"<";
			result=result+temp[i]+":.:"+temp[i+1]+":.:"+temp[i+2]+":.:"+temp[i+3]+">";
		}
		result=result.substring(1, result.length()-1);

		return "[MessageHistory]:;:"+result;
	}
	public String ShowFriendInfo(String friendlist) {
		System.out.println(friendlist);
		String []temp;
		temp = friendlist.split(":\\*:");
		if(temp.length<1){
			return "[ShowFriend]";
		}
		int i;
		String returnString="[ShowFriend]";
		for(i=0;i<temp.length;i++){
			System.out.println(temp[i]);
			String query="SELECT user_ID, first_name, last_name, phone from user where user_ID = '" + temp[i] + "';";
			returnString=returnString+"<"+ResultSetToString(executeQuery(query))+">";	
		}
		return returnString;
	}
	public String ShowPersonalTask(String user_ID) {
		user_ID = EscapeString(user_ID);
		String query = "SELECT task_ID, parent_task_ID, title, content, due_time, progress, type, weight, depth, time_stamp FROM `task` WHERE owner='" + user_ID + "';";
		System.out.println("query:"+query);
		String output=ResultSetToString(executeQuery(query));
		String [] temp=output.split(":;:");
		String result="";
		for(int i=0;i<temp.length;i++){
			if(i%10==0){
				result=result+"<";
			}
			else{
				result=result+":;:";
			}
			result=result+temp[i];

			if(i%10==9){
				result=result+">";
			}
		}
		return result;
		
	}
	public boolean AddTask(String content){
		content=content.substring(1,content.length()-1);
		String temp[]=content.split(":.:");
		String inputContent="";
		for(int i=0;i<temp.length;i++){
			if(i!=0){
				inputContent=inputContent+",";
			}
			inputContent=inputContent+temp[i];
		}
		String query = "INSERT INTO task (task_ID, parent_task_ID, owner, title, content, due_time, progress, type, weight, depth, time_stamp) VALUES ("+inputContent+", NOW());";
		
		System.out.println("query:"+query);

		return execute(query);
	}
	public boolean AddGroupTask(String content){
		String temp[]=content.split(":.:");
		String inputContent="";
		for(int i=0;i<temp.length;i++){
			if(i!=0){
				inputContent=inputContent+",";
			}
			inputContent=inputContent+temp[i];
		}
		//System.out.println(inputContent);
		String query = "INSERT INTO task (task_ID, parent_task_ID, owner, title, content, due_time, progress, type, weight, depth, group_ID, time_stamp) VALUES ("+inputContent+", NOW());";
		
		System.out.println("query:"+query);
		
		return execute(query);
	}
	public boolean ModifyTask(String content){
		String content1=content.substring(1,content.length()-1);
		String temp[]=content1.split(":.:");
		System.out.println("Modify:"+temp[0]);
		DeleteTask(temp[0]);		
		return AddTask(content);
	}
	public boolean DeleteTask(String task_id){
		String query = "DELETE FROM task WHERE task_ID= "+task_id +" ;";
		System.out.println(query);
		return execute(query);
	}
	public boolean DeleteGroup(String group_id){
		String query = "DELETE FROM groupN WHERE group_ID= "+group_id +" ;";
		System.out.println(query);
		return execute(query);
	}
	
	public static void main(String[] args) {

		Connector c = new Connector();
		c.Connect();
	}

}
