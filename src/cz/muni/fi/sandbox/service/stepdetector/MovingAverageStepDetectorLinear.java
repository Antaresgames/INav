package cz.muni.fi.sandbox.service.stepdetector;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import cz.muni.fi.sandbox.dsp.filters.CumulativeSignalPowerTD;
import cz.muni.fi.sandbox.dsp.filters.MovingAverageTD;

/**
 * MovingAverageStepDetector class, step detection filter based on two moving averages
 * with minimum signal power threshold 
 * 
 * @author Michal Holcik
 * 
 */
public class MovingAverageStepDetectorLinear extends StepDetector {
	
	//private static final String TAG = "MovingAverageStepDetectorLinear";
	private float[] maValues;
	private float previousState = 0.0f;
	private float prepreviousState = 0.0f;
	private MovingAverageTD[] ma;
	private CumulativeSignalPowerTD asp;
	private boolean stepDetected;
	private boolean signalPowerCutoff;
	private long mLastStepTimestamp;
	
	public static final double MA1_WINDOW = 0.2;
	public static final double MA2_WINDOW = 5 * MA1_WINDOW;
	public static final float POWER_CUTOFF_VALUE = 1000.0f;
	private static final long MAX_STRIDE_DURATION = 2 * 1000000000l; // in nano seconds
	
	private double mWindowMa1;
	private double mWindowMa2;
	private float mPowerCutoff;
	
	public MovingAverageStepDetectorLinear() {
		this(MA1_WINDOW, MA2_WINDOW, POWER_CUTOFF_VALUE);
	}

	public MovingAverageStepDetectorLinear(double windowMa1, double windowMa2, double powerCutoff) {
		
		mWindowMa1 = windowMa1;
		mWindowMa2 = windowMa2;
		mPowerCutoff = (float)powerCutoff;
		
		maValues = new float[4];
		ma = new MovingAverageTD[] { new MovingAverageTD(mWindowMa1),
				new MovingAverageTD(mWindowMa1),
				new MovingAverageTD(mWindowMa2) };
		asp = new CumulativeSignalPowerTD();
		stepDetected = false;
		signalPowerCutoff = true;
	}

	/*
	 * Obal pro data, které se pak dávají StepDetectionDemu
	 */
	public class MovingAverageStepDetectorState {
		float[] values;
		public boolean[] states;
		double duration;

		/*
		 * @param values 0 - pùvodní data, 1 - 1x filtrované MA, vyhlazený signál (okno 0.2), 2 - 2x filtrované MA, vážený prùmìr (okno 1), 3 - Cumulative Signal Power
		 */
		MovingAverageStepDetectorState(float[] values, boolean[] states) {
			this.values = values;
			this.states = states;
		}
	}

	public MovingAverageStepDetectorState getState() {
		return new MovingAverageStepDetectorState(new float[] { maValues[0],
				maValues[1], maValues[2], maValues[3] }, new boolean[] {
				stepDetected, signalPowerCutoff });
	}

	public float getPowerThreshold() {
		return mPowerCutoff;
	}

	private void processAccelerometerValues(long timestamp, float[] values) {

		float value = (float) Math.hypot(values[1], values[2]); //sqrt(Y^2 + Z^2) without overflow
		value = value * value;

		// compute moving averages
		maValues[0] = value; //remember unfiltered data
		for (int i = 0; i < 3; i++) { //i=1 jinak si nezapamatujeme pùvodní data //i=0 tzn. filtrujeme 2x filtrem 0.2
			ma[i].push(timestamp, value);
			maValues[i] = (float) ma[i].getAverage();
			value = maValues[i];
		}

		// detect moving average crossover

		//znaménka rovnosti jsou invertované, protože i vykreslení grafu je naopak
		stepDetected = (prepreviousState <= previousState && previousState > maValues[1]) && maValues[1] > maValues[2]; //detekujeme krok, když je to minimální hodnota na grafu a je pod moving average

		// compute signal power

		asp.push(timestamp, maValues[1] - maValues[2]);
		maValues[3] = (float) asp.getValue();
		signalPowerCutoff = maValues[3] < mPowerCutoff;
		
		if (stepDetected) {
			asp.reset(); //ikdyž je krok falešný, musíme vyresetovat akumulaèní filtr
			maValues[3] = (float) asp.getValue(); //aktualizace dat po resetu
		}

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
		prepreviousState = previousState;
		previousState = maValues[1];

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

	/*
	 * (non-Javadoc)
	 * @see cz.muni.fi.sandbox.service.stepdetector.StepDetector#onSensorChanged(android.hardware.SensorEvent)
	 * 
	 * Values passed into onSensorChanged() are cloned before they are assigned the class
	 * member data. This is because the SensorEvent object that is passed to onSensorChanged() may be
     * reused on subsequent calls. The use of clone() is needed to avoid the values getting overridden as
	 * the array points to a reference.
	 */
	@Override
	public void onSensorChanged(SensorEvent event) {
		// Log.d(TAG, "sensor: " + sensor + ", x: " + values[0] + ", y: " +
		// values[1] + ", z: " + values[2]);
			if ((event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) || (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)) {
				processAccelerometerValues(event.timestamp, event.values.clone());
			}
	}
}
