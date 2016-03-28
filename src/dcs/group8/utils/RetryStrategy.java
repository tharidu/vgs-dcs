package dcs.group8.utils;

public class RetryStrategy {
	public static final int DEFAULT_NUMBER_OF_RETRIES = 5;
	public static final long DEFAULT_WAIT_TIME = 1000;

	private int numberOfRetries; // total number of tries
	private int numberOfTriesLeft; // number left
	private long timeToWait; // wait interval
	private boolean successfullyTried = false;
	
	public boolean isSuccessfullyTried() {
		return successfullyTried;
	}

	public void setSuccessfullyTried(boolean successfullyTried) {
		this.successfullyTried = successfullyTried;
	}

	public RetryStrategy() {
		this(DEFAULT_NUMBER_OF_RETRIES, DEFAULT_WAIT_TIME);
	}

	public RetryStrategy(int numberOfRetries, long timeToWait) {
		this.numberOfRetries = numberOfRetries;
		numberOfTriesLeft = numberOfRetries;
		this.timeToWait = timeToWait;
	}

	/**
	 * @return true if there are tries left
	 */
	public boolean shouldRetry() {
		numberOfRetries--;
		return (numberOfTriesLeft > 0 && !successfullyTried);
	}

	/**
	 * This method should be called if a try fails.
	 *
	 * @throws RetryException
	 *             if there are no more tries left
	 */
	public void errorOccured() throws RetryException {
		numberOfTriesLeft--;
		if (!(numberOfRetries>0)) {
			throw new RetryException(
					numberOfRetries + " attempts to retry failed at " + getTimeToWait() + "ms interval");
		}
		waitUntilNextTry();
	}

	/**
	 * @return time period between retries
	 */
	public long getTimeToWait() {
		return timeToWait;
	}

	/**
	 * Sleeps for the duration of the defined interval
	 */
	private void waitUntilNextTry() {
		try {
			Thread.sleep(getTimeToWait());
		} catch (InterruptedException ignored) {
		}
	}
	
	///////////////////////////////
	// Use retry policy like this
	///////////////////////////////
//	while (retry.shouldRetry()) {
//		try {
//			break;
//		} catch (Exception e) {
//			try {
//				retry.errorOccured();
//			} catch (RetryException e1) {
//				e.printStackTrace();
//			}
//		}
//	}
}