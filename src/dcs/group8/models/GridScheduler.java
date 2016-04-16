package dcs.group8.models;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dcs.group8.messaging.ClientRemoteMessaging;
import dcs.group8.messaging.GridSchedulerRemoteMessaging;
import dcs.group8.messaging.JobMessage;
import dcs.group8.messaging.ResourceManagerRemoteMessaging;
import dcs.group8.messaging.StatusMessage;
import dcs.group8.utils.PropertiesUtil;
import dcs.group8.utils.RegistryUtil;
import dcs.group8.utils.RetryException;
import dcs.group8.utils.RetryStrategy;

/**
 * 
 * 
 * The GridScheduler class representing a GS inside a VO
 *
 *
 */
public class GridScheduler implements GridSchedulerRemoteMessaging, Runnable {
	
	private static Logger logger;
//	private static Properties clusterProps;
	private static Properties gsProps;
	
	private String host;
	private String backupHost;
	private ConcurrentLinkedQueue<Job> externalJobs;
	private static ConcurrentHashMap<UUID, GsClusterStatus> clusterStatus;
	private ArrayList<String> gridschedulers;
	private ArrayList<String> myClusters;
	private int nodesPerCluster;
	private Thread pollingThread;
	private boolean running;
	
	public boolean isBackup;


	/**
	 * 
	 * The GridScheduler constructor method for the instatiation of either a primary
	 * GS or a replica (auxiliary) GS
	 * @param isBackup Boolean flag to signify whether this is a replica(true) GS or primary(false) GS
	 * @param backup The replica GS url in case this is a primary GS
	 * 
	 */
	public GridScheduler(boolean isBackup, String backup, int noNodes, String[] clusters) {
		super();
		backupHost = backup;
		this.isBackup = isBackup;
		this.nodesPerCluster = noNodes;
		this.myClusters = new ArrayList<String>(Arrays.asList(clusters));

		try {
			this.host = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		System.setProperty("logfilegs", "gs@" + this.host);
		logger = LogManager.getLogger(GridScheduler.class);
		
		if(isBackup){
			logger.info("Initializing a replica gs@"+this.host);
		}
		else{
			logger.info("Initializing primary gs@" + this.host);
			logger.info("A replica GS is set at gs@"+this.backupHost);
		}
		
		
		setExternalJobs(new ConcurrentLinkedQueue<Job>());
		setClusterStatus(new ConcurrentHashMap<UUID, GsClusterStatus>());
		gridSchedulerInit();
		setUpRegistry();
		running = true;
		pollingThread = new Thread(this);
		pollingThread.start();
	}

	/**
	 * Initialization of a gridscheduler to save the addresses of the clusters
	 * under his responsibility as well as the addresses of the rest of the
	 * gridschedulers in other VOs
	 */
	private void gridSchedulerInit() {
		logger.info("Reading properties files for clusters and grid schedulers");
		gridschedulers = new ArrayList<String>();
		try {
//			clusterProps = PropertiesUtil.getProperties("dcs.group8.models.GridScheduler", "clusters.properties");
			gsProps = PropertiesUtil.getProperties("dcs.group8.models.GridScheduler", "gridschedulers.properties");
			for (String gsAddr : gsProps.getProperty("gsaddr").split(";")) {
				if (InetAddress.getLocalHost().getHostName().equals(gsAddr)) {
					continue;
				} else {
					gridschedulers.add(gsAddr);
				}
			}
//			myClusters = new ArrayList<String>(Arrays.asList(clusterProps.getProperty("claddr").split(";")));
//			nodesPerCluster = Integer.parseInt(clusterProps.getProperty("nodes"));
			
			logger.info("Initializing the status of the clusters");
			
			for (int i = 0; i < myClusters.size(); i++) {
				UUID id = UUID.randomUUID();
				GsClusterStatus status = new GsClusterStatus(id, myClusters.get(i), nodesPerCluster, 0, false);
				clusterStatus.put(id, status);
			}
		} catch (Exception e) {
			logger.error("Unable to read property files.." + e.toString());
			e.printStackTrace();
		}
	}

	
	/**
	 * 
	 * reportBusyCount is only used for the load balancing experiments
	 * of our distributed system
	 * 
	 */
	private void reportBusyCount(){
		String message = "VO load status";
		for (ConcurrentHashMap.Entry<UUID, GsClusterStatus> entry : clusterStatus.entrySet()) {
			message+= ","+entry.getValue().getClusterUrl()+","+entry.getValue().getBusyCount().toString();
		}
		logger.debug(message);
	}
	
	public void checkRmHeartBeatStatus()
	{
		for (ConcurrentHashMap.Entry<UUID, GsClusterStatus> entry : clusterStatus.entrySet()) {
			if (entry.getValue().isHasCrashed()) {
				continue;
			}
			
			RetryStrategy retry = new RetryStrategy();
			while (retry.shouldRetry()) {
				try {
					ResourceManagerRemoteMessaging gsm_stub = (ResourceManagerRemoteMessaging) RegistryUtil
							.returnRegistry(entry.getValue().getClusterUrl(), "ResourceManagerRemoteMessaging");
					int reply = gsm_stub.gsToRmStatusMessage();
					retry.setSuccessfullyTried(true);
				} catch (Exception e) {
					try {
						retry.errorOccured();
					} catch (RetryException e1) {
						logger.error("Heartbeat - Could not contact other rm@" + entry.getValue().getClusterUrl());
						entry.getValue().setHasCrashed(true);
						logger.info("Number of jobs that were running: "+entry.getValue().getBusyCount());
						entry.getValue().setBusyCount(0);
						rescheduleJobs(entry.getValue().getJobList());
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	/**
	 * 
	 * The polling thread monitors the queue of external jobs in order to try
	 * and delegate them to other VOs if it succeeds it sends the job to the
	 * appropriate GS or else it re-enters the job to the queue for a future try
	 * 
	 */

	@Override
	public void run() {
		
		logger.info("Starting gs@" + this.getUrl());

		while (running) {
			
			try {
				Thread.sleep(2000);
				checkRmHeartBeatStatus();
				reportBusyCount();
				if (!isBackup) {
					Job job = externalJobs.poll();
					if (job != null) {
						
						logger.info("Trying to reach other GSs to delegate this job");

						double lowestUtilzation = 1;
						String acceptedGsUrl = "";
						for (String gsUrl : gridschedulers) {
							RetryStrategy retry = new RetryStrategy();
							while (retry.shouldRetry()) {
								try {
									GridSchedulerRemoteMessaging gsm_stub = (GridSchedulerRemoteMessaging) RegistryUtil
											.returnRegistry(gsUrl, "GridSchedulerRemoteMessaging");
									StatusMessage reply = gsm_stub.gsToGsStatusMessage();
									if (reply.utilization < lowestUtilzation) {
										lowestUtilzation = reply.utilization;
										acceptedGsUrl = gsUrl;
									}
									retry.setSuccessfullyTried(true);
									
								} catch (Exception e) {
									try {
										retry.errorOccured();
										
										// TODO how are we going to handle
										// offline
										// gridscheduler nodes here
										
									} catch (RetryException e1) {
										logger.error("Could not contact other gs@" + gsUrl + " to offload job");
										e.printStackTrace();
									}
								}
							}
						}

						if (acceptedGsUrl.equals("")) {
							externalJobs.add(job);
							logger.info("No other GS is willing to take the job");
						} else if (!this.getBackupHost().equals("")) {
							// Update aux GS about job removal from externalJobs
							RetryStrategy retry = new RetryStrategy();

							while (retry.shouldRetry()) {
								try {
									logger.info("Removing Backup of job " + job.getJobNo() + " from " + this.getBackupHost());
									GridSchedulerRemoteMessaging gs_stub = (GridSchedulerRemoteMessaging) RegistryUtil
																			.returnRegistry(this.getBackupHost(), "GridSchedulerRemoteMessaging");
									gs_stub.backupExternalJobs(job, false);
									retry.setSuccessfullyTried(true);
								} catch (Exception e) {
									try {
										retry.errorOccured();
									} catch (RetryException e1) {
										logger.error("Could not remove job entry from backup GS");
									}
								}
							}
						}

						if (!acceptedGsUrl.equals("")) {
							RetryStrategy retry = new RetryStrategy();

							while (retry.shouldRetry()) {
								try {
									GridSchedulerRemoteMessaging gsm_stub = (GridSchedulerRemoteMessaging) RegistryUtil
											.returnRegistry(acceptedGsUrl, "GridSchedulerRemoteMessaging");
									gsm_stub.gsToGsJobMessage(new JobMessage(job));
									logger.info("Job successfully sent to gs@" + acceptedGsUrl);
									retry.setSuccessfullyTried(true);
								} catch (Exception e) {
									try {
										retry.errorOccured();
									} catch (RetryException e1) {
										logger.error("Could not offload the job to selected GS");
										e.printStackTrace();
									}
								}
							}
						}
					}
				}
			} catch (InterruptedException ex) {
				logger.error("Thread run was interrupted in gs@" + this.host);
			}
		}
	}

	/**
	 * 
	 * Receive a notification from a resource manager of a cluster that a job
	 * was successfully completed
	 * 
	 */
	@Override
	public void rmToGsMessage(JobMessage message) throws RemoteException {
		
		/* First communication of a RM with the replica GS, turn flag to false */
		if (isBackup) {
			isBackup = false;
			logger.info("Backup GS becoming active");
		}

		UUID cid = message.job.getClusterId();
		String clientid = message.job.getClientUrl();
		clusterStatus.get(cid).decreaseBusyCount();
		logger.info("Job with Job_id: " + message.job.getJobNo() + " was completed from cluster@"
																+ clusterStatus.get(cid).getClusterUrl());

		clusterStatus.get(cid).removeJob(message.job);
		RetryStrategy retry = new RetryStrategy();

		while (retry.shouldRetry()) {
			try {
				ClientRemoteMessaging crm_stub = (ClientRemoteMessaging) RegistryUtil.returnRegistry(clientid,
						"ClientRemoteMessaging");
				crm_stub.gsToClientMessage(message);
				retry.setSuccessfullyTried(true);
				break;
			} catch (Exception e) {
				try {
					retry.errorOccured();
				} catch (RetryException e1) {
					logger.error("Message for job completion with Job_Id:"+message.job.getJobNo()+" could not be send from gs to client@"
							+ message.job.getClientUrl());
					e.printStackTrace();
				}
			}
		}
	}

	
	/**
	 * 
	 * Receive a job from client here and push the job to a resource manager
	 * after you first check the resources available at each cluster
	 * 
	 */
	public String clientToGsMessage(JobMessage jb) {

		/* First communication of a client with the replica GS, turn flag to false */
		if (isBackup) {
			isBackup = false;
			logger.info("Backup GS becoming active");
		}

		ConcurrentHashMap.Entry<UUID, GsClusterStatus> selectedCluster = null;
		double lowestUtilization = 1;

		/*Get the cluster with lowest utilisation and assign the job, otherwise
		  offload to other GS */
		for (ConcurrentHashMap.Entry<UUID, GsClusterStatus> entry : clusterStatus.entrySet()) {
			/* If he know that a cluster is already offline we continue looping
			   over the rest of the clusters in the VO */
			if (entry.getValue().isHasCrashed()) {
				continue;
			}
			double utilization = (double) entry.getValue().getBusyCount() / (double) entry.getValue().getNodeCount();
			if (lowestUtilization > utilization) {
				lowestUtilization = utilization;
				selectedCluster = entry;
			}
		}
		
		/* Found suitable cluster to assign the job to */
		if (selectedCluster != null) {
		
			logger.info("Assigning Job with Job_id:" + jb.job.getJobNo() + " to cluster@"
					+ selectedCluster.getValue().getClusterUrl());
			
			RetryStrategy retry = new RetryStrategy();
			while (retry.shouldRetry()) {
				try {
					ResourceManagerRemoteMessaging rm_stub = (ResourceManagerRemoteMessaging) RegistryUtil
							.returnRegistry(selectedCluster.getValue().getClusterUrl(),
									"ResourceManagerRemoteMessaging");
					
					jb.job.setClusterId(selectedCluster.getKey());
					String ack = rm_stub.gsToRmJobMessage(jb);
					logger.info("rm@" + selectedCluster.getValue().getClusterUrl() + " responded: " + ack);
					retry.setSuccessfullyTried(true);
					clusterStatus.get(selectedCluster.getKey()).increaseBusyCount();
				} catch (Exception e) {
					try {
						retry.errorOccured();
					} catch (RetryException e1) {
						logger.error("Communication with rm@" + selectedCluster.getValue().getClusterUrl()
								+ " could not be established after 5 retries...the resource manager is offline");
						logger.info("Trying to reschedule all the jobs for this resource manager");
						// TODO an rm has crashed recover all the jobs that is
						// currently running and set the correct cluster status
						selectedCluster.getValue().setHasCrashed(true);
						
						/* SHOULD WE NOT RETRY HERE FINDING A CLUSTER THAT IS AVAILABLE TO TAKE THE JOB???*/
						clientToGsMessage(jb);
						
						/* call the method to start a thread that it will  reschedule the jobs of this rm*/
						rescheduleJobs(selectedCluster.getValue().getJobList());
						
						e.printStackTrace();
					}
				}
			}
			selectedCluster.getValue().setJob(jb.job);
			return "JobMessage was received forwarding it to a resource manager";
		} else {
			// Send it to external queue, either everything is crashed!!
			// or we could all the clusters in the VO are at their peak
			// of their utilization
			logger.info("Job with job_id: " + jb.job.getJobNo() + " for client@" + jb.job.getClientUrl()
					+ " is placed in the external jobs queue");
			externalJobs.add(jb.job);

			// backup to aux GS
			if(!this.getBackupHost().equals("")) {
				RetryStrategy retry = new RetryStrategy();

				while (retry.shouldRetry()) {
					try {
						logger.info("Backing up job " + jb.job.getJobNo() + " to " + this.getBackupHost());
						GridSchedulerRemoteMessaging gs_stub = (GridSchedulerRemoteMessaging) RegistryUtil
								.returnRegistry(this.getBackupHost(), "GridSchedulerRemoteMessaging");
						gs_stub.backupExternalJobs(jb.job, true);
						retry.setSuccessfullyTried(true);
					} catch (Exception e) {
						logger.error("Exception " + e.getMessage());
						try {
							retry.errorOccured();
						} catch (RetryException e1) {
							logger.error("Could not add the job to externalJobs in replica GS");
						}
					}
				}
			}
		}

		return "JobMessage was received";
	}
	
	
	/**
	 * 
	 * A method that is called when the GS finds out about a crashed RM
	 * it creates a new thread that is called periodically to schedule 
	 * jobs from the start
	 * 
	 */
	private void rescheduleJobs(ArrayList<Job> jlist){
		RescheduleThread rt = new RescheduleThread(jlist, this);
		Thread reThread = new Thread(rt);
		reThread.start();
	}

	
	
	/**
	 * setup up the registry of the grid scheduler for all messages it must
	 * handle from all entities of the DCS
	 */
	private void setUpRegistry() {

		try {
			GridSchedulerRemoteMessaging cgs_stub = (GridSchedulerRemoteMessaging) UnicastRemoteObject
					.exportObject(this, 0);
			Registry registry = LocateRegistry.getRegistry();
			registry.bind(GridSchedulerRemoteMessaging.registry, cgs_stub);
			logger.info("Grid scheduler's registry was properly set up");
			// System.out.println("GridScheduler registry is properly set up!");

		} catch (Exception e) {
			logger.info("GridScheduler registry wasn't set up " + e.toString());
			e.printStackTrace();
		}

	}

	public void stopPollThread() {
		logger.info("Stopping GS " + this.getUrl());
		try {
			java.rmi.Naming.unbind(this.getUrl());
		} catch (Exception e) {
			e.printStackTrace();
		}

		running = false;
		try {
			pollingThread.join();
		} catch (InterruptedException ex) {
			logger.error("Grid scheduler stopPollThread was interrupted");
			assert (false) : "Grid scheduler stopPollThread was interrupted";
		}
	}

	@Override
	/**
	 * Offloads the job to another GS
	 */
	public void gsToGsJobMessage(JobMessage message) throws RemoteException {
		logger.info("Job with Job_id: " + message.job.getJobNo() + " was received from another GS");
		clientToGsMessage(message);
	}

	@Override
	/**
	 * Returns overall utilization of all clusters under the GS
	 */
	public StatusMessage gsToGsStatusMessage() throws RemoteException {
		int busyCount = 0;
		for (ConcurrentHashMap.Entry<UUID, GsClusterStatus> entry : clusterStatus.entrySet()) {
			busyCount += entry.getValue().getBusyCount();
		}

		return new StatusMessage(busyCount / (nodesPerCluster * this.myClusters.size()));
	}

	/**
	 * @param clusterURL:
	 *            the url of the cluster for which the state is back online
	 *            again
	 */
	@Override
	public void rmToGsStatusMessage(String clusterURL) throws RemoteException {
		// TODO check the cluster status for this specific cluster and update it
		// to online
		UUID key = null;
		for (ConcurrentHashMap.Entry<UUID, GsClusterStatus> entry : clusterStatus.entrySet()) {
			if (entry.getValue().getClusterUrl().equals(clusterURL)) {
				key = entry.getKey();
				break;
			}
		}
		if (key != null) {
			clusterStatus.get(key).setHasCrashed(false);
		}

	}

	/**
	 * @param clusterStatus
	 *            the GsClusterStatus object which informs the replica about the
	 *            current status of the cluster in the VO after a crash of the
	 *            initial grid scheduler
	 */

	@Override
	public void rmToGsStatusMessage(String clusterURL, int nodesBusy) throws RemoteException {
		UUID key = null;
		for (ConcurrentHashMap.Entry<UUID, GsClusterStatus> entry : clusterStatus.entrySet()) {
			if (entry.getValue().getClusterUrl().equals(clusterURL)) {
				key = entry.getKey();
				break;
			}
		}
		if (key != null) {
			clusterStatus.get(key).setBusyCount(nodesBusy);
			clusterStatus.get(key).setHasCrashed(false);
		}

	}

	@Override
	public void backupExternalJobs(Job job, boolean add) throws RemoteException {
		if (add) {
			externalJobs.add(job);
			logger.info("Backup external job received " + job.getJobNo());
		} else {
			externalJobs.remove(job);
			logger.info("Backup external job removed " + job.getJobNo());
		}

	}
	
	/*** GETTERS AND SETTERS ***/
	public String getUrl() {
		return host;
	}

	public void setUrl(String url) {
		this.host = url;
	}

	public ConcurrentHashMap<UUID, GsClusterStatus> getClusterStatus() {
		return clusterStatus;
	}

	public void setClusterStatus(ConcurrentHashMap<UUID, GsClusterStatus> concurrentHashMap) {
		this.clusterStatus = concurrentHashMap;
	}

	public ConcurrentLinkedQueue<Job> getExternalJobs() {
		return externalJobs;
	}

	public void setExternalJobs(ConcurrentLinkedQueue<Job> externalJobs) {
		this.externalJobs = externalJobs;
	}

	public ArrayList<String> getGridschedulers() {
		return gridschedulers;
	}

	public void setGridschedulers(ArrayList<String> gridschedulers) {
		this.gridschedulers = gridschedulers;
	}

	public String getBackupHost() {
		return backupHost;
	}

	public void setBackupHost(String backupHost) {
		this.backupHost = backupHost;
	}

}
