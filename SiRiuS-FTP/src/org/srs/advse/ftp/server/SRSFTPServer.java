package org.srs.advse.ftp.server;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Class that will act as the online server for the FTP implementation
 * 
 * @author Subin
 * @version 0.1
 *
 */
public class SRSFTPServer {
	private Map<Path, ReentrantReadWriteLock> dataChannelMap;
	private Map<Integer, Path> commandChannelMap;
	private LinkedList<Integer> writeQueue;
	private Set<Integer> terminateSet;

	/**
	 * Constructor to initialize the declared variables
	 */
	public SRSFTPServer() {
		dataChannelMap = new HashMap<Path, ReentrantReadWriteLock>();
		commandChannelMap = new HashMap<Integer, Path>();
		writeQueue = new LinkedList<Integer>();
		terminateSet = new HashSet<Integer>();
	}

	public synchronized void terminate(int commandID) {
		terminateSet.add(commandID);
	}
}
