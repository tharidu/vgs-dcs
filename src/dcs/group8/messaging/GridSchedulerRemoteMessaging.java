package dcs.group8.messaging;

import dcs.group8.messaging.JobMessage;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface GridSchedulerRemoteMessaging extends Remote {
	
	public String clientToGsMessage(JobMessage message) throws RemoteException;
}
