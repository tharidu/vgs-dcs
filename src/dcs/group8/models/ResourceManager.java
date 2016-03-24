package dcs.group8.models;

import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.UUID;

import dcs.group8.messaging.JobMessage;
import dcs.group8.messaging.ResourceManagerRemoteMessaging;

public class ResourceManager implements ResourceManagerRemoteMessaging {
	private LinkedList<Job> jobQueue;
	private int rmNodes;
	public ArrayList<Node> nodes;
	public SortedMap<Long, Integer> jobEndTimes;
	public int busyCount;

	public ResourceManager(int nodeCount) {
		this.rmNodes = nodeCount;
		this.nodes = new ArrayList<Node>(nodeCount);
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
					Node node = nodes.get(i);
					node.setJob(jbm.job);
					nodes.set(i, node);
					long currentTime = new Date().getTime();
					jbm.job.setStartTimestamp(currentTime);
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
