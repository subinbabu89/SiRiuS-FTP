/**
 * 
 */
package org.srs.advse.ftp;

import java.io.IOException;
import java.net.ServerSocket;

import org.srs.advse.ftp.server.SRSFTPServer;
import org.srs.advse.ftp.server.ServerDaemon;
import org.srs.advse.ftp.server.TerminateDaemon;

/**
 * @author Subin
 *
 */
public class RunServer {

	private static ServerSocket nSocket, tSocket;

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

		try {
			SRSFTPServer server = new SRSFTPServer();
			(new Thread(new ServerDaemon(server, nSocket))).start();
			(new Thread(new TerminateDaemon(server, tSocket))).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
