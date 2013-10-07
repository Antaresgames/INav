package cz.muni.fi.sandbox.service.grid;

public class NormalBivariateDistribution implements ProbabilityDistribution {

	// the formula
	private double muX = 0.0f;
	private double muY = 0.0f;
	private double sigmaX = 1.0f;
	private double sigmaY = 1.0f;
	private double rho = 0.0f; // covariance / sigmaX / sigmaY;
	
	public NormalBivariateDistribution(double mu[], double sigma[], double rho) {
		this.muX = mu[0];
		this.muY = mu[1];
		this.sigmaX = sigma[0];
		this.sigmaY = sigma[1];
		this.rho = rho;
	}
	
	@Override
	public double getValue(double x, double y) {
				
		double result;
		double oneMinusRhoSquared =  1 - rho * rho;
		result = 1 / (2 * Math.PI * sigmaX * sigmaY * Math.sqrt(oneMinusRhoSquared));
		double tempX = (x - muX) / sigmaX;
		double tempY = (y - muY) / sigmaY;
		double exponent = - 1.0 / 2.0 / oneMinusRhoSquared;
		exponent *= (tempX * tempX + tempY * tempY - 2 * rho * tempX * tempY);
		result *= Math.exp(exponent);
		return result;
	}
	
	// TODO: current implementation is a simplification, needs rework
	public double[][] getStepProbabilityField(int size) {
		assert(size == 3 || size == 5);
		
		double[][] field = new double[size][size];
		
		// get normalization factor
		double sum = 0.0;
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				field[i][j] = getValue(i - size/2, j - size/2);
				sum += field[i][j];
			}
		}
		
		// initialize mStepProbability field
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				field[i][j] /= sum;
			}
		}
		return field;
	}

}
