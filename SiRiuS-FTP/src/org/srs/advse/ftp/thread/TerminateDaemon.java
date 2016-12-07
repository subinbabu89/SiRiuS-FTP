package org.srs.advse.ftp.thread;

import java.net.ServerSocket;

import org.srs.advse.ftp.commhandler.TerminateServerCommunicationHandler;
import org.srs.advse.ftp.server.SRSFTPServer;

/**
 * Class to act as a daemon thread for the terminate comm handler
 * 
 * @author Subin
 *
 */
public class TerminateDaemon implements Runnable {

	private SRSFTPServer server;
	private ServerSocket serverSocket;

	/**
	 * Constructor to initialize the class with
	 * 
	 * @param server
	 * @param serverSocket
	 */
	public TerminateDaemon(SRSFTPServer server, ServerSocket serverSocket) {
		this.server = server;
		this.serverSocket = serverSocket;
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
				(new Thread(new TerminateServerCommunicationHandler(server, serverSocket.accept()))).start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
