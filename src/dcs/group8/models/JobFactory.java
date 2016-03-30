package dcs.group8.models;

import java.util.Random;
import java.util.UUID;

import dcs.group8.messaging.JobMessage;


/**
 * 
 * JobFactory class generates jobs for a client
 * to submit them in the DCS, and also wraps them
 * in messages
 *
 */
public class JobFactory {
	
	private UUID client_id;
	private int lowestDuration;
	private int randomRange;
	private String client_url;
	
	
	/**
	 * 
	 * Creation of a JobFactory for a client
	 * @param cid This client's UUID id
	 * @param ld The lowest duration for a job
	 * @param rr The random range of duration added to the lowest duration
	 * @param cUrl The string url of this client
	 * 
	 */
	public JobFactory(UUID cid, int ld, int rr, String cUrl){
		this.client_id = cid;
		this.lowestDuration = ld;
		this.randomRange = rr;
		this.client_url = cUrl;
	}
	
	/**
	 * 
	 * Creates a Job for a client with the settings set in this JobFactory
	 * @return Job a job with the preferences set from the client to this JobFactory
	 * 
	 */
	public Job createJob(){
		UUID jid = UUID.randomUUID();
		Random rand = new Random();
		int duration = rand.nextInt(this.randomRange)+this.lowestDuration;
		return new Job(jid, duration, client_id, this.client_url);
	}
	
	/**
	 * Just wraps a Job in a JobMessage and returns it
	 * @param job The Job object to be included in the message
	 * @return JobMessage The message containing the job
	 */
	public static JobMessage createMessage(Job job){
		return new JobMessage(job);
	}

}
