package dcs.group8.models;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

public class Job implements Serializable {
	private UUID jobId;
	private long jobDuration;
	private JobStatus jobStatus;
	private Timestamp startTimestamp;
	private Timestamp endTimestamp;
	private UUID clientId;
	private UUID clusterId;
	
	public Job(UUID jobId, long jobDuration, UUID clientId) {
		this.jobId = jobId;
		this.jobDuration = jobDuration;
		this.clientId = clientId;
		this.startTimestamp = new Timestamp(new Date().getTime());
		this.jobStatus = JobStatus.Ready;
	}
	
	public UUID getJobId() {
		return jobId;
	}
	public void setJobId(UUID jobId) {
		this.jobId = jobId;
	}
	public long getJobDuration() {
		return jobDuration;
	}
	public void setJobDuration(long jobDuration) {
		this.jobDuration = jobDuration;
	}
	public JobStatus getJobStatus() {
		return jobStatus;
	}
	public void setJobStatus(JobStatus jobStatus) {
		this.jobStatus = jobStatus;
	}
	public Timestamp getStartTimestamp() {
		return startTimestamp;
	}
	public void setStartTimestamp(Timestamp startTimestamp) {
		this.startTimestamp = startTimestamp;
	}
	public Timestamp getEndTimestamp() {
		return endTimestamp;
	}
	public void setEndTimestamp(Timestamp endTimestamp) {
		this.endTimestamp = endTimestamp;
	}
	public UUID getClientId() {
		return clientId;
	}
	public void setClientId(UUID clientId) {
		this.clientId = clientId;
	}
	public UUID getClusterId() {
		return clusterId;
	}
	public void setClusterId(UUID clusterId) {
		this.clusterId = clusterId;
	}
}
