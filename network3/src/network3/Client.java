package network3;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;

public class Client implements Runnable{
	private String name;
	private boolean tx;
	
	
	public Client(String name){
		this.name = name;
		this.setTx(false);

		
	}
	//create a new byte array of the Request message
	private byte[] createRequestMessage(){
		byte[] messageRequest = new byte[20];
		byte[] name = this.name.getBytes();
		int randNumber = (int)(((int)Math.pow(2, 32)-1)*Math.random());
		byte[] number = ByteBuffer.allocate(4).putInt(randNumber).array();	
		for (int i=0; i<=15; i++){
			messageRequest[i] = name[i];
		}
		for (int i=16; i<20; i++){
			messageRequest[i] = number[i-16];
		}
		return messageRequest;
	}
	
	private void sendRequest(int port){
		byte[] messageRequest = createRequestMessage();
		try {
			DatagramPacket udpPacket = new DatagramPacket(messageRequest, messageRequest.length, InetAddress.getByName("255.255.255.255"), port);
			DatagramSocket udpSocket = new DatagramSocket();
			udpSocket.setBroadcast(true);
			udpSocket.send(udpPacket);
			Main.LOGGER.info(getName()+": "+ "Message Request has been created and sent");
			udpSocket.close();
			listenToOffer(6000);
		} catch (Exception e) {
			Main.LOGGER.info(getName()+": "+ e.getMessage());
			System.exit(0);
		}
	}
	
	private void listenToOffer(int port){
		byte[] offerMessage = new byte[26];
		DatagramSocket udpSocket = null;		
		try {
			udpSocket = new DatagramSocket(port);
			udpSocket.setSoTimeout(5000);
			Main.LOGGER.info(getName()+": "+ "Listenning on UDP port : "+ port);
			DatagramPacket datagram = new DatagramPacket(offerMessage, offerMessage.length);
			udpSocket.receive(datagram);
			Main.LOGGER.info(getName()+": "+ "Got Offer message");
			readOfferMessage(datagram);
			udpSocket.close();
			Main.LOGGER.info(getName()+": "+ "UDP port has been closed");
		} catch (SocketTimeoutException e){
			udpSocket.close();
			//sendRequest(port);		
		} catch (Exception e) {
			if(udpSocket!= null)
				udpSocket.close();
		}		
	}	
	private void readOfferMessage(DatagramPacket datagram){
		byte[] offerData = datagram.getData();
		byte[] serverIpByte = new byte[4];
		byte[] serverPortByte = new byte[2];
		byte[] uniqeNumByte = new byte[4];
		byte[] serverNameByte = new byte[16];
		
		for (int i=0; i<4; i++){
			serverIpByte[i] = offerData[i+20];
		}
		for (int i=0; i<2; i++){
			serverPortByte[i] = offerData[i+24];
		}
		for (int i=0; i<4; i++){
			uniqeNumByte[i] = offerData[i+16];
		}
		for (int i=0; i<16; i++){
			serverNameByte[i] = offerData[i];
		}
		InetAddress serverIp = null;
		try {
			serverIp = InetAddress.getByAddress(serverIpByte);
		} catch (UnknownHostException e) {
			Main.LOGGER.info(getName()+": "+ e.getMessage());
			System.exit(0);
		}
		int port = ByteBuffer.wrap(serverPortByte).getInt();
		int uniqeNum = ByteBuffer.wrap(uniqeNumByte).getInt();
		String serverName = new String(serverNameByte); 		
		Main.LOGGER.info(getName()+": "+ "offer: "+ serverName+" - "+ uniqeNum+" has been recivied");
		connectToServerByTcp(serverName,serverIp,port);
	}
	
	private void connectToServerByTcp(String name, InetAddress ip, int port){
		try {
			Socket clientSocket = new Socket(ip,port);
			Main.LOGGER.info(getName()+": "+ "create TCP connection with "+name);
			setTx(true);
		} catch (IOException e) {
			Main.LOGGER.info(getName()+": "+ e.getMessage());
			System.exit(0);
		}	
	}

	public boolean isTx() {
		return tx;
	}

	public void setTx(boolean tx) {
		String status = "-off";
		if (tx)
			status = "-on";
		Main.LOGGER.info(getName()+": "+ "Tx" + status);
		this.tx = tx;
	}
	
	public String getName(){
		return name;
	}
	@Override
	public void run() {
		if (!Main.server.isRx())
			sendRequest(6000);
		
	}

}
