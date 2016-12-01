/**
 * 
 */
package org.srs.advse.ftp.client;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Path;
import java.util.List;

/**
 * @author Subin
 *
 */
public class DownloadHandler implements Runnable {

	private SRSFTPClient client;
	private Socket socket;
	private Path path, serverPath;
	private List<String> inputs;
	private int terminateID;

	private InputStreamReader inputStreamReader;
	private BufferedReader bufferedReader;
	private DataInputStream dataInputStream;
	private OutputStream outputStream;
	private DataOutputStream dataOutputStream;

	/**
	 * @param client
	 * @param hostname
	 * @param nPort
	 * @param inputs
	 * @param serverPath
	 * @param path
	 * @throws Exception
	 */
	public DownloadHandler(SRSFTPClient client, String hostname, int nPort, List<String> inputs, Path serverPath,
			Path path) throws Exception {
		this.client = client;
		this.inputs = inputs;
		this.serverPath = serverPath;
		this.path = path;

		InetAddress address = InetAddress.getByName(hostname);
		socket = new Socket();
		socket.connect(new InetSocketAddress(address.getHostAddress(), nPort), 1000);

		inputStreamReader = new InputStreamReader(socket.getInputStream());
		bufferedReader = new BufferedReader(inputStreamReader);
		dataInputStream = new DataInputStream(socket.getInputStream());
		outputStream = socket.getOutputStream();
		dataOutputStream = new DataOutputStream(outputStream);
	}

	/**
	 * @throws Exception
	 */
	public void download() throws Exception {
		if (!client.transfer(serverPath.resolve(inputs.get(1)))) {
			System.out.println("File already transfering");
			return;
		}

		dataOutputStream.writeBytes("get " + serverPath.resolve(inputs.get(1)) + "\n");

		String line;
		if (!(line = bufferedReader.readLine()).equals("")) {
			System.out.println(line);
			return;
		}

		try {
			terminateID = Integer.parseInt(bufferedReader.readLine());
		} catch (Exception e) {
			System.out.println("invalid terminate ID");
		}

		client.transferIN(serverPath.resolve(inputs.get(1)), terminateID);

		if (client.terminateGET(path.resolve(inputs.get(1)), serverPath.resolve(inputs.get(1)), terminateID)) {
			return;
		}

		byte[] filebuffer = new byte[8];
		dataInputStream.read(filebuffer);
		ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(filebuffer);
		DataInputStream dis = new DataInputStream(arrayInputStream);
		long fileSize = dis.readLong();

		if (client.terminateGET(path.resolve(inputs.get(1)), serverPath.resolve(inputs.get(1)), terminateID)) {
			return;
		}

		FileOutputStream fileOutputStream = new FileOutputStream(new File(inputs.get(1)));
		int count = 0;
		byte[] buffer = new byte[8192];
		long bytesReceived = 0;
		while (bytesReceived < fileSize) {
			if (client.terminateGET(path.resolve(inputs.get(1)), serverPath.resolve(inputs.get(1)), terminateID)) {
				fileOutputStream.close();
				return;
			}
			count = dataInputStream.read(buffer);
			fileOutputStream.write(buffer, 0, count);
			bytesReceived += count;
		}
		fileOutputStream.close();

		client.transferOUT(serverPath.resolve(inputs.get(1)), terminateID);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			download();
			Thread.sleep(100);
			dataOutputStream.writeBytes("quit" + "\n");
		} catch (Exception e) {
			System.out.println("download handler error");
		}

	}

}
