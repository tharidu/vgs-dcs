package dcs.group8.models;

import java.util.UUID;

public class GsClusterStatus {
	private UUID clusterUUID;
	private Integer nodeCount;
	private Integer busyCount;
	
	public GsClusterStatus(UUID id,int nc,int bc){
		clusterUUID = id;
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
}
