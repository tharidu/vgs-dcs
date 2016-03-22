package dcs.group8.cluster;

import dcs.group8.models.Cluster;

public class RunCluster {

	public static void main(String[] args) {
		if(args.length < 3) {
			System.err.println("Please provide id, grid scheduler url and no of nodes!");
			return;
		}
		
		String clusterUrl = args[0];
		String gridSchedulerUrl = args[1];
		int nodeCount = Integer.valueOf(args[2]);
		
		System.out.println("Launching cluster...");
		
		try {
			Cluster cluster = new Cluster(clusterUrl, gridSchedulerUrl, nodeCount);
		}
		catch (Exception e)
		{
			
		}
	}

}
