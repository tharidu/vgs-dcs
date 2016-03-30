package dcs.group8.messaging;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientRemoteMessaging extends Remote {
	
	public static final String registry = "ClientRemoteMessaging";
	
	/**
	 * 
	 * Message send from a GS to a client to inform him about the completion of a job
	 * @param jcm The JobMessage object
	 * @throws RemoteException
	 * 
	 */
	public void gsToClientMessage(JobMessage jcm) throws RemoteException;
}
