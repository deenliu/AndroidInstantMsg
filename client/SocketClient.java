import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
public class SocketClient {

    private String hostname;
    private int port;
    Socket socketClient;

    public SocketClient(String hostname, int port){
        this.hostname = hostname;
        this.port = port;
    }

    public void connect() throws UnknownHostException, IOException{
        System.out.println("Attempting to connect to "+hostname+":"+port);
        socketClient = new Socket(hostname,port);
        System.out.println("Connection Established");
       
        try{
	        OutputStream os = socketClient.getOutputStream();
	        OutputStreamWriter osw = new OutputStreamWriter(os);
	        BufferedWriter bw = new BufferedWriter(osw);
	        //String sendMessage = number + "\n";
		//String sendMessage = "AddTask:;:Login:;:<004:.:001:.:'Login':.:'jiji':.:'make machine':.:'2014-02-23 00:12:43':.:0.3:.:'G':.:0.3:.:'L'>";
		//String sendMessage = "ReceiveMessage:;:Login";
		//String sendMessage="ReceiveMessage:;:test";
		//String sendMessage="CreateGroup:;:t:;:Login:;:ding:.:Login";
		//String sendMessage="GetGroupID:;:Login";
		//String sendMessage="GetGroupNames:;:15:.:16";
		//String sendMessage="GetGroupLeaderAndMember:;:15";
		//String sendMessage="GetUserName:;:Login";
		//String sendMessage="GetGroupTaskIDs:;:9999";
		//String sendMessage="GetOneGroupTasksInfo:;:003";
		//String sendMessage = "AddGroupTask:;:Login:;:004:.:001:.:'Login':.:'jiji':.:'make machine':.:'2014-02-23 00:12:43':.:0.3:.:'G':.:0.3:.:'L':.:15";
		Scanner input = new Scanner(System.in);
		System.out.print("Please input the protocol: ");
    		String sendMessage = input.next();
	        bw.write(sendMessage);
	        bw.flush();
	        System.out.println("Message sent to the server : "+sendMessage);
        }catch(Exception e){
        	System.out.println("error");
        }
       
    }

    public void readResponse() throws IOException{
        String userInput;
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(socketClient.getInputStream()));

        System.out.println("The owner is :");
        while ((userInput = stdIn.readLine()) != null) {
            System.out.println(userInput);
        }
    }

    public static void main(String arg[]){
        SocketClient client = new SocketClient ("lore.cs.purdue.edu",8089);

		try {
            	client.connect();
            	client.readResponse();

        	} catch (UnknownHostException e) {
            		System.err.println("Host unknown. Cannot establish connection");
        	} catch (IOException e) {
            		System.err.println("Cannot establish connection. Server may not be up."+e.getMessage());
        	}

/*
        try {
            client.connect();
            client.readResponse();

        } catch (UnknownHostException e) {
            System.err.println("Host unknown. Cannot establish connection");
        } catch (IOException e) {
            System.err.println("Cannot establish connection. Server may not be up."+e.getMessage());
        }
*/
    }
}

