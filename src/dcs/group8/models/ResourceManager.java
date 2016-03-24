package dcs.group8.models;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import dcs.group8.messaging.JobMessage;
import dcs.group8.messaging.ResourceManagerRemoteMessaging;

public class ResourceManager implements ResourceManagerRemoteMessaging{
	
	private HashMap<Node, Job> nodeStatus;
	private LinkedList<Job> jobQueue;
	private int rmNodes;
	private List<Node> nodes;
	
	/**
	 * The message send fromt the gs to this cluster's RM
	 * to get information about the status of the resources
	 * returns the number of available resources(nodes) currently
	 * in the cluster
	 */
	public int gsToRmStatusMessage(){
		return 5;
	}
	
	public ResourceManager(int nodeCount){
		this.rmNodes = nodeCount;
		this.nodes = new ArrayList<Node>(nodeCount);
		System.out.println("The resource manager is created..");
	}
	
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
	@Override
	public String gsToRmJobMessage(JobMessage jbm) throws RemoteException {
		//get a job here and assign it to one of the available nodes in the
		//cluster
		return null;
	}
}
