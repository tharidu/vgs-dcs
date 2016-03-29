package dcs.group8.gridscheduler;

import dcs.group8.models.GridScheduler;

public class RunGridScheduler {

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
