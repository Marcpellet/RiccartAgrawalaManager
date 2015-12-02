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

/**
 * @author Marc
 *
 */
public class ManagerLauncher {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Scanner reader = new Scanner(System.in);
		
		
		System.out.println("Bienvenue dans le manager");
		System.out.print("Entrez votre numéro de site: ");
		int siteNumber = reader.nextInt();
		try {
			RicartAgrawala manager = new RicartAgrawala(siteNumber);
			new Thread(new CommunicationWithClient(manager, (siteNumber*1000))).start();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		

	}

}
