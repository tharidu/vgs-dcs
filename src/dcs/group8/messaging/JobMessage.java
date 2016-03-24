package dcs.group8.messaging;

import dcs.group8.models.Job;

/**
 * 
 * @author greg
 * JobMessage is the message send from the client to 
 * the a GridScheduler of the DCS to submit a job
 * 
 */

public class JobMessage extends Message {
	private static final long serialVersionUID = 1453428681740343634L;
	
	public Job job;
	
	public JobMessage(Job jb){
		this.job = jb;
	}
	
}
