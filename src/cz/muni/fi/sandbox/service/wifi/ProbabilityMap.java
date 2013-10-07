package cz.muni.fi.sandbox.service.wifi;

import android.util.Log;
import cz.muni.fi.sandbox.utils.geometric.GridPoint2D;
import cz.muni.fi.sandbox.utils.geometric.Point2D;
import cz.muni.fi.sandbox.utils.geometric.Rectangle;

public class ProbabilityMap {
	
	private static final String TAG = "ProbabilityMap";
	private float[][] probabilityMap;
	private final int gridSize;
	private final GridPoint2D origin = new GridPoint2D();
	private final GridPoint2D size = new GridPoint2D();
	
	public ProbabilityMap(Rectangle box, int grid) {
		if (box == null) {
			origin.x = 0;
			origin.y = 0;
			size.x = 1;
			size.y = 1;
		} else {
			origin.x = (int)(box.left / grid);
			origin.y = (int)(box.top / grid);
			int topx = (int)(box.right / grid);
			int topy = (int)(box.bottom / grid);
			size.x = topx - origin.x;
			size.y = topy - origin.y;
		}
		gridSize = grid;
		Log.d(TAG, "size = " + size + " origin = " + origin);
		probabilityMap = new float[size.x][size.y];
	}
	
	public void clear() {
		if (probabilityMap != null) {
			for (int i = 0; i < size.x; i++) {
				for (int j = 0; j < size.y; j++) {
					probabilityMap[i][j] = 0.0f;
				}
			}
		}
	}
	
	public void set(Point2D loc, float prob) {
		// snap to grid;
		int x = (int)(loc.getX() / gridSize - origin.x);
		int y = (int)(loc.getY() / gridSize - origin.y);
		probabilityMap[x][y] = prob;
	}
	
	public void set(int x, int y, float prob) {
		x = x - origin.x;
		y = y - origin.y;
		probabilityMap[x][y] = prob;
	}
	
	public void randomize() {
		if (probabilityMap != null) {
			for (int i = 0; i < size.x; i++) {
				for (int j = 0; j < size.y; j++) {
					probabilityMap[i][j] = (float)Math.random();
				}
			}
		}
	}
	
	public int getGridSize() {
		return gridSize;
	}
	
	public GridPoint2D getSize() {
		return size;
	}
	
	public GridPoint2D getOrigin() {
		return origin;
	}
	
	public float getMaxProbability() {
		if (probabilityMap != null) {
			float max = Float.NEGATIVE_INFINITY;
			for (int i = 0; i < probabilityMap.length; i++) {
				for (int j = 0; j < probabilityMap[0].length; j++) {
					float value = probabilityMap[i][j];
					if (max < value) {
						max = value;
					}
				}				
			}
			return max;
			
		} else {
			return 0f;
		}
		
	}
	
	public float getProbability(Point2D loc) {
		return getProbability(loc.getX(), loc.getY());
	}
	
	public float getProbability(double xcoord, double ycoord) {
		
		if (probabilityMap == null) {
			return 0.0f;
		}
		
		// snap to grid;
		int x = (int)(xcoord / gridSize - origin.x);
		int y = (int)(ycoord / gridSize - origin.y);
		
		// out of the map is zero
		if (x < 0 || x >= size.x || y < 0 || y >= size.y)
			return 0.0f;
		
		return probabilityMap[x][y];
	}
}
