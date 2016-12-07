package org.srs.advse.ftp.thread;

import java.net.ServerSocket;

import org.srs.advse.ftp.commhandler.TelNetCommunicationHandler;

/**
 * Class to act as a daemon thread for the telnet comm handler
 * 
 * @author Subin
 *
 */
public class TelnetServerDaemon implements Runnable {

	private ServerSocket telnetSocket;

	/**
	 * Constructor to initialize the class with
	 * 
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
