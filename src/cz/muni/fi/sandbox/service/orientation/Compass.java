package cz.muni.fi.sandbox.service.orientation;

import java.util.ArrayList;
import java.util.List;


import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.util.Log;

public abstract class Compass {
	
	protected static final String TAG = "Compass";
	
	public static final String UNDEFINED = "undefined";
	public static final String GYROSCOPIC = "gyro";
	public static final String MAGNETIC = "magnetic";
	public static final String GYROMAGNETIC = "combined";
	
	protected SensorManager mSensorManager;
	protected List<HeadingListener> mListeners;
	
	public static Compass compassFactory(String type, SensorManager sensorManager) {
		Log.d(TAG, "providerFactory: " + type);
		if (type.equals(GYROSCOPIC)) {
			Log.d(TAG, "creating gyroscopic compass");
			return new GyroCompass(sensorManager, false);
		} else if (type.equals(GYROMAGNETIC)) {
			Log.d(TAG, "creating combined gyromagnetic compass");
			return new GyroCompass(sensorManager, true);
		} else if (type.equals(MAGNETIC)) {
			Log.d(TAG, "creating magnetic compass");
			return new MagneticCompass(sensorManager);
		}
		Log.d(TAG, "creating default magnetic compass");
		return new MagneticCompass(sensorManager);
	}
	
	public Compass(SensorManager sensorManager) {
		mSensorManager = sensorManager;
		mListeners = new ArrayList<HeadingListener>();
	}
	
	protected Sensor getSensor(int sensorType, String sensorName) {

		Sensor sensor = mSensorManager.getDefaultSensor(sensorType);
		if (sensor != null) {
			Log.d(TAG, "there is a " + sensorName);
		} else {
			Log.d(TAG, "there is no " + sensorName);
		}
		return sensor;
	}

	
	public void register(HeadingListener listener) {
		mListeners.add(listener);
	}

	public void unregister(HeadingListener listener) {
		mListeners.remove(listener);
	}

	protected void notifyHeadingUpdate(float heading) {
		for (HeadingListener listener : mListeners) {
				listener.headingChanged(heading);
		}
	}
	
	public abstract void start();
	public abstract void stop();
	public void reset() {}
	public abstract float getHeading();
	public abstract void draw(Canvas canvas);

	public void updatePreferences(SharedPreferences prefs) {}
	
}