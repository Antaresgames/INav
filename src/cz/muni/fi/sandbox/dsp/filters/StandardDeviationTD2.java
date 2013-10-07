package cz.muni.fi.sandbox.dsp.filters;

import java.util.LinkedList;
import java.util.Queue;

public class StandardDeviationTD2 {

	
	private long window;
	private Queue<Sample> queue;
	private long lastTimestamp;
	private long timeIntervalContained;
	private double sum;
	private double average;
	
	class Sample {
		long delta;
		double value;
		Sample(long delta, double value) {
			this.delta = delta;
			this.value = value;
		}
	}

	
	public StandardDeviationTD2(long window) {
		this.window = window;
		queue = new LinkedList<Sample>();
		queue.offer(new Sample(0, 0.0));
		lastTimestamp = 0;
		timeIntervalContained = 0;
		average = 0.0;
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
	
	public double getStandardDeviation() {
		double sum = 0.0;
		for (Sample s: queue) {
			double diff = s.value - average;
			sum += diff * diff;
		}
		double sd = Math.sqrt(sum / queue.size());
		return sd;
	}
	
	public double getAverage() {
		return average;
	}
	
	public double getRate() {
		return queue.size() * Math.pow(10, 9) / timeIntervalContained;
	}
	
}

