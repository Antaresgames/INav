package cz.muni.fi.sandbox.service.inertial;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorManager;

public class AccelerationModel {

	
	private final String TAG = "IntegratorView";

	private SensorManager mSensorManager;
	
	private Sensor mAccelerometer;
	private Sensor mMagnetometer;

	private Paint mPaint = new Paint();

	private long mLastAccTimeStamp;
	private double[] mLastAccValues;
	private long mLastMagTimeStamp;
	private double[] mLastMagValues;
	
	private float[] mPos;
	private double[] mVel;
	private double[] mAcc;
	
	private float[] minPos;
	private float[] maxPos;

	// 10 pixels per meter
	final float mScreenScale = 1.0f;
	
	private final long NANO = (long) Math.pow(10, 9);
		
	public AccelerationModel() {

	
		mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
	
		mVel = new double[] { 0.0, 0.0, 0.0 };
		mAcc = new double[] { 0.0, 0.0, 0.0 };
		mLastAccValues = new double[] { 0.0, 0.0, 0.0 };
		
		mPos = new float[] { 200.0f, 200.0f , 0.0f };
		minPos = new float[] { 0.0f, 0.0f , 0.0f };
		maxPos = new float[] { 0.0f, 0.0f , 0.0f };
		
	}
	
	
	public void reset() {
		mVel[0] = mVel[1] = mVel[2] = 0.0;
	}
	
	public void processAccelerometerEvent(long timestamp, float valueX, float valueY, float valueZ) {

		float[] values = new float[] {valueX, valueY, valueZ};

		//long timestamp = event.timestamp;
		if (mLastAccTimeStamp == 0) {
			mLastAccTimeStamp = timestamp;
			// throw away the first accelerometer event
			return;
		}
		double deltaT = (double) (timestamp - mLastAccTimeStamp) / NANO;
		mLastAccTimeStamp = timestamp;

		// Log.d(TAG, "values = (" + values[0] + ", " + values[1] + ", "
		// + values[2] + "), deltaT = " + deltaT);

		for (int i = 0; i < 3; i++) {

			mAcc[i] = values[i];
			mVel[i] += deltaT * ((mAcc[i] + mLastAccValues[i]) / 2);
			if (i == 0) {
				mPos[i] += (float) (deltaT * mVel[i]);
			}
			if (i == 1) {
				mPos[i] += (float) (deltaT * mVel[i]);
			}
			if (i == 2) {
				mPos[i] += (float) (deltaT * mVel[i]);
			}
			
			boolean collision = false;
			// truncate
			if (mPos[i] < minPos[i]) {
				// mPos[i] = minPos[i];
				mPos[i] = maxPos[i];
				collision = true;

			}
			if (mPos[i] > maxPos[i]) {
				// mPos[i] = maxPos[i];
				mPos[i] = minPos[i];
				collision = true;
			}
			if (collision) {
				// mVel[i] = -mVel[i];
			}
		}
		System.arraycopy(mAcc, 0, mLastAccValues, 0, values.length);
		// Log.d(TAG, "x = (" + minPos[0] + ", " + maxPos[0] + ")");
		// Log.d(TAG, "y = (" + minPos[1] + ", " + maxPos[1] + ")");
		// Log.d(TAG, "deltaT * mVel[0] = " + (float) (deltaT * mVel[0]));
		// Log.d(TAG, "acc = (" + mAcc[0] + ", " + mAcc[1] + ")");
		// Log.d(TAG, "vel = (" + mVel[0] + ", " + mVel[1] + ")");
		// Log.d(TAG, "pos = (" + mPos[0] + ", " + mPos[1] + ")");

	}
	
	private void drawVector(Canvas canvas, float scale, float x1, float y1,
			String label, double[] values, int color) {

		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(color);
		paint.setStyle(Paint.Style.STROKE);

		float x2 = x1 + (float) (scale * values[0]);
		float y2 = y1 - (float) (scale * values[1]);

		canvas.drawCircle(x1, y1, 10, paint);
		canvas.drawCircle(x2, y2, 10, paint);
		canvas.drawLine(x1, y1, x2, y2, paint);
		canvas.drawText(label, x1, y1+10, paint);
	}

	private void drawPositionMark(Canvas canvas) {

		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(Color.BLUE);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(2.0f);

		float posX = (mScreenScale * mPos[0]);
		float posY = (mScreenScale * mPos[1]);
		canvas.drawCircle(posX, posY, 10, paint);
	}
	
	public void draw(Canvas canvas) {
		Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
		p.setStyle(Paint.Style.STROKE);
	
		int width = canvas.getWidth();
		int height = canvas.getHeight();
		maxPos[0] = width;
		maxPos[1] = height;
		
		//canvas.drawColor(0xFFFFFFFF);
		
		double accSize = Math.sqrt(mAcc[0] * mAcc[0] + mAcc[1] * mAcc[1]);
		double accSigma = 0.0;
		
		// acceleration
		drawVector(canvas, 0.25f * width / (SensorManager.STANDARD_GRAVITY),
				width / 4, height - width / 4,
				"acc = " + String.format("%.3f", accSize) + "; " + "sigma = " + String.format("%.3f", accSigma),
				mAcc, Color.BLACK);
		
		
		double velSize = Math.sqrt(mVel[0] * mVel[0] + mVel[1] * mVel[1]);
		
		// velocity (rectangle integration)
		drawVector(canvas, 0.25f * width,
				3 * width / 4, height - width / 4,
				"vel = " + String.format("%.3f", velSize),
				mVel, Color.RED);
		
		drawPositionMark(canvas);
	}
	
}
