package network3;

import java.io.IOException;
import java.net.*;


public class Server implements Runnable {
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
	            return;
	        } catch (IOException ex) {
	        	this.tcpSocket = null;
	            continue; // try next port
	        }
	    }

	    // if the program gets here, no port in the range was found
	    throw new IOException("no free tcp port found");
	}
	
	///***UDP***///
	private void listenToRequests(int port){
		byte[] requestMessage = new byte[32];
		DatagramSocket udpSocket;
		try {
			udpSocket = new DatagramSocket(port);
			DatagramPacket datagram = new DatagramPacket(requestMessage, requestMessage.length);
			udpSocket.receive(datagram);
			udpSocket.send(createOffer(datagram.getData()));
			udpSocket.close();
		} catch (Exception e) {
			Main.LOGGER.info(getName()+": "+ e.getMessage());
			System.exit(0);
		}		
	}

	private DatagramPacket createOffer(byte[] requestMessage){
		byte[] offerMessage = new byte[26];		
		byte[] name = this.name.getBytes();
		for (int i=0; i<=15; i++){
			offerMessage[i] = name[i];
		}
		for (int i=16; i<20; i++){
			offerMessage[i] = requestMessage[i];
		}
		byte[] serverIp = tcpSocket.getInetAddress().getAddress();
		for (int i=20; i<=23; i++){
			offerMessage[i] = serverIp[i-20];
		}
		byte[] serverPort = (tcpSocket.getLocalPort()+"").getBytes();
		for (int i=24; i<26; i++){
			offerMessage[i] = requestMessage[i-24];
		}
		return new DatagramPacket(offerMessage, offerMessage.length);
	}

	
	public boolean isRx() {
		return rx;
	}
	
	public void setRx(boolean rx) {
		this.rx = rx;
	}
	
	public String getName(){
		return name;
	}
	@Override
	public void run() {
		try {
			createTcpSocket(6000, 7000);
		} catch (IOException e) {
			Main.LOGGER.info(getName()+": "+ e.getMessage());
			System.exit(0);
		}	
		listenToRequests(6000);			
	}

}
