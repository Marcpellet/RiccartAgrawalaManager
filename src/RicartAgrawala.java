/**
 * Author  : Marc Pellet & David Villa
 * Project : labo02 - Manager
 * File    : Manager.java
 * Date    : 2 d�c. 2015
 */

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;


public class RicartAgrawala {
	private int logicalClock = 1;
	private int sharedValue = 1;
	private List<Pair<InetAddress, Integer>> managers;
	private boolean[] waitingAnswer;
	private boolean scDemande = false;
	private int scHeure = 0;
	private int expectedAnswer = 0;
	private int siteNumber;
	private Object sync = new Object();
	
	public RicartAgrawala(int siteNumber) throws UnknownHostException{
		//remplissage de la liste des autres managers en dur pour 3
		managers = new ArrayList<Pair<InetAddress,Integer>>();
		InetAddress address = InetAddress.getByName("Marc-PC");
		managers.add(new Pair<InetAddress,Integer>(address, 1001));
		managers.add(new Pair<InetAddress,Integer>(address, 1002));
		managers.add(new Pair<InetAddress,Integer>(address, 1003));
		
		waitingAnswer = new boolean[managers.size()];
		

		this.siteNumber = siteNumber;
	}
	
	/**
	 * M�thode permettant d'acc�der � la section critique � la sortie de cette fonction
	 * l'appelant est en SC, il doit appeler leaveSC() pour lib�rer la SC
	 * 
	 * @throws Exception
	 */
	public void enterSC() throws Exception{
		logicalClock++;
		scHeure = logicalClock;
		scDemande = true;
		expectedAnswer = managers.size()-1;
		
		//envoi des demandes � tous les managers
		for(int i = 0; i< managers.size(); i++){
			if(siteNumber-1 != i){ // On ne veut pas s'envoyer de message � soi-m�me
				String text = "REQUEST:" + scHeure+ ":" + siteNumber;
				System.out.println("Envoi : " + text + " � " + i);
				sendMessage(text, managers.get(i).getFirst(), managers.get(i).getSecond()); 
			}
		}
		
		/*
		 * Attente des RESPONSE envoy�e par les autres sites � chaque notify
		 * envoy� par le Thread de communication avec les autres managers, test
		 * si le nombre de r�ponse attendues � atteint 0 et que le site peut
		 * acc�der � la section critique
		 */
		while(expectedAnswer > 0){
			synchronized (sync) {
				sync.wait();
				System.out.println(siteNumber + " en attente de la SC");
			}
			
		}
		
		System.out.println(siteNumber + " acc�de � la SC");
		
	}
	
	/**
	 * Methode permettant de lib�rer la SC quand nous avons fini de modifier
	 * la valeur
	 * 
	 * @throws Exception
	 */
	public void leaveSC() throws Exception{
		System.out.println(siteNumber + " lib�re la sc");
		scDemande = false;
		for(int i = 0; i < managers.size(); i++){
			if(siteNumber-1 != i){  // On ne veut pas s'envoyer de message � soi-m�me
				String message;
				/*
				 * Si le site est en attente d'une r�ponse qui a �t� diff�r�e envoie 
				 * RESPONSE au site concern� autrement un UPDATE suffit. La valeur partag�e
				 * est ajout�e au message RESPONSE afin de ne pas devoir envoyer un second
				 * message UPDATE pour renseigner le site en question de la modification
				 * de cette derni�re
				 */
				if(waitingAnswer[i]){ 
					message = "RESPONSE:" +logicalClock + ":" +siteNumber + ":" +sharedValue;
					waitingAnswer[i] = false;
				}else{ 
					message = "UPDATE:" +logicalClock + ":" + sharedValue;
				}
				sendMessage(message, managers.get(i).getFirst(), managers.get(i).getSecond());
			}
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

	/**
	 * @return the logicalClock
	 */
	public int getLogicalClock() {
		return logicalClock;
	}

	/**
	 * @param logicalClock the logicalClock to set
	 */
	public void setLogicalClock(int logicalClock) {
		this.logicalClock = logicalClock;
	}

	/**
	 * @return the sharedValue
	 */
	public int getSharedValue() {
		return sharedValue;
	}

	/**
	 * @param sharedValue the sharedValue to set
	 */
	public void setSharedValue(int sharedValue) {
		this.sharedValue = sharedValue;
	}

	/**
	 * @return the managers
	 */
	public List<Pair<InetAddress, Integer>> getManagers() {
		return managers;
	}

	/**
	 * @return the scDemande
	 */
	public boolean isScDemande() {
		return scDemande;
	}

	/**
	 * @param scDemande the scDemande to set
	 */
	public void setScDemande(boolean scDemande) {
		this.scDemande = scDemande;
	}

	/**
	 * @return the scHeure
	 */
	public int getScHeure() {
		return scHeure;
	}

	/**
	 * @param scHeure the scHeure to set
	 */
	public void setScHeure(int scHeure) {
		this.scHeure = scHeure;
	}

	/**
	 * @return the expectedAnswer
	 */
	public int getExpectedAnswer() {
		return expectedAnswer;
	}

	/**
	 * @param expectedAnswer the expectedAnswer to set
	 */
	public void setExpectedAnswer(int expectedAnswer) {
		this.expectedAnswer = expectedAnswer > 0 ? expectedAnswer : 0;
	}
	
	
	
	/**
	 * @return the siteNumber
	 */
	public int getSiteNumber() {
		return siteNumber;
	}
	
	
	/**
	 * @return the sync
	 */
	public Object getSync() {
		return sync;
	}

	/**
	 * 
	 * @param id : id of the site
	 * @param value : value to assign to the site
	 */
	public void setWaitinAnswer(int id, boolean value){
		if(id < waitingAnswer.length && id > -1){
			waitingAnswer[id] = value;
		}
	}
	

	
	
	
}
