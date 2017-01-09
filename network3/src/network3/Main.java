package network3;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.logging.Logger;

public class Main {	
	public final static Logger LOGGER = Logger.getLogger(Main.class.getName());
	public static Server server;
	public static Client client; 
	
	public static void main(String[] args) {
		server = new Server("SWilNetworking17");
		client = new Client("CWilNetworking17");
		try {
			server.createTcpSocket(6000, 7000);
		} catch (IOException e) {}
		while(true){	
			if (!server.isRx()){ //Rx-off-tx-of
				server.listenToRequests(6000);
			}
			if (!server.isRx() && !client.isTx()){ //Rx-off-tx-off
				client.sendRequest(6000);
				client.listenToOffer(6000);
			}
			if(!server.isRx() && client.isTx()){ //Rx-off-tx-on
				client.sendMessageByTcp();
			}
			
			if(server.isRx() && client.isTx()){ //Rx-on-tx-on
				String brokenMessage = server.makeBrokenMessage(); //get message from client and brake it!
				client.sendMessageByTcp(brokenMessage);
			}
			if(server.isRx() && !client.isTx()){ //Rx-on-tx-off
				System.out.println(server.getTcpMessage());
				
			}
			 
		}

	}

}
