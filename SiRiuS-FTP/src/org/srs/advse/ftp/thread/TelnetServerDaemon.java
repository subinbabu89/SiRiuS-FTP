/**
 * 
 */
package org.srs.advse.ftp.thread;

import java.net.ServerSocket;

import org.srs.advse.ftp.commhandler.TelNetCommunicationHandler;

/**
 * @author Subin
 *
 */
public class TelnetServerDaemon implements Runnable {

	private ServerSocket telnetSocket;

	/**
	 * @param telnetSocket
	 */
	public TelnetServerDaemon(ServerSocket telnetSocket) {
		super();
		this.telnetSocket = telnetSocket;
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
				(new Thread(new TelNetCommunicationHandler(telnetSocket.accept()))).start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

}
