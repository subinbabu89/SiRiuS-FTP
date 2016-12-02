/**
 * 
 */
package org.srs.advse.ftp.client;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * @author Subin
 *
 */
public class ClientCommunicationHandler implements Runnable {

	private InputStreamReader commandChannelReader;
	BufferedReader commandCbuffer;

	private DataInputStream dataChannelInputStream;
	private DataOutputStream dataChannelOutputStream;
	private OutputStream dataOutputStream;

	private SRSFTPClient client;
	private String host;
	private int port;
	private List<String> input;

	private Socket socket;
	private Path serverPath, userPath;

	private int terminateID;

	/**
	 * @param client
	 * @param host
	 * @param port
	 * @throws IOException
	 */
	public ClientCommunicationHandler(SRSFTPClient client, String host, int port, String clientDir) throws IOException {
		this.client = client;
		this.host = host;
		this.port = port;

		InetAddress hostAddress = InetAddress.getByName(host);
		socket = new Socket();
		socket.connect(new InetSocketAddress(hostAddress.getHostAddress(), port), 1000);

		commandChannelReader = new InputStreamReader(socket.getInputStream());
		commandCbuffer = new BufferedReader(commandChannelReader);

		dataChannelInputStream = new DataInputStream(socket.getInputStream());

		dataOutputStream = socket.getOutputStream();
		dataChannelOutputStream = new DataOutputStream(dataOutputStream);

		dataChannelOutputStream.writeBytes("pwd" + "\n");
		String line;
		if (!(line = commandCbuffer.readLine()).equals("")) {
			serverPath = Paths.get(line);
		}

		// userPath = Paths.get(System.getProperty("user.dir"));
		userPath = Paths.get(clientDir);
		System.out.println("Connected to: " + hostAddress);
	}

	/**
	 * @throws Exception
	 */
	public void pwd() throws Exception {
		// only one argument
		if (input.size() != 1) {
			invalid();
			return;
		}

		// send command
		dataChannelOutputStream.writeBytes("pwd" + "\n");

		// message
		System.out.println(commandCbuffer.readLine());
	}

	/**
	 * 
	 */
	public void invalid() {
		System.out.println("Invalid Arguments");
		System.out.println("Try `help' for more information.");
	}

	public void get() throws Exception {
		System.out.println("tempPath" + Paths.get(serverPath.toString()));
		System.out.println("tempPathClient" + Paths.get(userPath.toString()));
		if (input.get(1).endsWith(" &")) {
			input.set(1, input.get(1).substring(0, input.get(1).length() - 1).trim());

			List<String> list = new ArrayList<String>(input);
			Path tempServerPath = Paths.get(serverPath.toString());
			Path tempClientPath = Paths.get(userPath.toString());

			(new Thread(new DownloadHandler(client, host, port, list, tempServerPath, tempClientPath))).start();
			;

			Thread.sleep(50);

			return;
		}

		if (!client.transfer(serverPath.resolve(input.get(1)))) {
			System.out.println("file already downloading");
			return;
		}

//		dataChannelOutputStream.writeBytes("get " + serverPath.resolve(input.get(1)) + "\n");
		dataChannelOutputStream.writeBytes("get " + input.get(1) + "\n");

		String line;
		if (!(line = commandCbuffer.readLine()).equals("")) {
			System.out.println(line);
			return;
		}

		try {
			terminateID = Integer.parseInt(commandCbuffer.readLine());
		} catch (Exception e) {
			e.printStackTrace();
		}

		client.transferIN(serverPath.resolve(input.get(1)), terminateID);

		byte[] fileSizeBuffer = new byte[8];
		dataChannelInputStream.read(fileSizeBuffer);
		ByteArrayInputStream bis = new ByteArrayInputStream(fileSizeBuffer);
		DataInputStream dis = new DataInputStream(bis);
		long fileSize = dis.readLong();

//		FileOutputStream fileOutputStream = new FileOutputStream(new File(input.get(1)));
		FileOutputStream fileOutputStream = new FileOutputStream(new File(userPath + File.separator + input.get(1)));
		int count = 0;
		byte[] filebuffer = new byte[1000];
		long bytesReceived = 0;
		while (bytesReceived < fileSize) {
			count = dataChannelInputStream.read(filebuffer);
			fileOutputStream.write(filebuffer, 0, count);
			bytesReceived += count;
		}
		fileOutputStream.close();

		client.transferOUT(serverPath.resolve(input.get(1)), terminateID);
	}

	public void put() throws Exception {
		System.out.println("tempPath" + Paths.get(serverPath.toString()));
		System.out.println("tempPathClient" + Paths.get(userPath.toString()));
		if (input.get(1).endsWith(" &")) {
			input.set(1, input.get(1).substring(0, input.get(1).length() - 1).trim());

			List<String> list = new ArrayList<String>();
			Path tempServerPath = Paths.get(serverPath.toString());

			(new Thread(new UploadHandler(client, host, port, list, tempServerPath))).start();

			Thread.sleep(50);

			return;
		}

		if (!client.transfer(serverPath.resolve(input.get(1)))) {
			System.out.println("already downloading");
			return;
		}

		if (Files.notExists(userPath.resolve(input.get(1)))) {
			System.out.println("no such file");
		} else if (Files.isDirectory(userPath.resolve(input.get(1)))) {
			System.out.println("is a directory");
		} else {
			dataChannelOutputStream.writeBytes("put " + input.get(1) + "\n");
//			dataChannelOutputStream.writeBytes("put " + serverPath.resolve(input.get(1)) + "\n");

			try {
				terminateID = Integer.parseInt(commandCbuffer.readLine());
			} catch (Exception e) {
				e.printStackTrace();
			}

			client.transferIN(serverPath.resolve(input.get(1)), terminateID);

			commandCbuffer.readLine();

			Thread.sleep(100);

			byte[] fileBuffer = new byte[1000];
			try {
				File file = new File(userPath.resolve(input.get(1)).toString());

				long fileSize = file.length();
				byte[] fileSizeBytes = ByteBuffer.allocate(8).putLong(fileSize).array();
				dataChannelOutputStream.write(fileSizeBytes, 0, 8);

				Thread.sleep(100);

				BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
				int count = 0;
				while ((count = bis.read(fileBuffer)) > 0) {
					dataChannelOutputStream.write(fileBuffer, 0, count);
				}
				bis.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			client.transferOUT(serverPath.resolve(input.get(1)), terminateID);
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
			Scanner scanner = new Scanner(System.in);
			String command;
			do {
				System.out.print("srsftp >");
				command = scanner.nextLine();
				command = command.trim();

				input = new ArrayList<String>();
				Scanner enteredInput = new Scanner(command);

				if (enteredInput.hasNext()) {
					input.add(enteredInput.next());
				}

				if (enteredInput.hasNext())
					input.add(command.substring(input.get(0).length()).trim());
				enteredInput.close();

				if (input.isEmpty())
					continue;

				System.out.println(input.get(0));
				switch (input.get(0)) {
				case "test":
					System.out.println("printing test in client");
					break;
				case "get":
					get();
					break;
				case "put":
					put();
					break;
				case "quit":
					break;
				case "pwd":
					pwd();
					break;
				default:
					System.out.println("unrecognized command");
				}
			} while (!command.equalsIgnoreCase("quit"));
			scanner.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
