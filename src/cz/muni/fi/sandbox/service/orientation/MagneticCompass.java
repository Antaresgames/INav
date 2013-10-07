package cz.muni.fi.sandbox.service.orientation;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class MagneticCompass extends Compass implements SensorEventListener {

	private static final String TAG = "MagneticCompass";
	
	private Sensor mMagnetometer;
	private float mHeading; 
	private Path mNeedle;
	

	public MagneticCompass(SensorManager sensorManager) {
		super(sensorManager);
		//mMagnetometer = getSensor(Sensor.TYPE_MAGNETIC_FIELD, "magnetometer");
		mMagnetometer = getSensor(Sensor.TYPE_ORIENTATION, "magnetometer");
		setupNeedle();
	}

	public void start() {

		Log.d(TAG, "start():");
		if (mMagnetometer != null)
			mSensorManager.registerListener(this, mMagnetometer,
					SensorManager.SENSOR_DELAY_FASTEST);

	}

	public void stop() {
		Log.d(TAG, "stop():");
		mSensorManager.unregisterListener(this);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	@Override
	public void onSensorChanged(SensorEvent event) {

		synchronized (this) {

			if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
				processMagnetometerEvent(event);
			} else if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
				processMagnetometerEvent(event);
			}
			notifyHeadingUpdate(getHeading());
		}
	}
	
	public void processMagnetometerEvent(SensorEvent event) {
		
		//Log.d(TAG, "processMagnetometerEvent: heading = " + event.values[0]);
		mHeading = (float)(Math.PI * event.values[0] / 180);
	}

	@Override
	public float getHeading() {
		return mHeading;
	}
	
	
	public void setupNeedle() {
		mNeedle = new Path();
		mNeedle.moveTo(-20, 0);
		mNeedle.lineTo(0, -200);
		mNeedle.lineTo(20, 0);
		mNeedle.close();
	}
	
	private float toDegrees(float rad) {
		return (float)(180 * rad / Math.PI);
	}
	
	@Override
	public void draw(Canvas canvas) {
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(Color.BLACK);
			
		canvas.rotate(toDegrees(getHeading()));
		canvas.drawPath(mNeedle, paint);
	}

}
