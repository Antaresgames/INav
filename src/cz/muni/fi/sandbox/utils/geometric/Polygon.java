package cz.muni.fi.sandbox.utils.geometric;


public class Polygon {

	Point2D[] mPoints;

	public Polygon(Point2D[] points) {
		mPoints = new Point2D[points.length];
		System.arraycopy(points, 0, mPoints, 0, points.length);
	}

	public static boolean triangleContainsBarycentric(Point2D p, Point2D a, Point2D b,
			Point2D c) {

		// System.out.println("triangleContainsBarycentric:");
		// Compute vectors
		Point2D v0 = new Point2D(c).minus(a);
		Point2D v1 = new Point2D(b).minus(a);
		Point2D v2 = new Point2D(p).minus(a);
		// System.out.println("v0 = " + v0 + ", v1 = " + v1 + ", v2 = " + v2);

		// Compute dot products
		double dot00 = v0.dot(v0);
		double dot01 = v0.dot(v1);
		double dot02 = v0.dot(v2);
		double dot11 = v1.dot(v1);
		double dot12 = v1.dot(v2);

		// Compute barycentric coordinates
		double denom = (dot00 * dot11 - dot01 * dot01);
		double u = 0.0, v = 0.0;
		if (denom != 0.0) {
			u = (dot11 * dot02 - dot01 * dot12) / denom;
			v = (dot00 * dot12 - dot01 * dot02) / denom;
			// System.out.println("u = " + u + ", v = " + v);
			// Check if point is in triangle
			return (u >= 0.0) && (v >= 0.0) && (u + v <= 1);
		} else {
			/*
			 * System.out.println(v0); System.out.println(v1);
			 * System.out.println(v2); System.out.println("v0.contains(v2) " +
			 * v0.contains(v2)); System.out.println("v1.contains(v2) " +
			 * v1.contains(v2));
			 */
			return v0.contains(v2) || v1.contains(v2);
		}
	}

	// crossings test
	public boolean contains(double x, double y) {
		assert (mPoints.length == 4);
		Point2D thePoint = new Point2D(x, y);
		return triangleContainsBarycentric(thePoint, mPoints[0], mPoints[1],
				mPoints[2])
				|| triangleContainsBarycentric(thePoint, mPoints[2],
						mPoints[3], mPoints[0]);
	}

	public double getMinX() {
		double min = Double.MAX_VALUE;
		for (Point2D point : mPoints) {
			min = Math.min(min, point.x);
		}
		return min;
	}

	public double getMinY() {
		double min = Double.MAX_VALUE;
		for (Point2D point : mPoints) {
			min = Math.min(min, point.y);
		}
		return min;
	}

	public double getMaxX() {
		double max = -Double.MAX_VALUE;
		for (Point2D point : mPoints) {
			max = Math.max(max, point.x);
		}
		return max;
	}

	public double getMaxY() {
		double max = -Double.MAX_VALUE;
		for (Point2D point : mPoints) {
			max = Math.max(max, point.y);
		}
		return max;
	}

	public String toString() {
		return "poly(" + mPoints[0] + " " + mPoints[1] + " " + mPoints[2] + " "
				+ mPoints[3] + ")";
	}
}
