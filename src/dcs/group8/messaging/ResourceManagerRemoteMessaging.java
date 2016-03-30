package dcs.group8.messaging;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ResourceManagerRemoteMessaging extends Remote {

	public static final String registry = "ResourceManagerRemoteMessaging";
	
	
	/**
	 * 
	 * Method that sends a Job from a GS to one of the RM in the VO
	 * @param jbm The JobMessage object
	 * @return String An acknowledgement
	 * @throws RemoteException
	 * 
	 */
	public String gsToRmJobMessage(JobMessage jbm) throws RemoteException;
	
	/**
	 * 
	 * THIS IS NOT USED AT THE MOMENT
	 * @return
	 * @throws RemoteException
	 * 
	 */
	public int gsToRmStatusMessage() throws RemoteException;
}
