package cz.muni.fi.sandbox.service.wifi;

import thirdparty.delaunay.Point;
import thirdparty.delaunay.Triangle;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.util.Log;
import cz.muni.fi.sandbox.navigation.GridRenderer;

/**
 * 
 * @author Michal Holcik
 *
 */

public class DelaunayRenderer extends GridRenderer {
	
	private static final String TAG = "DelaunayTriangulationDemo";
	private IGetsYouRssFingerprints adapter;
	Path mPath;
	Paint mPaint;
	
	public DelaunayRenderer(IGetsYouRssFingerprints fingerprintAdapter) {
		this.adapter = fingerprintAdapter;
		mPath = new Path();
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	}
	
	/**
	 * Draw the Delaunay triangles.
	 */
	@Override
	public void draw(Canvas canvas) {
		Log.d(TAG, "drawDelaunay:");
		
		if (adapter.getRssFingerprints() != null
				&& adapter.getRssFingerprints() instanceof InterpolatedFingerprintModel) {
			for (Triangle triangle : ((InterpolatedFingerprintModel)adapter.getRssFingerprints()).getTriangulation()) {
				Log.d(TAG, "drawDelaunay: triangle");
				Point[] vertices = triangle.toArray(new Point[0]);
				draw(canvas, vertices, Color.BLACK, false);
			}
		}
	}
	
	/**
	 * Draw a polygon.
	 * 
	 * @param polygon
	 *            an array of polygon vertices
	 * @param fillColor
	 *            null implies no fill
	 */
	public void draw(Canvas canvas, Point[] polygon, int color, boolean noFill) {
		
		mPath.reset();
		mPath.moveTo((float)(scaleX * polygon[0].coord(0)), (float)(-scaleY * polygon[0].coord(1)));
		for (int i = 1; i < polygon.length; i++) {
			float x = scaleX * (float)polygon[i].coord(0);
			float y = -scaleY * (float)polygon[i].coord(1);
			mPath.lineTo(x, y);
		}
		mPath.close();
		
		mPaint.setStyle(Style.STROKE);
		mPaint.setColor(color);
		canvas.drawPath(mPath, mPaint);
	}

};

