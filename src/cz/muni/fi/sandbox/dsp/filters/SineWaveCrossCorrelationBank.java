package cz.muni.fi.sandbox.dsp.filters;

import java.util.ArrayList;


public class SineWaveCrossCorrelationBank {
	
	ArrayList<SineWaveCrossCorrelation> bank;
	double[] values;
	double[] maximums;
	
	public SineWaveCrossCorrelationBank(float sampleRate, float[] frequencies) {
		bank = new ArrayList<SineWaveCrossCorrelation>(frequencies.length);
		for (float frequency: frequencies) {
			bank.add(new SineWaveCrossCorrelation(sampleRate, frequency));
		}
		values = new double[frequencies.length];
		maximums = new double[frequencies.length];
	}
	
	public void push(double sample) {
		for (SineWaveCrossCorrelation xc: bank) {
			xc.push(sample);
		}
	}
	
	public double[] getValues() {
		int index = 0; 
		for (SineWaveCrossCorrelation xc: bank) {
			values[index] = xc.getValue();
			maximums[index] = Math.max(values[index], maximums[index]);
			index++;
		}
		return values;
	}
	
	
	public double[] getMaximums() {
		return maximums;
	}
	
	public void resetMaximums() {
		for (int i = 0; i < bank.size(); i++) {
			maximums[i] = 0.0;
		}
	}
	
}

