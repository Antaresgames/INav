package cz.muni.fi.sandbox.utils.geometric;

import java.util.Iterator;

public class RectangleGrid implements Iterable<Point2D> {
	private final Rectangle rectangle;
	private final double gridSize;
	public RectangleGrid(Rectangle rectangle, double gridSize) {
		this.rectangle = rectangle;
		this.gridSize = gridSize;
	}
		
	private class RectangleGridIterator implements Iterator<Point2D> {

		int x, y;
		
		int gridLeft = (int)(Math.min(rectangle.left, rectangle.right) / gridSize);
		int gridRight = (int)(Math.max(rectangle.left, rectangle.right) / gridSize);
		
		int gridTop = (int)(Math.max(rectangle.bottom, rectangle.top) / gridSize);
		int gridBottom = (int)(Math.min(rectangle.bottom, rectangle.top) / gridSize);
		
		RectangleGridIterator() {
			x = gridLeft - 1;
			y = gridBottom;
		}
		
		@Override
		public boolean hasNext() {
			return !(x == gridRight && y == gridTop);
		}
		
		@Override
		public Point2D next() {
			if (x == gridRight) {
				x = gridLeft;
				y++;
			} else {
				x++;
			}
			Point2D next = new Point2D(gridSize * x, gridSize * y);
			return next;
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException("Remove is not supported");
		}
	}

	@Override
	public Iterator<Point2D> iterator() {
		// TODO Auto-generated method stub
		return new RectangleGridIterator();
	};
};

