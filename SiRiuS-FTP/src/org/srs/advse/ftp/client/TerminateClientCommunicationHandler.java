/**
 * 
 */
package org.srs.advse.ftp.client;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @author Subin
 *
 */
public class TerminateClientCommunicationHandler implements Runnable {

	private Socket socket;
	private OutputStream outputStream;
	private DataOutputStream dataOutputStream;
	private int terminateID;

	/**
	 * @param hostname
	 * @param tPort
	 * @param terminateID
	 * @throws Exception
	 */
	public TerminateClientCommunicationHandler(String hostname, int tPort, int terminateID) throws Exception {
		this.terminateID = terminateID;

		InetAddress address = InetAddress.getByName(hostname);
		socket = new Socket();
		socket.connect(new InetSocketAddress(address.getHostAddress(), tPort), 1000);

		outputStream = socket.getOutputStream();
		dataOutputStream = new DataOutputStream(outputStream);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			dataOutputStream.writeBytes("terminate " + terminateID + "\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
