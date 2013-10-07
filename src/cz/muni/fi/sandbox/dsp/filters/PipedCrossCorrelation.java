package cz.muni.fi.sandbox.dsp.filters;

import java.util.*;

public class PipedCrossCorrelation {
	
	private double[] kernel;
	private Queue<Double> signal;
	private double kernelPower;
	private double nFactor;
	
	public PipedCrossCorrelation(SignalWindow window) {
		this(window.getSignal());
	}
	
	public PipedCrossCorrelation(double[] kernel) {
		this.kernel = new double[kernel.length];
		System.arraycopy(kernel, 0, this.kernel, 0, kernel.length);
		
		signal = new LinkedList<Double>();
		signal.clear();
		for (int i = 0; i < kernel.length; i++) {
			signal.add(0.0);
		}
		kernelPower = SignalPower.getPower(kernel);
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
		return 10 * crossCorrelate();
	}
	
	public double getRelativeValue() {
		return crossCorrelate() * nFactor;
	}
	
	private double crossCorrelate() {
		assert(kernel.length == signal.size());
		
		int index = 0;
		double result = 0.0;
		//double pwr = 0.0;
		
		double avg = 0.0;
		double max = 0.0;
		for (double value: signal) {
			avg += value;
			if (max < Math.abs(value)) {
				max = Math.abs(value);
			}
		}
		avg /= signal.size();
		
		for (double value: signal) {
			//pwr += value * value;
			//result += value * kernel[index++];
			double diff = (value - avg) - (max * kernel[index]);
			result += diff * diff;
		}
		nFactor = 1 / kernelPower / kernelPower;
		return result / kernel.length;
	}
	
}
