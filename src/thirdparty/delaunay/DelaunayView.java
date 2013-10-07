package thirdparty.delaunay;

/*
 * Copyright (c) 2005, 2007 by L. Paul Chew.
 *
 * Permission is hereby granted, without written agreement and without
 * license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

import java.util.HashSet;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * The DelaunayView.
 * 
 * Creates and displays a Delaunay Triangulation (DT) or a Voronoi Diagram
 * (VoD). Has a main program so it is an application as well as an applet.
 * 
 * @author Paul Chew
 * 
 *         Created July 2005. Derived from an earlier, messier version.
 * 
 *         Modified December 2007. Updated some of the Triangulation methods.
 *         Added the "Colorful" checkbox. Reorganized the interface between
 *         DelaunayAp and DelaunayPanel. Added code to find a Voronoi cell.
 *
 * @author Michal Holcik
 * 		   Modified May 2012. Conversion of the applet to Android View.
 */
public class DelaunayView extends View {
	
	
	private static final String TAG = "DelaunayView";
	private Bitmap mBitmap;
	private Paint mPaint;
	private Canvas mCanvas = new Canvas();
	

	public static final int voronoiColor = Color.MAGENTA;
	public static final int delaunayColor = Color.BLACK;
	public static final int pointRadius = 3;

	private Triangulation dt; // Delaunay triangulation
	private Triangle initialTriangle; // Initial triangle
	private static int initialSize = 10000; // Size of initial triangle
	
	

	public DelaunayView(Context context) {
		super(context);
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);


		initialTriangle = new Triangle(new Point(-initialSize, -initialSize),
				new Point(initialSize, -initialSize), new Point(0, initialSize));
		dt = new Triangulation(initialTriangle);
		//colorTable = new HashMap<Object, Integer>();
	}

	

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
		mCanvas.setBitmap(mBitmap);
		mCanvas.drawColor(Color.WHITE);

		super.onSizeChanged(w, h, oldw, oldh);
	}


	boolean moved = false;
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		switch (event.getAction()) {
		
		case MotionEvent.ACTION_MOVE:
			moved = true;
			break;
		case MotionEvent.ACTION_UP:
			if (moved) {
				moved = false;
				break;
			}
			Log.d(TAG,
					"touch event detected (" + event.getX() + ", "
							+ event.getY() + ")");
			Point point = new Point(event.getX(), event.getY());
			addSite(point);
			invalidate();
			break;
		}
		return true;
	}


	/**
	 * @return true iff the "colorful" box is selected
	 */
	public boolean isColorful() {
		return false;
	}

	/**
	 * @return true iff doing Voronoi diagram.
	 */
	public boolean isVoronoi() {
		//return voronoiButton.isSelected();
		return false;
	}

	/**
	 * @return true iff within circle switch
	 */
	public boolean showingCircles() {
		// return currentSwitch == circleSwitch;
		return false;
	}

	/**
	 * @return true iff within delaunay switch
	 */
	public boolean showingDelaunay() {
		//return currentSwitch == delaunaySwitch;
		return true;
	}

	/**
	 * @return true iff within voronoi switch
	 */
	public boolean showingVoronoi() {
		//return currentSwitch == voronoiSwitch;
		return false;
	}

	/**
	 * Add a new site to the DT.
	 * 
	 * @param point
	 *            the site to add
	 */
	public void addSite(Point point) {
		Log.d(TAG, "addSize(point = " + point + ")");
		dt.delaunayPlace(point);
	}

	/**
	 * Re-initialize the DT.
	 */
	public void clear() {
		dt = new Triangulation(initialTriangle);
	}

	/**
	 * Get the color for the spcified item; generate a new color if necessary.
	 * 
	 * @param item
	 *            we want the color for this item
	 * @return item's color
	 */
	private int getColor(Object item) {
		int color = Color.rgb((int)(255 * Math.random()), (int)(255 * Math.random()), (int)(255 * Math.random()));
		return color;
	}

	/* Basic Drawing Methods */

	/**
	 * Draw a point.
	 * 
	 * @param point
	 *            the Pnt to draw
	 */
	public void draw(Canvas canvas, Point point) {
		int r = pointRadius;
		int x = (int) point.coord(0);
		int y = (int) point.coord(1);
		mPaint.setStyle(Style.FILL_AND_STROKE);
		canvas.drawCircle(x, y, r, mPaint);
	}

	/**
	 * Draw a circle.
	 * 
	 * @param center
	 *            the center of the circle
	 * @param radius
	 *            the circle's radius
	 * @param fillColor
	 *            null implies no fill
	 */
	public void draw(Canvas canvas, Point center, double radius, int fillColor) {
		int x = (int) center.coord(0);
		int y = (int) center.coord(1);
		int r = (int) radius;
		mPaint.setColor(Color.WHITE);
		canvas.drawCircle(x, y, r, mPaint);
	}

	/**
	 * Draw a polygon.
	 * 
	 * @param polygon
	 *            an array of polygon vertices
	 * @param fillColor
	 *            null implies no fill
	 */
	public void draw(Canvas canvas, Point[] polygon, int fillColor, boolean noFill) {
		Path path = new Path();
		
		path.moveTo((float)polygon[0].coord(0), (float)polygon[0].coord(1));
		for (int i = 1; i < polygon.length; i++) {
			path.lineTo((float)polygon[i].coord(0), (float)polygon[i].coord(1));
		}
		path.close();
		
		
		mPaint.setStyle(Style.STROKE);
		mPaint.setColor(Color.WHITE);
		canvas.drawPath(path, mPaint);
	}
	
	/* Higher Level Drawing Methods */
	
	/**
	 * Handles painting entire contents of DelaunayView. Called automatically;
	 * requested via call to invalidate().
	 * 
	 * @param canvas
	 */
	public void onDraw(Canvas canvas) {

		// Flood the drawing area with a "background" color
		if (!isVoronoi())
			canvas.drawColor(delaunayColor);
		else if (dt.contains(initialTriangle))
			canvas.drawColor(Color.DKGRAY);
		else
			canvas.drawColor(voronoiColor);
		
		// Draw the appropriate picture
		if (isVoronoi())
			drawAllVoronoi(canvas, true);
		else
			drawAllDelaunay(canvas);

		// Draw any extra info due to the mouse-entry switches
		mPaint.setColor(Color.WHITE);
		if (showingCircles())
			drawAllCircles(canvas);
		if (showingDelaunay())
			drawAllDelaunay(canvas);
		if (showingVoronoi())
			drawAllVoronoi(canvas, false);
		
	}
	
	/**
	 * Draw all the Delaunay triangles.
	 * 
	 * @param withFill
	 *            true iff drawing Delaunay triangles with fill colors
	 */
	public void drawAllDelaunay(Canvas canvas) {
		Log.d(TAG, "drawAllDelaunay:");
		for (Triangle triangle : dt) {
			Log.d(TAG, "drawAllDelaunay: triangle");
			Point[] vertices = triangle.toArray(new Point[0]);
			draw(canvas, vertices, getColor(triangle), false);
		}
	}

	/**
	 * Draw all the Voronoi cells.
	 * 
	 * @param withFill
	 *            true iff drawing Voronoi cells with fill colors
	 * @param withSites
	 *            true iff drawing the site for each Voronoi cell
	 */
	public void drawAllVoronoi(Canvas canvas, boolean withSites) {
		Log.d(TAG, "drawAllVoronoi");
		// Keep track of sites done; no drawing for initial triangles sites
		HashSet<Point> done = new HashSet<Point>(initialTriangle);
		for (Triangle triangle : dt)
			for (Point site : triangle) {
				Log.d(TAG, "drawAllVoronoi: triangle");
				if (done.contains(site))
					continue;
				done.add(site);
				List<Triangle> list = dt.surroundingTriangles(site, triangle);
				Point[] vertices = new Point[list.size()];
				int i = 0;
				for (Triangle tri : list)
					vertices[i++] = tri.getCircumcenter();
				draw(canvas, vertices, getColor(site), false);
				if (withSites)
					draw(canvas, site);
			}
	}

	/**
	 * Draw all the empty circles (one for each triangle) of the DT.
	 */
	public void drawAllCircles(Canvas canvas) {
		Log.d(TAG, "drawAllCircles");
		// Loop through all triangles of the DT
		for (Triangle triangle : dt) {
			Log.d(TAG, "drawAllCircles: triangle");
			// Skip circles involving the initial-triangle vertices
			if (triangle.containsAny(initialTriangle))
				continue;
			Point c = triangle.getCircumcenter();
			double radius = c.subtract(triangle.get(0)).magnitude();
			draw(canvas, c, radius, Color.BLACK);
		}
	}
}
