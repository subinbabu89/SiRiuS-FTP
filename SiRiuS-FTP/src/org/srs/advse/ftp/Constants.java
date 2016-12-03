/**
 * 
 */
package org.srs.advse.ftp;

/**
 * @author Subin
 *
 */
public class Constants {

	enum WORKING_MODE {
		EC2, LOCAL
	}

	private static WORKING_MODE currentMode = WORKING_MODE.EC2;

	public static String getServerPath() {
		if (currentMode == WORKING_MODE.EC2) {
			return "/home/ubuntu";
		} else {
			return System.getProperty("user.home");
		}
	}
}
