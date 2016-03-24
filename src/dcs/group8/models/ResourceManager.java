package dcs.group8.models;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.SortedMap;

import dcs.group8.messaging.JobMessage;
import dcs.group8.messaging.ResourceManagerRemoteMessaging;

public class ResourceManager implements ResourceManagerRemoteMessaging {
	
	private LinkedList<Job> jobQueue;
	private int rmNodes;
	public ArrayList<Node> nodes;
	public SortedMap<Long, Integer> jobEndTimes;
	public int busyCount;

	/**
	 * The message send fromt the gs to this cluster's RM
	 * to get information about the status of the resources
	 * returns the number of available resources(nodes) currently
	 * in the cluster
	 */
	public int gsToRmStatusMessage(){
		return 5;
	}
	
	public ResourceManager(int nodeCount) {
		this.rmNodes = nodeCount;
		this.nodes = new ArrayList<Node>(nodeCount);
		System.out.println("The resource manager is created..");
	}

	public LinkedList<Job> getJobQueue() {
		return jobQueue;
	}

	public void setJobQueue(LinkedList<Job> jobQueue) {
		this.jobQueue = jobQueue;
	}

	@Override
	public String gsToRmJobMessage(JobMessage jbm) throws RemoteException {
		if (busyCount < rmNodes) {
			for (int i = 0; i < nodes.size(); i++) {
				if (nodes.get(i) == null) {
					long currentTime = new Date().getTime();
					Node node = nodes.get(i);
					jbm.job.setJobStatus(JobStatus.Running);
					jbm.job.setStartTimestamp(currentTime);
					node.setJob(jbm.job);
					nodes.set(i, node);
					jobEndTimes.put(currentTime + jbm.job.getJobDuration(), i);
				}
			}
		} else {
			// Send a message to GS that RM is full
			// Should never happen
			System.err.println("RM full but job received");
		}
		return "Job Accepted";
	}
}
