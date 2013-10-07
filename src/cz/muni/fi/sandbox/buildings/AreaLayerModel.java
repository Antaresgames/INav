package cz.muni.fi.sandbox.buildings;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import android.util.Log;
import cz.muni.fi.sandbox.utils.geometric.Grid;
import cz.muni.fi.sandbox.utils.geometric.Line2D;
import cz.muni.fi.sandbox.utils.geometric.Point2D;
import cz.muni.fi.sandbox.utils.geometric.Polygon;
import cz.muni.fi.sandbox.utils.geometric.Rectangle;
import cz.muni.fi.sandbox.utils.geometric.RectangleGrid;

public class AreaLayerModel implements Serializable {

	private static final long serialVersionUID = 5816771515877331278L;
	
	
	private static final String TAG = "AreaLayerModel";
	private Set<Line2D> completeSet, workingSet, displaySet;
	private static final Set<Line2D> emptySet = new HashSet<Line2D>();
	private double gridSize;
	private Point2D motionVector;
	private Rectangle mBoundingBox;
	private Collection<Point2D> collisionSet;
	private int mHalfWindowSize;
	private HashMap<Point2D, Set<Line2D>> bucketMap;

	public AreaLayerModel() {
		completeSet = new HashSet<Line2D>();
		workingSet = new HashSet<Line2D>();
		displaySet = new HashSet<Line2D>();
		mBoundingBox = new Rectangle();
		collisionSet = null;
		mHalfWindowSize = 0;
		gridSize = 2.0;
		bucketMap = null;
	}

	public boolean addWall(Line2D wall) {
		return completeSet.add(wall);
	}

	public double arrayMin(ArrayList<Double> array) {
		Double extreme = Double.MAX_VALUE;
		for (Double d : array) {
			extreme = Math.min(extreme, d);
		}
		return extreme;
	}

	public double arrayMax(ArrayList<Double> array) {
		Double extreme = -Double.MAX_VALUE;
		for (Double d : array) {
			extreme = Math.max(extreme, d);
		}
		return extreme;
	}

	public void computeBuckets(float gridSize) {

		Log.d(TAG, "computeBuckets");
		/*
		ArrayList<Double> xCoords = new ArrayList<Double>(), yCoords = new ArrayList<Double>();
		for (Line2D line : completeSet) {
			xCoords.add(line.getX1());
			xCoords.add(line.getX2());
			yCoords.add(line.getY1());
			yCoords.add(line.getY2());
		}
		Log.d(TAG, "computeBuckets: bounding box for all walls = " + box);
		*/
		
		long start = System.nanoTime();

		bucketMap = new HashMap<Point2D, Set<Line2D>>();
		Rectangle bucketBox = new Rectangle();
		Rectangle box = new Rectangle();

		for (Line2D line : completeSet) {

			box.set(Math.min(line.getX1(), line.getX2()),
					Math.min(line.getY1(), line.getY2()),
					Math.max(line.getX1(), line.getX2()),
					Math.max(line.getY1(), line.getY2()));
			box.enlarge(gridSize);
			
			for (Point2D bucket : new RectangleGrid(box, gridSize)) {
				//Log.d(TAG, "computeBuckets: iteration");
				bucketBox.set(bucket.getX(), bucket.getY(), bucket.getX(),
						bucket.getY());
				bucketBox.enlarge(2 * gridSize);
				
				if (bucketBox.hasIntersection(line)) {
					if (!bucketMap.containsKey(bucket))
						bucketMap.put(bucket, new HashSet<Line2D>());
					bucketMap.get(bucket).add(line);
				}	
			}
		
		}
		Log.d(TAG,
				"computeBuckets: hash computation took "
						+ (System.nanoTime() - start) / Math.pow(10, 9));
		double avg = 0.0;
		for (Point2D point: bucketMap.keySet()) {
			avg += bucketMap.get(point).size();
		}
		if (!bucketMap.keySet().isEmpty()) {
			avg /= bucketMap.keySet().size();
			Log.d(TAG, "computeBuckets: buckets have average items = " + avg);
		}
		

	}
	
	public Collection<Point2D> getBuckets() {
		return Collections.unmodifiableCollection(bucketMap.keySet()); 
	}

	public HashMap<Point2D, Set<Line2D>> getBucketMap() {
		return bucketMap;
	}

	public boolean addWall(double x1, double y1, double x2, double y2) {
		return completeSet.add(new Line2D(x1, y1, x2, y2));
	}

	public Collection<Line2D> getCompleteSet() {
		return Collections.unmodifiableCollection(completeSet);
	}

	public Collection<Line2D> getWorkingSet() {
		return Collections.unmodifiableCollection(workingSet);
	}

	private Point2D tmpBucket = new Point2D(0,0);
	public Collection<Line2D> getWorkingSet(double x, double y) {
		if (bucketMap == null) {
			return Collections.unmodifiableCollection(workingSet);
		}
		tmpBucket.set(Grid.snapToGrid(x, gridSize),	Grid.snapToGrid(y, gridSize));
		if (!bucketMap.containsKey(tmpBucket)) {
			return Collections.unmodifiableCollection(emptySet);
		}
		return Collections.unmodifiableCollection(bucketMap.get(tmpBucket));
	}

	public Collection<Line2D> getDisplaySet() {
		return Collections.unmodifiableCollection(displaySet);
	}

	public void setMotionVector(double x, double y, int windowSize) {
		this.motionVector = new Point2D(x, y);
		mHalfWindowSize = windowSize / 2;
	}

	public void setDisplayCropBox(Rectangle box) {
		setBoundingBox(box, displaySet);
	}

	public void setBoundingBox(Rectangle box) {
		// TODO: setBoundingBox(box, workingSet);
	}
	
	private void setBoundingBox(Rectangle box, Set<Line2D> wallSet) {

		if (box != null) {
			mBoundingBox.set(box);
		} else {
			mBoundingBox = null;
		}

		wallSet.clear();
		for (Line2D wall : completeSet) {
			if (mBoundingBox == null || mBoundingBox.hasIntersection(wall)) {
				wallSet.add(wall);
			}
		}
		Log.d(TAG, "wall set has " + wallSet.size() + " items (out of "
				+ completeSet.size() + ").");

	}

	public void computeCollisionSet() {

		collisionSet = new HashSet<Point2D>();
		computeCollisionSet(0, 0, motionVector.getX() + mHalfWindowSize,
				motionVector.getY() + mHalfWindowSize, collisionSet);
		computeCollisionSet(0, 0, motionVector.getX() - mHalfWindowSize,
				motionVector.getY() + mHalfWindowSize, collisionSet);
		computeCollisionSet(0, 0, motionVector.getX() + mHalfWindowSize,
				motionVector.getY() - mHalfWindowSize, collisionSet);
		computeCollisionSet(0, 0, motionVector.getX() - mHalfWindowSize,
				motionVector.getY() - mHalfWindowSize, collisionSet);
		computeCollisionSet(motionVector.getX() + mHalfWindowSize,
				motionVector.getY() - mHalfWindowSize, motionVector.getX()
						- mHalfWindowSize, motionVector.getY()
						- mHalfWindowSize, collisionSet);
		computeCollisionSet(motionVector.getX() - mHalfWindowSize,
				motionVector.getY() - mHalfWindowSize, motionVector.getX()
						+ mHalfWindowSize, motionVector.getY()
						- mHalfWindowSize, collisionSet);
		computeCollisionSet(motionVector.getX() - mHalfWindowSize,
				motionVector.getY() + mHalfWindowSize, motionVector.getX()
						- mHalfWindowSize, motionVector.getY()
						- mHalfWindowSize, collisionSet);
		computeCollisionSet(motionVector.getX() - mHalfWindowSize,
				motionVector.getY() + mHalfWindowSize, motionVector.getX()
						+ mHalfWindowSize, motionVector.getY()
						+ mHalfWindowSize, collisionSet);

	}

	public void computeCollisionSet(double x1, double y1, double x2, double y2,
			Collection<Point2D> collisionSet) {

		for (Line2D wall : workingSet) {

			Polygon poly = new Polygon(new Point2D[] {
					new Point2D(wall.getX1() + x1, wall.getY1() + y1),
					new Point2D(wall.getX2() + x1, wall.getY2() + y1),
					new Point2D(wall.getX2() + x2, wall.getY2() + y2),
					new Point2D(wall.getX1() + x2, wall.getY1() + y2) });

			// System.out.println("wall bounds: " + poly.getMinX() + " " +
			// poly.getMaxX()
			// + " " + poly.getMinY()+ " " + poly.getMaxY());
			for (double i = Math.ceil(poly.getMinX()); i < poly.getMaxX(); i++) {
				for (double j = Math.ceil(poly.getMinY()); j < poly.getMaxY(); j++) {

					if (mBoundingBox.hasIntersection(i, j)
							&& poly.contains(i, j)) {
						// System.out.println("hit: " + i + ", " + j);
						collisionSet.add(new Point2D(i, j));
					}
				}
			}
		}
	}

	public Collection<Point2D> getCollisionSet() {
		return collisionSet;
	}
}
