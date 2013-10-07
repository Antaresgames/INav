package cz.muni.fi.sandbox.dsp.filters;

public class SineWave extends SignalWindow {
	
	public SineWave(float rate, float frequency) {
		
		super((int)(rate / frequency));
		
		for (int i = 0; i < signal.length; i++) {
			signal[i] = Math.sin(i * 2.0 * Math.PI / signal.length);
		}
	}
}
