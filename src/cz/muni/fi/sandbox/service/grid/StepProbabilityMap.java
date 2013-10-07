package cz.muni.fi.sandbox.service.grid;

public class StepProbabilityMap {
	
	private int STEP_PROBABILITY_SIZE;
	private double[][] mStepProbability;
	
	public StepProbabilityMap(ProbabilityDistribution distribution, int size) {
		STEP_PROBABILITY_SIZE = size;
		mStepProbability = distribution.getStepProbabilityField(size);
	}
	
	public double[][] getMap() {
		return mStepProbability;
	}
	
	public int getSize() {
		return STEP_PROBABILITY_SIZE;
	}
}
