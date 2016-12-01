package org.srs.advse.ftp.server;

import java.net.ServerSocket;

public class TerminateDaemon implements Runnable {

	private SRSFTPServer server;
	private ServerSocket serverSocket;

	/**
	 * @param server
	 * @param serverSocket
	 */
	public TerminateDaemon(SRSFTPServer server, ServerSocket serverSocket) {
		this.server = server;
		this.serverSocket = serverSocket;
	}

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
