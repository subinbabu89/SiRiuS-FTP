package org.srs.advse.ftp.server;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
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

	/**
	 * Method used to signify start of download operation
	 * 
	 * @param path
	 * @return
	 */
	public synchronized int downloadIN(Path path) {
		int commandID = 0;

		if (dataChannelMap.containsKey(path)) {
			if (dataChannelMap.get(path).readLock().tryLock()) {
				while (commandChannelMap.containsKey(commandID = generateID()))
					commandChannelMap.put(commandID, path);
				return commandID;
			} else {
				return -1;
			}
		} else {
			dataChannelMap.put(path, new ReentrantReadWriteLock());
			dataChannelMap.get(path).readLock().lock();

			while (commandChannelMap.containsKey(commandID = generateID()))
				;
			commandChannelMap.put(commandID, path);
			return commandID;
		}
	}

	/**
	 * Method used to signify completion of download operation
	 * 
	 * @param path
	 * @param commandID
	 */
	public synchronized void downloadOUT(Path path, int commandID) {
		try {
			dataChannelMap.get(path).readLock().unlock();
			commandChannelMap.remove(commandID);

			if (dataChannelMap.get(path).getReadLockCount() == 0 && !dataChannelMap.get(path).isWriteLocked()) {
				dataChannelMap.remove(path);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method used to fetch id for the upload operation
	 * 
	 * @param path
	 * @return
	 */
	public synchronized int uploadIN_ID(Path path) {
		int commandID = 0;

		while (commandChannelMap.containsKey(commandID = generateID()))
			;
		commandChannelMap.put(commandID, path);
		writeQueue.add(commandID);
		return commandID;
	}

	/**
	 * Method used to signify start of upload operation
	 * 
	 * @param path
	 * @param commandID
	 * @return
	 */
	public synchronized boolean uploadIN(Path path, int commandID) {
		if (writeQueue.peek() == commandID) {
			if (dataChannelMap.containsKey(path)) {
				if (dataChannelMap.get(path).writeLock().tryLock()) {
					return true;
				} else
					return false;
			} else {
				dataChannelMap.put(path, new ReentrantReadWriteLock());
				dataChannelMap.get(path).writeLock().lock();
				return true;
			}
		}
		return false;
	}

	/**
	 * Method used to signify completion of upload operation
	 * 
	 * @param path
	 * @param commandID
	 */
	public synchronized void uploadOUT(Path path, int commandID) {

		try {
			dataChannelMap.get(path).writeLock().unlock();
			commandChannelMap.remove(commandID);
			writeQueue.poll();

			if (dataChannelMap.get(path).getReadLockCount() == 0 && !dataChannelMap.get(path).isWriteLocked())
				dataChannelMap.remove(path);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Method used to generate random ID
	 * 
	 * @return
	 */
	public int generateID() {
		return new Random().nextInt(90000) + 10000;
	}

	/**
	 * method used to add for termination
	 * 
	 * @param commandID
	 */
	public synchronized void terminate(int commandID) {
		terminateSet.add(commandID);
	}

	/**
	 * method used to check the terminate of download
	 * 
	 * @param path
	 * @param commandID
	 * @return
	 * @throws Exception
	 */
	public synchronized boolean terminateDOWNLOAD(Path path, int commandID) throws Exception {
		try {
			if (terminateSet.contains(commandID)) {
				terminateSet.remove(commandID);
				commandChannelMap.remove(commandID);
				dataChannelMap.get(path).readLock().unlock();

				if (dataChannelMap.get(path).getReadLockCount() == 0 && !dataChannelMap.get(path).isWriteLocked()) {
					dataChannelMap.remove(path);
				}
				return true;

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * method used to check for termination of upload
	 * 
	 * @param path
	 * @param commandID
	 * @return
	 */
	public synchronized boolean terminateUPLOAD(Path path, int commandID) {
		try {
			if (terminateSet.contains(commandID)) {
				terminateSet.remove(commandID);
				commandChannelMap.remove(commandID);
				dataChannelMap.get(path).writeLock().unlock();
				writeQueue.poll();
				Files.deleteIfExists(path);

				if (dataChannelMap.get(path).getReadLockCount() == 0 && !dataChannelMap.get(path).isWriteLocked())
					dataChannelMap.remove(path);
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * remove files from the files map
	 * 
	 * @param path
	 * @return
	 */
	public boolean delete(Path path) {
		return !dataChannelMap.containsKey(path);
	}
}
