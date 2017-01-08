package network3;
import java.util.logging.Logger;

public class Main {	
	public final static Logger LOGGER = Logger.getLogger(Main.class.getName());
	public static Server server;
	public static Client client; 
	
	public static void main(String[] args) {
		server = new Server("SWiliNetworking17");
		client = new Client("CWiliNetworking17");
		Thread serverThread = new Thread(server);
		Thread clientThread = new Thread(client);
		serverThread.start();
		clientThread.start();
	}

}
