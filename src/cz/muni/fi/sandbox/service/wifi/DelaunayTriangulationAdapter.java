package cz.muni.fi.sandbox.service.wifi;

import java.util.Iterator;
import java.util.Map.Entry;

import thirdparty.delaunay.Point;
import thirdparty.delaunay.Triangle;
import thirdparty.delaunay.Triangulation;
import android.util.Log;
import cz.muni.fi.sandbox.utils.geometric.Point2D;

/**
 * 
 * @author Michal Holcik
 *
 */
public class DelaunayTriangulationAdapter implements Iterable<Entry<Point2D, Triangle>> {
	
	private static final String TAG = "DelaunayTriangulationAdapter";
	private IGetsYouRssFingerprints mFingerprintsAdaptor;
	
	
	private Triangulation triangulation; // Delaunay triangulation
	private Triangle initialTriangle; // Initial triangle
	private static int initialSize = 10000; // Size of initial triangle
	
	public DelaunayTriangulationAdapter() {
		
		initialTriangle = new Triangle(
				new Point(-initialSize, -initialSize),
				new Point(initialSize, -initialSize),
				new Point(0, initialSize));
		triangulation = new Triangulation(initialTriangle);

	}
	
	public Triangulation getTriangulation() {
		return triangulation;
	}
	
	public void add(Point2D point) {
		Log.d(TAG, "add(point = " + point + ")");
		triangulation.delaunayPlace(new Point(point.getX(), point.getY()));
		//recomputeInterpolated();
	} 
	
	public void recomputeInterpolated() {
		Log.d(TAG, "recreate()");
		this.triangulation = new Triangulation(initialTriangle);
		WifiLayerModel set = mFingerprintsAdaptor.getRssFingerprints();
		if (set == null) {
			Log.e(TAG, "set==null");
		}
		for (RssFingerprint fingerprint: set.getFingerprints()) {
			Point2D loc = fingerprint.getLocation();
			triangulation.delaunayPlace(new Point(loc.getX(), loc.getY()));
		}
	}
	public boolean hasInitialTriangleVertex(Triangle triangle) {
		return triangle.containsAny(initialTriangle);
	}
	
	private class TriangulationIterator implements Iterator<Entry<Point2D, Triangle>> {
		
		Entry<Point2D, Triangle> next;
		
		@Override
		public boolean hasNext() {
			next = null;
			return false;
		}
		
		@Override
		public Entry<Point2D, Triangle> next() {
			return next;
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException("Remove is not supported");
		}
	}
	
	@Override
	public Iterator<Entry<Point2D, Triangle>> iterator() {
		return new TriangulationIterator();
	}
}
