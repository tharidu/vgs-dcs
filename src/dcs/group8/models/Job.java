package dcs.group8.models;

import java.io.Serializable;
import java.util.Date;
import java.sql.Timestamp;

public class Job implements Serializable {
	private long jobId;
	private long jobDuration;
	private JobStatus jobStatus;
	private Timestamp startTimestamp;
	private Timestamp endTimestamp;
	private long clientId;
	private long clusterId;
	
	public Job(long jobId, long jobDuration, long clientId) {
		this.jobId = jobId;
		this.jobDuration = jobDuration;
		this.clientId = clientId;
		this.startTimestamp = new Timestamp(new Date().getTime());
		this.jobStatus = JobStatus.Ready;
	}
	
	public long getJobId() {
		return jobId;
	}
	public void setJobId(long jobId) {
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
	public long getClientId() {
		return clientId;
	}
	public void setClientId(long clientId) {
		this.clientId = clientId;
	}
	public long getClusterId() {
		return clusterId;
	}
	public void setClusterId(long clusterId) {
		this.clusterId = clusterId;
	}
}
