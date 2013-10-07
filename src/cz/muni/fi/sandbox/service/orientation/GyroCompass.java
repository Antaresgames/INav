package cz.muni.fi.sandbox.service.orientation;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import cz.muni.fi.sandbox.dsp.filters.MovingAverageTD;
import cz.muni.fi.sandbox.dsp.filters.StandardDeviationTD;
import cz.muni.fi.sandbox.utils.linear.Matrix;
import cz.muni.fi.sandbox.utils.linear.Transform;
import cz.muni.fi.sandbox.utils.linear.Vector3d;
import cz.muni.fi.sandbox.utils.linear.VectorBase;

public class GyroCompass extends Compass implements SensorEventListener {

	private static final String TAG = "GyroCompass";
	private final long NANO = (long) Math.pow(10, 9);
	private final double ACCELEROMETER_STANDARD_DEVIATION_THRESHOLD = 0.015;
	private static final double ACC_COMPLEMENTARY_FILTER_FACTOR = 0.01;
	private static final double MAG_COMPLEMENTARY_FILTER_FACTOR = 0.01;
	
	private RealignMode mRealignMode = RealignMode.CONTINUOUS;
	private double mAccComplementaryFilterFactor = ACC_COMPLEMENTARY_FILTER_FACTOR;
	private double mMagComplementaryFilterFactor = MAG_COMPLEMENTARY_FILTER_FACTOR;
	private int MAX_FAST_COMPASS_REALIGN_ATTEMPTS = 10;
	private int mFastCompassRealignAttempts = MAX_FAST_COMPASS_REALIGN_ATTEMPTS;

	private VectorBase mWorldBase;

	private SensorEvent mLastAccEvent;
	private long mLastGyroTimeStamp = 0;
	private float[] lastGyroValues = new float[3];

	// realignment data
	private long lastRealignTimestamp = 0;
	private StandardDeviationTD[] mAccDeviation;
	private MovingAverageTD[] mAccMovingAverage;

	private VectorModel mAccelerationVector;
	private VectorModel mMagnetometerVector;
	private Matrix mMagneticInWorld;
	
	private Sensor mAccelerometer;
	private Sensor mMagnetometer;
	private Sensor mGyroscope;

	private double mHeading;

	// visuals
	private CubeModel mCube;
	private CrossModel mCoordCross;

	public GyroCompass(SensorManager sensorManager, boolean realign) {
		super(sensorManager);
		mWorldBase = new VectorBase(3);
		mAccelerationVector = new VectorModel(mWorldBase,
				SensorManager.GRAVITY_EARTH);
		mAccelerationVector.setColor(Color.RED);

		mMagnetometerVector = new VectorModel(mWorldBase,
				SensorManager.MAGNETIC_FIELD_EARTH_MAX);
		mMagnetometerVector.setColor(Color.GREEN);

		mAccMovingAverage = new MovingAverageTD[] { new MovingAverageTD(0.25),
				new MovingAverageTD(0.25), new MovingAverageTD(0.25) };
		mAccDeviation = new StandardDeviationTD[] {
				new StandardDeviationTD(mAccMovingAverage[0]),
				new StandardDeviationTD(mAccMovingAverage[1]),
				new StandardDeviationTD(mAccMovingAverage[2]) };

		mMagnetometer = getSensor(Sensor.TYPE_MAGNETIC_FIELD, "magnetometer");
		mAccelerometer = getSensor(Sensor.TYPE_ACCELEROMETER, "accelerometer");
		mGyroscope = getSensor(Sensor.TYPE_GYROSCOPE, "gyroscope");

		mCube = new CubeModel(mWorldBase);
		mCoordCross = new CrossModel(mWorldBase);
		
		mRealignMode = realign?RealignMode.CONTINUOUS:RealignMode.NONE;

	}

	public void start() {

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

	public void stop() {
		mSensorManager.unregisterListener(this);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	@Override
	public void onSensorChanged(SensorEvent event) {

		synchronized (this) {

			if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
				processGyroscopeEvent(event);

			} else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				processAccelerometerEvent(event);

			} else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
				processMagnetometerEvent(event);
			}
			mHeading = computeHeading();
			
			// FIXME: this happened sometimes
			// if it happens again reset the compass
			// user doesn't want to see a stuck compass...
			if (mHeading == Double.NaN) {
				reset();
			} else {
				notifyHeadingUpdate(getHeading());
			}
		}
	}

	public void reset() {
		mWorldBase.setBase(3);
		mFastCompassRealignAttempts = MAX_FAST_COMPASS_REALIGN_ATTEMPTS;
	}

	private double standardDeviationVector3dSize(
			StandardDeviationTD[] deviationVector) {
		double sum = 0.0;
		for (StandardDeviationTD d : deviationVector) {
			double sd = d.getStandardDeviation();
			sum += sd * sd;
		}
		return Math.sqrt(sum);
	}

	private boolean realignCondition() {

		return (System.nanoTime() - lastRealignTimestamp > NANO)
				&& (standardDeviationVector3dSize(mAccDeviation)
						/ SensorManager.GRAVITY_EARTH < ACCELEROMETER_STANDARD_DEVIATION_THRESHOLD);
	}

	private void realignWorldBase() {

		//Log.d(TAG, "realignWorldBase(): ");
		lastRealignTimestamp = System.nanoTime();

		// worldBase.realign(new Matrix(1, 3, new double[] {} ), 3);
		Vector3d axisZ = mWorldBase.getAxis(2); // z axis

		Vector3d acc = new Vector3d(mAccMovingAverage[0].getAverage(),
				mAccMovingAverage[1].getAverage(),
				mAccMovingAverage[2].getAverage());

		Vector3d rotationVector = axisZ.normalize().crossProduct(
				acc.normalize());

		double[] rotationMatrix = Transform
				.getRotationMatrixSORA(rotationVector.getValues());

		mWorldBase.transform(new Matrix(3, 3, rotationMatrix));
	}
	
	private void accelerometerRealignContinuous() {

		//Log.d(TAG, "continuousRealign(): ");
		
		// realign to gravity
		Vector3d axisZ = mWorldBase.getAxis(2); // z axis

		Vector3d acc = new Vector3d(mAccMovingAverage[0].getAverage(),
				mAccMovingAverage[1].getAverage(),
				mAccMovingAverage[2].getAverage());

		Vector3d rotationVector = axisZ.normalize().crossProduct(
				acc.normalize());
		
		rotationVector.scalarMultiple(mAccComplementaryFilterFactor);

		double[] rotationMatrix = Transform
				.getRotationMatrixSORA(rotationVector.getValues());

		mWorldBase.transform(new Matrix(3, 3, rotationMatrix));
		
	}
	
	private void magneticRealignContinuous() {
		Vector3d axisZ = mWorldBase.getAxis(1); // y axis
		
		Matrix magneticXyInDevice = mWorldBase.from(new Matrix(1, 3, new double[] {
				mMagneticInWorld.getValue(0, 0),
				mMagneticInWorld.getValue(1, 0),
				0.0}));
		
		//Log.d(TAG, "projected mag vector = (" + magneticInWorld.getValue(0, 0) + ", " + magneticInWorld.getValue(1, 0) + ")");
		
		Vector3d mag = new Vector3d(magneticXyInDevice.getValue(0, 0),
				magneticXyInDevice.getValue(1, 0),
				magneticXyInDevice.getValue(2, 0));
		
		//Log.d(TAG, "projected mag vector in device coordinates = " + Arrays.toString(mag.getValues()));
		
		Vector3d rotationVector = axisZ.normalize().crossProduct(
				mag.normalize());
		
		if (mFastCompassRealignAttempts == 0)
			// this multiplies the sinus of the angle but what the ...
			rotationVector.scalarMultiple(mMagComplementaryFilterFactor);
		else {
			Log.d(TAG, "fast compass realign");
			rotationVector.scalarMultiple(1.0);
			mFastCompassRealignAttempts--;
		}
		
		

		double[] rotationMatrix = Transform
				.getRotationMatrixSORA(rotationVector.getValues());

		mWorldBase.transform(new Matrix(3, 3, rotationMatrix));
		
	}

	private void processGyroscopeEvent(SensorEvent event) {
		float[] values = event.values;

		long timestamp = event.timestamp;
		if (mLastGyroTimeStamp == 0) {
			mLastGyroTimeStamp = timestamp; // throw away the first gyro event
			System.arraycopy(values, 0, lastGyroValues, 0, 3);
			return;
		}

		double deltaT = (double) (timestamp - mLastGyroTimeStamp) / NANO;
		// Log.d(TAG, "values = (" + values[0] + ", " + values[1] + ", "
		// + values[2] + "), deltaT = " + deltaT);

		if (timestamp != 0) {

			double deltaRotationVector[] = new double[3];

			final double TO_RADIANS = Math.PI / 180;

			deltaRotationVector[0] = -(values[0] + lastGyroValues[0]) / 2
					* deltaT * TO_RADIANS;
			deltaRotationVector[1] = -(values[1] + lastGyroValues[1]) / 2
					* deltaT * TO_RADIANS;
			deltaRotationVector[2] = -(values[2] + lastGyroValues[2]) / 2
					* deltaT * TO_RADIANS;
			double[] rotationMatrix = Transform
					.getRotationMatrixSORA(deltaRotationVector);

			mWorldBase.transform(new Matrix(3, 3, rotationMatrix));

		}
		mLastGyroTimeStamp = timestamp;
		System.arraycopy(values, 0, lastGyroValues, 0, 3);
	}

	enum RealignMode {
		NONE, CONDITIONAL_SNAP, CONTINUOUS,
	};
	
	public void processAccelerometerEvent(SensorEvent event) {

		// Log.d(TAG, "values = (" + values[0] + ", " + values[1] + ", " +
		// values[2] + "), deltaT = " + deltaT);

		for (int i = 0; i < 3; i++) {
			mAccMovingAverage[i].push(event.timestamp, event.values[i]);
		}

		if (mLastAccEvent == null) {
			mLastAccEvent = event;
			// throw away the first event if
			return;
		}

		switch (mRealignMode) {
		case CONDITIONAL_SNAP:
			if (realignCondition()) {
				realignWorldBase();
			}
			break;
		case CONTINUOUS:
			accelerometerRealignContinuous();
			break;
		case NONE:
			// do nothing
			break;
		default:
			Log.e(TAG, "bad realign mode");
		}

		// double deltaT = (double) (event.timestamp - mLastAccEvent.timestamp)
		// / NANO;
		
		Matrix accelerationInWorld = mWorldBase.to(new Matrix(1, 3,
				new double[] { event.values[0], event.values[1],
						event.values[2] }));

		mAccelerationVector.setValues(accelerationInWorld.getValue(0, 0),
				accelerationInWorld.getValue(1, 0),
				accelerationInWorld.getValue(2, 0));

		mLastAccEvent = event;
	}

	
	public void processMagnetometerEvent(SensorEvent event) {

		mMagneticInWorld = mWorldBase.to(new Matrix(1, 3, new double[] {
				event.values[0], event.values[1], event.values[2] }));

		mMagnetometerVector.setValues(mMagneticInWorld.getValue(0, 0),
				mMagneticInWorld.getValue(1, 0), mMagneticInWorld.getValue(2, 0));
		
		if (mRealignMode == RealignMode.CONTINUOUS) {
			magneticRealignContinuous();
		}
		
	}

	private double computeHeading() {
		Matrix headingVector = mWorldBase.to(new Matrix(1, 3, new double[] {
				0.0, 1.0, 0.0 }));

		double heading = Math.atan2(headingVector.getValue(0, 0),
				headingVector.getValue(1, 0));

		// Log.d(TAG, "heading = " + mHeading);
		return heading;
	}

	@Override
	public float getHeading() {
		return (float) mHeading;
	}

	public void draw(Canvas canvas) {
		mCube.draw(canvas);
		mCoordCross.draw(canvas);
		mAccelerationVector.draw(canvas);
		mMagnetometerVector.draw(canvas);
	}
	
	@Override
	public void updatePreferences(SharedPreferences prefs) {
		
		try {
			mAccComplementaryFilterFactor = Double.valueOf(prefs.getString(
					"acc_complementary_filter_factor_preference",
					String.valueOf(ACC_COMPLEMENTARY_FILTER_FACTOR)));
		} catch(NumberFormatException e) {
			e.printStackTrace();
			mAccComplementaryFilterFactor = ACC_COMPLEMENTARY_FILTER_FACTOR;
		}
		Log.d(TAG, "acc filter factor = " + mAccComplementaryFilterFactor);
		
		try {
			mMagComplementaryFilterFactor = Double.valueOf(prefs.getString(
					"mag_complementary_filter_factor_preference",
					String.valueOf(MAG_COMPLEMENTARY_FILTER_FACTOR)));
		} catch(NumberFormatException e) {
			e.printStackTrace();
			mMagComplementaryFilterFactor = MAG_COMPLEMENTARY_FILTER_FACTOR;
		}
		Log.d(TAG, "mag filter factor = " + mMagComplementaryFilterFactor);
		
	}
	
}
