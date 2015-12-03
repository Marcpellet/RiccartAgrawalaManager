/**
 * Author  : Marc
 * Project : labo02 - Manager
 * File    : ManagerThread.java
 * Date    : 2 d�c. 2015
 */

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


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
			// cr�ation du DatagramSocket
			DatagramSocket socket = new DatagramSocket(port);
			// le manager se met � l'�coute des messages venant des clients
			while(true){
				String receivedMessage;
				byte[] buffer = new byte[256];
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				// reception d'un message et extraction des donn�es
				socket.receive(packet);
				receivedMessage = new String(packet.getData(), packet.getOffset(), packet.getLength());
				System.out.println("receivedMessage: " + receivedMessage);
				
				String[] splitedMessage = receivedMessage.split(":");
				// les clients peuvent envoyer que deux types de messages : GET_VALUE et UPDATE_VALUE
				// dans les deux cas , le manager retourne la valeur courante (avant modification dans 
				// le cas de UPDATE_VALUE)
				buffer = ("VALUE:" +manager.getSharedValue()).getBytes();
				sendResponse(buffer, packet, socket);
				if (splitedMessage[0].equals("UPDATE_VALUE")){
					// dans le cas d'UPDATE_VALUE, le manager r�cup�re la nouvelle valeur
					int value = Integer.valueOf(splitedMessage[1]);
					// puis doit obtenir l'acc�s � la section critique
					manager.enterSC();
					// modifie la valeur avec la nouvelle valeur
					manager.setSharedValue(value);
					Thread.sleep(10000);
					// sort de la section critique
					manager.leaveSC();
					// puis retourne la valeur modifi� au client
					buffer = ("UPDATED_VALUE:" + manager.getSharedValue()).getBytes();
					sendResponse(buffer, packet, socket);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void sendResponse(byte[] buffer, DatagramPacket packet, DatagramSocket socket) throws IOException{
		// on r�cup�re les informations sur le client
		InetAddress addr = packet.getAddress();
		int port = packet.getPort();
		// on cr�e la r�ponse
		DatagramPacket response = new DatagramPacket(buffer, buffer.length, addr, port);
		// et on l'envoie
		socket.send(response);
	}

}
