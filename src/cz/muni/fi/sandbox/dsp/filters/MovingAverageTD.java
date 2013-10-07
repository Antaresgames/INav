package cz.muni.fi.sandbox.dsp.filters;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

public class MovingAverageTD {
	
	class Sample {
		long delta;
		double value;
		Sample(long delta, double value) {
			this.delta = delta;
			this.value = value;
		}
	}
	
	private long window;
	private Queue<Sample> queue;
	private long lastTimestamp;
	private long timeIntervalContained;
	private double sum;
	private double average;

	private long NANO = (long)Math.pow(10, 9); 

	/**
	 * MovingAverageTD class. Moving average in time domain.
	 * 
	 * @param windowSize size of the window in seconds.
	 */
	public MovingAverageTD(double windowSize) {
		this.window = (long)(windowSize * NANO); // convert to nanoseconds
		queue = new LinkedList<Sample>();
		queue.offer(new Sample(0, 0.0));
		lastTimestamp = 0;
		timeIntervalContained = 0;
		average = 0.0;
	}

	public Collection<Sample> getQueue() {
		return queue;
	}
	

	public void push(long timestamp, double value) {
		
		// use the first call to push only to set the initial timestamp value
		if (lastTimestamp == 0) {
			lastTimestamp = timestamp;
			return;
		}
		
		while (timeIntervalContained > window) {
			//System.out.println("drop, tc="+ timeIntervalContained);
			Sample tmp = queue.poll();
			sum -= tmp.value * tmp.delta;
			timeIntervalContained -= tmp.delta;
		}
		
		long newDelta = timestamp - lastTimestamp;
		boolean inserted = queue.offer(new Sample(newDelta, value));
		
		if (!inserted) {
			throw new RuntimeException("value not inserted");
		}
		
		sum += value * newDelta;
		timeIntervalContained += newDelta;
		average = sum / timeIntervalContained;
		
		lastTimestamp = timestamp;
		
	}
	
	public double getAverage() {
		return average;
	}
	
	public double getRate() {
		return queue.size() * Math.pow(10, 9) / timeIntervalContained;
	}
	
}
