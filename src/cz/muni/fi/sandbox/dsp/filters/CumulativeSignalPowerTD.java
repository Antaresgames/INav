package cz.muni.fi.sandbox.dsp.filters;

public class CumulativeSignalPowerTD {
	
	private double power;
	private long lastTimestamp;
	private static final double NANO = Math.pow(10, 9);
	
	public CumulativeSignalPowerTD() {
		reset();
	}
	
	public void reset() {
		power = 0.0;
	}
	
	public void push(long timestamp, double value) {
		
		// use the first call to push only to set the initial timestamp value
		if (lastTimestamp == 0) {
			lastTimestamp = timestamp;
			return;
		}
		
		double deltaT = (timestamp - lastTimestamp) / NANO;
		power += value * value / deltaT;
		lastTimestamp = timestamp;
	}
	
	public double getValue() {
		return power;
	}
	
}
