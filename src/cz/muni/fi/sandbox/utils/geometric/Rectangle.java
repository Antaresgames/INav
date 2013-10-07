package cz.muni.fi.sandbox.utils.geometric;

import java.io.Serializable;


public class Rectangle implements Serializable{

	private static final long serialVersionUID = -796403529418962860L;
	
	// private static final String TAG = "Rectangle";
	public double left, right, top, bottom;
	private Line2D leftSide, rightSide, topSide, bottomSide;

	public Rectangle() {
		left = Double.POSITIVE_INFINITY;
		right = Double.NEGATIVE_INFINITY;
		top = Double.POSITIVE_INFINITY;
		bottom = Double.NEGATIVE_INFINITY;
		
	}
	
	public Rectangle(double x1, double y1, double x2, double y2) {
		set(x1, y1, x2, y2);
	}

	public void set(Rectangle other) {
		set(other.left, other.top, other.right, other.bottom);
	}
	
	public void set(double x1, double y1, double x2, double y2) {
		this.left = Math.min(x1, x2);
		this.top = Math.min(y1, y2);
		this.right = Math.max(x1, x2);
		this.bottom = Math.max(y1, y2);
		
		leftSide = new Line2D(left, top, left, bottom);
		rightSide = new Line2D(right, top, right, bottom);
		topSide = new Line2D(left, top, right, top);
		bottomSide = new Line2D(left, bottom, right, bottom);
	}
	
	public void consolidate() {
		this.left = Math.min(left, right);
		this.top = Math.min(top, bottom);
		this.right = Math.max(left, right);
		this.bottom = Math.max(top, bottom);
		
		leftSide = new Line2D(left, top, left, bottom);
		rightSide = new Line2D(right, top, right, bottom);
		topSide = new Line2D(left, top, right, top);
		bottomSide = new Line2D(left, bottom, right, bottom);
	}

	public double getX1() {
		return left;
	}

	public double getX2() {
		return right;
	}

	public double getY1() {
		return top;
	}

	public double getY2() {
		return bottom;
	}

	public void enlarge(double length) {
		set(left - length, top - length, right + length, bottom + length);
		/*
		left -= length;
		top -= length;
		right += length;
		bottom += length;
		*/
	}
	
	/**
	 * Enlarges the rectangle to contain the point
	 * Must call consolidate after all points are inserted!!! 
	 * @param point inserted point
	 */
	public void enlargeToFit(Point2D point) {
		this.left = Math.min(left, point.x);
		this.top = Math.min(top, point.y);
		this.right = Math.max(right, point.x);
		this.bottom = Math.max(bottom, point.y);
	}

	public boolean hasIntersection(double x, double y) {
		return left <= x && x <= right && top <= y && y <= bottom;
		
		/*
		return Math.min(left, right) <= x && x <= Math.max(left, right)
				&& Math.min(top, bottom) <= y && y <= Math.max(top, bottom);
		*/
	}

	public boolean hasIntersection(Point2D point) {
		return hasIntersection(point.x, point.y);
	}

	public boolean hasIntersection(Line2D line) {
		
		/*
		boolean b1 = hasIntersection(line.getX1(), line.getY1());
		boolean b2 = hasIntersection(line.getX2(), line.getY2());
		boolean b3 = (leftSide != null && line.intersect(leftSide));
		boolean b4 = (rightSide != null && line.intersect(rightSide));
		Log.d(TAG, "topSide = " + topSide);
		boolean b5 = (topSide != null && line.intersect(topSide));
		Log.d(TAG, "bottomSide = " + bottomSide);
		boolean b6 = (bottomSide != null && line.intersect(bottomSide));
		Log.d(TAG, "intersection of " + this);
		Log.d(TAG, "with = " + line);
		Log.d(TAG, "b1 = " + b1 + ", b2 = " + b2 + ", b3 = " + b3 +
				", b4 = " + b4 + ", b5 = " + b5 + ", b6 = " + b6);
		
		return b1 || b2 || b3 || b4 || b5 || b6;
		*/
		return hasIntersection(line.getX1(), line.getY1())
				|| hasIntersection(line.getX2(), line.getY2())
				|| (leftSide != null && line.intersect(leftSide))
				|| (rightSide != null && line.intersect(rightSide))
				|| (topSide != null && line.intersect(topSide))
				|| (bottomSide != null && line.intersect(bottomSide));
	}

	public boolean hasIntersection(Rectangle other) {

		return left <= other.right && right >= other.left
				&& top <= other.bottom && bottom >= other.top;
	}

	public void intersect(Rectangle other) {

		if (hasIntersection(other)) {
			left = Math.max(left, other.left);
			top = Math.max(top, other.top);
			right = Math.min(right, other.right);
			bottom = Math.min(bottom, other.bottom);
		} else {
			left = Double.POSITIVE_INFINITY;
			right = Double.NEGATIVE_INFINITY;
			top = Double.POSITIVE_INFINITY;
			bottom = Double.NEGATIVE_INFINITY;
		}
	}

	public Rectangle intersection(Rectangle other) {

		if (hasIntersection(other)) {
			return new Rectangle(Math.max(left, other.left), Math.max(top,
					other.top), Math.min(right, other.right), Math.min(bottom,
					other.bottom));
		} else
			return null;
	}
	
	public String toString() {
		return "rect(" + left + " " + top + " " + right + " " + bottom + ")";
	}
}
