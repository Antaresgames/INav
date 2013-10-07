package cz.muni.fi.sandbox.service.grid;

public interface ProbabilityDistribution {
	public double getValue(double x, double y);
	public double[][] getStepProbabilityField(int size);
}
