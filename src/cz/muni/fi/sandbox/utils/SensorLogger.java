package cz.muni.fi.sandbox.utils;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

public class SensorLogger implements SensorEventListener {

	private final String TAG = "SensorLogger";
	private Writer writer;
	private final long NANO = (long)Math.pow(10, 9);
	private long startTime = 0;
	private boolean stopped;
	
	// field separator
	final private String FS = " ";

	public SensorLogger() {
		this("sensor_dump.txt");
	}

	public SensorLogger(String fileName) {
		writer = new Writer(fileName);
		Log.d(TAG, "created writer for " + writer.getFile().getAbsolutePath());
		start();
	}

	public void onSensorChanged(SensorEvent event) {
		processSensorEvent(event.sensor.getType(), event.timestamp, event.values);
	}
	
	private void processSensorEvent(int sensor, long timestamp, float[] values) {

		if (stopped) {
			return;
		}
		
		double timeSeconds = (double)(timestamp - startTime) / NANO;
		if (sensor == Sensor.TYPE_ORIENTATION) {
			writer.writeln("ori" + FS + timeSeconds + FS + values[0] + FS + values[1] + FS + values[2]);
		} else if (sensor == Sensor.TYPE_MAGNETIC_FIELD) {
			writer.writeln("mag" + FS + timeSeconds + FS + values[0] + FS + values[1] + FS + values[2]);
		} else if (sensor == Sensor.TYPE_ACCELEROMETER) {
			writer.writeln("acc" + FS + timeSeconds + FS + values[0] + FS + values[1] + FS + values[2]);
		}

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	public void start() {
		startTime = System.nanoTime();
		writer.open(false);
		stopped = false;
	}

	public void stop() {
		stopped = true;
		writer.close();
	}
}
