package network3;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.ByteBuffer;

public class Client{
	private String name;
	private String serverName;
	private boolean tx;
	private Socket tcpOpenClientSocket;
	
	
	public Client(String name){
		this.name = name;
		this.tx = false;	
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
		Main.LOGGER.info(getName()+": "+ "Request No."+randNumber+ " created");
		return messageRequest;
	}
	
	public void sendRequest(int port){
		byte[] messageRequest = createRequestMessage();
		try {
			DatagramPacket udpPacket = new DatagramPacket(messageRequest, messageRequest.length, InetAddress.getByName("255.255.255.255"), port);
			DatagramSocket udpSocket = new DatagramSocket(port);
			udpSocket.setBroadcast(true);
			udpSocket.send(udpPacket);
			Main.LOGGER.info(getName()+": "+ "Request sent to server");
			udpSocket.close();						
		} catch (Exception e) {
			Main.LOGGER.info(getName()+": "+ e.getMessage());
			System.exit(0);
		}
	}
	
	public void listenToOffer(int port){
		byte[] offerMessage = new byte[26];
		DatagramSocket udpSocket = null;		
		try {
			udpSocket = new DatagramSocket(port);
			udpSocket.setSoTimeout(1000);
			Main.LOGGER.info(getName()+": "+ "Listenning to offer on UDP port : "+ port);
			DatagramPacket datagram = new DatagramPacket(offerMessage, offerMessage.length);
			while(true){
				udpSocket.receive(datagram);
				if(datagram.getLength() == offerMessage.length)
					break;
			}
			
			Main.LOGGER.info(getName()+": "+ "Got Offer message");
			udpSocket.close();
			Main.LOGGER.info(getName()+": "+ "UDP port has been closed");		
			readOfferMessage(datagram,udpSocket.getInetAddress());		
		} catch (SocketTimeoutException e){
			udpSocket.close();
			Main.LOGGER.info(getName()+": "+ "UDP Offer timeout");
		} catch (Exception e) {
			System.out.println(e.getMessage());
			if(udpSocket!= null)
				udpSocket.close();
		}		
	}	
	private void readOfferMessage(DatagramPacket datagram,InetAddress ipSocket){
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
		InetAddress myIp = null;
		int port =0;
		int uniqeNum = 0;
		
		try {
			myIp = InetAddress.getLocalHost();
			serverIp = InetAddress.getByAddress(serverIpByte);
			port = ByteBuffer.wrap(serverPortByte).getShort();
			uniqeNum = ByteBuffer.wrap(uniqeNumByte).getInt();
			this.serverName = new String(serverNameByte); 
		} catch (UnknownHostException e) {
			Main.LOGGER.info(getName()+": "+ "problem to find ip , take from socket");
			serverIp = ipSocket;
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		if(!serverName.contains("Networking17") || serverIp.equals(myIp)){
			Main.LOGGER.info(getName()+": "+ "The offer is not from 'Networking17' !!");
			return;
		}
		Main.LOGGER.info(getName()+": "+ "offer: "+ serverName+":"+ port+ " - "+ uniqeNum+" has been recivied");
		connectToServerByTcp(serverName,serverIp,port);
	}
	
	private void connectToServerByTcp(String name, InetAddress ip, int port){
		try {
			this.tcpOpenClientSocket = new Socket(ip,port);
			Main.LOGGER.info(getName()+": "+ "create TCP connection with "+name+" :"+ip+":"+port);
			setTx(true); //tx-on-rx-off	
		} catch (IOException e) {
			Main.LOGGER.info(getName()+": "+ e.getMessage());
			System.exit(0);
		}	
	}
	
	public void sendMessageByTcp(){
		String input;
		BufferedReader inputUser = new BufferedReader( new InputStreamReader(System.in));
		try {
			System.out.print("Enter your secret: ");
			input = inputUser.readLine();
			sendMessageByTcp(input);
		} catch (IOException e) {
			Main.LOGGER.info(getName()+": "+ e.getMessage());
			System.exit(0);
		}	
	}
	
	public void sendMessageByTcp(String sentence){
		try {
			DataOutputStream outToServer = new DataOutputStream(this.tcpOpenClientSocket.getOutputStream());
			outToServer.writeBytes(sentence + '\n');
			Main.LOGGER.info(getName()+": Send Message '"+sentence+"' to "+this.serverName);
		} catch (IOException e) {
			Main.LOGGER.info(getName()+": "+ e.getMessage());
			System.exit(0);
		}	
	}
	
	public void closeTcpSocket(){
		try {
			this.tcpOpenClientSocket.close();
			Main.LOGGER.info(getName()+": Tcp socket has been closed");
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
	
	public String getTx() {
		String status = "-off";
		if (tx)
			status = "-on";
		return "Tx" + status;
	}
	
	public String getName(){
		return name+"("+Main.server.getRx()+"-"+getTx()+")";
	}

}
