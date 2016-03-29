package dcs.group8.gridscheduler;

import dcs.group8.models.GridScheduler;

public class RunGridScheduler {

	public static void main(String[] args) {
		System.out.println("Launching grid scheduler...");

		if(args.length < 2) {
			System.err.println("Arguments format: <isBackup> <secondaryGS_url>");
			return;
		}
		
		boolean isBackup = Boolean.valueOf(args[0]);
		String backupGridSchedulerUrl = args[1];
		
		try {
			GridScheduler gridScheduler = new GridScheduler(isBackup, backupGridSchedulerUrl);
		} catch (Exception e) {

		}
	}

}
