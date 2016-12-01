package org.srs.advse.ftp.client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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

	/**
	 * @param client
	 * @param host
	 * @param port
	 * @throws IOException
	 */
	public ClientCommunicationHandler(SRSFTPClient client, String host, int port) throws IOException {
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

		String line;
		if (!(line = commandCbuffer.readLine()).equals("")) {
			serverPath = Paths.get(line);
		}

		userPath = Paths.get(System.getProperty("user.dir"));
	}

	@Override
	public void run() {
		Scanner scanner = new Scanner(System.in);
		String command;
		finishThread:
		while(true){
			try{
				command = scanner.nextLine().trim();
				
				input = new ArrayList<String>();
				Scanner enteredInput = new Scanner(command);
				
				if(enteredInput.hasNext()){
					input.add(enteredInput.next());
				}
				
				if (enteredInput.hasNext())
					input.add(commandCbuffer.readLine().substring(input.get(0).length()).trim());
				enteredInput.close();
				
				switch (input.get(0)) {
				case "test":
					System.out.println("printing test in client");
					break;
				case "quit":
					break finishThread;

				default:
					break;
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
