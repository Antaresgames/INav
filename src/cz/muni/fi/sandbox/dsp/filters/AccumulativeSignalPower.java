package cz.muni.fi.sandbox.dsp.filters;

public class AccumulativeSignalPower {
	
	private double power;
	
	public AccumulativeSignalPower() {
		reset();
	}
	
	public void reset() {
		power = 0.0;
	}
	
	public void push(double value) {
		power += value * value;
	}
	
	public double getValue() {
		return power;
	}
	
}
