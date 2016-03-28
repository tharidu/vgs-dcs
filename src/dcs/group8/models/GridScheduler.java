package dcs.group8.models;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
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

public class GridScheduler implements GridSchedulerRemoteMessaging, Runnable {
	private static Logger logger;
	private String host;
	private String backupHost;
	private ConcurrentLinkedQueue<Job> externalJobs;
	private ConcurrentHashMap<UUID, GsClusterStatus> clusterStatus;
	private ArrayList<String> gridschedulers;
	private ArrayList<String> myClusters;
	private int nodesPerCluster;
	private ConcurrentHashMap<UUID, GsClusterStatus> backupClusterStatus;
	private ArrayList<String> backupMyClusters;
	private static Properties clusterProps;
	private static Properties gsProps;
	// polling thread
	private Thread pollingThread;
	private boolean running;
	private ConcurrentLinkedQueue<Job> backupExternalJobs;

	public ConcurrentLinkedQueue<Job> getBackupExternalJobs() {
		return backupExternalJobs;
	}

	public void setBackupExternalJobs(ConcurrentLinkedQueue<Job> backupExternalJobs) {
		this.backupExternalJobs = backupExternalJobs;
	}

	public ConcurrentHashMap<UUID, GsClusterStatus> getBackupClusterStatus() {
		return backupClusterStatus;
	}

	public void setBackupClusterStatus(ConcurrentHashMap<UUID, GsClusterStatus> backupClusterStatus) {
		this.backupClusterStatus = backupClusterStatus;
	}

	public ArrayList<String> getBackupMyClusters() {
		return backupMyClusters;
	}

	public void setBackupMyClusters(ArrayList<String> backupMyClusters) {
		this.backupMyClusters = backupMyClusters;
	}
	
	public GridScheduler(String backup) {
		super();
		backupHost = backup;

		try {
			this.host = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		System.setProperty("logfilegs", "gs@" + this.host);
		setExternalJobs(new ConcurrentLinkedQueue<Job>());
		setClusterStatus(new ConcurrentHashMap<>());
		logger = LogManager.getLogger(GridScheduler.class);
		// start the polling thread
		logger.info("Initializing gs@" + this.host);
		gridSchedulerInit();
		setUpRegistry();
		running = true;
		pollingThread = new Thread(this);
		pollingThread.start();
	}

	public String getUrl() {
		return host;
	}

	public void setUrl(String url) {
		this.host = url;
	}

	public ConcurrentHashMap<UUID, GsClusterStatus> getClusterStatus() {
		return clusterStatus;
	}

	public void setClusterStatus(ConcurrentHashMap<UUID, GsClusterStatus> clusterStatus) {
		this.clusterStatus = clusterStatus;
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

	/**
	 * Initialization of a gridscheduler to save the addresses of the clusters
	 * under his responsibility as well as the addresses of the rest of the
	 * gridschedulers in other VOs
	 */
	private void gridSchedulerInit() {
		logger.info("Reading properties files for clusters and grid schedulers");
		gridschedulers = new ArrayList<String>();
		try {
			clusterProps = PropertiesUtil.getProperties("dcs.group8.models.GridScheduler", "clusters.properties");
			gsProps = PropertiesUtil.getProperties("dcs.group8.models.GridScheduler", "gridschedulers.properties");
			for (String gsAddr : gsProps.getProperty("gsaddr").split(";")) {
				if (InetAddress.getLocalHost().getHostAddress() == gsAddr) {
					continue;
				} else {
					gridschedulers.add(gsAddr);
				}
			}
			myClusters = new ArrayList<String>(Arrays.asList(clusterProps.getProperty("claddr").split(";")));
			nodesPerCluster = Integer.parseInt(clusterProps.getProperty("nodes"));
			// initialize the clusterStatus data structure
			logger.info("Initializing the status of the clusters");
			for (int i = 0; i < myClusters.size(); i++) {
				UUID id = UUID.randomUUID();
				GsClusterStatus status = new GsClusterStatus(id, myClusters.get(i), nodesPerCluster, 0,false);
				clusterStatus.put(id, status);
			}
		} catch (Exception e) {
			logger.error("Unable to read property files.." + e.toString());
			e.printStackTrace();
		}
	}
	
	/**
	 * The polling thread monitors the queue of external jobs
	 * in order to try and delegate them to other VOs if it succeeds
	 * it sends the job to the appropriate gs or else it re-enters 
	 * the job to the queue for a future try
	 */

	@Override
	public void run() {
		// jobs receive and handover to RM
		logger.info("Starting gs@" + this.getUrl());

		while (running) {
			// sleep
			try {
				Thread.sleep(100);

				Job job = externalJobs.poll();
				if (job != null) {
					logger.info("Trying to reach other GSs to delegate this job");
					// Check local resorces also

					// Check remote gs
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
								break;
							} catch (Exception e) {
								try {
									retry.errorOccured();
								//TODO how are we going to handle offline gridscheduler nodes here
								} catch (RetryException e1) {
									logger.error("Could not contact other gs@"+gsUrl+" to offload job");
									e.printStackTrace();
								}
							}
						}
					}

					if (acceptedGsUrl == "") {
						externalJobs.add(job);
						logger.info("No other GS is willing to take the job");
					}

					RetryStrategy retry = new RetryStrategy();

					while (retry.shouldRetry()) {
						try {
							GridSchedulerRemoteMessaging gsm_stub = (GridSchedulerRemoteMessaging) RegistryUtil
									.returnRegistry(acceptedGsUrl, "GridSchedulerRemoteMessaging");
							gsm_stub.gsToGsJobMessage(new JobMessage(job));
							logger.info("Job successfully sent to gs@" + acceptedGsUrl);
							// System.out.println("Job successfully sent to gs "
							// +
							// acceptedGsUrl);
							break;
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
			} catch (InterruptedException ex) {
				logger.error("Thread run was interrupted in gs@" + this.host);
			}
		}
	}

	/**
	 * Receive a notification from a resource manager of a cluster that a job
	 * was successfully completed
	 */
	@Override
	public void rmToGsMessage(JobMessage message) throws RemoteException {
		// first update the clusterStatus data structure based on the UUID
		// of the cluster
		UUID cid = message.job.getClusterId();
		String clientid = message.job.getClientUrl();
		clusterStatus.get(cid).decreaseBusyCount();
		logger.info("Job with Job_id: " + message.job.getJobId() + " was completed from cluster@"
				+ clusterStatus.get(cid).getClusterUrl());
		//removing job from the list of jobs for this specific cluster
		clusterStatus.get(cid).removeJob(message.job);
		RetryStrategy retry = new RetryStrategy();
		
		
		//try to send a response to the client about the completion of this job
		while (retry.shouldRetry()) {
			try {
				ClientRemoteMessaging crm_stub = (ClientRemoteMessaging) RegistryUtil.returnRegistry(clientid,
						"ClientRemoteMessaging");
				crm_stub.gsToClientMessage(message);
				break;
			} catch (Exception e) {
				try {
					retry.errorOccured();
				} catch (RetryException e1) {
					logger.error("Message for job completion could not be send from gs to client@"
							+ message.job.getClientUrl());
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Receive a job from client here and push the job to a resource manager
	 * after you first check the resources available at each cluster
	 */
	public String clientToGsMessage(JobMessage jb) {
		ConcurrentHashMap.Entry<UUID, GsClusterStatus> selectedCluster = null;
		double lowestUtilization = 1;

		// Get the cluster with lowest utilisation and assign the job, otherwise
		// offload to other GS
		for (ConcurrentHashMap.Entry<UUID, GsClusterStatus> entry : clusterStatus.entrySet()) {
			// if he know that a cluster is already offline we continue looping
			// over the rest of the clusters in the VO
			if (entry.getValue().isHasCrashed()){
				continue;
			}
			double utilization = entry.getValue().getBusyCount() / entry.getValue().getNodeCount();
			if (lowestUtilization > utilization) {
				lowestUtilization = utilization;
				selectedCluster = entry;
			}
		}

		if (selectedCluster != null) {
			// Found out one cluster to assign the job
			logger.info("Assigning Job with Job_id:" + jb.job.getJobId() + " to cluster@"
					+ selectedCluster.getValue().getClusterUrl());
			RetryStrategy retry = new RetryStrategy();
			while (retry.shouldRetry()) {
				try {
					ResourceManagerRemoteMessaging rm_stub = (ResourceManagerRemoteMessaging) RegistryUtil
							.returnRegistry(selectedCluster.getValue().getClusterUrl(),
									"ResourceManagerRemoteMessaging");
					// set the cluster id in the job assigned to the cluster
					jb.job.setClusterId(selectedCluster.getKey());
					String ack = rm_stub.gsToRmJobMessage(jb);
					logger.info("rm@" + selectedCluster.getValue().getClusterUrl() + " responded: " + ack);
					clusterStatus.get(selectedCluster.getKey()).increaseBusyCount();
				} catch (Exception e) {
					try {
						retry.errorOccured();
					} catch (RetryException e1) {
						logger.error("Communication with rm@" + selectedCluster.getValue().getClusterUrl()
								+ " could not be established after 5 retries...the resource manager is offline");
						//TODO an rm has crashed recover all the jobs that is currently running and set the correct cluster status
						selectedCluster.getValue().setHasCrashed(true);
						e.printStackTrace();
					}
				}
			}
			selectedCluster.getValue().setJob(jb.job);
			return "JobMessage was received forwarding it to a resource manager";
		} 
		else {
			// Send it to external queue, either everything is crashed!!
			// or we could all the clusters in the VO are at their peak
			// of their utilization
			logger.info("Job with job_id: "+jb.job.getJobId()+" for client@"+jb.job.getClientUrl()+" is placed in the external jobs queue");
			externalJobs.add(jb.job);
		}
		
		return "JobMessage was received";
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
		logger.info("Job with Job_id: " + message.job.getJobId() + " was received from another GS");
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

	@Override
	public void sendBackupGS(ConcurrentLinkedQueue<Job> externalJobs,
			ConcurrentHashMap<UUID, GsClusterStatus> clusterStatus, ArrayList<String> myClusters)
			throws RemoteException {
		backupExternalJobs = externalJobs;
		backupClusterStatus = clusterStatus;
		backupMyClusters = myClusters;

	}

	@Override
	public void rmToGsStatusMessage(String clusterURL) throws RemoteException {
		//TODO check the cluster status for this specific cluster and update it to online
		UUID key=null;
		for (ConcurrentHashMap.Entry<UUID, GsClusterStatus> entry : clusterStatus.entrySet()) {
			if (entry.getValue().getClusterUrl().equals(clusterURL)){
				key = entry.getKey();
				break;
			}
		}
		if (key!=null){
			clusterStatus.get(key).setHasCrashed(false);
		}
		
	}
	
}
