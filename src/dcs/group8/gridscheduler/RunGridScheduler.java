package dcs.group8.gridscheduler;

import dcs.group8.models.GridScheduler;

public class RunGridScheduler {

	public static void main(String[] args) {
		if (args.length < 1) {
			System.err.println("Please provide grid scheduler url!");
			return;
		}

		String gridSchedulerUrl = args[0];

		System.out.println("Launching cluster...");

		try {
			GridScheduler gridScheduler = new GridScheduler(gridSchedulerUrl);
		} catch (Exception e) {

		}
	}

}
