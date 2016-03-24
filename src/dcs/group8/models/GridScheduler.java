package dcs.group8.models;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import dcs.group8.messaging.GridSchedulerRemoteMessaging;
import dcs.group8.messaging.JobMessage;
import dcs.group8.messaging.ResourceManagerRemoteMessaging;

public class GridScheduler implements GridSchedulerRemoteMessaging, Runnable {
	private String host;
	private ConcurrentLinkedQueue<Job> externalJobs;
	private ConcurrentHashMap<UUID, GsClusterStatus> clusterStatus;
	private ArrayList<String> gridschedulers;

	// polling thread
	private Thread pollingThread;
	private boolean running;

	public GridScheduler(String host) {
		super();
		this.host = host;
		setExternalJobs(new ConcurrentLinkedQueue<>());
		setClusterStatus(new ConcurrentHashMap<>());

		// start the polling thread
		running = true;
		pollingThread = new Thread(this);
		pollingThread.start();

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

	@Override
	public void run() {
		// jobs receive and handover to RM
		System.out.println("Starting GS " + this.getUrl());

		while (running) {
			// sleep
			try {
				Thread.sleep(100);
			} catch (InterruptedException ex) {

			}
		}
	}
	/**
	 * Receive a job from client here and push the job to a resource
	 * manager after you first check the resources available at each
	 * cluster
	 */
	public String clientToGsMessage(JobMessage jb){
		UUID assignedCluster = null;
		double lowestUtilization = 1;
		
		// Get the cluster with lowest utilisation and assig the job, otherwise offload to other GS
		for (ConcurrentHashMap.Entry<UUID, GsClusterStatus> entry : clusterStatus.entrySet()) {
			double utilization = entry.getValue().getBusyCount() / entry.getValue().getNodeCount();
			if (lowestUtilization > utilization) {
				lowestUtilization = utilization;
				assignedCluster = entry.getKey();
			}
		}

		if (assignedCluster != null) {
			// Found out one cluster to assign the job
			try {
				Registry registry = LocateRegistry.getRegistry("localhost");
				ResourceManagerRemoteMessaging rm_stub = (ResourceManagerRemoteMessaging) registry
						.lookup("ResourceManagerRemoteMessaging");
				String ack = rm_stub.gsToRmJobMessage(jb);
				
				GsClusterStatus gsClusterStatus = clusterStatus.get(assignedCluster);
				gsClusterStatus.setBusyCount(gsClusterStatus.getBusyCount() + 1);
				clusterStatus.put(assignedCluster, gsClusterStatus);
				
				System.out.println("The resource manager responded with: " + ack);
			} catch (Exception e) {
				System.err.println("Communication with resource manager was not established: " + e.toString());
				e.printStackTrace();
			}
			return "JobMessage was received forwarding it to a resource manager";

		} else {
			// Send it to external queue
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
			System.out.println("GridScheduler registry is properly set up!");

		} catch (Exception e) {
			System.err.println("GridScheduler registry wasn't set up: " + e.toString());
			e.printStackTrace();
		}

	}

	public void stopPollThread() {
		System.out.println("Stopping GS " + this.getUrl());
		try {
			java.rmi.Naming.unbind(this.getUrl());
		} catch (Exception e) {
			e.printStackTrace();
		}

		running = false;
		try {
			pollingThread.join();
		} catch (InterruptedException ex) {
			assert (false) : "Grid scheduler stopPollThread was interrupted";
		}

	}
}
