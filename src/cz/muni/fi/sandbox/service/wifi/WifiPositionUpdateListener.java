package cz.muni.fi.sandbox.service.wifi;

public interface WifiPositionUpdateListener {
	public void wifiPositionChanged(float heading, float x, float y);
	// public void probabilityMapChanged(HashMap<Point2D, Double> map, double gridSize);
	public void probabilityMapChanged(ProbabilityMap map);
}
