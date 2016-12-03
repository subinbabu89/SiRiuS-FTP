/**
 * 
 */
package org.srs.advse.ftp.commhandler;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.StringTokenizer;

import org.srs.advse.ftp.Constants;

/**
 * @author Subin
 *
 */
public class TelNetCommunicationHandler implements Runnable {

	private DataInputStream telnetDataInputStream;
	private DataOutputStream telnetDataOutputStream;

	String ftpPath = Constants.getServerPath() + File.separator + "ftp";

	public TelNetCommunicationHandler(Socket telnetSocket) throws Exception {
		telnetDataInputStream = new DataInputStream(telnetSocket.getInputStream());
		telnetDataOutputStream = new DataOutputStream(telnetSocket.getOutputStream());
	}

	@Override
	public void run() {
		boolean run = true;
		while (run) {
			try {
				boolean success = false;
				String telnet_user_string = telnetDataInputStream.readUTF();
				BufferedReader bufferedReader = new BufferedReader(
						new FileReader(ftpPath + File.separator + "users.txt"));
				String LoginInfo = new String("");

				StringTokenizer tokens = new StringTokenizer(telnet_user_string, "_");
				tokens.nextToken();
				String usertoken = tokens.nextToken();
				String password = tokens.nextToken();
				System.out.println("The username and password is " + usertoken + " " + password);

				/* Add code to add the usernames to the file */
				while ((LoginInfo = bufferedReader.readLine()) != null) {
					StringTokenizer st = new StringTokenizer(LoginInfo, " ");
					if (usertoken.equals(st.nextToken()) && password.equals(st.nextToken())) {
						success = true;
						break;
					}
				}
				telnetDataOutputStream.writeUTF(String.valueOf(success));
				if (success) {
					run = false;
					
					String ftpPath = Constants.getServerPath() + File.separator + "ftp";
					Path path = Paths.get(ftpPath + File.separator + usertoken);

					if (Files.notExists(path)) {
						Files.createDirectories(path);
					}
					telnetDataOutputStream.writeUTF(path.toString());
					telnetDataInputStream.close();
					telnetDataOutputStream.close();
				}
				bufferedReader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
