package dcs.group8.models;

import java.util.ArrayList;
import java.util.List;

public class Cluster {
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

		this.resourceManager = new ResourceManager();
		this.backupResourceManager = new ResourceManager();
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
}
