package dcs.group8.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

/**
 * 
 * GsClusterStatus is the object in which the grid schedulers
 * store any cluster related information they want to know
 * 
 */
public class GsClusterStatus implements Serializable {

	private static final long serialVersionUID = -2966305870446790227L;
	
	private UUID clusterUUID;
	private String clusterUrl;
	private Integer nodeCount;
	private Integer busyCount;
	private boolean hasCrashed;
	private ArrayList<Job> jobList;
	
	/**
	 * 
	 * Initialization of GsClusterStatus object
	 * @param id The UUID id of this cluster
	 * @param cluUrl The cluster's url 
	 * @param nc The number of nodes of this cluster
	 * @param bc The number of busy nodes in this cluster
	 * @param status The crash status of this cluster
	 * 
	 */
	public GsClusterStatus(UUID id,String cluUrl, int nc,int bc,boolean status){
		clusterUUID = id;
		clusterUrl = cluUrl;
		nodeCount = nc;
		busyCount = bc;
		hasCrashed = status;
		jobList = new ArrayList<Job>();
	}
	
	/**
	 * 
	 * Set a job in the list that is currently running in
	 * the cluster. For fault tolerance reasons
	 * @param job The Job object
	 * 
	 */
	public void setJob(Job job){
		this.jobList.add(job);
	}
	
	/**
	 * 
	 * Remove a job that was completed by a node in this
	 * cluster from the queue. For fault tolerance reasons
	 * @param job
	 * 
	 */
	public void removeJob(Job job){
		int index=0;
		for (Job j : jobList){
			if (j.getJobId().equals(job.getJobId())){
				System.out.println("Found the job and breaking now");
				break;
			}
			else{
				index++;
			}
		}
		jobList.remove(index);
	}

	
	
	/*** GETTERS AND SETTERS ***/
	
	public ArrayList<Job> getJobList() {
		return jobList;
	}

	public void setJobList(ArrayList<Job> jobList) {
		this.jobList = jobList;
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
