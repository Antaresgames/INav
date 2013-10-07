package cz.muni.fi.sandbox.service.orientation;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import cz.muni.fi.sandbox.utils.linear.Matrix;
import cz.muni.fi.sandbox.utils.linear.VectorBase;

public class VectorModel extends WireframeObject {

	private final float LENGTH;
	private final double SCALE;
	private final double NORM_FACTOR;
	
	private Matrix mUnscaledPoints;

	public VectorModel(VectorBase base, float normFactor) {
		super(base);

		LENGTH = 100.0f;
		SCALE = 2 * LENGTH;
		NORM_FACTOR = normFactor;
		
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setColor(Color.RED);
		
		mUnscaledPoints = new Matrix(8,3, new double[] {
				 0.0, 0.0, 0.0,
				 0.0, 1.0, 0.0,
				 1.0, 1.0, 0.0,
				 1.0, 0.0, 0.0,
				 0.0, 0.0, 1.0,
				 0.0, 1.0, 1.0,
				 1.0, 1.0, 1.0,
				 1.0, 0.0, 1.0
				 });
		
		mPointsInBase = mPoints = mUnscaledPoints;
		
		mLines = new int[][] {
				{ 0, 1 },
				{ 0, 3 },
				{ 0, 4 },
				{ 1, 2 },
				{ 1, 5 },
				{ 2, 3 },
				{ 2, 6 },
				{ 3, 7 },
				{ 4, 5 },
				{ 4, 7 },
				{ 5, 6 },
				{ 6, 7 }};

		mProjectedPoints = new float[8][2];

	}

	public void setValues(double x, double y, double z) {
		
		Matrix scaleMatrix = new Matrix(3, 3, new double[] {
				SCALE * x / NORM_FACTOR, 0, 0,
				0, SCALE * y / NORM_FACTOR, 0,
				0, 0, SCALE * z / NORM_FACTOR});
		
		if (mBase == null) {
			throw new RuntimeException("mBase == null");
		}
		
		mPoints = mUnscaledPoints.times(scaleMatrix);
	}


	protected void drawSpecific(Canvas canvas) {
		
		// draw vector line
		mPaint.setStrokeWidth(3);
		canvas.drawLine(mProjectedPoints[0][0], -mProjectedPoints[0][1],
				mProjectedPoints[6][0], -mProjectedPoints[6][1], mPaint);
		mPaint.setStrokeWidth(0.5f);
	}
}
