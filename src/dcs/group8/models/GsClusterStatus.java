package dcs.group8.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

public class GsClusterStatus implements Serializable {
	/**
	 * GsClusterStatus is the object in which the grid schedulers
	 * store any cluster related information they want to know
	 */
	private static final long serialVersionUID = -2966305870446790227L;
	
	private UUID clusterUUID;
	private String clusterUrl;
	private Integer nodeCount;
	private Integer busyCount;
	private boolean hasCrashed;
	private ArrayList<Job> jobList;
	
	public GsClusterStatus(UUID id,String cluUrl, int nc,int bc,boolean status){
		clusterUUID = id;
		clusterUrl = cluUrl;
		nodeCount = nc;
		busyCount = bc;
		hasCrashed = status;
		jobList = new ArrayList<Job>();
	}
	
	
	public void setJob(Job job){
		this.jobList.add(job);
	}
	
	public void removeJob(Job job){
		// remove the job from the job list 
		// when it is finished
		int index = jobList.indexOf(job);
		jobList.remove(index);
	}


	public boolean isHasCrashed() {
		return hasCrashed;
	}

	public void setHasCrashed(boolean hasCrashed) {
		this.hasCrashed = hasCrashed;
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
