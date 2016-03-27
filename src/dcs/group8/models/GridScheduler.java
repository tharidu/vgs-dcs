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

public class GridScheduler implements GridSchedulerRemoteMessaging, Runnable {
	private static Logger logger;
	
	private String host;
	private ConcurrentLinkedQueue<Job> externalJobs;
	private ConcurrentHashMap<UUID, GsClusterStatus> clusterStatus;
	private ArrayList<String> gridschedulers;
	private ArrayList<String> myClusters;
	private int nodesPerCluster;

	private static Properties clusterProps;
	private static Properties gsProps;
	// polling thread
	private Thread pollingThread;
	private boolean running;

	public GridScheduler() {
		super();
		try {
			this.host = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		System.setProperty("logfilegs", "gs@"+this.host);
		setExternalJobs(new ConcurrentLinkedQueue<Job>());
		setClusterStatus(new ConcurrentHashMap<>());
		logger = LogManager.getLogger(GridScheduler.class);
		// start the polling thread
		logger.info("Initializing gs@"+this.host);
		running = true;
		pollingThread = new Thread(this);
		pollingThread.start();
		gridSchedulerInit();
		setUpRegistry();
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

	/**
	 * Initialization of a gridscheduler to save the addresses of the clusters
	 * under his responsibility as well as the addresses of the rest of the
	 * gridschedulers in other VOs
	 */
	private void gridSchedulerInit() {
		logger.info("Reading properties files for clusters and grid schedulers");
		try {
			clusterProps = PropertiesUtil.getProperties("dcs.group8.models.GridScheduler", "clusters.properties");
			gsProps = PropertiesUtil.getProperties("dcs.group8.models.GridScheduler", "gridschedulers.properties");
			
			gridschedulers = new ArrayList<String>();
			for (String gsAddr : gsProps.getProperty("gsaddr").split(";")) {
				if (InetAddress.getLocalHost().getHostAddress() == gsAddr){
					continue;
				}
				else{
					gridschedulers.add(gsAddr);
				}
			}
			myClusters = new ArrayList<String>(Arrays.asList(clusterProps.getProperty("claddr").split(";")));
			nodesPerCluster = Integer.parseInt(clusterProps.getProperty("nodes"));
			// initialize the clusterStatus data structure
			logger.info("Initializing the status of the clusters");
			for (int i = 0; i < myClusters.size(); i++) {
				UUID id = UUID.randomUUID();
				GsClusterStatus status = new GsClusterStatus(id, myClusters.get(i), nodesPerCluster, 0);
				clusterStatus.put(id, status);
			}
		} catch (Exception e) {
			logger.error("Unable to read property files.."+e.toString());
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		// jobs receive and handover to RM
		logger.info("Starting gs@"+this.getUrl());

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
						GridSchedulerRemoteMessaging gsm_stub = (GridSchedulerRemoteMessaging) RegistryUtil
								.returnRegistry(gsUrl, "GridSchedulerRemoteMessaging");
						StatusMessage reply = gsm_stub.gsToGsStatusMessage();
						if (reply.utilization < lowestUtilzation) {
							lowestUtilzation = reply.utilization;
							acceptedGsUrl = gsUrl;
						}
					}
					
					if(acceptedGsUrl == "") {
						externalJobs.add(job);
						logger.info("No other GS is willing to take the job");
						//System.err.println("Failed to find a suitable GS to offload the job");
					}

					GridSchedulerRemoteMessaging gsm_stub = (GridSchedulerRemoteMessaging) RegistryUtil
							.returnRegistry(acceptedGsUrl, "GridSchedulerRemoteMessaging");
					gsm_stub.gsToGsJobMessage(new JobMessage(job));
					logger.info("Job successfully sent to gs@"+acceptedGsUrl);
					//System.out.println("Job successfully sent to gs " + acceptedGsUrl);
				}

			} catch (InterruptedException ex) {
				logger.error("Thread run was interrupted in gs@"+this.host);
				ex.printStackTrace();

			} catch (RemoteException e) {
				logger.error("Remote error exception in gs@"+this.host+e.toString());
				e.printStackTrace();
			} catch (NotBoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Receive a notification from a resource manager of a cluster that a job
	 * was completed
	 */
	@Override
	public void rmToGsMessage(JobMessage message) throws RemoteException {
		// first update the clusterStatus data structure based on the UUID
		// of the cluster
		UUID cid = message.job.getClusterId();
		String clientid = message.job.getClientUrl();
		clusterStatus.get(cid).decreaseBusyCount();
		logger.info("Job with Job_id: "+message.job.getJobId()+" was completed from cluster@"+clusterStatus.get(cid).getClusterUrl());
		try {
			ClientRemoteMessaging crm_stub = (ClientRemoteMessaging) RegistryUtil.returnRegistry(clientid,
					"ClientRemoteMessaging");
			crm_stub.gsToClientMessage(message);
		} catch (Exception e) {
			//System.err.println("Message to client from GS : " + host + " could not be send");
			logger.error("Message for job completion could not be send from gs to client@"+message.job.getClientUrl());
			e.printStackTrace();
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
			double utilization = entry.getValue().getBusyCount() / entry.getValue().getNodeCount();
			if (lowestUtilization > utilization) {
				lowestUtilization = utilization;
				selectedCluster = entry;
			}
		}

		if (selectedCluster != null) {
			// Found out one cluster to assign the job
			logger.info("Assigning Job with Job_id:"+jb.job.getJobId()+" to cluster@"+selectedCluster.getValue().getClusterUrl());
			try {
				ResourceManagerRemoteMessaging rm_stub = (ResourceManagerRemoteMessaging) RegistryUtil
						.returnRegistry(selectedCluster.getValue().getClusterUrl(), "ResourceManagerRemoteMessaging");
				// set the cluster id in the job assigned to the cluster
				jb.job.setClusterId(selectedCluster.getKey());
				String ack = rm_stub.gsToRmJobMessage(jb);

				clusterStatus.get(selectedCluster.getKey()).increaseBusyCount();
				//System.out.println("The resource manager responded with: " + ack);
				logger.info("rm@"+selectedCluster.getValue().getClusterUrl()+" responded: "+ack);
			} catch (Exception e) {
				//System.err.println("Communication with resource manager was not established: " + e.toString());
				logger.error("Communication with rm@"+selectedCluster.getValue().getClusterUrl()+" could not be established");
				e.printStackTrace();
			}
			return "JobMessage was received forwarding it to a resource manager";

		} else {
			// Send it to external queue
			logger.info("Adding the Job with Job_id"+jb.job.getJobId()+" to external queue");
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
			//System.out.println("GridScheduler registry is properly set up!");

		} catch (Exception e) {
			logger.info("GridScheduler registry wasn't set up "+e.toString());
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
		logger.info("Job with Job_id: "+message.job.getJobId()+" was received from another GS");
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
}
