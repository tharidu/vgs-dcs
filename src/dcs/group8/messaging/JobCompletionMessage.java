package dcs.group8.messaging;

import java.util.UUID;

public class JobCompletionMessage extends Message {
	private static final long serialVersionUID = 5824958458129457382L;
	
	private UUID job_id;

	public UUID getJob_id() {
		return job_id;
	}

	public void setJob_id(UUID job_id) {
		this.job_id = job_id;
	}

}
