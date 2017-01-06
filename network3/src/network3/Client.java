package network3;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
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
			Main.LOGGER.info(getName()+": "+ "Message Request has been created and sent");
		} catch (UnknownHostException e) {
			Main.LOGGER.info(getName()+": "+ e.getMessage());
			System.exit(0);
		}
	}

	public boolean isTx() {
		return tx;
	}

	public void setTx(boolean tx) {
		this.tx = tx;
	}
	
	public String getName(){
		return name;
	}
	@Override
	public void run() {
		sendRequest(6000);
	}

}
