package cz.muni.fi.sandbox.service.stepdetector;

public class KalmanFilter {
	private float weight;
	private float guess;//TODO
	private float guessVariance;//TODO
	private final float sensorVariance;
	private float estimate;
	private float estimateVariance;

	public KalmanFilter(float guessVariance, float sensorVariance) {
		this.guessVariance = guessVariance;
		this.sensorVariance = sensorVariance;
	}

	public void pushMeasurement(float measurement) {
		weight = guessVariance / (guessVariance + sensorVariance);
		estimate = guess + weight * (measurement - guess);
		estimateVariance = guessVariance*sensorVariance / (guessVariance+sensorVariance);
		guessVariance = estimateVariance;
		guess = estimate;
	}
	
	public float getEstimate() {
		return estimate;
	}
}
