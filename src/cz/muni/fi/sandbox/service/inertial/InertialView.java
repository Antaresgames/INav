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
import cz.muni.fi.sandbox.dsp.filters.MovingAverageTD;
import cz.muni.fi.sandbox.dsp.filters.StandardDeviationTD;
import cz.muni.fi.sandbox.service.orientation.CrossModel;
import cz.muni.fi.sandbox.service.orientation.CubeModel;
import cz.muni.fi.sandbox.service.orientation.VectorModel;
import cz.muni.fi.sandbox.utils.linear.Matrix;
import cz.muni.fi.sandbox.utils.linear.Transform;
import cz.muni.fi.sandbox.utils.linear.Vector3d;
import cz.muni.fi.sandbox.utils.linear.VectorBase;

public class InertialView extends View implements SensorEventListener {

	private final String TAG = "WireCubeView";

	private final long NANO = (long) Math.pow(10, 9);

	VectorBase worldBase;
	VectorBase deviceBase;
	
	CubeModel mCube;
	CubeModel mCube2;

	CrossModel mCoordCross;

	VectorModel mAccelerationVector;
	VectorModel mMagnetometerVector;
	VectorModel mMagnetometerVector2;
	
	AccelerationModel mAccModel;

	float[] lastValues = new float[3];

	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private Sensor mMagnetometer;
	private Sensor mGyroscope;

	private Paint mPaint = new Paint();

	private long mLastAccTimeStamp;
	private long mLastGyroTimeStamp;

	// 10 pixels per meter
	final float mScreenScale = 1.0f;
	
	StandardDeviationTD[] mAccDeviation;
	MovingAverageTD[] mAccMovingAverage;

	public InertialView(Context context) {
		super(context);
		
		worldBase = new VectorBase(3);
		deviceBase = new VectorBase(3);

		mCube = new CubeModel(worldBase);
		mCube2 = new CubeModel(worldBase);
		mCube2.setColor(Color.BLUE);
		
		mCoordCross = new CrossModel(worldBase);

		mAccelerationVector = new VectorModel(worldBase, SensorManager.GRAVITY_EARTH);
		mAccelerationVector.setColor(Color.RED);
		
		mMagnetometerVector = new VectorModel(worldBase, SensorManager.MAGNETIC_FIELD_EARTH_MAX);
		mMagnetometerVector.setColor(Color.GREEN);
		
		mAccModel = new AccelerationModel();

		mSensorManager = (SensorManager) context
				.getSystemService(Context.SENSOR_SERVICE);

		mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);

		mMagnetometer = getSensor(Sensor.TYPE_MAGNETIC_FIELD, "magnetometer");
		mAccelerometer = getSensor(Sensor.TYPE_ACCELEROMETER, "accelerometer");
		mGyroscope = getSensor(Sensor.TYPE_GYROSCOPE, "gyroscope");

		mAccMovingAverage = new MovingAverageTD[] {
			new MovingAverageTD(0.25),
			new MovingAverageTD(0.25),
			new MovingAverageTD(0.25)
		};
		mAccDeviation = new StandardDeviationTD[] {
				new StandardDeviationTD(mAccMovingAverage[0]),
				new StandardDeviationTD(mAccMovingAverage[1]),
				new StandardDeviationTD(mAccMovingAverage[2])
		};
	}

	public void onResume() {

		if (mAccelerometer != null)
			mSensorManager.registerListener(this, mAccelerometer,
					SensorManager.SENSOR_DELAY_FASTEST);

		if (mMagnetometer != null)
			mSensorManager.registerListener(this, mMagnetometer,
					SensorManager.SENSOR_DELAY_FASTEST);

		if (mGyroscope != null)
			mSensorManager.registerListener(this, mGyroscope,
					SensorManager.SENSOR_DELAY_FASTEST);

	}

	public void onStop() {
		mSensorManager.unregisterListener(this);
	}
	
	
	boolean mTouched = false; 
	@Override
	public boolean onTouchEvent(MotionEvent event) {
			
		switch (event.getAction()) {
		case MotionEvent.ACTION_UP:
			System.out.println("realign");
			//mTouched = true;
			worldBase.setBase(3); // TODO: realign constructor
			break;
		}
		return true;
	}

	
	@Override
	public void onSensorChanged(SensorEvent event) {

		synchronized (this) {

			if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
				processGyroscopeEvent(event);
				invalidate();
			} else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				processAccelerometerEvent(event);
				invalidate();
			}
			 else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
					processMagnetometerEvent(event);
					invalidate();
				}
		}
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
	public void onAccuracyChanged(Sensor arg0, int arg1) {

	}
	
	private void processGyroscopeEvent(SensorEvent event) {
		float[] values = event.values;

		long timestamp = event.timestamp;
		if (mLastGyroTimeStamp == 0) {
			mLastGyroTimeStamp = timestamp; // throw away the first gyro event
			System.arraycopy(values, 0, lastValues, 0, 3);
			return;
		}

		double deltaT = (double) (timestamp - mLastGyroTimeStamp) / NANO;
		// Log.d(TAG, "values = (" + values[0] + ", " + values[1] + ", "
		// + values[2] + "), deltaT = " + deltaT);

		// This timestep's delta rotation to be multiplied by the current
		// rotation after computing it from the gyro sample data.
		if (timestamp != 0) {

			double deltaRotationVector[] = new double[3];

			final double TO_RADIANS = Math.PI  / 180;

			deltaRotationVector[0] = -(values[0] + lastValues[0]) / 2 * deltaT
					* TO_RADIANS;
			deltaRotationVector[1] = -(values[1] + lastValues[1]) / 2 * deltaT
					* TO_RADIANS;
			deltaRotationVector[2] = -(values[2] + lastValues[2]) / 2 * deltaT
					* TO_RADIANS;
			double[] rotationMatrix = Transform
					.getRotationMatrixSORA(deltaRotationVector);

			//double x = -(values[0]) * deltaT * SLOW_DOWN;
			//double y = (values[1]) * deltaT * SLOW_DOWN;
			//double z = (values[2]) * deltaT * SLOW_DOWN;
			//double[] rotationMatrix2 = Transform.getRotationMatrixSORA(x, y, z);

			// System.out.println("rotationMatrix:\n" + rotationMatrix[0]+" " +
			// rotationMatrix[1] + " " + rotationMatrix[2] + "\n"
			// + rotationMatrix[3] + " " + rotationMatrix[4] + " " +
			// rotationMatrix[5]+ "\n"
			// + rotationMatrix[6] + " " + rotationMatrix[7] + " " +
			// rotationMatrix[8]);

			worldBase.transform(new Matrix(3, 3, rotationMatrix));
			
			//mCube.rotatePoints(rotationMatrix);
			//mCube2.rotatePoints(rotationMatrix2);
			// mCube3.rotatePoints(rotationMatrix3);
			// rotatePoints(deltaRotationVector[0], deltaRotationVector[1],
			// deltaRotationVector[2]);
		}
		mLastGyroTimeStamp = timestamp;
		System.arraycopy(values, 0, lastValues, 0, 3);
	}
	
	private double standardDeviationVector3dSize(StandardDeviationTD[] deviationVector) {
		double sum = 0.0;
		for (StandardDeviationTD d: deviationVector) {
			double sd = d.getStandardDeviation();
			sum += sd * sd;
		}
		return Math.sqrt(sum);
	}
	
	
	long lastRealignTimestamp = 0;
	
	private boolean realignCondition() {
		return (standardDeviationVector3dSize(mAccDeviation)
		/ SensorManager.GRAVITY_EARTH) < 0.015;
	}
	
	private void processAccelerometerEvent(SensorEvent event) {
		
		for (int i = 0; i < 3; i++) {
			mAccMovingAverage[i].push(event.timestamp, event.values[i]);
		}

		long timestamp = event.timestamp;
		if (mLastAccTimeStamp == 0) {
			mLastAccTimeStamp = timestamp; // throw away the first event if
											// timestamp not set
			return;
		}
		
		boolean realign = realignCondition();
		
		long now = System.nanoTime();
		if (realign && now - lastRealignTimestamp > NANO) {
			lastRealignTimestamp = now;
			mTouched = false;
			//worldBase.realign(new Matrix(1, 3, new double[] {} ), 3);
			Vector3d axisZ = worldBase.getAxis(2); // z axis
			
			Vector3d acc = new Vector3d(
					mAccMovingAverage[0].getAverage(),
					mAccMovingAverage[1].getAverage(),
					mAccMovingAverage[2].getAverage()
				);
				
			Vector3d rotationVector = axisZ.normalize().crossProduct(acc.normalize());
			 
			double[] rotationMatrix = Transform
					.getRotationMatrixSORA(rotationVector.getValues());
			
			worldBase.transform(new Matrix(3, 3, rotationMatrix));
			
			mAccModel.reset();
		}
		
		

		
		// double deltaT = (double) (timestamp - mLastAccTimeStamp) / NANO;
		// Log.d(TAG, "values = (" + values[0] + ", " + values[1] + ", " +
		// values[2] + "), deltaT = " + deltaT);

		if (timestamp != 0) {
		
			Matrix accelerationInWorld = worldBase.to(new Matrix(1, 3,
					new double[]{event.values[0], event.values[1], event.values[2]}));
						
			mAccModel.processAccelerometerEvent(timestamp,
					(float)accelerationInWorld.getValue(0, 0),
					(float)accelerationInWorld.getValue(1, 0),
					(float)accelerationInWorld.getValue(2, 0));
			
			mAccelerationVector.setValues(accelerationInWorld.getValue(0, 0),
					accelerationInWorld.getValue(1, 0),
					accelerationInWorld.getValue(2, 0));
			
		}
		
		mLastAccTimeStamp = timestamp;

	}
	
	private void processMagnetometerEvent(SensorEvent event) {
		

		Matrix magneticInWorld = worldBase.to(new Matrix(1, 3,
				new double[]{event.values[0], event.values[1], event.values[2]}));
		
		mMagnetometerVector.setValues(magneticInWorld.getValue(0, 0),
				magneticInWorld.getValue(1, 0),
				magneticInWorld.getValue(2, 0));
		
		
	}


	@Override
	protected void onDraw(Canvas canvas) {

		
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setColor(Color.BLACK);
		canvas.drawColor(0xFFFFFFFF);
		
		canvas.drawText("Cube rotation demo", 5, 15, mPaint);
		canvas.drawText("acceleration:", 5, 25, mPaint);
		canvas.drawText("rate: " + mAccMovingAverage[0].getRate(), 5, 35, mPaint);
		boolean realign = realignCondition();
		if (realign) {
			mPaint.setColor(Color.RED);
		}
		canvas.drawText("sigma: " + standardDeviationVector3dSize(mAccDeviation), 5, 45, mPaint);
		mPaint.setColor(Color.BLACK);
		
		canvas.translate(getWidth() / 2, getHeight() / 2);
		
		mCube.draw(canvas);
		mCube2.draw(canvas);
		mCoordCross.draw(canvas);
		mAccelerationVector.draw(canvas);
	
		mMagnetometerVector.draw(canvas);
		
		canvas.restore();
		mAccModel.draw(canvas);
		
	}
}
