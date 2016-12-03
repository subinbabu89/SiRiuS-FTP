/**
 * 
 */
package org.srs.advse.ftp.commhandler;

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
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.srs.advse.ftp.Constants;
import org.srs.advse.ftp.server.SRSFTPServer;

/**
 * @author Subin
 *
 */
public class ServerCommunicationHandler implements Runnable {

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
	public ServerCommunicationHandler(SRSFTPServer server, Socket socket, String username) throws Exception {
		this.server = server;
		this.socket = socket;
		// path = Paths.get(System.getProperty("user.dir"));
		 String ftpPath = Constants.getServerPath() + File.separator + "ftp";
		path = Paths.get(ftpPath + File.separator + username);

		if (Files.notExists(path)) {
			Files.createDirectories(path);
		}

		commandChannelReader = new InputStreamReader(socket.getInputStream());
		commandCbuffer = new BufferedReader(commandChannelReader);
		dataChannelInputStream = new DataInputStream(socket.getInputStream());
		dataOutputStream = socket.getOutputStream();
		dataChannelOutputStream = new DataOutputStream(dataOutputStream);
	}

	/**
	 * @throws Exception
	 */
	public void pwd() throws Exception {
		// send path
		dataChannelOutputStream.writeBytes(path + "\n");
	}

	/**
	 * @throws Exception
	 */
	public void download() throws Exception {
		if (Files.notExists(path.resolve(input.get(1)))) {
			System.out.println("file not exits");
			dataChannelOutputStream
					.writeBytes("down " + path.resolve(input.get(1)).getFileName() + " : No such thing" + "\n");
			return;
		}

		if (Files.isDirectory(path.resolve(input.get(1)))) {
			System.out.println("is directory");
			dataChannelOutputStream
					.writeBytes("down " + path.resolve(input.get(1)).getFileName() + " : is a directory" + "\n");
			return;
		}

		int lockID = server.downloadIN(path.resolve(input.get(1)));
		if (lockID == -1) {
			System.out.println("no lock id");
			dataChannelOutputStream
					.writeBytes("down " + path.resolve(input.get(1)).getFileName() + " : No such thing" + "\n");
			return;
		}

		dataChannelOutputStream.writeBytes("\n");
		dataChannelOutputStream.writeBytes(lockID + "\n");

		Thread.sleep(100);

		if (server.terminateDOWNLOAD(path.resolve(input.get(1)), lockID)) {
			System.out.println("whatever this is");
			quit();
			return;
		}

		byte[] fileBuffer = new byte[1000];
		try {
			File file = new File(path.resolve(input.get(1)).toString());
			long fileSize = file.length();
			byte[] fileSizeBytes = ByteBuffer.allocate(8).putLong(fileSize).array();
			dataChannelOutputStream.write(fileSizeBytes, 0, 8);

			if (server.terminateDOWNLOAD(path.resolve(input.get(1)), lockID)) {
				System.out.println("whatever this is");
				quit();
				return;
			}

			BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
			int count = 0;
			while ((count = bufferedInputStream.read(fileBuffer)) > 0) {
				if (server.terminateDOWNLOAD(path.resolve(input.get(1)), lockID)) {
					System.out.println("whatever this is");
					bufferedInputStream.close();
					quit();
					return;
				}
				dataChannelOutputStream.write(fileBuffer, 0, count);
			}
			bufferedInputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		server.downloadOUT(path.resolve(input.get(1)), lockID);

	}

	/**
	 * @throws Exception
	 */
	public void upload() throws Exception {
		int lockID = server.uploadIN_ID(path.resolve(input.get(1)));

		dataChannelOutputStream.writeBytes(lockID + "\n");

		while (!server.uploadIN(path.resolve(input.get(1)), lockID)) {
			Thread.sleep(10);
		}

		if (server.terminateUPLOAD(path.resolve(input.get(1)), lockID)) {
			quit();
			return;
		}

		dataChannelOutputStream.writeBytes("\n");

		if (server.terminateUPLOAD(path.resolve(input.get(1)), lockID)) {
			quit();
			return;
		}

		byte[] fileSizeBuffer = new byte[8];
		dataChannelInputStream.read(fileSizeBuffer);
		ByteArrayInputStream bis = new ByteArrayInputStream(fileSizeBuffer);
		DataInputStream dis = new DataInputStream(bis);
		long fileSize = dis.readLong();
		
		System.out.println("filesize is "+fileSize);

		if (server.terminateUPLOAD(path.resolve(input.get(1)), lockID)) {
			quit();
			return;
		}

		FileOutputStream fileOutputStream = new FileOutputStream(
				new File(path + File.separator + input.get(1)).toString());
		System.out.println("path is : "+path + File.separator + input.get(1));
		int count = 0;
		byte[] filebuffer = new byte[1000];
		long bytesReceived = 0;
		while (bytesReceived < fileSize) {
			if (server.terminateUPLOAD(path.resolve(input.get(1)), lockID)) {
				fileOutputStream.close();
				quit();
				return;
			}
			count = dataChannelInputStream.read(filebuffer);
			fileOutputStream.write(filebuffer, 0, count);
			bytesReceived += count;
		}
		fileOutputStream.close();

		server.uploadOUT(path.resolve(input.get(1)), lockID);
	}

	public void list() throws Exception {
		try {
			DirectoryStream<Path> dirStream = Files.newDirectoryStream(path);
			for (Path entry : dirStream)
				dataChannelOutputStream.writeBytes(entry.getFileName() + "\n");
			dataChannelOutputStream.writeBytes("\n");
		} catch (Exception e) {
			dataChannelOutputStream.writeBytes("list: failed" + "\n");
			dataChannelOutputStream.writeBytes("\n");
		}
	}

	/**
	 * @throws IOException
	 * @throws Exception
	 */
	private void quit() throws Exception {
		socket.close();
		throw new Exception();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		System.out.println("Server communication thread started : " + Thread.currentThread().getName());
		finishThread: while (true) {
			try {
				while (!commandCbuffer.ready())
					Thread.sleep(10);

				input = new ArrayList<String>();
				String command = commandCbuffer.readLine();
				Scanner enteredInput = new Scanner(command);

				if (enteredInput.hasNext()) {
					input.add(enteredInput.next());
				}

				if (enteredInput.hasNext())
					input.add(command.substring(input.get(0).length()).trim());
				enteredInput.close();

				switch (input.get(0)) {
				case "down":
					download();
					break;

				case "up":
					upload();
					break;

				case "pwd":
					pwd();
					break;

				case "test":
					System.out.println("printing test in server");
					break;

				case "list":
					list();
					break;

				case "quit":
					break finishThread;

				default:
					System.out.println("invalid command");
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (Exception e) {
			}
		}
	}

}
