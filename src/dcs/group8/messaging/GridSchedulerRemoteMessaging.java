package dcs.group8.messaging;

import dcs.group8.messaging.JobMessage;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface GridSchedulerRemoteMessaging extends Remote {
	
	//this is called by the client to submit a job
	public String clientToGsMessage(JobMessage message) throws RemoteException;
	
	//this is called by the resource manager to notify for the completion of a job
	public void rmToGsMessage(JobMessage message) throws RemoteException;
	
	public static final String registry = "GridSchedulerRemoteMessaging";
}
