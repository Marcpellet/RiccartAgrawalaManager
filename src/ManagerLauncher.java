/**
 * Project : labo02 - Manager
 * File    : ManagerLauncher.java
 * Date    : 2 déc. 2015
 */


import java.net.UnknownHostException;



public class ManagerLauncher {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		System.out.println("Lancement de 3 managers");
		try {
			for(int i = 1; i < 4; i++){
				RicartAgrawala manager = new RicartAgrawala(i);
				new Thread(new CommunicationWithClient(manager, (i*1000))).start();
				new Thread(new CommunicationWithManagers(manager, 1000 + i)).start();
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}



	}

}
