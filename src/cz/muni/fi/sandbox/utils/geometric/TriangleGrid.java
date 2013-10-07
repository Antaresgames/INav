package cz.muni.fi.sandbox.utils.geometric;

import java.util.Iterator;

public class TriangleGrid implements Iterable<Point2D> {
	private final Triangle triangle;
	private final double gridSize;
	public TriangleGrid(Triangle triangle, double gridSize) {
		this.triangle = triangle;
		this.gridSize = gridSize;
	}
	
	private class TriangleGridIterator implements Iterator<Point2D> {
		
		RectangleGrid boxGrid;
		Iterator<Point2D> boxIterator;
		Point2D next;
		
		TriangleGridIterator() {
			boxGrid = new RectangleGrid(triangle.getBox(), gridSize);
			boxIterator = boxGrid.iterator();
		}
		
		@Override
		public boolean hasNext() {
			next = null;
			while (boxIterator.hasNext()) {
				Point2D candidate = boxIterator.next();
				//System.out.println("candidate point = " + candidate);
				if (triangle.contains(candidate)) {
					next = candidate;
					return true;
				}
			}
			return false;
		}
		
		@Override
		public Point2D next() {
			return next;
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException("Remove is not supported");
		}
	}

	@Override
	public Iterator<Point2D> iterator() {
		return new TriangleGridIterator();
	};
};

