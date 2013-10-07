package cz.muni.fi.sandbox.service.wifi;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import thirdparty.delaunay.Point;
import thirdparty.delaunay.Triangulation;
import android.util.Log;
import cz.muni.fi.sandbox.utils.geometric.GridPoint2D;
import cz.muni.fi.sandbox.utils.geometric.Point2D;
import cz.muni.fi.sandbox.utils.geometric.TriangleGrid;

/**
 * 
 * @author Michal Holcik
 */
public class InterpolatedFingerprintModel extends WifiLayerModel {

	private static final long serialVersionUID = -495819651042662755L;
	private static final String TAG = "InterpolatedFingerprintSet";
	private DelaunayTriangulationAdapter interpolator;
	public Map<Point2D, RssFingerprint> interpolationMap;
	
	public InterpolatedFingerprintModel() {
		this(2);
		
	}
	
	public InterpolatedFingerprintModel(int grid) {
		super(grid);
		interpolator = new DelaunayTriangulationAdapter();
		interpolationMap = new HashMap<Point2D, RssFingerprint>();
	}
	

	/**
	 * reads set of fingerprints from the filename
	 * 
	 * @param filename
	 *            of the file from which the fingerprint set is read
	 */
	@Override
	public void read(String filename) {
		set.clear();
		map.clear();
		RssFingerprintReader.readFingerprints(filename, set);
		copyFrom(set);
	}

	@Override
	public void copyFrom(Collection<RssFingerprint> set) {
		clear();
		super.copyFrom(set);
		computeInterpolation();
	}
	
	@Override
	public void copyFrom(WifiLayerModel model) {
		clear();
		super.copyFrom(model);
		computeInterpolation();
	}

	
	@Override
	public boolean add(RssFingerprint fingerprint) {

		boolean inserted = !interpolationMap.containsKey(fingerprint.getLocation());
		if (inserted) {
			interpolationMap.put(fingerprint.getLocation(), fingerprint);
			interpolator.add(fingerprint.getLocation());
		}
		return inserted;
	}
	
	public void clear() {
		interpolationMap.clear();
		interpolator = new DelaunayTriangulationAdapter();
	}
	
	public Triangulation getTriangulation() {
		return interpolator.getTriangulation();
	}
	
	public void computeInterpolation() {
		Log.d(TAG, "computeInterpolation()");
		
		map.clear();
		set.clear();
		for (thirdparty.delaunay.Triangle triangle : getTriangulation()) {
			System.out.println("triangle");
			Point[] vertices = triangle.toArray(new Point[0]);
			
			if (interpolator.hasInitialTriangleVertex(triangle)) {
				continue;
			}
			
			Point2D a = new Point2D(vertices[0].coord(0), vertices[0].coord(1));
			Point2D b = new Point2D(vertices[1].coord(0), vertices[1].coord(1));
			Point2D c = new Point2D(vertices[2].coord(0), vertices[2].coord(1));
			
			cz.muni.fi.sandbox.utils.geometric.Triangle triangle2 = new cz.muni.fi.sandbox.utils.geometric.Triangle(
					new Point2D[] { a, b, c });
			
			// presence check
			for (int i = 0; i < triangle2.getPoints().length; i++) {
				Point2D point = triangle2.getPoints()[i];
				if (interpolationMap.containsKey(point)) {
					//System.out.println("contains key = " + point);
				} else {
					System.out.println("doesn't contain key = " + point);
				}
			}
			// presence check end			

			TriangleGrid grid = new TriangleGrid(triangle2, GRID_SPACING);

			

			for (Point2D point : grid) {
				double[] barycentricCoordinates = triangle2
						.getBarycentricCoords(point);
				RssFingerprint fp = new RssFingerprint(point);
				// add rss vector

				if (interpolationMap.containsKey(triangle2.getPoints()[0])
						&& interpolationMap.containsKey(triangle2.getPoints()[1])
						&& interpolationMap.containsKey(triangle2.getPoints()[2])) {
					
					Set<String> aps = new HashSet<String>();
					for (Point2D p: triangle2.getPoints()) {
						aps.addAll(interpolationMap.get(p).getVector().keySet());
					}
					
					for (String ap : aps) {
						
						double[] values = new double[] { 0, 0, 0 };
						
						if (interpolationMap.get(triangle2.getPoints()[0]).getVector()
								.containsKey(ap)
								&& interpolationMap.get(triangle2.getPoints()[1])
										.getVector().containsKey(ap)
								&& interpolationMap.get(triangle2.getPoints()[2])
										.getVector().containsKey(ap)) {
							values = new double[] {
										interpolationMap.get(triangle2.getPoints()[0])
											.getVector().get(ap).getRss(),
										interpolationMap.get(triangle2.getPoints()[1])
											.getVector().get(ap).getRss(),
										interpolationMap.get(triangle2.getPoints()[2])
											.getVector().get(ap).getRss() };
							
							// System.out.println("values = " + Arrays.toString(values));
							double value = triangleInterpolation(
									barycentricCoordinates, values);
							// System.out.println(point.toString() + " " + fp);
							fp.add(ap, value);
						}
						
						
					}
				} else {
					System.out.println("key not found");
				}
				if (!map.containsKey(point)) {
					map.put(GridPoint2D.snap(point, GRID_SPACING), fp);
					set.add(fp);
				} else {
					System.out.println("map already contains key");
				}
			}
		}
		Log.d(TAG, "computeInterpolation(): end");
	}
	
	public double triangleInterpolation(double[] barycentric, double[] values) {
		double interpolation = 0;
		for (int i = 0; i < 3; i++)
			interpolation += values[i] * barycentric[i];
		return interpolation;
	}
	
}

