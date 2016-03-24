package dcs.group8.messaging;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ResourceManagerRemoteMessaging extends Remote {

	public static final String registry = "ResourceManagerRemoteMessaging";
	
	public String gsToRmJobMessage(JobMessage jbm) throws RemoteException;
}
