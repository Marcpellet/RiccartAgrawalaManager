/**
 * Author  : Marc Pellet et David Villa
 * Project : labo02 - Manager
 * File    : ManagerThread.java
 * Date    : 2 d�c. 2015
 * 
 * DESCRIPTION :
 * 
 * Cette classe g�re la communication avec les clients connect�s au manager.
 * 
 * Elle s'occupe de r�cup�rer les requ�tes des clients et de r�pondre en cons�quence.
 * 
 * CONCEPTION :
 * 
 * Type de message :
 * Chaque op�ration n�cessite l'�change de diff�rents types de message entre le client et le manager 
 * (une requ�te et la r�ponse associ�e).
 * 
 * Nous avons donc d�finit les messages suivants :
 * Op�ration d'affichage :
 * GET_VALUE : aucun param�tre - signifie au manager que l'utilisateur souhaite obtenir la valeur
 * VALUE     : int value - reponse du serveur contenant la valeur demand�e
 * 
 * Op�ration de modification :
 * UPDATE_VALUE  : int newValue - signifie au manager que l'utilisateur souhaite modifie la valeur
 * 						et la remplacer par la valeur 'newValue'
 * VALUE         : int value - reponse du serveur contenant la valeur avant modification
 * VALUE_UPDATED : int updatedValue - r�ponse du serveur contenant la valeur modifi�e. Cette r�ponse
 * 						est envoy�e uniquement lorsque l'op�ration est termin�e
 * Remarques :
 * Comme l'op�ration de modification est bloquante tant qu'elle ne s'est pas ex�cut�e, il n'y a pas
 * besoin d'ajouter de message pour signifier que la section critique n'est pas encore disponible.
 * 
 * Il est aussi a noter que lorsqu'un client demande une modification de la valeur en section critique,
 * le manager lui envoie un message VALUE contenant la valeur avant modification puis un message 
 * VALUE_UPDATED contenant la valeur apr�s modification.
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
	 * Constructeur de la classe, il n�cessite le manager Ricart-Agrawala pour la gestion de la 
	 * section critique ainsi que le port du socket du manager de communication avec le client.
	 * 
	 * @param manager - RicartAgrawala : manager de la section critique
	 * @param port - int : port d'�coute du manager de communication avec le client
	 */
	public CommunicationWithClient(RicartAgrawala manager, int port) {
		this.manager = manager;
		this.port = port;
	}
	
	/**
	 * Initialise le Datagram socket puis �coute en boucle les requ�tes des clients et y 
	 * r�pond en cons�quence.
	 */
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
	
	/**
	 * 
	 * @param buffer - byte[] : buffer contenant le message
	 * @param packet - DatagramPacket : paquet re�u par le manager. Ce dernier est utilis� pour extraire les donn�es
	 * 									concernant le client (InetAddress et port)
	 * @param socket - DatagramSocket : socket utilis� pour envoy� la r�ponse
	 * @throws IOException - en cas d'erreur lors de l'envoi ou la reception d'un message
	 */
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
