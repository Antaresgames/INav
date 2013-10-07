package cz.muni.fi.sandbox.service.stepdetector;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.util.Log;
import cz.muni.fi.sandbox.dsp.filters.CumulativeSignalPowerTD;
import cz.muni.fi.sandbox.dsp.filters.MovingAverageTD;
import cz.muni.fi.sandbox.dsp.filters.SignalPowerTD;

/**
 * MovingAverageStepDetector class, step detection filter based on two moving averages
 * with minimum signal power threshold 
 * 
 * @author Michal Holcik
 * 
 */
public class StepDetectorGravity extends StepDetector {
	
	private static final String TAG = "GravityStepDetector";
	private float[] mValue;
	private CumulativeSignalPowerTD asp;
	private final MovingAverageTD[] ma;
	private boolean stepDetected;
	private boolean signalPowerCutoff;
	private long mLastStepTimestamp;
	
	public static final float POWER_CUTOFF_VALUE = 2000.0f;
	private static final double MAX_STRIDE_DURATION = 2.0; // in seconds
	private float mPowerCutoff;
	private float preprev;
	private float prev;
	
	public StepDetectorGravity() {
		this(POWER_CUTOFF_VALUE);
	}

	public StepDetectorGravity(double powerCutoff) {
		mValue = new float[4];
		ma = new MovingAverageTD[] { new MovingAverageTD(0.2),
				new MovingAverageTD(0.2),
				new MovingAverageTD(1) };
		
		mPowerCutoff = (float)powerCutoff;
		
		asp = new CumulativeSignalPowerTD();
		stepDetected = false;
		signalPowerCutoff = true;
	}

	public class StepDetectorGravityState {
		float[] value;
		public boolean[] states;

		StepDetectorGravityState(float[] value, boolean[] states) {
			this.value = value;
			this.states = states;
		}
	}

	public StepDetectorGravityState getState() {
		return new StepDetectorGravityState(new float[] { mValue[0],
				mValue[1], mValue[2], mValue[3] }, new boolean[] {
				stepDetected, signalPowerCutoff });
	}

	public float getPowerThreshold() {
		return mPowerCutoff;
	}

	private void processSensorValues(long timestamp, float[] values) {
		float value = values[0];
		
		mValue[0] = value;
		
		for (int i = 1; i < 3; i++) {
			ma[i].push(timestamp, value);
			mValue[i] = (float) ma[i].getAverage();
			value = mValue[i];
		}
		
		// detect crossover
		stepDetected = (preprev <= prev) && (prev > value) && mValue[1] > mValue[2];
		
		asp.push(timestamp, mValue[1]);
		
		mValue[3] = (float) asp.getValue();
		signalPowerCutoff = mValue[3] < mPowerCutoff;

		if (stepDetected) {
			asp.reset();
		}
		
		preprev = prev; //update pøedposlední hodnoty
		prev = mValue[1]; //update poslední hodnoty

		// step event
		if (stepDetected && !signalPowerCutoff) {
			final long strideDuration = getStrideDuration(timestamp);
			if (strideDuration < MAX_STRIDE_DURATION) {
				notifyOnStep(new StepEvent(1.0, strideDuration));
				mLastStepTimestamp = timestamp;
			} else {
				signalPowerCutoff = true; //jen kvùli správnému obarvování v StepDetectionDemo
				mLastStepTimestamp = 0; //jinak by èas od posledního kroku stále rostl a vše by bylo false
			}
		}
	}

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
			if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
				processSensorValues(event.timestamp, event.values.clone());
			}
	}
}
