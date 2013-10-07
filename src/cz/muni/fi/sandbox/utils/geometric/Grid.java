package cz.muni.fi.sandbox.utils.geometric;

public class Grid {
	
	/**
	 * Returns a coordinate snapped to the grid.
	 * 
	 * @param coord coordinate to snap to the grid.
	 * @param gridSize size of the grid to snap to. The grid has coordinate origin in 0, 0
	 * @return
	 */
	public static double snapToGrid(double coord, double gridSize) {
		return gridSize * (int)Math.round(coord / gridSize);
	}
}
