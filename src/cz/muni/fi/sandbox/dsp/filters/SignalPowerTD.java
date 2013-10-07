package cz.muni.fi.sandbox.dsp.filters;

import java.util.LinkedList;
import java.util.Queue;

public class SignalPowerTD {

	private static final double NANO_INV = Math.pow(10, 9);
	
	class Sample {
		long delta;
		double value;
		Sample(long delta, double value) {
			this.delta = delta;
			this.value = value;
		}
	}
	
	private long interval;
	private Queue<Sample> queue;
	private double power;
	private long lastTimestamp;
	private long timeIntervalContained;
	private double sum;
	

	
	public SignalPowerTD(long interval) {
		this.interval = interval;
		queue = new LinkedList<Sample>();
		queue.offer(new Sample(0, 0.0));
		power = 0.0;
		lastTimestamp = 0;
		timeIntervalContained = 0;
	}
	
	public void push(long timestamp, double value) {
		
		// use the first call to push only to set the initial timestamp value
		if (lastTimestamp == 0) {
			lastTimestamp = timestamp;
			return;
		}
		
		while (timeIntervalContained > interval) {
			//System.out.println("drop, tc="+ timeIntervalContained);
			Sample tmp = queue.poll();
			sum -= tmp.value * tmp.value;
			timeIntervalContained -= tmp.delta;
		}
		
		long newDelta = timestamp - lastTimestamp;
		boolean retval = queue.offer(new Sample(newDelta, value));
		assert(retval);
		
		sum += value * value;
		timeIntervalContained += newDelta;
		power = sum * NANO_INV / timeIntervalContained;
		lastTimestamp = timestamp;
		
	}
	
	public double getPower() {
		return power;
	}

	
	public static void main(String[] args) {
		SignalPowerTD sp = new SignalPowerTD(4);
		
		long delta = 1;
		long timestamp = 1; 
		sp.push(timestamp, 0);
		
		for (Double i: new double[] {1, 2, 3, 4, 5, 6, 7, 8, 9}) {
			
			sp.push(timestamp += delta, i);
			double j = sp.getPower();
			System.out.println("(" + timestamp + ", " + i + "): " + j);
		}
	}
}

