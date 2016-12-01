/**
 * 
 */
package org.srs.advse.ftp.client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
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
	
	public DownloadHandler(SRSFTPClient client,String hostname,int nPort,List<String> inputs,Path serverPath,Path path) throws Exception {
		this.client = client;
		this.inputs = inputs;
		this.serverPath = serverPath;
		this.path = path;
		
		InetAddress address = InetAddress.getByName(hostname);
		socket = new Socket();
		socket.connect(new InetSocketAddress(address.getHostAddress(), nPort),1000);
		
		inputStreamReader = new InputStreamReader(socket.getInputStream());
		bufferedReader = new BufferedReader(inputStreamReader);
		dataInputStream = new DataInputStream(socket.getInputStream());
		outputStream = socket.getOutputStream();
		dataOutputStream = new DataOutputStream(outputStream);
	}
	
	public void download() throws Exception{

	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try{
			download();
			Thread.sleep(100);
			dataOutputStream.writeBytes("quit"+"\n");
		}catch (Exception e) {
			System.out.println("download handler error");
		}

	}

}
