package dcs.group8.models;

import java.util.Date;



/**
 * 
 * 
 * A node is a Thread running a specific duration and then
 * call the callback function to signal the RM that it finished
 *
 */
public class Node implements Runnable{
	
	private Job job;
	private CallBack cb;
	private long endtime;
	
	/**
	 * 
	 * Constructor of a Node running a job in the cluster
	 * @param job The Job that this node is running
	 * @param cb The callback function to be called when the job is finished
	 * 
	 */
	public Node(Job job,CallBack cb){
		this.job = job;
		this.cb = cb;
	}
	
	
	public void run(){
		System.out.println(new Date().getTime());
		try {
			Thread.sleep(job.getJobDuration());
		} catch (InterruptedException e) {
			System.err.println("Interrupted exception in node running job"+e.toString());
			e.printStackTrace();
		}
		this.job.setJobStatus(JobStatus.Finished);
		System.out.println(new Date().getTime());
		this.cb.callback();
	}
	
	
	/*** GETTERS AND SETTERS ***/
	public Job getJob() {
		return job;
	}
	public void setJob(Job job) {
		this.job = job;
	}
}
