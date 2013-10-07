package cz.muni.fi.sandbox.service.inertial;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class IntegratorView extends View implements SensorEventListener {

	private static final String TAG = "IntegratorView";

	private SensorManager mSensorManager;
	
	private Sensor mAccelerometer;
	private Sensor mMagnetometer;

	private Paint mPaint = new Paint();

	private long mLastAccTimeStamp;
	private double[] mLastAccValues;
	private long mLastMagTimeStamp;
	private double[] mLastMagValues;
	
	private float[] mPos;
	private double[] mVel, mVel2;
	private double[] mAcc;
	private int[] mScreenSize;
	private float[] minPos;
	private float[] maxPos;

	// 10 pixels per meter
	final float mScreenScale = 1.0f;

	public IntegratorView(Context context) {
		super(context);

		mSensorManager = (SensorManager) context
				.getSystemService(Context.SENSOR_SERVICE);

		mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);

		mVel = new double[] { 0.0, 0.0, 0.0 };
		mVel2 = new double[] { 0.0, 0.0, 0.0 };
		mAcc = new double[] { 0.0, 0.0, 0.0 };
		mLastAccValues = new double[] { 0.0, 0.0, 0.0 };
		mScreenSize = new int[] { getWidth(), getHeight() };
		minPos = new float[] { 0.0f, 0.0f, 0.0f };
		maxPos = new float[] { mScreenSize[0], mScreenSize[1], 100.0f };
		mPos = new float[] { (float) mScreenSize[0] / 2,
				(float) mScreenSize[1] / 2, 0.0f };

		// mMagnetometer = getSensor(Sensor.TYPE_MAGNETIC_FIELD,
		// "magnetometer");
		mAccelerometer = getSensor(Sensor.TYPE_ACCELEROMETER, "accelerometer");

		mMagnetometer = getSensor(Sensor.TYPE_GYROSCOPE, "magnetic");
	}

	private Sensor getSensor(int sensorType, String sensorName) {

		Sensor sensor = mSensorManager.getDefaultSensor(sensorType);
		if (sensor != null) {
			Log.d(TAG, "there is a " + sensorName);
		} else {
			Log.d(TAG, "there is no " + sensorName);
		}
		return sensor;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {

		mScreenSize[0] = w;
		mScreenSize[1] = h;
		maxPos[0] = w;
		maxPos[1] = h;
	}

	@Override
	protected void onDraw(Canvas canvas) {

		mPaint.setStyle(Paint.Style.STROKE);

		canvas.drawColor(0xFFFFFFFF);

		drawVelocityVector(canvas);
		drawAccelerationVector(canvas);
		drawPositionMark(canvas);

		drawVector(canvas, 0.25f * mScreenSize[0] / (1000),
				3 * mScreenSize[0] / 4, mScreenSize[1] - mScreenSize[0] / 4,
				mVel2);

	}

	private void drawVector(Canvas canvas, float scale, float x1, float y1,
			double[] values) {

		mPaint.setColor(Color.RED);
		mPaint.setStyle(Paint.Style.STROKE);

		float x2 = x1 + (float) (scale * values[0]);
		float y2 = y1 - (float) (scale * values[1]);

		canvas.drawCircle(x1, y1, 10, mPaint);
		canvas.drawCircle(x2, y2, 10, mPaint);
		canvas.drawLine(x1, y1, x2, y2, mPaint);
	}

	private void drawVelocityVector(Canvas canvas) {

		mPaint.setColor(Color.BLACK);
		mPaint.setStyle(Paint.Style.STROKE);

		float scale = 0.25f * mScreenSize[0] / (1000);

		float x1 = 3 * mScreenSize[0] / 4;
		float y1 = mScreenSize[1] - mScreenSize[0] / 4;

		// float x2 = x1 + scale * Math.signum((float)mVel[0]) *
		// (float)Math.log(1 + Math.abs(mVel[0]));
		// float y2 = y1 - scale * Math.signum((float)mVel[1]) *
		// (float)Math.log(1 + Math.abs(mVel[1]));
		float x2 = x1 + (float) (scale * mVel[0]);
		float y2 = y1 - (float) (scale * mVel[1]);

		canvas.drawCircle(x1, y1, 10, mPaint);
		canvas.drawCircle(x2, y2, 10, mPaint);
		canvas.drawLine(x1, y1, x2, y2, mPaint);

	}

	private void drawAccelerationVector(Canvas canvas) {
		mPaint.setColor(Color.BLACK);
		mPaint.setStyle(Paint.Style.STROKE);

		// float scale = 0.25f * mScreenSize[0] /
		// (SensorManager.STANDARD_GRAVITY);
		float scale = 0.25f * mScreenSize[0] / (float) (Math.PI);
		// / (SensorManager.GRAVITY_EARTH);

		float x1 = mScreenSize[0] / 4;
		float x2 = x1 + scale * (float) mAcc[0];
		float y1 = mScreenSize[1] - mScreenSize[0] / 4;
		float y2 = y1 - scale * (float) mAcc[1];

		canvas.drawCircle(x1, y1, 10, mPaint);
		canvas.drawCircle(x2, y2, 10, mPaint);
		canvas.drawLine(x1, y1, x2, y2, mPaint);

	}

	private void drawPositionMark(Canvas canvas) {

		mPaint.setColor(Color.BLUE);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(2.0f);

		float posX = mScreenScale * mPos[0];
		float posY = mScreenScale * mPos[1];
		canvas.drawCircle(posX, posY, 10, mPaint);
	}

	private String mMode = "acc";

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if (event.getAction() == MotionEvent.ACTION_UP) {
			if (mMode.equals("acc")) {
				mMode = "gyro";
			} else {
				mMode = "acc";
			}
		}

		return true;
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	private final long NANO = (long) Math.pow(10, 9);
	private final float MASS = 0.01f;

	private void processAccelerometerEvent(SensorEvent event) {

		float[] values = event.values;

		long timestamp = event.timestamp;
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

			mAcc[i] = -values[i];
			mVel[i] += deltaT * (mAcc[i]) / MASS;
			mVel2[i] += deltaT * ((mAcc[i] + mLastAccValues[i]) / 2) / MASS;
			if (i == 0) {
				mPos[i] += (float) (deltaT * mVel[i]);
			}
			if (i == 1) {
				mPos[i] -= (float) (deltaT * mVel[i]);
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

			} else if (mPos[i] > maxPos[i]) {
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

	private void processMagEvent(SensorEvent event) {
		float[] values = event.values;

		long timestamp = event.timestamp;
		if (mLastMagTimeStamp == 0) {
			mLastMagTimeStamp = timestamp; // throw away the first gyro event
			return;
		}

		double deltaT = (double) (timestamp - mLastMagTimeStamp) / NANO;
		Log.d(TAG, "values = (" + values[0] + ", " + values[1] + ", "
				+ values[2] + "), deltaT = " + deltaT);

		if (timestamp != 0) {
			
			mLastMagValues[0] = event.values[0];
			mLastMagValues[1] = event.values[1];
			mLastMagValues[2] = event.values[2];

		}
		
		mLastMagTimeStamp = timestamp;


	}

	@Override
	public void onSensorChanged(SensorEvent event) {

		synchronized (this) {

			if (mMode.equals("acc")) {
				if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
					processAccelerometerEvent(event);
					invalidate();
				}
			} else {
				if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
					processMagEvent(event);
					invalidate();
				}
			}

		}
	}

	public void onResume() {

		if (mAccelerometer != null)
			mSensorManager.registerListener(this, mAccelerometer,
					SensorManager.SENSOR_DELAY_FASTEST);
		
		if (mMagnetometer != null)
			mSensorManager.registerListener(this, mMagnetometer,
					SensorManager.SENSOR_DELAY_FASTEST);

	}

	public void onStop() {
		mSensorManager.unregisterListener(this);
	}
}
