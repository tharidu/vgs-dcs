package dcs.group8.gridscheduler;

import java.util.Arrays;

import dcs.group8.models.GridScheduler;


/**
 * 
 * RunGridScheduler runs a primary GS or a backup GS for fault tolerance
 *
 */
public class RunGridScheduler {
	
	
	/**
	 * 
	 * Main method creating a primary of backup GS for the VO
	 * @param args 0: boolean signifying whether this is primary(false) or backup(true) GS
	 * @param args 1: String url mandatory if this a primary GS to denote the address of the backup GS 
	 * 
	 */
	public static void main(String[] args) {
		System.out.println("Launching grid scheduler...");

		if(args.length < 3) {
			System.err.println("Arguments format: <isBackup> <secondaryGS_url> <noNodesPerCluster> <cluster1> <cluster2> ...");
			return;
		}
		
		boolean isBackup = Boolean.valueOf(args[0]);
		String backupGridSchedulerUrl = args[1];
		int noNodes = Integer.valueOf(args[2]);
		
		try {
			GridScheduler gridScheduler = new GridScheduler(isBackup, backupGridSchedulerUrl, noNodes, Arrays.copyOfRange(args, 3, args.length));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
