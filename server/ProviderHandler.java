import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/* Server - ProviderHandler
 * Handles the client input for one server socket connection.
 * 
 */

public class ProviderHandler implements Runnable {
	private Socket incoming;
	public ProviderHandler(Socket i) {
		incoming = i;
	}
	public void run() {
		// TODO Auto-generated method stub
		try {
			try {
				InputStream inStream = incoming.getInputStream();
				OutputStream outStream = incoming.getOutputStream();

				Scanner in = new Scanner(inStream);
				PrintWriter out = new PrintWriter(outStream, true);
				// get client input
				// Diconnect when receive "disconnect"
				boolean done = false;
				while (!done && in.hasNextLine()) {
					String line = in.nextLine();
					System.out.println(line);
					Command command = new Command(line);
					String output = command.getOutput();
					System.out.println(output);
					out.println(output);

					if (line.trim().equals("disconnect"))
						done = true;
				}
			} finally {
				incoming.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
