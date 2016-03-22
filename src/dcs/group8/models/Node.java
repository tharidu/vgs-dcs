package dcs.group8.models;

public class Node {
	private NodeStatus nodeStatus;
	private Job job;
	public NodeStatus getNodeStatus() {
		return nodeStatus;
	}
	public void setNodeStatus(NodeStatus nodeStatus) {
		this.nodeStatus = nodeStatus;
	}
	public Job getJob() {
		return job;
	}
	public void setJob(Job job) {
		this.job = job;
	}
}
