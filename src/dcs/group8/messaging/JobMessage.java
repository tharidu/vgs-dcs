package dcs.group8.messaging;

import java.io.Serializable;
import java.util.UUID;

import dcs.group8.messaging.Message;

/**
 * 
 * @author greg
 * JobMessage is the message send from the client to 
 * the a GridScheduler of the DCS to submit a job
 * 
 */

public class JobMessage extends Message {
	private static final long serialVersionUID = 1453428681740343634L;
	
	private int job_duration;
	private UUID job_id;
	private UUID client_id;
	
	public JobMessage(){}

	public int getJob_duration() {
		return job_duration;
	}

	public void setJob_duration(int job_duration) {
		this.job_duration = job_duration;
	}

	public UUID getJob_id() {
		return job_id;
	}

	public void setJob_id(UUID job_id) {
		this.job_id = job_id;
	}

	public UUID getClient_id() {
		return client_id;
	}

	public void setClient_id(UUID client_id) {
		this.client_id = client_id;
	}
	
	
}
