import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * Author  : Marc
 * Project : labo02 - Manager
 * File    : CommunicationWithManagers.java
 * Date    : 2 déc. 2015
 */

/**
 * @author Marc
 *
 */
public class CommunicationWithManagers implements Runnable{

	RicartAgrawala manager;
	int port;
	
	/**
	 * 
	 */
	public CommunicationWithManagers(RicartAgrawala manager, int port) {
		this.manager = manager;
		this.port = port;
	}
	

	@Override
	public void run() {
		try {
			DatagramSocket socket = new DatagramSocket(port);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
