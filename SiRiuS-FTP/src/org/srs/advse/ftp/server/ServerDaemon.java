/**
 * 
 */
package org.srs.advse.ftp.server;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * @author Subin
 *
 */
public class ServerDaemon implements Runnable {

	private SRSFTPServer ftpServer;
	private ServerSocket socket;

	/**
	 * @param ftpServer
	 * @param socket
	 */
	public ServerDaemon(SRSFTPServer ftpServer, ServerSocket socket) {
		super();
		this.ftpServer = ftpServer;
		this.socket = socket;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {

		while (true) {
			try {
				new Thread(new ServerCommunicationHandler(ftpServer, socket.accept())).start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
