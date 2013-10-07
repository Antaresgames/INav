package cz.muni.fi.sandbox.service.orientation;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import cz.muni.fi.sandbox.utils.linear.Matrix;
import cz.muni.fi.sandbox.utils.linear.VectorBase;

public class WireframeObject {

	protected VectorBase mBase;
	

	protected final double focalLength = 1000.0;

	protected float[][] points;
	Matrix mPoints, mPointsInBase;

	protected float[][] mProjectedPoints;
	
	protected Paint mPaint;

	
	protected int[][] mLines;
	
	public WireframeObject(VectorBase base) {
		
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setColor(Color.BLACK);
		
		mBase = base;
	}
	
	public void setColor(int color) {
		mPaint.setColor(color);
	}
	
	public void applyProjection() {
		mPointsInBase = mBase.from(mPoints);
		
		for (int w = 0; w < mProjectedPoints.length; w++) {
			mProjectedPoints[w][0] = (float)(mPointsInBase.getValue(0, w) * focalLength / (focalLength - mPointsInBase.getValue(2, w)));
			mProjectedPoints[w][1] = (float)(mPointsInBase.getValue(1, w) * focalLength / (focalLength - mPointsInBase.getValue(2, w)));
			//System.out.println("original = " + points[w][0] + " projected = " + projectedPoints[w][0]);
		}
	}

	public void draw(Canvas canvas) {
		drawWireframe(canvas);
		drawSpecific(canvas);
	}
	
	public void drawWireframe(Canvas canvas) {
		
		applyProjection();
		
		for (int i = 0; i < mLines.length; i++) {
			float startX = mProjectedPoints[mLines[i][0]][0];
			float startY = mProjectedPoints[mLines[i][0]][1];
			float stopX = mProjectedPoints[mLines[i][1]][0];
			float stopY = mProjectedPoints[mLines[i][1]][1];
			
			//System.out.println(startX + " " + startY + " " + stopX + " " + stopY);
			canvas.drawLine(startX, -startY, stopX, -stopY, mPaint);
		}
	}
	
	protected void drawSpecific(Canvas canvas) {}
	
}
