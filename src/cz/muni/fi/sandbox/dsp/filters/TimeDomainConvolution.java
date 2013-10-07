package cz.muni.fi.sandbox.dsp.filters;

import java.util.*;

public class TimeDomainConvolution {
	
	private double[] kernel;
	
	// FIXME: private class 
	private Queue<Double> signal;
	
	public TimeDomainConvolution(SignalWindow window) {
		this(window.getSignal());
	}
	
	public TimeDomainConvolution(double[] kernel) {
		this.kernel = new double[kernel.length];
		System.arraycopy(kernel, 0, this.kernel, 0, kernel.length);
		
		signal = new LinkedList<Double>();
		signal.clear();
		for (int i = 0; i < kernel.length; i++) {
			signal.add(0.0);
		}
		
	}

	
	public double process(double value) {
		push(value);
		return getValue();
	}
	
	public void push(double value) {
		signal.poll();
		signal.add(value);
	}
	
	public double getValue() {
		return convolve();
	}
	
	
	private double convolve() {
		assert(kernel.length == signal.size());
		
		int index = 0;
		double result = 0.0;
		for (double value: signal) {
			result += value * kernel[index++];
		}
		return result;
	}
	

}
