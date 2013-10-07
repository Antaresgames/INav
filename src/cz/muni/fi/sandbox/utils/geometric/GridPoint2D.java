package cz.muni.fi.sandbox.utils.geometric;

import java.io.Serializable;

public class GridPoint2D implements Serializable {
	

	private static final long serialVersionUID = -375081492054612006L;

	public int x, y;
	
	public GridPoint2D () {
		x = 0;
		y = 0;
	}
	
	public GridPoint2D (GridPoint2D other) {
		this.x = other.x;
		this.y = other.y;
	}
	
	public GridPoint2D minus (GridPoint2D other) {
		this.x -= other.x;
		this.y -= other.y;
		return this;
	}
	
	
	public GridPoint2D (int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	
	public static GridPoint2D snap(Point2D point, int grid) {
		return new GridPoint2D(grid * (int)(point.getX() / grid),
				grid * (int)(point.getY() / grid));
	}
	
	public void set(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public int getX() {
		return this.x;
	}
	
	public int getY() {
		return this.y;
	}
	
	
	@Override
    public boolean equals(Object obj) {
    	if (obj instanceof GridPoint2D) {
    		GridPoint2D that = (GridPoint2D)obj;
    		return this.x == that.x && this.y == that.y;
    	}
    	return false;
    }
	
	@Override
	public int hashCode() {
		return x + 13063 * y;
	}
	
	@Override
	public String toString() {
		return "(" + x + ", " + y + ")";
	}
	
}
