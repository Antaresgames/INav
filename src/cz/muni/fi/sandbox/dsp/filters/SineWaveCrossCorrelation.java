package cz.muni.fi.sandbox.dsp.filters;

public class SineWaveCrossCorrelation extends PipedCrossCorrelation {
	/**
	 * 
	 * @param sampleRate
	 * @param frequency
	 */
	public SineWaveCrossCorrelation(float sampleRate, float frequency) {
		super(new SineWave(sampleRate, frequency));
	}
}

