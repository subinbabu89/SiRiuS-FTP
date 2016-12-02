/**
 * 
 */
package org.srs.advse.ftp.thread;

import java.net.ServerSocket;

import org.srs.advse.ftp.commhandler.ServerCommunicationHandler;
import org.srs.advse.ftp.server.SRSFTPServer;

/**
 * @author Subin
 *
 */
public class ServerDaemon implements Runnable {

	private SRSFTPServer ftpServer;
	private ServerSocket socket;
	private String username;

	/**
	 * @param ftpServer
	 * @param socket
	 */
	public ServerDaemon(SRSFTPServer ftpServer, ServerSocket socket, String username) {
		super();
		this.ftpServer = ftpServer;
		this.socket = socket;
		this.username = username;
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
				new Thread(new ServerCommunicationHandler(ftpServer, socket.accept(), username)).start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}