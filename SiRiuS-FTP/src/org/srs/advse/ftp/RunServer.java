/**
 * 
 */
package org.srs.advse.ftp;

import java.io.IOException;
import java.net.ServerSocket;

import org.srs.advse.ftp.server.SRSFTPServer;
import org.srs.advse.ftp.thread.ServerDaemon;
import org.srs.advse.ftp.thread.TerminateDaemon;

/**
 * @author Subin
 *
 */
public class RunServer {

	private static ServerSocket nSocket, tSocket;
	private static String username;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		int nPort = 0;
		nPort = Integer.parseInt(args[0]);

		int tPort = 0;
		tPort = Integer.parseInt(args[1]);

		try {
			nSocket = new ServerSocket(nPort);
			tSocket = new ServerSocket(tPort);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		username = args[2];
		
		try {
			SRSFTPServer server = new SRSFTPServer();
			(new Thread(new ServerDaemon(server, nSocket,username))).start();
			(new Thread(new TerminateDaemon(server, tSocket))).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
