/**
 * 
 */
package org.srs.advse.ftp.client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author Subin
 *
 */
public class UploadHandler implements Runnable {

	private SRSFTPClient client;
	private Socket socket;
	private Path path, serverPath;
	private List<String> inputs;
	private int terminateID;

	private InputStreamReader inputStreamReader;
	private BufferedReader bufferedReader;
	private OutputStream outputStream;
	private DataOutputStream dataOutputStream;

	public UploadHandler(SRSFTPClient client, String hostname, int nPort, List<String> inputs, Path serverPath)
			throws Exception {
		this.client = client;
		this.inputs = inputs;
		this.serverPath = serverPath;

		InetAddress address = InetAddress.getByName(hostname);
		socket = new Socket();
		socket.connect(new InetSocketAddress(address.getHostAddress(), nPort), 1000);

		inputStreamReader = new InputStreamReader(socket.getInputStream());
		bufferedReader = new BufferedReader(inputStreamReader);
		outputStream = socket.getOutputStream();
		dataOutputStream = new DataOutputStream(outputStream);

		path = Paths.get(System.getProperty("user.dir"));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

}
