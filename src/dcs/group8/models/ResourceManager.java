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
	private static String myGS;
	private static Cluster myCluster;
	
	private LinkedList<Job> jobQueue;
	private int rmNodes;
	
	public Node[] nodes;
	public SortedMap<Long, Integer> jobEndTimes;
	public static int busyCount;
	

	/**
	 * 
	 * Constructor method for the Resource Manager of a cluster in a VO
	 * @param nodeCount integer denoting the number of nodes under rm's supervision
	 * @param cl The cluster reference that this RM belongs
	 * 
	 */
	public ResourceManager(int nodeCount, Cluster cl) {
		this.rmNodes = nodeCount;
		this.nodes = new Node[nodeCount];
		this.jobEndTimes = new TreeMap<Long, Integer>();
		
		myCluster = cl;
		myGS = myCluster.getGridSchedulerUrl();
		logger = LogManager.getLogger(ResourceManager.class);
		logger.info("The resource manager rm@"+myCluster.host+" was created");
	}


	/**
	 * 
	 * callBackHandler is called when a job is finished in a node in the cluster
	 * and it calls the rmToGsMessage method of the responsible GS
	 * @param job The Job object associated with this callBackHandler
	 * 
	 */
	public static void callBackHandler(Job job) {
		
		RetryStrategy retry = new RetryStrategy();

		while (retry.shouldRetry()) {
			try {
				GridSchedulerRemoteMessaging gs_stub = (GridSchedulerRemoteMessaging) RegistryUtil
													   .returnRegistry(myGS, "GridSchedulerRemoteMessaging");
				gs_stub.rmToGsMessage(new JobMessage(job));
				
				logger.info("Job with Job_id:"+job.getJobNo()+" was completed");
				
				retry.setSuccessfullyTried(true);
				
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("Could not communicate with gs@"+myCluster.getGridSchedulerUrl());
				logger.error(e.getMessage());
				
				try {
					retry.errorOccured();
				} catch (RetryException e1) {
					
					/* this is supposed to only happen when the primary grid scheduler is down and we need to communicate with the replica */
					logger.error("Communication with GS was not established..assuming that the gs@: "+myCluster.getGridSchedulerUrl()+" is offline" + e.toString());
					logger.info("Trying to communicate with the auxiliary Grid Scheduler, gs@"+myCluster.getAuxGridSchedulerHost());
					myGS = myCluster.getAuxGridSchedulerHost();
					
					/* first inform the replica grid scheduler about the status of the cluster */
					informReplicaGs();
					callBackHandler(job);
				}
			}
		}

		busyCount--;
		logger.info("One more node is available now");
	}

	
	/**
	 * 
	 * A Job has arrived from the GS to this RM, the RM creates a new
	 * thread with the appropriate callBackHandler set to signal the end 
	 * of the job after the specified job duration
	 * 
	 */
	public String gsToRmJobMessage(final JobMessage jbm) throws RemoteException {
		
		/* DO WE NEED TO CHECK FOR THE BUSYCOUNT HERE */
//		if (busyCount < rmNodes) {
			Thread th = new Thread(new Node(jbm.job, new CallBack() {

				@Override
				public void callback() {
					callBackHandler(jbm.job);

				}
			}));
			th.start();
			busyCount++;
			
			logger.info("Adding job with Job_id: "+ jbm.job.getJobNo()+" to a node at cluster@"+jbm.job.getClientUrl());
//		} else {
//			logger.info("Lost job with Job_id: "+ jbm.job.getJobId()+" to a node at cluster@"+jbm.job.getClientUrl());
//		}
		return "Job accepted by the resource manager and assigned to a node";
	}
	
	
	/**
	 * 
	 * In this method we send the data structures needed for the replica
	 * grid scheduler to take on the management of the VO this method should
	 * be called only when this RM's GS url is changed to the one where 
	 * the replica GS is located
	 * 
	 */
	private static void informReplicaGs(){
		RetryStrategy retry = new RetryStrategy();
		
		while(retry.shouldRetry()){
			try {
				GridSchedulerRemoteMessaging gs_stub = (GridSchedulerRemoteMessaging) RegistryUtil
														.returnRegistry(myGS, "GridSchedulerRemoteMessaging");
				gs_stub.rmToGsStatusMessage(myCluster.getUrl(),busyCount);
				retry.setSuccessfullyTried(true);
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
	
	
	/**
	 * WE DO NOT NEED THIS YET
	 */
	@Override
	public int gsToRmStatusMessage() throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}
	
	
	
	/*** GETTERS AND SETTERS ***/
	public LinkedList<Job> getJobQueue() {
		return jobQueue;
	}

	public void setJobQueue(LinkedList<Job> jobQueue) {
		this.jobQueue = jobQueue;
	}

}
