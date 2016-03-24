package dcs.group8.models;

import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import dcs.group8.messaging.GridSchedulerRemoteMessaging;
import dcs.group8.messaging.JobMessage;
import dcs.group8.messaging.ResourceManagerRemoteMessaging;
import dcs.group8.utils.RegistryUtil;

public class Cluster implements Remote, Runnable {
	private ResourceManager resourceManager;
	private ResourceManager backupResourceManager;
	private List<Node> nodes;
	private String host;
	private String gridSchedulerHost;

	// polling thread
	private Thread pollingThread;
	private boolean running;

	public Cluster(String url, String gridSchedulerUrl, int nodeCount) {
		super();
		this.host = url;
		this.gridSchedulerHost = gridSchedulerUrl;
		nodes = new ArrayList<Node>(nodeCount);

		this.resourceManager = new ResourceManager(nodeCount);
		this.backupResourceManager = new ResourceManager(nodeCount);

		this.setUpRegistry();

		// start the polling thread
		running = true;
		pollingThread = new Thread(this);
		pollingThread.start();
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
			System.out.println("Resource Manager registry is properly set up!");

		} catch (Exception e) {
			System.err.println("Resource Manager registry wasn't set up: " + e.toString());
			e.printStackTrace();
		}

	}

	@Override
	public void run() {
		System.out.println("Starting Cluster " + this.getUrl());
		while (running) {
			for (Iterator<Map.Entry<Long, Integer>> it = this.resourceManager.jobEndTimes.entrySet().iterator(); it.hasNext();) {
				Entry<Long, Integer> entry = it.next();
				if (entry.getKey() <= new Date().getTime()) {
					// Job done
					this.resourceManager.busyCount--;
					Job job = this.resourceManager.nodes.get(entry.getValue()).getJob();
					this.resourceManager.nodes.set(entry.getValue(), null);
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
	}

	public void stopPollThread() {
		System.out.println("Stopping Cluster " + this.getUrl());
		try {
			java.rmi.Naming.unbind(this.getUrl());
		} catch (Exception e) {
			e.printStackTrace();
		}

		running = false;
		try {
			pollingThread.join();
		} catch (InterruptedException ex) {
			assert (false) : "Cluster stopPollThread was interrupted";
		}

	}
}
