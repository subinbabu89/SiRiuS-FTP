package org.srs.advse.ftp.thread;

import java.net.ServerSocket;

import org.srs.advse.ftp.commhandler.ServerCommunicationHandler;
import org.srs.advse.ftp.server.SRSFTPServer;

/**
 * Class to act as a daemon thread for the server comm handler
 * 
 * @author Subin
 *
 */
public class ServerDaemon implements Runnable {

	private SRSFTPServer ftpServer;
	private ServerSocket socket;

	/**
	 * Constructor to initialize the class with
	 * 
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
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
