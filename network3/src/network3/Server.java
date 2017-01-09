package network3;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;


public class Server {
	private String name;
	private boolean rx;
	private ServerSocket tcpSocket = null;
	
	public Server(String name){
		this.name = name;
		this.rx = false;
		Main.LOGGER.info(getName()+": "+ "Rx-Off");
		
	}
	//this function find and create a new tcp socket from unused tcp port
	public void createTcpSocket(int fromPort, int toPort) throws IOException {
	    while (tcpSocket == null){
	        try {
	        	int port = (int) (Math.random() * (toPort - fromPort)) + fromPort;
	            this.tcpSocket = new ServerSocket(port);
	            this.tcpSocket.setSoTimeout(5000);
	            Main.LOGGER.info(getName()+": "+ " create socket with TCP port : "+ port);
	            return;
	        } catch (IOException ex) {
	        	this.tcpSocket = null;
	            continue; // try next port
	        }
	    }
	    // if the program gets here, no port in the range was found
	    throw new IOException("no free tcp port found");
	}
	public void establishTCPConnection(){
		try{
		    Main.LOGGER.info(getName()+": "+ " Listenning on TCP port : "+ this.tcpSocket.getLocalPort());
		    Socket client = this.tcpSocket.accept();
			Main.LOGGER.info(getName()+": "+ " establish TCP connection with: "+ client.getRemoteSocketAddress());
			setRx(true);
			//TODO - getMessage
			client.close();
		}
		catch (SocketTimeoutException s) {
			Main.LOGGER.info(getName()+": "+ " TCP socket timeout");
		}
		catch (IOException e) {
			Main.LOGGER.info(getName()+": "+ e.getMessage());
			System.exit(0);
		}	
	}
	
	///***UDP***///
	public void listenToRequests(int port){
		byte[] requestMessage = new byte[20];
		DatagramSocket udpSocket = null;
		try {
			udpSocket = new DatagramSocket(port);
			udpSocket.setSoTimeout(1000);
			Main.LOGGER.info(getName()+": "+ "Listenning for Requests on UDP port : "+ port);
			DatagramPacket datagram = new DatagramPacket(requestMessage, requestMessage.length);
			udpSocket.receive(datagram);
			Main.LOGGER.info(getName()+": "+ "request message has been recivied in Server UDP Socket");
			udpSocket.setBroadcast(true);
			DatagramPacket packetToSend = createOffer(datagram);
			udpSocket.send(packetToSend);					
			udpSocket.close();
			establishTCPConnection();	
		}catch (SocketTimeoutException s) {
			udpSocket.close();
			Main.LOGGER.info(getName()+": "+ "UDP Requests Timeout");
		} catch (Exception e) {
			if(udpSocket!= null)
				udpSocket.close();
			Main.LOGGER.info(getName()+": "+ e.getMessage());
		}		
	}

	public DatagramPacket createOffer(DatagramPacket datagram){
		byte[] requestMessage = datagram.getData();
		byte[] offerMessage = new byte[26];		
		byte[] name = this.name.getBytes();
		int portNum = tcpSocket.getLocalPort();
		byte[] serverPort = ByteBuffer.allocate(4).putInt(portNum).array();	;
		for (int i=0; i<=15; i++){
			offerMessage[i] = name[i];
		}
		for (int i=16; i<20; i++){
			offerMessage[i] = requestMessage[i];
		}
		byte[] serverIp = null;
		InetAddress ip = null;
		try {
			ip = InetAddress.getLocalHost();
			serverIp = ip.getAddress();
		} catch (UnknownHostException e) {}
		for (int i=20; i<=23; i++){
			offerMessage[i] = serverIp[i-20];
		}
		for (int i=24; i<26; i++){
			offerMessage[i] = serverPort[i-24+2];
		}
		Main.LOGGER.info(getName()+": "+ "offer message has been created and send to "+datagram.getAddress());			
		return new DatagramPacket(offerMessage, offerMessage.length,datagram.getAddress(),6000);
	}

	
	public boolean isRx() {
		return rx;
	}
	
	public void setRx(boolean rx) {
		String status = "-off";
		if (rx)
			status = "-on";
		Main.LOGGER.info(getName()+": "+ "Rx" + status);
		this.rx = rx;
	}
	
	public String getName(){
		return name;
	}
}
