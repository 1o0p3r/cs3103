package main.tracker;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import main.heartbeat.HeartBeatListener;
import main.utilities.constants.Constant;
import main.utilities.constants.NetworkConstant;

/**
 * This class acts as the main class for a central directory server
 * @author cs3103 group 3
 *
 */
public class Tracker{
	static ServerSocket serverSocket;

	//ArrayList of all the files with unique names
	public static ArrayList<Record> fileArrList = new ArrayList<>();

	//To allow faster access, use a hash : fileName to its associated chunks
	public static Hashtable<String, ArrayList<Record>> recordTable = new Hashtable<>();
	
	//Tuple : 1) Ip 2) Port No.		Value = Socket
	public static Hashtable<Tuple, Socket> ipPortToSocketTable = new Hashtable<>();
	public static Hashtable<Tuple, Socket> dataTransferTable = new Hashtable<>();
	
	public static void main(String[] args) {
		System.out.println("Starting Server");

		//Starts new instance of server
		//		try {
		//			serverSocket = new ServerSocket(NetworkConstant.TRACKER_LISTENING_PORT);
		//		} catch(IOException ioe) {
		//			System.out.println("Unable to create Server Socket");
		//			System.exit(1);
		//		}

//		Timer timer = new Timer();
//        timer.schedule(new TrackerCleanUp(), 0, Constant.HEARTBEAT_TRACKER_CLEANUP_INTERVAL);
		listenHeartBeat();
		listenRequest();
	}
	
	private static void listenHeartBeat() {
	    HeartBeatListener heartbeatListener = new HeartBeatListener();
        heartbeatListener.start();
	}

	private static void listenRequest() {
		ExecutorService executor= null;
		//While server is still alive
		try {

			serverSocket = new ServerSocket(NetworkConstant.TRACKER_LISTENING_PORT);
			executor = Executors.newFixedThreadPool(20000);

			System.out.println("Waiting for client");

			while(true) {
				Socket clientSocket = serverSocket.accept();	
				// Will be printed twice whenever new client joins (2 persistent connection, client n server)
				System.out.println("Accepted a client");
				Runnable worker = new HelperThread(clientSocket);
				executor.execute(worker);
			}

		} catch(IOException ioe) {
			System.out.println("Error in creating listening socket");
			System.exit(1);
		}
	}

	public static void removeUnresponsivePeersFromRecord(Set<Tuple> listOfPeersWhoResponded) {
		recordTable.forEach((filename,recordList) -> { 
//			System.out.println(filename + " and recordList size: " + recordList.size());
			for (int i=0; i<recordList.size(); i++ ) {
				Record record = recordList.get(i);
				Tuple peer = new Tuple(record.getipAdd(), record.getPortNumber());
				if (!listOfPeersWhoResponded.contains(peer)) {
//					System.out.println("peer: " + peer.ipAdd + ":" + peer.portNo + " did not respond");
					recordList.remove(i);
					if (recordTable.get(filename) == null) {
						recordTable.remove(filename);
					}
					ipPortToSocketTable.remove(peer);
					i--;
				} else {
//					System.out.println("peer: " + peer.ipAdd + ":" + peer.portNo + " responded");
				}
			}
		});
		
		removeFileWithEmptyRecords();
		printEverythInsideRecordAndIpToSocketTable();
	}

	public static void removeFileWithEmptyRecords() {
		Iterator<String> iterator = recordTable.keySet().iterator();
		while (iterator.hasNext()){
			if (recordTable.get(iterator.next()).size() < 1 ) {
				iterator.remove();
			}
		}
	}
	
	public static void printEverythInsideRecordAndIpToSocketTable(){
		System.out.println("===============================recordTable========================");
		recordTable.forEach((filename,recordList) -> { 
			for (int i=0; i<recordList.size(); i++ ) {
				Record record = recordList.get(i);
				Tuple peer = new Tuple(record.getipAdd(), record.getPortNumber());
				System.out.println("Filename: " + filename + ", Peer: " + record.getipAdd() + ": " + record.getPortNumber() + ", chunk: " + record.chunkNumber);
			}
		});
		System.out.println("==================================================================");
		
		System.out.println("=====================ipPortToSocketTable==========================");
		ipPortToSocketTable.forEach((peer,socket) -> {
			System.out.println(peer.ipAdd + ": " +peer.portNo);
		});
		System.out.println("==================================================================");
	}
	
    
}

//class TrackerCleanUp extends TimerTask {
//    public void run() {
////        Tracker.removeUnresponsivePeersFromRecord(listOfRespondedPeerInfo);
//    	System.out.println("=====================recordTable==========================");
//		Tracker.recordTable.forEach((filename,recordList) -> { 
//			for (int i=0; i<recordList.size(); i++ ) {
//				Record record = recordList.get(i);
//				Tuple peer = new Tuple(record.getipAdd(), record.getPortNumber());
//				System.out.println("Filename: " + filename + ", Peer: " + record.getipAdd() + ": " + record.getPortNumber() + ", chunk: " + record.chunkNumber);
//			}
//		});
//		System.out.println("===============================================");
//		
//		System.out.println("=====================ipPortToSocketTable==========================");
//		Tracker.ipPortToSocketTable.forEach((peer,socket) -> {
//			System.out.println(peer.ipAdd + ": " +peer.portNo);
//		});
//		System.out.println("===============================================");
//        
//    }
//}
