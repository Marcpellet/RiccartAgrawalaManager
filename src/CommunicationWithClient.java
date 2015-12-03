/**
 * Author  : Marc Pellet et David Villa
 * Project : labo02 - Manager
 * File    : ManagerThread.java
 * Date    : 2 déc. 2015
 * 
 * DESCRIPTION :
 * 
 * Cette classe gère la communication avec les clients connectés au manager.
 * 
 * Elle s'occupe de récupérer les requêtes des clients et de répondre en conséquence.
 * 
 * CONCEPTION :
 * 
 * Type de message :
 * Chaque opération nécessite l'échange de différents types de message entre le client et le manager 
 * (une requête et la réponse associée).
 * 
 * Nous avons donc définit les messages suivants :
 * Opération d'affichage :
 * GET_VALUE : aucun paramètre - signifie au manager que l'utilisateur souhaite obtenir la valeur
 * VALUE     : int value - reponse du serveur contenant la valeur demandée
 * 
 * Opération de modification :
 * UPDATE_VALUE  : int newValue - signifie au manager que l'utilisateur souhaite modifie la valeur
 * 						et la remplacer par la valeur 'newValue'
 * VALUE         : int value - reponse du serveur contenant la valeur avant modification
 * VALUE_UPDATED : int updatedValue - réponse du serveur contenant la valeur modifiée. Cette réponse
 * 						est envoyée uniquement lorsque l'opération est terminée
 * Remarques :
 * Comme l'opération de modification est bloquante tant qu'elle ne s'est pas exécutée, il n'y a pas
 * besoin d'ajouter de message pour signifier que la section critique n'est pas encore disponible.
 * 
 * Il est aussi a noter que lorsqu'un client demande une modification de la valeur en section critique,
 * le manager lui envoie un message VALUE contenant la valeur avant modification puis un message 
 * VALUE_UPDATED contenant la valeur après modification.
 * 
 */

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class CommunicationWithClient implements Runnable{

	private RicartAgrawala manager;
	private int port;
	
	/**
	 * Constructeur de la classe, il nécessite le manager Ricart-Agrawala pour la gestion de la 
	 * section critique ainsi que le port du socket du manager de communication avec le client.
	 * 
	 * @param manager - RicartAgrawala : manager de la section critique
	 * @param port - int : port d'écoute du manager de communication avec le client
	 */
	public CommunicationWithClient(RicartAgrawala manager, int port) {
		this.manager = manager;
		this.port = port;
	}
	
	/**
	 * Initialise le Datagram socket puis écoute en boucle les requêtes des clients et y 
	 * répond en conséquence.
	 */
	@Override
	public void run() {
		try {
			// création du DatagramSocket
			DatagramSocket socket = new DatagramSocket(port);
			// le manager se met à l'écoute des messages venant des clients
			while(true){
				String receivedMessage;
				byte[] buffer = new byte[256];
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				// reception d'un message et extraction des données
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
					// dans le cas d'UPDATE_VALUE, le manager récupère la nouvelle valeur
					int value = Integer.valueOf(splitedMessage[1]);
					// puis doit obtenir l'accès à la section critique
					manager.enterSC();
					// modifie la valeur avec la nouvelle valeur
					manager.setSharedValue(value);
					Thread.sleep(10000);
					// sort de la section critique
					manager.leaveSC();
					// puis retourne la valeur modifié au client
					buffer = ("UPDATED_VALUE:" + manager.getSharedValue()).getBytes();
					sendResponse(buffer, packet, socket);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param buffer - byte[] : buffer contenant le message
	 * @param packet - DatagramPacket : paquet reçu par le manager. Ce dernier est utilisé pour extraire les données
	 * 									concernant le client (InetAddress et port)
	 * @param socket - DatagramSocket : socket utilisé pour envoyé la réponse
	 * @throws IOException - en cas d'erreur lors de l'envoi ou la reception d'un message
	 */
	private void sendResponse(byte[] buffer, DatagramPacket packet, DatagramSocket socket) throws IOException{
		// on récupère les informations sur le client
		InetAddress addr = packet.getAddress();
		int port = packet.getPort();
		// on crée la réponse
		DatagramPacket response = new DatagramPacket(buffer, buffer.length, addr, port);
		// et on l'envoie
		socket.send(response);
	}

}
