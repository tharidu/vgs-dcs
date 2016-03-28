package dcs.group8.models;

import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dcs.group8.messaging.GridSchedulerRemoteMessaging;
import dcs.group8.messaging.ResourceManagerRemoteMessaging;
import dcs.group8.utils.RegistryUtil;
import dcs.group8.utils.RetryException;
import dcs.group8.utils.RetryStrategy;

public class Cluster implements Remote {
	private static Logger logger;
	
	private ResourceManager resourceManager;
	private ResourceManager backupResourceManager;
	private List<Node> nodes;
	public String host;
	private String gridSchedulerHost;
	private String auxGridSchedulerHost;

	// polling thread
	private Thread pollingThread;
//	private boolean running;

	public Cluster(String url, String gridSchedulerUrl,String auxGridScheduler,int nodeCount) {
		super();
		this.host = url;
		System.setProperty("logfilecluster", "cluster@"+this.host);
		System.setProperty("logfilerm", "rm@"+this.host);
		logger = LogManager.getLogger(Cluster.class);
		logger.info("Initializing cluster@"+this.host);
		this.gridSchedulerHost = gridSchedulerUrl;
		this.auxGridSchedulerHost = auxGridScheduler;
		nodes = new ArrayList<Node>(nodeCount);
		logger.info("Creating a resource manager for this cluster with "+nodeCount+" nodes");
		this.resourceManager = new ResourceManager(nodeCount,this);
		this.backupResourceManager = new ResourceManager(nodeCount,this);
		this.setUpRegistry();
		this.informGS(this.host);
	}

	public String getAuxGridSchedulerHost() {
		return auxGridSchedulerHost;
	}

	public void setAuxGridSchedulerHost(String auxGridSchedulerHost) {
		this.auxGridSchedulerHost = auxGridSchedulerHost;
	}

	public ResourceManager getResourceManager() {
		return resourceManager;
	}

	public void setResourceManager(ResourceManager resourceManager) {
		this.resourceManager = resourceManager;
	}

	public ResourceManager getBackupResourceManager() {
		return this.backupResourceManager;
	}

	public void setBackupResourceManager(ResourceManager backupResourceManager) {
		this.backupResourceManager = backupResourceManager;
	}

	public List<Node> getNodes() {
		return nodes;
	}

	public void setNodes(List<Node> nodes) {
		this.nodes = nodes;
	}

	public String getUrl() {
		return host;
	}

	public void setUrl(String url) {
		this.host = url;
	}

	public String getGridSchedulerUrl() {
		return gridSchedulerHost;
	}

	public void setGridSchedulerUrl(String gridSchedulerUrl) {
		this.gridSchedulerHost = gridSchedulerUrl;
	}
	
	/**
	 * informGs is called to send a message to the GS of this
	 * VO that the cluster and the Resource Manager are online
	 */
	private void informGS(String myURL){
		logger.info("Sending a message to gs@"+this.gridSchedulerHost+" that i am up and running");
		RetryStrategy retry = new RetryStrategy(100,1000);
		while(retry.shouldRetry()){
			try{
				GridSchedulerRemoteMessaging gs_stub = (GridSchedulerRemoteMessaging) RegistryUtil
									.returnRegistry(gridSchedulerHost, "GridSchedulerRemoteMessaging");
				gs_stub.rmToGsStatusMessage(myURL);
				retry.setSuccessfullyTried(true);
			}
			catch(Exception e){
				try{
					retry.errorOccured();
				}
				//TODO what happens when the cluster cannot reach the gs for a long time
				catch (RetryException re){
					logger.error("Maximum number of retries reached and gs@"+gridSchedulerHost+" did not respond");
				}
				
			}
		}
	}

	/**
	 * setup up the registry of the resource manager for all messages it must
	 * handle from all entities of the DCS
	 */
	private void setUpRegistry() {

		try {
			ResourceManagerRemoteMessaging cgs_stub = (ResourceManagerRemoteMessaging) UnicastRemoteObject
					.exportObject(this.resourceManager, 0);
			// the methods are exposed by the resource manager...
			Registry registry = LocateRegistry.getRegistry(host);
			registry.bind(ResourceManagerRemoteMessaging.registry, cgs_stub);
			logger.info("Resource Manager registry is properly set up!");

		} catch (Exception e) {
			logger.error("Resource Manager registry wasn't set up: " + e.toString());
			e.printStackTrace();
		}

	}
	
	// in this thread we are checking for finished jobs in the resource manager 
	// in order to notify the gridscheduler..
	/*@Override
	public void run() {
		System.out.println("Starting Cluster " + this.getUrl());
		while (running) {
			for (Iterator<Map.Entry<Long, Integer>> it = this.resourceManager.jobEndTimes.entrySet().iterator(); it.hasNext();) {
				Entry<Long, Integer> entry = it.next();
				if (entry.getKey() <= new Date().getTime()) {
					// Job done
					this.resourceManager.busyCount--;
					// we get the nodes structure from rm here concurrently?
					Job job = this.resourceManager.nodes[entry.getValue()].getJob();
					this.resourceManager.nodes[entry.getValue()] = null;
					job.setEndTimestamp(new Date().getTime());
					job.setJobStatus(JobStatus.Finished);
					JobMessage jobMessage = new JobMessage(job);
					it.remove();
					try {
						GridSchedulerRemoteMessaging gs_stub = (GridSchedulerRemoteMessaging) RegistryUtil
								.returnRegistry(this.getGridSchedulerUrl(), "GridSchedulerRemoteMessaging");
						gs_stub.rmToGsMessage(jobMessage);
						System.out.println("Job completion sent to GS");
					} catch (Exception e) {
						System.err.println("Communication with GS was not established: " + e.toString());
						e.printStackTrace();
					}
				}
			}
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException ex) {

			}
		}
	}*/

	public void stopPollThread() { 
		logger.error("Stopping Cluster " + this.getUrl());
		try {
			java.rmi.Naming.unbind(this.getUrl());
		} catch (Exception e) {
			e.printStackTrace();
		}

//		running = false;
		try {
			pollingThread.join();
		} catch (InterruptedException ex) {
			logger.error("Cluster stopPollThread was interrupted");
			assert (false) : "Cluster stopPollThread was interrupted";
		}

	}
}
