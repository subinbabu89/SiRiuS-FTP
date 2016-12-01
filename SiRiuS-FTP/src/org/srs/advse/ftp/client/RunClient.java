/**
 * 
 */
package org.srs.advse.ftp.client;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author Subin
 *
 */
public class RunClient {
	public static int nPort, tPort;
	public static String hostname;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			InetAddress.getByName(hostname);
			hostname = args[0];
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		nPort = Integer.parseInt(args[1]);
		tPort = Integer.parseInt(args[2]);
		
		try {
			SRSFTPClient client = new SRSFTPClient();
			(new Thread(new ClientCommunicationHandler(client, hostname, nPort))).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
