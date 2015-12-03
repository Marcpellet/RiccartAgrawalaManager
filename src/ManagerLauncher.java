import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Author  : Marc
 * Project : labo02 - Manager
 * File    : ManagerLauncher.java
 * Date    : 2 déc. 2015
 */


public class ManagerLauncher {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Scanner reader = new Scanner(System.in);


		System.out.println("Lancement de 3 managers");
		try {
			for(int i = 1; i < 4; i++){
				RicartAgrawala manager = new RicartAgrawala(i);
				new Thread(new CommunicationWithClient(manager, (i*1000))).start();
				new Thread(new CommunicationWithManagers(manager, 1000 + i)).start();
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



	}

}
