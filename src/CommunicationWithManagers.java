/**
 * Author  : Marc Pellet et David Villa
 * Project : labo02 - Manager
 * File    : CommunicationWithManagers.java
 * Date    : 2 déc. 2015
 */


import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Gestion de la réception des messages venant des autres managers
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
				
				//séparation des différentes parties du message
				String[] splitedMessage = message.split(":");
				String messageType = splitedMessage[0];
				
				//mise à jour de LogicalClock si nécessaire
				int logicalClock = Integer.valueOf(splitedMessage[1]);
				manager.setLogicalClock(Math.max(manager.getLogicalClock(), logicalClock) + 1);
				
				switch (messageType) {
				//en cas d'update on met juste à jour la valeur partagée
				case "UPDATE":
					manager.setSharedValue(Integer.valueOf(splitedMessage[2]));
					break;
				/* en cas de REQUEST on peut soit envoyer une REPONSE si l'expéditeur
				 * a la priorité d'accès sur le site ou si le site ne souhaite pas
				 * accéder à la section critique autrement l'ajoute à la liste
				 * des demande différées qui recevront leur RESPONSE après l'accès
				 * à la section critique		
				 */	
				case "REQUEST":
					//récupération de l'estampille et de l'id du site
					int stamp = Integer.valueOf(splitedMessage[1]);
					int sender = Integer.valueOf(splitedMessage[2]);
					
					//détérmination de la priorité en fonction de l'estampille
					if(manager.isScDemande() && (stamp > manager.getScHeure() 
							|| (stamp == manager.getScHeure() && manager.getSiteNumber() < sender))){
						//ajout du site à la liste des réponses différées
						manager.setWaitinAnswer(sender-1, true);
					}else{
						//envoie d'un message de type RESPONSE
						String response = "RESPONSE:" + manager.getLogicalClock() + ":" + manager.getSiteNumber();
						sendMessage(response, manager.getManagers().get(sender-1).getFirst(), 
								manager.getManagers().get(sender-1).getSecond());
					}
					break;
				case "RESPONSE":
					//diminution du nombre de réponse attendues
					manager.setExpectedAnswer(manager.getExpectedAnswer()-1);
					//mise à jour de la valeur partagée si elle a été transmise avec la RESPONSE
					if(splitedMessage.length == 4){
						manager.setSharedValue(Integer.valueOf(splitedMessage[3]));
					}
					/*
					 * notify que le nombre de réponse a été modifié et qu'il faut
					 * retester si le site peut accéder à la section critique
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
	 * Envoi un message a l'adresse spécifiée sur le port donné
	 * 
	 * @param text    : message à envoyer
	 * @param address : InetAddress du destinataire
	 * @param port	  : numéro de port du destinataire
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
