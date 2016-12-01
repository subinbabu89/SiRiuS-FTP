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
	
	public synchronized boolean transfer(Path path) {
		return !dataChannelSet.contains(path);
	}
	
	public synchronized void transferIN(Path path, int commandID) {
		dataChannelSet.add(path);
		commandChannelMap.put(commandID, path);
	}
	
	public synchronized void transferOUT(Path path, int commandID) {
		try {
			dataChannelSet.remove(path);
			commandChannelMap.remove(commandID);
		} catch(Exception e) {}
	}
	
	public synchronized boolean terminatePUT(Path path, int commandID) {
		try {
			if (terminateSet.contains(commandID)) {
				commandChannelMap.remove(commandID);
				dataChannelSet.remove(path);
				terminateSet.remove(commandID);
				return true;
			}
		} catch (Exception e) {}
		
		return false;
	}

}
