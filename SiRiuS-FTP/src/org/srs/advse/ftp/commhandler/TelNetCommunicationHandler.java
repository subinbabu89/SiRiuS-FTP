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
import java.util.StringTokenizer;

/**
 * @author Subin
 *
 */
public class TelNetCommunicationHandler implements Runnable {

	private DataInputStream telnetDataInputStream;
	private DataOutputStream telnetDataOutputStream;

	String ftpPath = System.getProperty("user.home") + File.separator + "ftp";

	public TelNetCommunicationHandler(Socket telnetSocket) throws Exception {
		telnetDataInputStream = new DataInputStream(telnetSocket.getInputStream());
		telnetDataOutputStream = new DataOutputStream(telnetSocket.getOutputStream());
	}

	@Override
	public void run() {
		while (true) {
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
				bufferedReader.close();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
