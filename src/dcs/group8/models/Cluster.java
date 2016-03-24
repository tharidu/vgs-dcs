package dcs.group8.models;

import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import dcs.group8.messaging.GridSchedulerRemoteMessaging;
import dcs.group8.messaging.ResourceManagerRemoteMessaging;

public class Cluster implements Remote {
	private ResourceManager resourceManager;
	private ResourceManager backupResourceManager;
	private List<Node> nodes;
	private String host;
	private String gridSchedulerHost;

	public Cluster(String url, String gridSchedulerUrl, int nodeCount) {
		super();
		this.host = url;
		this.gridSchedulerHost = gridSchedulerUrl;
		nodes = new ArrayList<Node>(nodeCount);

		this.resourceManager = new ResourceManager(nodeCount);
		this.backupResourceManager = new ResourceManager(nodeCount);

		this.setUpRegistry();
	}

	public ResourceManager getResourceManager() {
		return resourceManager;
	}

	public void setResourceManager(ResourceManager resourceManager) {
		this.resourceManager = resourceManager;
	}

	public ResourceManager getBackupResourceManager() {
		return this.backupResourceManager;
	}

	public void setBackupResourceManager(ResourceManager backupResourceManager) {
		this.backupResourceManager = backupResourceManager;
	}

	public List<Node> getNodes() {
		return nodes;
	}

	public void setNodes(List<Node> nodes) {
		this.nodes = nodes;
	}

	public String getUrl() {
		return host;
	}

	public void setUrl(String url) {
		this.host = url;
	}

	public String getGridSchedulerUrl() {
		return gridSchedulerHost;
	}

	public void setGridSchedulerUrl(String gridSchedulerUrl) {
		this.gridSchedulerHost = gridSchedulerUrl;
	}

	/**
	 * setup up the registry of the resource mamanger for all messages it must
	 * handle from all entities of the DCS
	 */
	private void setUpRegistry() {

		try {
			ResourceManagerRemoteMessaging cgs_stub = (ResourceManagerRemoteMessaging) UnicastRemoteObject.exportObject(this.resourceManager,0);
			//set the registry at the address of the cluster
			//the methods are exposed by the resource manager...
			Registry registry = LocateRegistry.getRegistry(host);
			registry.bind(ResourceManagerRemoteMessaging.registry, cgs_stub);
			System.out.println("Resource Manager registry is properly set up!");
			
		}
		catch(Exception e){
			System.err.println("Resource Manager registry wasn't set up: " + e.toString());
			e.printStackTrace();
		}

	}

	public double returnUtilization() {
		int count = 0;
		for (int i = 0; i < nodes.size(); i++) {
			if(nodes.get(i).getNodeStatus() == NodeStatus.Busy) {
				count++;
			}
		}
		return count/nodes.size();
	}
}
