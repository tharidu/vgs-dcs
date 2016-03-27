package dcs.group8.models;

import java.io.Serializable;
import java.util.UUID;

public class GsClusterStatus implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2966305870446790227L;
	private UUID clusterUUID;
	private String clusterUrl;
	private Integer nodeCount;
	private Integer busyCount;
	
	public GsClusterStatus(UUID id,String cluUrl, int nc,int bc){
		clusterUUID = id;
		clusterUrl = cluUrl;
		nodeCount = nc;
		busyCount = bc;
	}
	
	public void decreaseBusyCount(){
		this.busyCount-=1;
	}
	
	public void increaseBusyCount(){
		this.busyCount+=1;
	}
	
	public UUID getClusterUUID() {
		return clusterUUID;
	}
	public void setClusterUUID(UUID clusterUUID) {
		this.clusterUUID = clusterUUID;
	}
	public Integer getNodeCount() {
		return nodeCount;
	}
	public void setNodeCount(Integer nodeCount) {
		this.nodeCount = nodeCount;
	}
	public Integer getBusyCount() {
		return busyCount;
	}
	public void setBusyCount(Integer busyCount) {
		this.busyCount = busyCount;
	}

	public String getClusterUrl() {
		return clusterUrl;
	}

	public void setClusterUrl(String clusterUrl) {
		this.clusterUrl = clusterUrl;
	}
}
