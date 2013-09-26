import java.sql.*;
import java.io.*;
import java.util.*;
import java.util.Calendar;

public class Connector {
	public static final String SUCCESSFUL = "1";
	public static final String FAILED = "0";
	public static final String SIGN_UP_USERNAME_CRASHED = "2";
	public static final String ADD_NEW_USERNAME_NOT_FOUND = "2";
	public static final String USER_APPROVED = "1";
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

	public String isValidLogin(String userName, String password, String ipaddr){
		String cc=authenticateUser(userName, password, ipaddr);
		if(cc.length()<=0)
		{
			return "";
		}

		userName = EscapeString(userName);
		password = EscapeString(password);
		String sqlCheck = "select id from users where username = '" + userName+"' AND password = '"+password+"' limit 1;";
		String user_ID = ResultSetToString(executeQuery(sqlCheck));
		String sql = "select u.Id, u.username, (NOW()-u.authenticationTime) as authenticateTimeDifference, u.IP,f.providerId, f.requestId, f.status, u.port from friends f left join users u on u.Id = if ( f.providerId = "+user_ID+", f.requestId, f.providerId ) where (f.providerId = "+user_ID+" and f.status= 1 ) or f.requestId = "+user_ID+"; ";
		

		String sqlmessage = "SELECT m.id, m.fromuid, m.touid, m.sentdt, m.read, m.readdt, m.messagetext, u.username from messages m left join users u on u.Id = m.fromuid WHERE `touid` = "+user_ID+" AND `read` = 0 LIMIT 0, 30 ;";

		System.out.println("output:   \n" + sql);
		String output1 = ResultSetToString(executeQuery(sql));
		String output2 = ResultSetToString(executeQuery(sqlmessage));
		String out = "";
		String friendName, status, ip, id, port;
		out += "<data>";
		out += "<user userKey='" +user_ID+ "' />";
		String[] parts1 = output1.split(":;:");
		int i = 0;
		for(i = 0; i < parts1.length/8; i++){
			status = "offline";			

			friendName = parts1[i*8+1];
			status = parts1[i*8+6];
			if( !status.equals("1")){
				status = "unApproved";
			}else{
				status = "offline";
			}
			ip = parts1[i*8+3];
			id = parts1[i*8];
			port = parts1[i*8+7];
			out += "<friend  username = '"+ friendName+"'  status='"+status+"' IP='"+ip+"' userKey = '"+id+"'  port='"+port+"'/>";
		}
		String uName, sendt, messagetxt;
		String[] parts2 = output2.split(":;:");
		for(i = 0; i < parts2.length/8; i++){
			uName = parts2[i*8+7];
			sendt = parts2[i*8+3];
			messagetxt = parts2[i*8+6];
			out += "<meesage from='"+uName+ "'sendt='"+sendt+"' text='"+messagetxt+ "' />";
		}
		// need to update
		System.out.println(out);
		//String result = ResultSetToString(executeQuery(sql));

		return out;

	}

	public boolean sendMessage(String fromUser, String password ,String message, String toUser, String ipaddr){
		String cc=authenticateUser(fromUser, password, ipaddr);
		if(cc.length()<=0)
		{
			return false;
		}

		String sqlto = " select Id from users where username = '" + toUser + "' limit 1 ;" ;
		if(execute(sqlto)){

			String sql22 = "INSERT INTO messages (fromuid, touid, sentdt, messagetext) VALUES ("+fromUser+",  "+toUser+", NOW(), '"+message+"');";
//DATE("Y-m-d H:i")	
			
			return execute(sql22);

		}
	return false;

	}

	public boolean SignUpUser(String email, String UserName, String password, String ipaddr){
		//String cc=authenticateUser(UserName, password, ipaddr);
		
		String sqlCheck = "select id from users where username = '" +UserName+"' limit 1;";
		if(execute(sqlCheck)){
			String sql = "insert into users (username, password, email) values ('"+UserName+"','"+password+"','"+email+"');";
			authenticateUser(UserName, password, ipaddr);
			return execute(sql);			
		
	

		}

		return false;
	}
	public boolean addNewFriend(String userName, String password , String friendUserName, String ipaddr){
		String cc=authenticateUser(userName, password, ipaddr);
		if(cc.length()<=0)
		{
			return false;
		}

		String sqlCheck = "select id from users where username = '" +userName+"' and password = '" +password+"' limit 1;";
		if(execute(sqlCheck)){
			String sqlCheckAg = "select id from users where username = '"+ friendUserName+ "' limit 1;";
			if(execute(sqlCheckAg)){
				String userID = ResultSetToString(executeQuery("select id from users where username = '" +userName+"' limit 1;"));
				String requestID = ResultSetToString(executeQuery("select id from users where username = '" +friendUserName+"' limit 1;"));
				
				String sql = "insert into friends (providerID, requestId, status) values (" + userID + "," + requestID+", " + 0 +" );";
				return execute(sql);
			}
		}
		return false;
	}

	public boolean reponseOfFriendReqs(String userName, String password, String approvedName, String discardName, String ipaddr){
		String cc=authenticateUser(userName, password, ipaddr);
		if(cc.length()<=0)
		{
			return false;
		}



		String sqlCheck = "select id from users where username = '" +userName+"' and password = '" +password+"' limit 1;";
		String friendUserName = "";
		int approveORdiscard = 1;
		String sql;
		if(approvedName.equals("none")) { friendUserName = discardName; approveORdiscard = 0;}
		if(discardName.equals("none")) { friendUserName = approvedName; approveORdiscard = 1;} 

		if(execute(sqlCheck)){
			String sqlCheckAg = "select id from users where username = '"+ friendUserName+ "' limit 1;";
			if(execute(sqlCheckAg)){
				String userID = ResultSetToString(executeQuery("select id from users where username = '" +userName+"' limit 1;"));
				String requestID = ResultSetToString(executeQuery("select id from users where username = '" +friendUserName+"' limit 1;"));
				sql = "update friends set status = " + approveORdiscard + " where requestID = "+userID+" AND providerId = " +requestID+";";
				return execute(sql);			
			}
		}
		return false;
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


	public String authenticateUser(String username, String password, String ipaddr)
	{
		String query="select * from users where username = '" + username + "' and password = '" +password +"' limit 1;";
		String output = ResultSetToString(executeQuery(query));
		//System.out.println(output);
		String [] temp;
		temp = output.split(":;:");
		ipaddr=getAddr(ipaddr);
		if(output.length()>0)
		{
			query="update users set authenticationTime = NOW(), IP = '"+ipaddr+"' ,port = 15145 where Id = "+temp[0]+" limit 1";		
			if(execute(query))
			{
				System.out.println("Update Successfully");
			}
			else
			{
				System.out.println("failed to update");
			}

		}
		return temp[0];
	}
	private String getAddr(String addr)
	{
		//System.out.println(addr);
		String output=addr.subSequence(1, addr.lastIndexOf(":")).toString();
		//System.out.println("******"+addr);
		return output;
	}
	
	
	public static void main(String[] args) {

		Connector c = new Connector();
		c.Connect();
	}

}
