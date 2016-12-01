/**
 * 
 */
package org.srs.advse.ftp.server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * @author Subin
 *
 */
public class ServerCommunicationHandler implements Runnable{
	
	private SRSFTPServer server;
	private Socket socket;
	private Path path;
	private List<String> input;
	
	private InputStreamReader commandChannelReader;
	BufferedReader commandCbuffer;
	
	private DataInputStream dataChannelInputStream;
	private DataOutputStream dataChannelOutputStream;
	private OutputStream dataOutputStream;
	
	

	/**
	 * @param server
	 * @param socket
	 */
	public ServerCommunicationHandler(SRSFTPServer server, Socket socket) {
		this.server = server;
		this.socket = socket;
		path = Paths.get(System.getProperty("user.dir"));
		
		try {
			commandChannelReader = new InputStreamReader(socket.getInputStream());
			commandCbuffer = new BufferedReader(commandChannelReader);
			dataChannelInputStream = new DataInputStream(socket.getInputStream());
			dataOutputStream = socket.getOutputStream();
			dataChannelOutputStream = new DataOutputStream(dataOutputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}



	@Override
	public void run() {
		System.out.println("Server communication thread started : "+Thread.currentThread().getName());
		finishThread:
		while(true){
			try {
				while (!commandCbuffer.ready())
					Thread.sleep(10);
				
				System.out.println("on server received: "+commandCbuffer.readLine());
				
				input = new ArrayList<String>();
				Scanner enteredInput = new Scanner(commandCbuffer.readLine());
				
				if(enteredInput.hasNext()){
					input.add(enteredInput.next());
				}
				
				if (enteredInput.hasNext())
					input.add(commandCbuffer.readLine().substring(input.get(0).length()).trim());
				enteredInput.close();
				
				System.out.println("relevant input is : "+input.get(0));
				
				switch (input.get(0)) {
				case "test":
					System.out.println("printing test in server");
					break;
				case "quit":
					break finishThread;

				default:
					break;
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
