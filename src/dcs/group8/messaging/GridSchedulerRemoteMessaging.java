package dcs.group8.messaging;

import java.rmi.Remote;
import java.rmi.RemoteException;

import dcs.group8.models.Job;

public interface GridSchedulerRemoteMessaging extends Remote {
	
	//this is called by the client to submit a job
	public String clientToGsMessage(JobMessage message) throws RemoteException;
	
	//this is called by the resource manager to notify for the completion of a job
	public void rmToGsMessage(JobMessage message) throws RemoteException;
	
	//this message is send from an rm when it is back online
	public void rmToGsStatusMessage(String clusterURL) throws RemoteException;
	
	//this message is send to inform the replica about the current status of the cluster
	public void rmToGsStatusMessage(String clusterURL, int busyCount) throws RemoteException;
	
	//this a message from gs to gs to delegate a job to another grid scheduler
	public void gsToGsJobMessage(JobMessage message) throws RemoteException;
	
	public StatusMessage gsToGsStatusMessage() throws RemoteException;
	
	public void backupExternalJobs(Job job, boolean add) throws RemoteException;
	
	public static final String registry = "GridSchedulerRemoteMessaging";
}
