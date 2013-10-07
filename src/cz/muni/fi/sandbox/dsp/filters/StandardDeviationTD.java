package cz.muni.fi.sandbox.dsp.filters;

public class StandardDeviationTD {

	private MovingAverageTD ma;
		
	public StandardDeviationTD(MovingAverageTD ma) {
		this.ma = ma;
	}
	
	public double getStandardDeviation() {
		double sum = 0.0;
		double average = ma.getAverage();
		for (MovingAverageTD.Sample s: ma.getQueue()) {
			double diff = s.value - average;
			sum += diff * diff;
		}
		double sd = Math.sqrt(sum / ma.getQueue().size());
		return sd;
	}
	
	public MovingAverageTD getMovingAverage() {
		return ma;
	}
}
