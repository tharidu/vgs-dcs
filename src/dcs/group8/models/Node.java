package dcs.group8.models;

import java.util.Date;


//make the Node a thread that runs a job until it ends
public class Node implements Runnable{
	
	private Job job;
	private CallBack cb;
	private long endtime;
	//instantiate the node with a job to run
	//and a callback function that is created
	//by the resource manager
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
	
	public Job getJob() {
		return job;
	}
	public void setJob(Job job) {
		this.job = job;
	}
}
