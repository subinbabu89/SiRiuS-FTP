/**
 * 
 */
package org.srs.advse.ftp.client;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.file.Files;
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

	/**
	 * @param client
	 * @param hostname
	 * @param nPort
	 * @param inputs
	 * @param serverPath
	 * @throws Exception
	 */
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

	/**
	 * @throws Exception
	 */
	public void upload() throws Exception {
		if (!client.transfer(serverPath.resolve(inputs.get(1)))) {
			System.out.println("file already transfering");
			return;
		}

		if (Files.notExists(path.resolve(inputs.get(1)))) {
			System.out.println("no such file or directory");
		} else if (Files.isDirectory(path.resolve(inputs.get(1)))) {
			System.out.println("cannot upload directory");
		} else {
			dataOutputStream.writeBytes("put " + serverPath.resolve(inputs.get(1)) + "\n");

			try {
				terminateID = Integer.parseInt(bufferedReader.readLine());
			} catch (Exception e) {
				System.out.println("invalid terminate ID");
			}

			client.transferIN(serverPath.resolve(inputs.get(1)), terminateID);

			if (client.terminatePUT(serverPath.resolve(inputs.get(0)), terminateID)) {
				return;
			}

			bufferedReader.readLine();
			Thread.sleep(100);

			if (client.terminatePUT(serverPath.resolve(inputs.get(0)), terminateID)) {
				return;
			}

			byte[] uploadBuffer = new byte[1000];

			try {
				File file = new File(path.resolve(inputs.get(1)).toString());
				long fileSize = file.length();
				byte[] fileSizeBytes = ByteBuffer.allocate(8).putLong(fileSize).array();
				dataOutputStream.write(fileSizeBytes, 0, 8);

				if (client.terminatePUT(serverPath.resolve(inputs.get(1)), terminateID)) {
					return;
				}

				BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
				int count = 0;
				while ((count = bufferedInputStream.read()) > 0) {
					if (client.terminatePUT(serverPath.resolve(inputs.get(1)), terminateID)) {
						bufferedInputStream.close();
						return;
					}
					dataOutputStream.write(uploadBuffer, 0, count);
				}
				bufferedInputStream.close();
			} catch (Exception e) {
				System.out.println("transfer error");
			}

			client.transferOUT(serverPath.resolve(inputs.get(1)), terminateID);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			upload();
			Thread.sleep(100);
			dataOutputStream.writeBytes("quit" + "\n");
		} catch (Exception e) {
			System.out.println("upload handler error");
		}
	}

}
