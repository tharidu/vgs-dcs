package dcs.group8.models;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dcs.group8.messaging.GridSchedulerRemoteMessaging;
import dcs.group8.messaging.JobMessage;
import dcs.group8.messaging.ResourceManagerRemoteMessaging;
import dcs.group8.utils.RegistryUtil;

public class ResourceManager implements ResourceManagerRemoteMessaging {
	private static Logger logger;
	private LinkedList<Job> jobQueue;
	private int rmNodes;
	public Node[] nodes;
	public SortedMap<Long, Integer> jobEndTimes;
	public static int busyCount;
	
	private static Cluster myCluster;

	/**
	 * The message send from the gs to this cluster's RM
	 * to get information about the status of the resources
	 * returns the number of available resources(nodes) currently
	 * in the cluster
	 */
	public int gsToRmStatusMessage(){
		return 5;
	}
	
	public ResourceManager(int nodeCount,Cluster cl) {
		this.rmNodes = nodeCount;
		this.nodes = new Node[nodeCount];
		this.jobEndTimes = new TreeMap<>();
		/*System.out.println("The resource manager is created..");*/
		myCluster = cl;
		System.setProperty("logfilerm", "rm@"+myCluster.host);
		logger = LogManager.getLogger(ResourceManager.class);
		logger.info("The resource manager rm@"+myCluster.host+" was created");
	}

	public LinkedList<Job> getJobQueue() {
		return jobQueue;
	}

	public void setJobQueue(LinkedList<Job> jobQueue) {
		this.jobQueue = jobQueue;
	}
	
	//here you call the rmToGsMessage
	public static void callBackHandler(Job job){
		try {
			GridSchedulerRemoteMessaging gs_stub = (GridSchedulerRemoteMessaging) RegistryUtil
					.returnRegistry(myCluster.getGridSchedulerUrl(), "GridSchedulerRemoteMessaging");
			gs_stub.rmToGsMessage(new JobMessage(job));
			System.out.println("Job completion sent to GS from callback");
		} catch (Exception e) {
			System.err.println("Communication with GS was not established: " + e.toString());
			e.printStackTrace();
		}
		
		busyCount--;
		System.out.println("Decreasing busy count "+busyCount);
	}
	
	public String gsToRmJobMessage(JobMessage jbm) throws RemoteException {
		if(busyCount < rmNodes){
			Thread th = new Thread(new Node(jbm.job, new CallBack() {
				
				@Override
				public void callback() {
					callBackHandler(jbm.job);
					
				}
			}));
			th.start();
			busyCount++;
			System.out.println("Adding busy count "+busyCount);
		}
		return "Job accepted by the resource manager and assigned to a node";
	}

	/*@Override
	public String gsToRmJobMessage(JobMessage jbm) throws RemoteException {
		if (busyCount < rmNodes) {
			for (int i = 0; i < nodes.length; i++) {
				if (nodes[i] == null) {
					nodes[i] = new Node();
					long currentTime = new Date().getTime();
					jbm.job.setJobStatus(JobStatus.Running);
					jbm.job.setStartTimestamp(currentTime);
					nodes[i].setJob(jbm.job);
					jobEndTimes.put(currentTime + jbm.job.getJobDuration(), i);
				}
			}
		} else {
			// Send a message to GS that RM is full
			// Should never happen
			System.err.println("RM full but job received");
		}
		return "Job Accepted";
	}*/
}
