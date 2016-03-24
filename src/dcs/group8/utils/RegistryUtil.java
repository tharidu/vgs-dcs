package dcs.group8.utils;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RegistryUtil {
	public static Remote returnRegistry(String url, String remoteInterface) throws RemoteException, NotBoundException
	{
			Registry registry = LocateRegistry.getRegistry(url);
			return registry.lookup(remoteInterface);
	}
}
