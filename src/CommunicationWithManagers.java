/**
 * Author  : Marc
 * Project : labo02 - Manager
 * File    : CommunicationWithManagers.java
 * Date    : 2 déc. 2015
 */

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class CommunicationWithManagers implements Runnable{

	RicartAgrawala manager;
	int port;
	
	/**
	 * 
	 */
	public CommunicationWithManagers(RicartAgrawala manager, int port) {
		this.manager = manager;
		this.port = port;
	}
	

	@Override
	public void run() {
		try {
			DatagramSocket socket = new DatagramSocket(port);
			
			while(true){
				byte[] buffer = new byte[256];
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				socket.receive(packet);
				String message = new String(packet.getData(), packet.getOffset(), packet.getLength());
				System.out.println(manager.getSiteNumber() + " - message : " + message);
				String[] splitedMessage = message.split(":");
				String messageType = splitedMessage[0];
				int logicalClock = Integer.valueOf(splitedMessage[1]);
				manager.setLogicalClock(Math.max(manager.getLogicalClock(), logicalClock));
				
				switch (messageType) {
				case "UPDATE":
					manager.setSharedValue(Integer.valueOf(splitedMessage[2]));
					break;
				case "REQUEST":
					int stamp = Integer.valueOf(splitedMessage[1]);
					int sender = Integer.valueOf(splitedMessage[2]);
					if(manager.isScDemande() && (stamp > manager.getScHeure() 
							|| (stamp == manager.getScHeure() && manager.getSiteNumber() < sender))){
						manager.setWaitinAnswer(sender-1, true);
					}else{
						String response = "RESPONSE:" + manager.getLogicalClock() + ":" + manager.getSiteNumber();
						sendMessage(response, manager.getManagers().get(sender-1).getFirst(), 
								manager.getManagers().get(sender-1).getSecond());
					}
					break;
				case "RESPONSE":
					manager.setExpectedAnswer(manager.getExpectedAnswer()-1);
					if(splitedMessage.length == 4){
						manager.setSharedValue(Integer.valueOf(splitedMessage[3]));
					}
					break;
				default:
					System.out.println("Invalid message type");
					break;
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private void sendMessage(String text, InetAddress address, int port) throws Exception{
		byte[] buffer = text.getBytes();
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
		DatagramSocket socket = new DatagramSocket();
		socket.send(packet);
	    socket.close();
	}

}
