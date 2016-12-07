package org.srs.advse.ftp;

import java.io.File;

/**
 * 
 * Constants file for the server code
 * 
 * @author Subin
 *
 */
public class Constants {

	/**
	 * enum to configure working mode
	 * 
	 * @author Subin
	 *
	 */
	enum WORKING_MODE {
		EC2, LOCAL
	}

	/**
	 * constant to configure the current working mode
	 */
	private static WORKING_MODE currentMode = WORKING_MODE.LOCAL;

	/**
	 * method to fetch the current server path based on working mode
	 * 
	 * @return string with serverpath
	 */
	public static String getServerPath() {
		if (currentMode == WORKING_MODE.EC2) {
			return File.separator + "home" + File.separator + "ubuntu";
		} else {
			return System.getProperty("user.home");
		}
	}
}
