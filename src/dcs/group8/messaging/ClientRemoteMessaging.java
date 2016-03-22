package dcs.group8.messaging;

import java.rmi.Remote;

public interface ClientRemoteMessaging extends Remote {
	
	public String gsToClientMessage(JobCompletionMessage jcm);

	public static final String registry = "ClientRemoteMessaging";
}
