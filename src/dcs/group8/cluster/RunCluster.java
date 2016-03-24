package dcs.group8.cluster;

import java.util.Properties;

import dcs.group8.models.Cluster;
import dcs.group8.utils.PropertiesUtil;

public class RunCluster {

	private static Properties ss;

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
			ss = PropertiesUtil.getProperties("dcs.group8.cluster.RunCluster", "gridschedulers.properties");
//			ss.getProperty("gsaddresses").split(";")
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			Cluster cluster = new Cluster(clusterUrl, gridSchedulerUrl, nodeCount);
		}
		catch (Exception e)
		{
			
		}
	}

}
