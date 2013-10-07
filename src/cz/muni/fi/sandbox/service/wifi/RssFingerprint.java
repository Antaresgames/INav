package cz.muni.fi.sandbox.service.wifi;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import cz.muni.fi.sandbox.utils.geometric.Point2D;

public class RssFingerprint {
	
	private Point2D location;
	private HashMap<String, RssMeasurement> rssVector;
	
	//private static final String TAG = "RssFingerprint";
	
	public static final double RSS_STANDARD_DEVIATION = 2.0;
	public static final double RSS_DIFFERENCE_THRESHOLD = 3 * RSS_STANDARD_DEVIATION;
	public static final double MIN_RSS = -100;
	
	public class RssMeasurement implements Comparable<RssMeasurement> {
		private int count;
		private double rss;
		
		public RssMeasurement() {
			count = 0;
			rss = 0.0;
		}
		
		public void addRss(double newRss) {
			// recompute average rss
			rss = (rss * count +  newRss) / (count + 1);
			count++;
		}
		
		public double getRss() {
			return rss;
		}

		@Override
		public int compareTo(RssMeasurement other) {
			if (rss == other.rss) {
				return 0;
			} else {
				return (int) Math.signum(rss - other.rss);
			}
			
		}
		
		
	}
	

	public RssFingerprint() {
		rssVector = new HashMap<String, RssMeasurement>();
	}
	
	public RssFingerprint(Point2D location) {
		this();
		setLocation(location);
	}
	
	public HashMap<String, RssMeasurement> getVector() {
		return rssVector;
	}

	private static double sqr(double x) {
		return x * x;
	}
	
	/**
	 * add a measurement sample to the vector
	 */
	public void add(String accessPointId, double rss) {
		//Log.d(TAG, "add(accessPointId = " + accessPointId + ", " + rss + ")");
		if (!rssVector.containsKey(accessPointId)) {
			rssVector.put(accessPointId, new RssMeasurement());
		}
		rssVector.get(accessPointId).addRss(rss);
	}

	/**
	 * Compute Euclidian distance from the other fingerprints based on a common
	 * subset of mac addresses.
	 * 
	 * @param other The fingerprint from which the distance is measured.
	 * @return Euclidian distance from the other fingerprint.
	 */
	public double distanceSimpleEuclidian(RssFingerprint other) {

		double sum = 0.0f;
		boolean atLeastOneFound = false;
		for (String accessPoint : other.rssVector.keySet()) {
			if (rssVector.containsKey(accessPoint) && other.rssVector.containsKey(accessPoint)) {
				sum += sqr(rssVector.get(accessPoint).getRss() - other.rssVector.get(accessPoint).getRss());
				atLeastOneFound = true;
			}
		}
		if (atLeastOneFound) 
			return Math.sqrt(sum);
		else {
			
			return Double.POSITIVE_INFINITY;
		}
	}

	public double distanceSimpleEuclidian(RssFingerprint other, boolean subsituteLeft, boolean substituteRight) {

		double MIN_RSS = -100;
		double sum = 0.0f;
		Set<String> aps = new HashSet<String>();
		aps.addAll(rssVector.keySet());
		aps.addAll(other.rssVector.keySet());
		for (String accessPoint : aps) {
			if (subsituteLeft || rssVector.containsKey(accessPoint)
				&& substituteRight || other.rssVector.containsKey(accessPoint)) {
				double thisRss = rssVector.containsKey(accessPoint)?rssVector.get(accessPoint).getRss():MIN_RSS;
				double otherRss = other.rssVector.containsKey(accessPoint)?other.rssVector.get(accessPoint).getRss():MIN_RSS;
				sum += sqr(thisRss - otherRss);
			}
		}
		return Math.sqrt(sum);
	}

	
	// TODO: weights not set
	public double distanceWeightedEuclidian(RssFingerprint other) {

		double WEIGHT = 1.0;
		
		double sum = 0.0f;
		for (String accessPoint : other.rssVector.keySet()) {
			double thisRss = rssVector.containsKey(accessPoint)?rssVector.get(accessPoint).getRss():MIN_RSS;
			double otherRss = other.rssVector.get(accessPoint).getRss();
			sum += sqr(thisRss - otherRss) * WEIGHT;
		}
		return Math.sqrt(sum);
	}
	
	
	public double distanceHamming(RssFingerprint other) {

		double sum = 0.0;
		Set<String> aps = new HashSet<String>();
		aps.addAll(rssVector.keySet());
		aps.addAll(other.rssVector.keySet());
		for (String accessPoint : aps) {
			if (! rssVector.containsKey(accessPoint)
				|| ! other.rssVector.containsKey(accessPoint)) {
				sum++;
			}
		}
		return sum / aps.size();
	}
	
	public double distanceHammingIntervalBased(RssFingerprint other) {

		double sum = 0.0;
		Set<String> aps = new HashSet<String>();
		aps.addAll(rssVector.keySet());
		aps.addAll(other.rssVector.keySet());
		
		for (String accessPoint : aps) {
			if (! rssVector.containsKey(accessPoint)
				|| ! other.rssVector.containsKey(accessPoint)
				|| Math.abs(rssVector.get(accessPoint).getRss() - other.rssVector.get(accessPoint).getRss()) > 3 * RSS_STANDARD_DEVIATION) {
				sum++;
			}
		}
		return sum / aps.size();
	}
	
	
	public Point2D getLocation() {
		return location;
	}

	public void setLocation(Point2D location) {
		this.location = location;
	}
	
	public String toString() {
		String retval = "loc = " + location;
		for (String accessPoint : rssVector.keySet()) {
			retval += " (" + accessPoint + " " + rssVector.get(accessPoint).getRss() + ")";
		}
		return retval; 
	}

}
