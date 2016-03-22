package dcs.group8.messaging;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientRemoteMessaging extends Remote {
	
	public String gsToClientMessage(JobCompletionMessage jcm) throws RemoteException;

	public static final String registry = "ClientRemoteMessaging";
}
