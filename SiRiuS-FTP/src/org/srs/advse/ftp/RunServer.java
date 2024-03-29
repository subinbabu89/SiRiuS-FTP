package org.srs.advse.ftp;

import java.io.IOException;
import java.net.ServerSocket;

import org.srs.advse.ftp.server.SRSFTPServer;
import org.srs.advse.ftp.thread.ServerDaemon;
import org.srs.advse.ftp.thread.TelnetServerDaemon;
import org.srs.advse.ftp.thread.TerminateDaemon;

/**
 * Code file to run the server
 * 
 * @author Subin
 *
 */
public class RunServer {

	private static ServerSocket nSocket, tSocket, telnetSocket;

	/**
	 * Main function to start the code
	 * 
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
			telnetSocket = new ServerSocket(23);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		try {
			SRSFTPServer server = new SRSFTPServer();
			(new Thread(new ServerDaemon(server, nSocket))).start();
			(new Thread(new TerminateDaemon(server, tSocket))).start();
			(new Thread(new TelnetServerDaemon(telnetSocket))).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
