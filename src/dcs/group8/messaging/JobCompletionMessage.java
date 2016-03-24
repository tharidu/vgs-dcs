package dcs.group8.messaging;

import java.util.UUID;

import dcs.group8.models.Job;

public class JobCompletionMessage extends Message {
	private static final long serialVersionUID = 5824958458129457382L;
	public Job job;
	
	public JobCompletionMessage(Job jb){
		this.job=jb;
	}

}
