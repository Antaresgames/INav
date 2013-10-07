package cz.muni.fi.sandbox.service.orientation;

import android.graphics.Canvas;
import cz.muni.fi.sandbox.utils.linear.Matrix;
import cz.muni.fi.sandbox.utils.linear.VectorBase;

public class CrossModel extends WireframeObject {
	
	protected final float LENGTH = 200.0f;
	
	public CrossModel(VectorBase base) {
		
		super(base);
		
		mPoints = new Matrix(4,3, new double[] {
						0.0,0.0,0.0,
						LENGTH,0.0,0.0,
						0.0,LENGTH,0.0,
						0.0,0.0,LENGTH
						}
		);
		
		mLines = new int[][] {
				{ 0, 1 },
				{ 0, 2 },
				{ 0, 3 }
		};
		
		mPointsInBase = mPoints;
		
		mProjectedPoints = new float[4][2];
	}
	

	protected void drawSpecific(Canvas canvas) {
		canvas.drawText("x", mProjectedPoints[1][0], -mProjectedPoints[1][1], mPaint);
		canvas.drawText("y", mProjectedPoints[2][0], -mProjectedPoints[2][1], mPaint);
		canvas.drawText("z", mProjectedPoints[3][0], -mProjectedPoints[3][1], mPaint);
	}
	
}
