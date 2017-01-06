package network3;
import java.util.logging.Logger;

public class Main {	
	public final static Logger LOGGER = Logger.getLogger(Main.class.getName());
	
	public static void main(String[] args) {
		Server server = new Server("SWiliNetworking17");
		Client client = new Client("CWiliNetworking17");
		Thread serverThread = new Thread(server);
		Thread clientThread = new Thread(client);
		serverThread.start();
		clientThread.start();
	}

}
