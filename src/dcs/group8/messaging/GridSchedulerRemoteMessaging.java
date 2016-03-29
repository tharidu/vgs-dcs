package dcs.group8.messaging;

import java.rmi.Remote;
import java.rmi.RemoteException;

import dcs.group8.models.Job;

public interface GridSchedulerRemoteMessaging extends Remote {
	
	/**
	 * Method called from the client to a GS in order to submit a job 
	 * to a VO in the distributed system
	 * @param message A JobMessage object containing the job
	 * @return A String acknowledgement 
	 * @throws RemoteException
	 */
	public String clientToGsMessage(JobMessage message) throws RemoteException;
	
	/**
	 * This method is called from a RM to GS to inform the GS that a job
	 * was completed from this specific cluster
	 * @param message
	 * @throws RemoteException
	 */
	public void rmToGsMessage(JobMessage message) throws RemoteException;
	
	/**
	 * Sends a message from a RM to GS to inform him about its
	 * current status when it is back online 
	 * @param clusterURL The url of the cluster
	 * @throws RemoteException
	 */
	public void rmToGsStatusMessage(String clusterURL) throws RemoteException;
	
	/**
	 * Sends a message from a RM to auxiliary GS to inform him about the current
	 * status in the cluster. This method is called when a fault on a GS was
	 * encountered
	 * @param clusterURL This cluster's url
	 * @param busyCount The number of nodes currently busy running jobs
	 * @throws RemoteException 
	 */
	public void rmToGsStatusMessage(String clusterURL, int busyCount) throws RemoteException;
	
	/**
	 * Method called from GS to another GS to offload jobs when the caller is
	 * swarmed with jobs 
	 * @param message A JobMessage containing the Job 
	 * @throws RemoteException
	 */
	public void gsToGsJobMessage(JobMessage message) throws RemoteException;
	
	/**
	 * Method called from a GS to all other GS in the distributed
	 * system to get their statuses and decide where to delegate a job
	 * @return StatusMessage containing the status of the VO of this GS
	 * @throws RemoteException
	 */
	public StatusMessage gsToGsStatusMessage() throws RemoteException;
	
	/**
	 * A message to inform the aux grid scheduler when a job should be add 
	 * or removed from it's external queue structure 
	 * @param job The Job object with information about client's job
	 * @param add Boolean, true if you want to add a job or false if it going to be removed
	 * @throws RemoteException
	 * 
	 */
	public void backupExternalJobs(Job job, boolean add) throws RemoteException;
	
	public static final String registry = "GridSchedulerRemoteMessaging";
}
