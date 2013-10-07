package cz.muni.fi.sandbox.utils.geometric;

import java.io.Serializable;


public class Line2D implements Serializable {
	
	private static final long serialVersionUID = -8478406753638537687L;

	private Point2D start, end;
	
	// precompute intersection expressions
	private double detXY12;
	private double X1minusX2;
	private double Y1minusY2;
	private double minX, minY, maxX, maxY;
	
	public Line2D() {
		setCoords(0.0, 0.0, 0.0, 0.0);
	}
	
	public Line2D(double x1, double y1, double x2, double y2) {
		setCoords(x1, y1, x2, y2);
	}
	
	public String toString() {
		return "line(" + start.x + ", " + start.y + ", " + end.x + ", " + end.y + ")";
	}
	
	public void setCoords(double x1, double y1, double x2, double y2) {
		start = new Point2D(x1, y1);
		end = new Point2D(x2, y2);
		
		detXY12 = (x1 * y2 - y1 * x2);
		X1minusX2 = (x1 - x2);
		Y1minusY2 = (y1 - y2);
		minX = Math.min(x1, x2);
		minY = Math.min(y1, y2);
		maxX = Math.max(x1, x2);
		maxY = Math.max(y1, y2);
	}
	
	public double getX1() {
		return start.x;
	}
	public double getX2() {
		return end.x;
	}
	public double getY1() {
		return start.y;
	}
	public double getY2() {
		return end.y;
	}
	

	
	/**
	 * Intersection of two lines.
	 * Formula taken from http://en.wikipedia.org/wiki/Line-line_intersection
	 * 
	 * @param that other line
	 * @return true iff this and that lines intersect.
	 */
	public boolean intersect(Line2D that) {
		return intersect(that, null);
	}
	
	public boolean intersect(Line2D that, Point2D intersection) {
		
		double delimiter = X1minusX2 * that.Y1minusY2 - Y1minusY2 * that.X1minusX2;
		double pX = detXY12 * that.X1minusX2 - X1minusX2 * that.detXY12;
		pX /= delimiter;
		double pY = detXY12 * that.Y1minusY2 - Y1minusY2 * that.detXY12;
		pY /= delimiter;
		
		//System.out.println("intersection at = " + pX + ", " + pY);
		
		if (intersection != null) {
			intersection.x = pX;
			intersection.y = pY;
		}
		
		return (minX == maxX || minX <= pX && pX <= maxX)
				&& (minY == maxY || minY <= pY && pY <= maxY)
				&& (that.minX == that.maxX || that.minX <= pX && pX <= that.maxX)
				&& (that.minY == that.maxY || that.minY <= pY && pY <= that.maxY);
	}
	
}
