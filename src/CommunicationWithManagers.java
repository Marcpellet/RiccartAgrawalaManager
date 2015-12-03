/**
 * Author  : Marc Pellet et David Villa
 * Project : labo02 - Manager
 * File    : CommunicationWithManagers.java
 * Date    : 2 d�c. 2015
 */


import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Gestion de la r�ception des messages venant des autres managers
 *
 */
public class CommunicationWithManagers implements Runnable{

	private RicartAgrawala manager;
	private int port;
	
	
	public CommunicationWithManagers(RicartAgrawala manager, int port) {
		this.manager = manager;
		this.port = port;
	}
	

	@Override
	public void run() {
		try {
			@SuppressWarnings("resource")
			DatagramSocket socket = new DatagramSocket(port);
			
			while(true){
				//lecture du message
				byte[] buffer = new byte[256];
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				socket.receive(packet);
				String message = new String(packet.getData(), packet.getOffset(), packet.getLength());
				System.out.println(manager.getSiteNumber() + " - message : " + message);
				
				//s�paration des diff�rentes parties du message
				String[] splitedMessage = message.split(":");
				String messageType = splitedMessage[0];
				
				//mise � jour de LogicalClock si n�cessaire
				int logicalClock = Integer.valueOf(splitedMessage[1]);
				manager.setLogicalClock(Math.max(manager.getLogicalClock(), logicalClock) + 1);
				
				switch (messageType) {
				//en cas d'update on met juste � jour la valeur partag�e
				case "UPDATE":
					manager.setSharedValue(Integer.valueOf(splitedMessage[2]));
					break;
				/* en cas de REQUEST on peut soit envoyer une REPONSE si l'exp�diteur
				 * a la priorit� d'acc�s sur le site ou si le site ne souhaite pas
				 * acc�der � la section critique autrement l'ajoute � la liste
				 * des demande diff�r�es qui recevront leur RESPONSE apr�s l'acc�s
				 * � la section critique		
				 */	
				case "REQUEST":
					//r�cup�ration de l'estampille et de l'id du site
					int stamp = Integer.valueOf(splitedMessage[1]);
					int sender = Integer.valueOf(splitedMessage[2]);
					
					//d�t�rmination de la priorit� en fonction de l'estampille
					if(manager.isScDemande() && (stamp > manager.getScHeure() 
							|| (stamp == manager.getScHeure() && manager.getSiteNumber() < sender))){
						//ajout du site � la liste des r�ponses diff�r�es
						manager.setWaitinAnswer(sender-1, true);
					}else{
						//envoie d'un message de type RESPONSE
						String response = "RESPONSE:" + manager.getLogicalClock() + ":" + manager.getSiteNumber();
						sendMessage(response, manager.getManagers().get(sender-1).getFirst(), 
								manager.getManagers().get(sender-1).getSecond());
					}
					break;
				case "RESPONSE":
					//diminution du nombre de r�ponse attendues
					manager.setExpectedAnswer(manager.getExpectedAnswer()-1);
					//mise � jour de la valeur partag�e si elle a �t� transmise avec la RESPONSE
					if(splitedMessage.length == 4){
						manager.setSharedValue(Integer.valueOf(splitedMessage[3]));
					}
					/*
					 * notify que le nombre de r�ponse a �t� modifi� et qu'il faut
					 * retester si le site peut acc�der � la section critique
					 */
					synchronized (manager.getSync()) {
						manager.getSync().notify();
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
	
	/**
	 * 
	 * Envoi un message a l'adresse sp�cifi�e sur le port donn�
	 * 
	 * @param text    : message � envoyer
	 * @param address : InetAddress du destinataire
	 * @param port	  : num�ro de port du destinataire
	 * @throws Exception
	 */
	private void sendMessage(String text, InetAddress address, int port) throws Exception{
		byte[] buffer = text.getBytes();
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
		DatagramSocket socket = new DatagramSocket();
		socket.send(packet);
	    socket.close();
	}

}
