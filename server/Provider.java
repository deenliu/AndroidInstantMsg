import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/* Server - Provider
 * Multithreaded server that listens to port 8089
 * waiting connection of client
 *  
 */
public class Provider {
	public static void main(String[] agrs) {
		try {
			int i = 1;
			ServerSocket s = new ServerSocket(8089);
			while (true) {
				System.out.println("start");
				Socket incoming = s.accept();
				System.out.println("Spawning " + i);
				Runnable r = new ProviderHandler(incoming);
				Thread t = new Thread(r);
				t.start();
				i++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
