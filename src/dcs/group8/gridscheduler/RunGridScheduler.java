package dcs.group8.gridscheduler;

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

		if(args.length < 1) {
			System.err.println("Arguments format: <isBackup> <secondaryGS_url> or Arguments format: <isBackup>");
			return;
		}
		
		boolean isBackup = Boolean.valueOf(args[0]);
		String backupGridSchedulerUrl = "";
		
		if(args.length == 2) {
			backupGridSchedulerUrl = args[1];	
		}
				
		try {
			GridScheduler gridScheduler = new GridScheduler(isBackup, backupGridSchedulerUrl);
		} catch (Exception e) {

		}
	}

}
