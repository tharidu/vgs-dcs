package dcs.group8.models;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import dcs.group8.messaging.GridSchedulerRemoteMessaging;
import dcs.group8.messaging.JobMessage;

public class GridScheduler implements GridSchedulerRemoteMessaging, Runnable {
	private String host;
	private ConcurrentLinkedQueue<Job> externalJobs;
	private ConcurrentHashMap<String, String> clusterStatus;
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

	public ConcurrentHashMap<String, String> getClusterStatus() {
		return clusterStatus;
	}

	public void setClusterStatus(ConcurrentHashMap<String, String> clusterStatus) {
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
		// jobs recive and handover to RM
		System.out.println("Starting GS " + this.getUrl());

		while (running) {
			// sleep
			try {
				Thread.sleep(100);
			} catch (InterruptedException ex) {

			}
		}
	}
	
	public String clientToGsMessage(JobMessage jb){
		System.out.println("Message with id: "+jb.getJob_id()+
				"\n From client with id: "+jb.getClient_id()+
				"\n and job duration: "+jb.getJob_duration());
		return "JobMessage was received";
	}
	
	/**
	 * setup up the registry of the grid scheduler for all
	 * messages it must handle from all entities of the DCS
	 */
	private void setUpRegistry(){
		
		try{
			GridSchedulerRemoteMessaging cgs_stub = (GridSchedulerRemoteMessaging) UnicastRemoteObject.exportObject(this,0);
			Registry registry = LocateRegistry.getRegistry();
			registry.bind("GridSchedulerRemoteMessaging",cgs_stub);
			System.out.println("GridScheduler registry is properly set up!");
			
		}
		catch(Exception e){
			System.err.println("GridScheduler registry wasn't set up: " + e.toString());
			e.printStackTrace();
		}
		
	}
	
	public void getJob() {
		
		
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
