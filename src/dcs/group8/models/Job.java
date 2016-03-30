package dcs.group8.models;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;


/**
 * 
 *
 * The Job object representing a job that is submitted to the DCS
 *
 *
 */
public class Job implements Serializable {

	private static final long serialVersionUID = -1629778320006112305L;
	private String clientUrl;
	private UUID jobId;
	private long jobDuration;
	private JobStatus jobStatus;
	private long startTimestamp;
	private long executeTimestamp;
	private long endTimestamp;
	private UUID clientId;
	private UUID clusterId;
	
	
	/**
	 * 
	 * Constructor of a Job object
	 * @param jobId A UUID for the job
	 * @param jobDuration The duration in millisecs for the job
	 * @param clientId The id of the client submitting the job
	 * @param clientUrl The url address of the client submitting the job
	 * 
	 */
	public Job(UUID jobId, long jobDuration, UUID clientId, String clientUrl) {
		this.clientUrl = clientUrl;
		this.jobId = jobId;
		this.jobDuration = jobDuration;
		this.clientId = clientId;
		this.startTimestamp = new Date().getTime();
		this.jobStatus = JobStatus.Ready;
	}
	
	
	/**
	 * 
	 * A user friendly string representation of the Job object
	 * 
	 */
	@Override
	public String toString(){
		return " Job_id: "+this.jobId+" from client@"+this.clientUrl+" ";
		
	}
	
	/***  GETTERS AND SETTERS METHODS  ***/
	
	public String getClientUrl() {
		return clientUrl;
	}

	public void setClientUrl(String clientUrl) {
		this.clientUrl = clientUrl;
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
	public long getStartTimestamp() {
		return startTimestamp;
	}
	public void setStartTimestamp(long startTimestamp) {
		this.startTimestamp = startTimestamp;
	}
	public long getEndTimestamp() {
		return endTimestamp;
	}
	public void setEndTimestamp(long endTimestamp) {
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

	public long getExecuteTimestamp() {
		return executeTimestamp;
	}

	public void setExecuteTimestamp(long executeTimestamp) {
		this.executeTimestamp = executeTimestamp;
	}
}
