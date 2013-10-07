package cz.muni.fi.sandbox.service.stepdetector;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.preference.PreferenceManager;
import android.util.Log;
import cz.muni.fi.sandbox.dsp.filters.CumulativeSignalPowerTD;
import cz.muni.fi.sandbox.dsp.filters.MovingAverageTD;

/**
 * MovingAverageStepDetector class, step detection filter based on two moving averages
 * with minimum signal power threshold 
 * 
 * @author Michal Holcik
 * 
 */
public class MovingAverageStepDetector extends StepDetector {
	
	//private static final String TAG = "MovingAverageStepDetector";
	private float[] maValues;
	private MovingAverageTD[] ma;
	private CumulativeSignalPowerTD asp;
	private boolean mMASwapState;
	private boolean stepDetected;
	private boolean signalPowerCutoff;
	private long mLastStepTimestamp;
	private double strideDuration;
	
	public static final double MA1_WINDOW = 0.2;
	public static final double MA2_WINDOW = 5 * MA1_WINDOW;
	public static final float POWER_CUTOFF_VALUE = 1000.0f;
	private static final long MAX_STRIDE_DURATION = 2 * 1000000000l; // in nano seconds
	private double accelComplementary;
	
	private double mWindowMa1;
	private double mWindowMa2;
	private float mPowerCutoff;
	
	public MovingAverageStepDetector() {
		this(MA1_WINDOW, MA2_WINDOW, POWER_CUTOFF_VALUE, null);
	}

	public MovingAverageStepDetector(double windowMa1, double windowMa2, double powerCutoff) {
		this(windowMa1, windowMa2, powerCutoff, null);
	}
	public MovingAverageStepDetector(double windowMa1, double windowMa2, double powerCutoff, Context context) {
		
		mWindowMa1 = windowMa1;
		mWindowMa2 = windowMa2;
		mPowerCutoff = (float)powerCutoff;
		
		maValues = new float[4];
		mMASwapState = true;
		ma = new MovingAverageTD[] { new MovingAverageTD(mWindowMa1),
				new MovingAverageTD(mWindowMa1),
				new MovingAverageTD(mWindowMa2) };
		asp = new CumulativeSignalPowerTD();
		stepDetected = false;
		signalPowerCutoff = true;
	
		if (context != null) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			if (prefs != null) {
				try {
					accelComplementary = Double.valueOf(prefs.getString(
							"accel_complementary", "0.0"));
					Log.d("Akcelerator comp faktor", ""+accelComplementary);
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public class MovingAverageStepDetectorState {
		float[] values;
		public boolean[] states;
		double duration;

		MovingAverageStepDetectorState(float[] values, boolean[] states, double duration) {
			this.values = values;
			this.states = states;
		}
	}

	public MovingAverageStepDetectorState getState() {
		return new MovingAverageStepDetectorState(new float[] { maValues[0],
				maValues[1], maValues[2], maValues[3] }, new boolean[] {
				stepDetected, signalPowerCutoff }, strideDuration);
	}

	public float getPowerThreshold() {
		return mPowerCutoff;
	}

	private void processAccelerometerValues(long timestamp, float[] values) {

		float value = (float) (Math.hypot(values[1], values[2]) - accelComplementary); //sqrt(y*y + z*z)
		final float origValue = value;

		// compute moving averages
	
		for (int i = 0; i < 3; i++) {
			ma[i].push(timestamp, value);
			maValues[i] = (float) ma[i].getAverage();
			value = maValues[i];
		}
		maValues[0] = origValue; //remember unfiltered data

		// detect moving average crossover
		stepDetected = false;
		boolean newSwapState = maValues[1] > maValues[2];
		if (newSwapState != mMASwapState) {
			mMASwapState = newSwapState;
			if (mMASwapState) {
				stepDetected = true;
			}
		}

		// compute signal power
		asp.push(timestamp, maValues[1] - maValues[2]);
		maValues[3] = (float) asp.getValue();
		signalPowerCutoff = maValues[3] < mPowerCutoff;

		if (stepDetected) {
			asp.reset();
			maValues[3] = (float) asp.getValue();
		}

		// step event
		if (stepDetected && !signalPowerCutoff) {
			strideDuration = getStrideDuration(timestamp);
			if (strideDuration < MAX_STRIDE_DURATION) {
				notifyOnStep(new StepEvent(1.0, strideDuration));
				mLastStepTimestamp = timestamp;
			} else {
				signalPowerCutoff = true; //jen kvùli správnému obarvování v StepDetectionDemo
				mLastStepTimestamp = 0; //jinak by èas od posledního kroku stále rostl a vše by bylo false
			}
		}

	}

	/* 
	 * @return stride duration
	 */
	private long getStrideDuration(long currentTimestamp) {
		if (mLastStepTimestamp == 0) {
			mLastStepTimestamp = currentTimestamp;
		}
		
		return currentTimestamp - mLastStepTimestamp;
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// Log.d(TAG, "sensor: " + sensor + ", x: " + values[0] + ", y: " +
		// values[1] + ", z: " + values[2]);
			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				processAccelerometerValues(event.timestamp, event.values.clone());
			}
	}
}
