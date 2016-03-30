package dcs.group8.messaging;


/**
 * 
 * A message that sends the status of a GS to whom it requests for it
 *
 */

public class StatusMessage extends Message {

	private static final long serialVersionUID = 8329283164812478237L;
	
	public StatusMessage(double utilization) {
		super();
		this.utilization = utilization;
	}

	public double utilization;
}
