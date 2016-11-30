package org.srs.advse.ftp.client;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class that will act as the local client for the FTP implementation
 * 
 * @author Subin
 * @version 0.1
 *
 */
public class SRSFTPClient {
	private Set<Path> dataChannelSet;
	private Set<Integer> terminateSet;
	private Map<Integer, Path> commandChannelMap;

	/**
	 * Constructor to initialize the declared variables
	 */
	public SRSFTPClient() {
		dataChannelSet = new HashSet<Path>();
		terminateSet = new HashSet<Integer>();
		commandChannelMap = new HashMap<Integer, Path>();
	}

}
