package cz.muni.fi.sandbox.service.motion;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.util.Log;
import cz.muni.fi.sandbox.service.orientation.Compass;
import cz.muni.fi.sandbox.service.orientation.HeadingListener;
import cz.muni.fi.sandbox.service.stepdetector.IStepListener;
import cz.muni.fi.sandbox.service.stepdetector.MovingAverageStepDetector;
import cz.muni.fi.sandbox.service.stepdetector.StepDetector;
import cz.muni.fi.sandbox.service.stepdetector.StepEvent;
import cz.muni.fi.sandbox.utils.SensorLogger;

/**
 * 
 * @author Michal Holcik
 *
 */
public class SensorMotionProvider extends MotionProvider implements HeadingListener,
		IStepListener {

	private SensorManager mSensorManager;
	private StepDetector mStepDetector;
	private SensorLogger mSensorLogger;
	private Compass mCompass;
	
	private Sensor mAccelerometer;
	private Sensor mMagnetometer;
	private Sensor mOrientation;

	// need to cache orientation
	private float mHeading = 0.0f;

	private boolean mPaused = false;

	private final double stepSize = .7; // in meters
	private String mCompassType = "magnetic"; //default value
	
	private boolean logAccelerometer = true;
	private boolean logMagnetometer = false;
	private boolean logOrientation = false;

	public SensorMotionProvider(Context context) {

		mSensorManager = (SensorManager) context
				.getSystemService(Context.SENSOR_SERVICE);

		
		// dirty
		// mStepDetector = StepDetector.stepDetectorFactory("moving_average");
		mStepDetector = new MovingAverageStepDetector();
		mStepDetector.addStepListener(this);
		
		mCompass = Compass.compassFactory(mCompassType, mSensorManager);
		mCompass.register(this);
		

		mSensorLogger = new SensorLogger();
		
		mMagnetometer = getSensor(Sensor.TYPE_MAGNETIC_FIELD, "magnetometer");
		mAccelerometer = getSensor(Sensor.TYPE_ACCELEROMETER, "accelerometer");
		mOrientation = getSensor(Sensor.TYPE_ORIENTATION, "orientation");
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


	public void resume() {
		
		mSensorManager.registerListener(mStepDetector, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
		if (logAccelerometer)
			mSensorManager.registerListener(mSensorLogger, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
		if (logMagnetometer)
			mSensorManager.registerListener(mSensorLogger, mMagnetometer, SensorManager.SENSOR_DELAY_FASTEST);
		if (logOrientation)
			mSensorManager.registerListener(mSensorLogger, mOrientation, SensorManager.SENSOR_DELAY_FASTEST);
				

		mCompass.start();
		mSensorLogger.start();
	}

	public void stop() {
		
		mSensorManager.unregisterListener(mStepDetector);
		mSensorManager.unregisterListener(mSensorLogger);

		mCompass.stop();
		mSensorLogger.stop();

	}

	@Override
	public void setPaused(boolean paused) {
		mPaused = paused;
	}
	
	@Override
	public void onStepEvent(StepEvent event) {
		Log.d(TAG, "onStepEvent(event = " + event + "):");

		if (!mPaused) {
			notifyPositionUpdate(mHeading,
					(float) (stepSize * Math.sin(mHeading)),
					(float) (stepSize * Math.cos(mHeading)));
		}
	}
	

	@Override
	public void headingChanged(float heading) {
		if (!mPaused) {
			mHeading = heading;
			notifyPositionUpdate(heading, 0.0f, 0.0f);
		}		
	}
	
	@Override
	public void reset() {
		mCompass.reset();
	}
	
	@Override
	public void updatePreferences(SharedPreferences prefs) {
		
		
		double movingAverage1 = MovingAverageStepDetector.MA1_WINDOW;
		double movingAverage2 = MovingAverageStepDetector.MA2_WINDOW;
		double powerCutoff = MovingAverageStepDetector.POWER_CUTOFF_VALUE;
		try {
			movingAverage1 = Double.valueOf(prefs.getString(
					"short_moving_average_window_preference", "0.2"));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		try {
			movingAverage2 = Double.valueOf(prefs.getString(
					"long_moving_average_window_preference", "1.0"));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		try {
			powerCutoff = Double.valueOf(prefs.getString(
					"step_detection_power_cutoff_preference", "1000"));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		
		mStepDetector = new MovingAverageStepDetector(movingAverage1, movingAverage2, powerCutoff);
		mStepDetector.addStepListener(this);
		
		String newCompassType = prefs.getString(
				"compass_type_preference", "unkn");
		
		if (!newCompassType.equals(mCompassType)) {
			if (mCompass != null)
				mCompass.stop();
			mCompassType = newCompassType;
			Log.d(TAG, "compass type changed, new compass type = " + mCompassType);
			mCompass = Compass.compassFactory(mCompassType, mSensorManager);
			mCompass.updatePreferences(prefs);
			mCompass.register(this);
			mCompass.start();
		}
	}
}
