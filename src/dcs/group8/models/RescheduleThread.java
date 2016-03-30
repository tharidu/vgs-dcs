package dcs.group8.models;

import java.util.ArrayList;

public class RescheduleThread implements Runnable {
	
	private ArrayList<Job> jlist;
	private GridScheduler gsref;
	
	public RescheduleThread(ArrayList<Job> jlist,GridScheduler gsref){
		this.jlist = jlist;
		this.gsref = gsref;
	}
	
	public void run(){
		for (Job job : jlist){
			gsref.clientToGsMessage(JobFactory.createMessage(job));
			try{
				Thread.sleep(2000);
			}
			catch(InterruptedException ie){
				System.err.println("Rescheduling thread was interrupted..");
				ie.printStackTrace();
			}
		}
		
	}

}
