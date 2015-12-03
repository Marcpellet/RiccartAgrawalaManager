/**
 * Author  : Marc
 * Project : labo02 - Manager
 * File    : Manager.java
 * Date    : 2 déc. 2015
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
	
	public RicartAgrawala(int siteNumber) throws UnknownHostException{
		managers = new ArrayList<Pair<InetAddress,Integer>>();
		InetAddress address = InetAddress.getByName("Marc-PC");
		managers.add(new Pair<InetAddress,Integer>(address, 1001));
		managers.add(new Pair<InetAddress,Integer>(address, 1002));
		managers.add(new Pair<InetAddress,Integer>(address, 1003));
		
		waitingAnswer = new boolean[managers.size()];
		

		this.siteNumber = siteNumber;
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
	
	public void setWaitinAnswer(int id, boolean value){
		if(id < waitingAnswer.length && id > -1){
			waitingAnswer[id] = value;
		}
	}

	public void enterSC() throws Exception{
		logicalClock++;
		scHeure = logicalClock;
		scDemande = true;
		expectedAnswer = managers.size()-1;
		
		//envoi des demandes à tous les managers
		for(int i = 0; i< managers.size(); i++){
			if(siteNumber-1 != i){
				String text = "REQUEST:" + scHeure+ ":" + siteNumber;
				System.out.println("Envoi : " + text + " à " + i);
				sendMessage(text, managers.get(i).getFirst(), managers.get(i).getSecond()); 
			}
		}
		
		while(expectedAnswer > 0){
			Thread.sleep(1000);
			System.out.println(siteNumber + " en attente de la SC");
		}
		
		System.out.println(siteNumber + " accède à la SC");
		
	}
	
	public void leaveSC() throws Exception{
		scDemande = false;
		for(int i = 0; i < managers.size(); i++){
			if(siteNumber-1 != i){
				String message;
				if(waitingAnswer[i]){
					message = "RESPONSE:" +logicalClock + ":" +siteNumber + ":" +sharedValue;
				}else{
					message = "UPDATE:" +logicalClock + ":" + sharedValue;
				}
				sendMessage(message, managers.get(i).getFirst(), managers.get(i).getSecond());
			}
		}
		System.out.println(siteNumber + " libère la sc");
		
	}
	
	private void sendMessage(String text, InetAddress address, int port) throws Exception{
		byte[] buffer = text.getBytes();
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
		DatagramSocket socket = new DatagramSocket();
		socket.send(packet);
	    socket.close();
	}
	
	
	
}
