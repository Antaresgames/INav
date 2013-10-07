package cz.muni.fi.sandbox.dsp.filters;

public class SignalPower {
	public static double getPower(double[] signal) {
		double sum = 0.0;
		for (double value: signal) {
			sum += value * value;
		}
		return sum;
	}
}
