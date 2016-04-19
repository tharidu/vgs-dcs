package dcs.group8.utils;

public class TimerUtil {
	
	long totalTime = 0; 
	long startTime;
	long endTime;
	
	public void startTimer(){
		 startTime = System.currentTimeMillis();
	}
	
	public void stopTimer(){
		endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		totalTime += duration;
	}
	
	public long getTotalTime(){
		return totalTime;
	}
	
	public long getTimeSoFar(){
		return System.currentTimeMillis() - startTime;
	}
	
}
