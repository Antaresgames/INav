package cz.muni.fi.sandbox.service.wifi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

import android.util.Log;
import cz.muni.fi.sandbox.utils.geometric.GridPoint2D;
import cz.muni.fi.sandbox.utils.geometric.Point2D;
import cz.muni.fi.sandbox.utils.geometric.Rectangle;

/**
 * class RssFingerprintModel
 * @author Michal Holcik
 */
public class WifiLayerModel implements Serializable {

	private static final long serialVersionUID = -1650163018732192782L;
	
	private static final String TAG = "WifiLayerModel";
	protected HashSet<RssFingerprint> set;
	protected HashMap<GridPoint2D, RssFingerprint> map;
	protected ProbabilityMap probMap;
	protected final int GRID_SPACING;
	private static final double WIFI_RSS_SIGMA = 3.0;
	
	
	public enum ProbabilityMapMetric {
		RANDOM,
		IDENTITY,
		SIMPLE_EUCLIDIAN,
		EUCLIDIAN_INTERSECTION,
		WEIGHTED_EUCLIDIAN,
		HAMMING,
		HAMMING_INTERVAL
	}
	
	public WifiLayerModel() {
		this(2);
	}

	
	public WifiLayerModel(int grid) {
		GRID_SPACING = grid;
		map = new HashMap<GridPoint2D, RssFingerprint>();
		set = new HashSet<RssFingerprint>();
	}

	public WifiLayerModel(String filename) {
		this(filename, 2);
	}
	
	
	public WifiLayerModel(String filename, int grid) {
		this(grid);
		read(filename);
	}
	
	

	/**
	 * reads set of fingerprints from the filename
	 * 
	 * @param filename
	 *            of the file from which the fingerprint set is read
	 */
	public void read(String filename) {
		set.clear();
		map.clear();
		RssFingerprintReader.readFingerprints(filename, set);
		copyFrom(set);
	}
	
	public void copyFrom(Collection<RssFingerprint> set) {
		for (RssFingerprint fingerprint: set) {
			add(fingerprint);
		}
	}
	
	public void copyFrom(WifiLayerModel set2) {
		for (RssFingerprint fingerprint: set2.getFingerprints()) {
			add(fingerprint);
		}
	}

	/**
	 * Adds the fingerprint to the fingerprint set
	 * 
	 * @param fingerprint
	 *            to be added
	 * @return true iff the fingerprint was added to the set
	 */
	public boolean add(RssFingerprint fingerprint) {
		boolean retval = false;
		if (!map.containsKey(fingerprint.getLocation())) {
			map.put(GridPoint2D.snap(fingerprint.getLocation(), GRID_SPACING), fingerprint);
			retval = true;
		}
		retval &= set.add(fingerprint);
		return retval;
	}


	/**
	 * clears the fingerprint set
	 */
	public void clear() {
		set.clear();
		map.clear();
	}

	/**
	 * Returns the distance between two adjacent grid points.
	 * @return the grid spacing
	 */
	public double getGridSpacing() {
		return GRID_SPACING;
	}


	
	/**
	 * Returns a collection of fingerprints from the set
	 * 
	 * @return a collection of fingerprints from the set.
	 */
	public Collection<RssFingerprint> getFingerprints() {
		return Collections.unmodifiableCollection(set);
	}

	/**
	 * Returns the fingerprint corresponding to the location in the grid. The
	 * location with coordinates (x, y) is truncated to the corresponding grid
	 * square.
	 * 
	 * @param x
	 *            the x coordinate of the location in meters
	 * @param y
	 *            the y coordinate of the location in meters
	 * @return the fingerprint on the location x, y
	 */
	public RssFingerprint getFingerprintNear(double x, double y) {
		double locX = GRID_SPACING * (int) (x / GRID_SPACING);
		double locY = GRID_SPACING * (int) (y / GRID_SPACING);
		return map.get(new Point2D(locX, locY));
	}

	/**
	 * Finds the best match corresponding to the measured fingerprint using the
	 * Euclidian distance metric.
	 * 
	 * @param measurement
	 *            the measured fingerprint vector
	 * @return best match for the measured fingerprint
	 */
	public RssFingerprint findNearest(RssFingerprint measurement, String method) {

		double minDistance = Double.POSITIVE_INFINITY;
		RssFingerprint bestMatch = null;
		
		for (RssFingerprint fp : set) {
			double distance;
			distance = measurement.distanceSimpleEuclidian(fp, true, true);
			// distance = measurement.distanceWeightedEuclidian(fp);
			// distance = measurement.distanceHamming(fp);
			if (distance < minDistance) {
				minDistance = distance;
				bestMatch = fp;
			}
		}
		return bestMatch;
	}
	
	
	/**
	 * Compares fingerprints based on distance from a common reference fingerprint.
	 * 
	 */
	private class RssFingerprintComparator implements Comparator<RssFingerprint> {
			
		private RssFingerprint measurement;
		
		public RssFingerprintComparator(RssFingerprint measurement) {
			this.measurement = measurement;
		}

		@Override
		public int compare(RssFingerprint fp1, RssFingerprint fp2) {
			return (int)Math.signum(fp1.distanceSimpleEuclidian(measurement) - fp2.distanceSimpleEuclidian(measurement));
		}
	};
	
	/**
	 * 
	 * @param measurement reference fingerprint
	 * @param k
	 * @return
	 */
	public Collection<RssFingerprint> findKNearestNeighbors(RssFingerprint measurement, int k) {
	
		List<RssFingerprint> list = new ArrayList<RssFingerprint>();
		
		for (RssFingerprint fp: getFingerprints()) {	
			list.add(fp);
		}
		Collections.sort(list, new RssFingerprintComparator(measurement));
		List<RssFingerprint> nearest = list.subList(0, k-1);
			
		return nearest;
	}
	
	

	public Point2D computeAverageFingerprintLocation(Collection<RssFingerprint> fps) {
		// neighbor average
		double avgx = 0;
		double avgy = 0;
		for (RssFingerprint fp : fps) {
			avgx += fp.getLocation().getX();
			avgy += fp.getLocation().getY();
		}
		avgx /= fps.size();
		avgy /= fps.size();
		return new Point2D(avgx, avgy);
	}
		

	/**
	 * Returns a set of all BSSIDs in the fingerprint set.
	 * @return a set of BSSIDs in the fingerprint set. 
	 */
	public String[] getBssidList() {
		Log.d(TAG, "getBssidList()");

		TreeSet<String> bssidList = new TreeSet<String>();
		bssidList.add("maximum");
		for (RssFingerprint fp : set) {
			bssidList.addAll(fp.getVector().keySet());
		}
		String[] array = bssidList.toArray(new String[] {});
		Arrays.sort(array);
		return array;
	}
	
	private ProbabilityMapMetric getMethodEnum(String method) {
		if (method.equals("random"))
			return ProbabilityMapMetric.RANDOM;
		else if (method.equals("identity"))
			return ProbabilityMapMetric.IDENTITY;
		else if (method.equals("euclidian"))
			return ProbabilityMapMetric.SIMPLE_EUCLIDIAN;
		else if (method.equals("euclidian_intersection"))
			return ProbabilityMapMetric.EUCLIDIAN_INTERSECTION;
		else if (method.equals("weighted_euclidian"))
			return ProbabilityMapMetric.WEIGHTED_EUCLIDIAN;
		else if (method.equals("hamming"))
			return ProbabilityMapMetric.HAMMING;
		else if (method.equals("hamming_interval"))
			return ProbabilityMapMetric.HAMMING_INTERVAL;
		else 
			// if all fail
			return ProbabilityMapMetric.RANDOM;
	}
	
	private double metric(RssFingerprint measurement, RssFingerprint fp, ProbabilityMapMetric metric) {
		double probability = 0.0;
		switch(metric) {
			case RANDOM:
				probability = Math.random();
				break;
			case IDENTITY:
				probability = 1.0;
				break;
			case SIMPLE_EUCLIDIAN:
				double dist = measurement.distanceSimpleEuclidian(fp, true, true);
				probability = 1 / (1 + Math.sqrt(dist) / WIFI_RSS_SIGMA);
				break;
			case EUCLIDIAN_INTERSECTION:
				probability = measurement.distanceSimpleEuclidian(fp, false, false);
				break;
			case WEIGHTED_EUCLIDIAN:
				probability = measurement.distanceWeightedEuclidian(fp);
				break;
			case HAMMING:
				probability = measurement.distanceHamming(fp);
				break;
			case HAMMING_INTERVAL:
				probability = measurement.distanceHammingIntervalBased(fp);
				break;
			default:
				probability = Math.random();
				
		}
		return probability;
	}
	
	public ProbabilityMap getProbabilityMap(RssFingerprint measurement, String method) {
		Log.d(TAG, "getProbabilityMap: method = " + method);
		computeProbabilityMap(measurement, getMethodEnum(method));
		return probMap;
	}
	
	private void computeProbabilityMap(RssFingerprint measurement, ProbabilityMapMetric metricType) {
		
		if (probMap == null) {
			probMap = new ProbabilityMap(getBox(), GRID_SPACING);
		} else {
			probMap.clear();
		}
		
		for (RssFingerprint fp : set) {
			probMap.set(fp.getLocation(), (float)metric(measurement, fp, metricType));
		}
		/*
		for (int x = probMap.getOrigin().x; x < probMap.getOrigin().x + probMap.getSize().x; x++) {
			for (int y = probMap.getOrigin().y; y < probMap.getOrigin().y + probMap.getSize().y; y++) {
				probMap.set(x, y, (float)Math.random());				
			}
		}
		*/
		
		/*
		
		if (distanceMap != null)
			distanceMap.clear();

		for (RssFingerprint fp : set) {
			double distance;
			distance = measurement.distanceSimpleEuclidian(fp, true, true);
			// distance = measurement.distanceWeightedEuclidian(fp);
			// distance = measurement.distanceHamming(fp);
			if (distanceMap != null)
				distanceMap.put(fp.getLocation(), distance);
			if (distance < minDistance) {
				minDistance = distance;
				bestMatch = fp;
			}
		}
		return bestMatch;
		
		
		for (Entry<GridPoint2D, Double> entry: map.entrySet()) {
			double value = entry.getValue();
			if (min > value)
				min = value;
			if (max < value)
				max = value;
			// sum += entry.getValue();
		}
		
		for (Entry<Point2D, Double> entry: map.entrySet()) {
			sum += max - entry.getValue();
		}
		
		for (Entry<Point2D, Double> entry: map.entrySet()) {
			// double prob = (max - entry.getValue()) / sum;
			double prob = 1.0 / map.size();
			// System.out.println(prob);
			probabilityMap.setProbability(entry.getKey(), prob);
		}
		*/
	}
	
	private Rectangle getBox() {
		if (set.isEmpty())
			return null;
		Rectangle box = new Rectangle();
		for (RssFingerprint fp : set) {
			box.enlargeToFit(fp.getLocation());
		}
		box.enlarge(2 * GRID_SPACING);
		box.consolidate();
		return box;
	}
	

}
