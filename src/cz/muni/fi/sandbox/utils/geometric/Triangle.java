package cz.muni.fi.sandbox.utils.geometric;


public class Triangle {

	private Point2D a, b, c;
	private Point2D[] mPoints;
	private Rectangle box;
	private Point2D v0, v1, v2;
	double dot00, dot01, dot02, dot11, dot12;
	

	public Triangle(Point2D[] points) {
		a = new Point2D(points[0].getX(), points[0].getY());
		b = new Point2D(points[1].getX(), points[1].getY());
		c = new Point2D(points[2].getX(), points[2].getY());
		mPoints = new Point2D[] {a, b, c};
		box = new Rectangle(getMinX(), getMinY(), getMaxX(), getMaxY());
		
		v0 = new Point2D(c).minus(a);
		v1 = new Point2D(b).minus(a);
		
		// Precompute dot products
		dot00 = v0.dot(v0);
		dot01 = v0.dot(v1);
		dot11 = v1.dot(v1);
	}


	public double[] getBarycentricCoords(Point2D p) {

		v2 = new Point2D(p).minus(a);
		
		// Compute remaining dot products
		dot02 = v0.dot(v2);
		dot12 = v1.dot(v2);

		// Compute barycentric coordinates
		double denom = (dot00 * dot11 - dot01 * dot01);
		double u = 0.0, v = 0.0, w = 0.0;
		if (denom == 0.0) {
			return null;
		}
		u = (dot11 * dot02 - dot01 * dot12) / denom;
		v = (dot00 * dot12 - dot01 * dot02) / denom;
		w = 1 - (u + v);
		return new double[] {w, v, u};
	}

	
	public boolean contains(Point2D p) {
		
		double[] coords = getBarycentricCoords(p);
		if (coords == null) {
			v2 = new Point2D(p).minus(a);
			return v0.contains(v2) || v1.contains(v2);
		}
		//System.out.println("contains: barycentric coordinates = " + Arrays.toString(coords));
		
		boolean retval = true;
		for (double s: coords) {
			retval &= (0.0 <= s) && (s <= 1.0); 
		}
		return retval;
	}
	
	public Point2D[] getPoints() {
		return mPoints;
	}
	
	public Rectangle getBox() {
		return box;
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
		return "Triangle(" + a + " " + b + " " + c + ")";
	}
}
