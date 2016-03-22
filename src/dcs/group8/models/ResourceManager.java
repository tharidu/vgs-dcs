package dcs.group8.models;

import java.util.HashMap;
import java.util.LinkedList;

public class ResourceManager {
	private HashMap<Node, Job> nodeStatus;
	private LinkedList<Job> jobQueue;
	
	public HashMap<Node, Job> getNodeStatus() {
		return nodeStatus;
	}
	public void setNodeStatus(HashMap<Node, Job> nodeStatus) {
		this.nodeStatus = nodeStatus;
	}
	public LinkedList<Job> getJobQueue() {
		return jobQueue;
	}
	public void setJobQueue(LinkedList<Job> jobQueue) {
		this.jobQueue = jobQueue;
	}
}
