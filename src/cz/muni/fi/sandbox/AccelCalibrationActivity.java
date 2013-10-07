package cz.muni.fi.sandbox;

import android.app.Activity;
import android.content.SharedPreferences.Editor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class AccelCalibrationActivity extends Activity implements SensorEventListener {
	
	private static final String TAG = AccelCalibrationActivity.class.getCanonicalName();
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private int passedCycles = 0;
	
	private double min, max;
	
	private static final int X = 0;
	private static final int Y = 1;
	private static final int Z = 2;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mAccelerometer = getSensor(Sensor.TYPE_ACCELEROMETER,
				"accelerometer");
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mAccelerometer != null)
			mSensorManager.registerListener(this, mAccelerometer,
					SensorManager.SENSOR_DELAY_FASTEST);
	}
	
	@Override
	protected void onStop() {
		mSensorManager.unregisterListener(this);
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		mSensorManager.unregisterListener(this);
		super.onDestroy();
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
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// do nothing
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
    	passedCycles++;
    	if (passedCycles <= 500) {
    		final float[] values = event.values.clone();
    		final double value = Math.hypot(values[Y], values[Z]);
        	if (value > max) {
        		max = value;
        	}
        	
        	if (value < min) {
        		min = value;
        	}
    	} else {
    		mSensorManager.unregisterListener(this);
    		Editor prefs = PreferenceManager.getDefaultSharedPreferences(AccelCalibrationActivity.this).edit();
    		prefs.putString("accel_complementary", String.valueOf((min+max)/2));
    		prefs.commit();
    		
    		Toast.makeText(AccelCalibrationActivity.this, "Accel compl. factor: " + ((min+max)/2), Toast.LENGTH_LONG).show();
    		
    		this.finish();
    	}
	}
}
