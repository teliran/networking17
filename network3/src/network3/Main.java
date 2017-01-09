package network3;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.logging.Logger;

public class Main {	
	public final static Logger LOGGER = Logger.getLogger(Main.class.getName());
	public static Server server;
	public static Client client; 
	
	public static void main(String[] args) {
		server = new Server("SWiliNetworking17");
		client = new Client("CWiliNetworking17");
		try {
			server.createTcpSocket(6000, 7000);
		} catch (IOException e) {}
		while(true){	
			if (!server.isRx()){
				server.listenToRequests(6000);
				client.sendRequest(6000);
				client.listenToOffer(6000);

			}
		}

	}

}
