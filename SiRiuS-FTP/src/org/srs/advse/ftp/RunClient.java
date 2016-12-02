/**
 * 
 */
package org.srs.advse.ftp;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.srs.advse.ftp.client.ClientCommunicationHandler;
import org.srs.advse.ftp.client.SRSFTPClient;

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
			hostname = args[0];
			InetAddress.getByName(hostname);
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
