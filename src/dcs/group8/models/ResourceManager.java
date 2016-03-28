package dcs.group8.models;

import java.rmi.NotBoundException;
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
import dcs.group8.utils.RetryException;
import dcs.group8.utils.RetryStrategy;

public class ResourceManager implements ResourceManagerRemoteMessaging {
	private static Logger logger;
	private LinkedList<Job> jobQueue;
	private int rmNodes;
	public Node[] nodes;
	public SortedMap<Long, Integer> jobEndTimes;
	public static int busyCount;
	private static String myGS;
	private static Cluster myCluster;

	/**
	 * The message send from the gs to this cluster's RM
	 * to get information about the status of the resources
	 * returns the number of available resources(nodes) currently
	 * in the cluster
	 */
	public int gsToRmStatusMessage() {
		return 5;
	}

	public ResourceManager(int nodeCount, Cluster cl) {
		this.rmNodes = nodeCount;
		this.nodes = new Node[nodeCount];
		this.jobEndTimes = new TreeMap<>();
		/*System.out.println("The resource manager is created..");*/
		myCluster = cl;
		myGS = myCluster.getGridSchedulerUrl();
		//System.setProperty("logfilerm", "rm@"+myCluster.host);
		logger = LogManager.getLogger(ResourceManager.class);
		logger.info("The resource manager rm@"+myCluster.host+" was created");
	}

	public LinkedList<Job> getJobQueue() {
		return jobQueue;
	}

	public void setJobQueue(LinkedList<Job> jobQueue) {
		this.jobQueue = jobQueue;
	}

	// HERE WE CALL THE rmToGsMessage
	public static void callBackHandler(Job job) {
		RetryStrategy retry = new RetryStrategy();

		while (retry.shouldRetry()) {
			try {
				GridSchedulerRemoteMessaging gs_stub = (GridSchedulerRemoteMessaging) RegistryUtil
						.returnRegistry(myGS, "GridSchedulerRemoteMessaging");
				gs_stub.rmToGsMessage(new JobMessage(job));
				logger.info("Job with Job_id:"+job.getJobId()+" was completed");
				retry.setSuccessfullyTried(true);
				//break;
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("Could not communicate with gs@"+myCluster.getGridSchedulerUrl());
				logger.error(e.getMessage());
				try {
					retry.errorOccured();
				} catch (RetryException e1) {
					// this is supposed to only happen when the primary grid scheduler is down and we need to communicate with the replica
					logger.error("Communication with GS was not established..assuming that the gs@: "+myCluster.getGridSchedulerUrl()+" is offline" + e.toString());
					logger.info("Trying to communicate with the auxiliary Grid Scheduler, gs@"+myCluster.getAuxGridSchedulerHost());
					myGS = myCluster.getAuxGridSchedulerHost();
					//first inform the replica grid scheduler about the status of the cluster
					informReplicaGs();
					callBackHandler(job);
				}
			}
		}

		busyCount--;
		logger.info("One more node is available now");
	}
	
	/**
	 * In this method we send the data structures needed for the replica
	 * grid scheduler to take on the the VO
	 */
	private static void informReplicaGs(){
		RetryStrategy retry = new RetryStrategy();
		
		while(retry.shouldRetry()){
			try {
				GridSchedulerRemoteMessaging gs_stub = (GridSchedulerRemoteMessaging) RegistryUtil
						.returnRegistry(myGS, "GridSchedulerRemoteMessaging");
				//GsClusterStatus gsc = new GsClusterStatus(myCluster., cluUrl, nc, bc, status)
				gs_stub.rmToGsStatusMessage(myCluster.getUrl(),busyCount);
			}
			catch (Exception e) {
				logger.error("Unable to connect to replica gs@"+myGS);
				e.printStackTrace();
				try{
					retry.errorOccured();
				}
				//THIS SHOULD NEVER HAPPEN ACTUALLY
				catch(RetryException re){
					logger.error("Maximum retries for connection to replica gs@"+myGS+" are reached , giving up...");
				}
			} 
		}
	}

	public String gsToRmJobMessage(JobMessage jbm) throws RemoteException {
		if (busyCount < rmNodes) {
			Thread th = new Thread(new Node(jbm.job, new CallBack() {

				@Override
				public void callback() {
					callBackHandler(jbm.job);

				}
			}));
			th.start();
			busyCount++;
			//System.out.println("Adding busy count "+busyCount);
			logger.info("Adding a job to a node");
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
