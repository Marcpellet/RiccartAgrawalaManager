import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import javax.swing.plaf.SliderUI;

/**
 * Author  : Marc
 * Project : labo02 - Manager
 * File    : ManagerThread.java
 * Date    : 2 déc. 2015
 */


public class CommunicationWithClient implements Runnable{

	RicartAgrawala manager;
	int port;
	
	/**
	 * 
	 */
	public CommunicationWithClient(RicartAgrawala manager, int port) {
		this.manager = manager;
		this.port = port;
	}

	@Override
	public void run() {
		try {
			
			DatagramSocket socket = new DatagramSocket(port);
			
			String receivedMessage;
			while(true){
				byte[] buffer = new byte[256];
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				socket.receive(packet);
				receivedMessage = new String(packet.getData(), packet.getOffset(), packet.getLength());
				System.out.println("receivedMessage: " + receivedMessage);
				
				String[] splitedMessage = receivedMessage.split(":");
				if(splitedMessage[0].equals("GET_VALUE")){
					buffer = ("VALUE:" +manager.getSharedValue()).getBytes();
				}else{
					int value = Integer.valueOf(splitedMessage[1]);
					manager.enterSC();
					manager.setSharedValue(value);
					Thread.sleep(10000);
					manager.leaveSC();
					buffer = ("UPDATED_VALUE:" + manager.getSharedValue()).getBytes();
				}

				// Obtenir l'adresse et le port du client
				InetAddress addresseClient = packet.getAddress();
				int portClient = packet.getPort();

				// Reemettre le message recu
				packet = new DatagramPacket(buffer,buffer.length,addresseClient,
						portClient);
				socket.send(packet);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
