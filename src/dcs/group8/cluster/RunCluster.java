package dcs.group8.cluster;

import dcs.group8.models.Cluster;

public class RunCluster {

	public static void main(String[] args) {
		if(args.length < 4) {
			System.err.println("Arguments format: <cluster_url> <primaryGS_url> <secondaryGS_url> <no_of_nodes_per_cluster>");
			return;
		}
		String clusterUrl = args[0];
		String gridSchedulerUrl = args[1];
		String  auxGridSchedulerUrl = args[2];
		int nodeCount = Integer.valueOf(args[3]);	
		try {
			Cluster cluster = new Cluster(clusterUrl, gridSchedulerUrl,auxGridSchedulerUrl, nodeCount);
		}
		catch (Exception e)
		{
		
		}
	}

}
