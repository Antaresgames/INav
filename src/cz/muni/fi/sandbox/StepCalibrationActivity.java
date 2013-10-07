package cz.muni.fi.sandbox;

import cz.muni.fi.sandbox.service.stepdetector.IStepListener;
import cz.muni.fi.sandbox.service.stepdetector.MovingAverageStepDetector;
import cz.muni.fi.sandbox.service.stepdetector.StepEvent;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class StepCalibrationActivity extends Activity implements SensorEventListener {
	private static final String TAG = "Step calibration";
	
	private MovingAverageStepDetector mStepDetector;
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	
	private TextView stepsCountView;
	
	private int stepsCountTemp = 0;
	private int calibrationNumber = 0;
	private final int CALIBRATION_COUNT = 3;
	private final double CALIBRATION_DISTANCE = 10.0d;
	private double[] stepLengthDetected = new double[CALIBRATION_COUNT];


	/**
	 * Initialization of the Activity after it is first created. Must at least
	 * call {@link android.app.Activity#setContentView setContentView()} to
	 * describe what is to be displayed in the screen.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Be sure to call the super class.
		super.onCreate(savedInstanceState);

		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mAccelerometer = getSensor(Sensor.TYPE_ACCELEROMETER, "accelerometer");
		
		// load parameters from configuration
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		double movingAverage1 = MovingAverageStepDetector.MA1_WINDOW;
		double movingAverage2 = MovingAverageStepDetector.MA2_WINDOW;
		double powerCutoff = MovingAverageStepDetector.POWER_CUTOFF_VALUE;
		if (prefs != null) {
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
		}
		
		mStepDetector = new MovingAverageStepDetector(movingAverage1, movingAverage2, powerCutoff);
		
		setContentView(R.layout.activity_calibrate_step);
		
		stepsCountView = (TextView)findViewById(R.id.stepsCountView);
		
		Button stopButton = (Button)findViewById(R.id.stopButton);
		stopButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mStepDetector.clearStepListeners();  
				
				stepLengthDetected[calibrationNumber-1] = CALIBRATION_DISTANCE / ((double)stepsCountTemp);
				
				if (calibrationNumber < CALIBRATION_COUNT) {
					startCalibration();
				} else {
					finishCalibration();
				}
			}
		});
		

		
		startCalibration();
	}

	private void startCalibration() {
		calibrationNumber++;
		stepsCountTemp = 0;
		stepsCountView.setText("0");
		
		AlertDialog dialog = new AlertDialog.Builder(this)
		.setTitle("Step-length Calibration "+calibrationNumber+"//"+CALIBRATION_COUNT)
		.setMessage("Hold your device in fixed position, click OK and walk 10 meters, then press the Stop button, please.")
		.setCancelable(false)
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mStepDetector.addStepListener(new IStepListener() {
					@Override
					public void onStepEvent(StepEvent event) {
						stepsCountTemp++;
						stepsCountView.setText(""+stepsCountTemp);
					}
				});
			}
		})
		.create();
		dialog.show();
	}
	
	private void finishCalibration() {
		
		double stepLengthSum = 0.0d;
		
		double stepLengthPowSum = 0.0d;
		for (int i=0; i<CALIBRATION_COUNT; i++) {
			double stepLength = stepLengthDetected[i];
			stepLengthSum += stepLength;
			stepLengthPowSum += stepLength * stepLength;
		}
		
		double averageStepLength = stepLengthSum / (double) CALIBRATION_COUNT;
		
		double stepLengthSpread =  Math.sqrt(
				(1.0d/(((double)CALIBRATION_COUNT)-1.0d))
				*
				(stepLengthPowSum - (CALIBRATION_COUNT*averageStepLength*averageStepLength))
				);
		
		Toast.makeText(StepCalibrationActivity.this, "average step length: "+averageStepLength+", spread: "+stepLengthSpread, Toast.LENGTH_LONG).show();
		
		Editor prefs = PreferenceManager.getDefaultSharedPreferences(StepCalibrationActivity.this).edit();
		prefs.putString("step_length_preference", String.valueOf((float)averageStepLength));
		prefs.putString("step_length_spread_preference", String.valueOf((float)stepLengthSpread));
		prefs.commit();
		
		finish();
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (mAccelerometer != null)
			mSensorManager.registerListener(this, mAccelerometer,
					SensorManager.SENSOR_DELAY_FASTEST);
		/*
		 * if (mMagnetometer != null) mSensorManager.registerListener(this,
		 * mMagnetometer, SensorManager.SENSOR_DELAY_FASTEST); if
		 * (mAccelerometer != null) mSensorManager.registerListener(mGraphView,
		 * mGyroscope, SensorManager.SENSOR_DELAY_FASTEST);
		 */

	}

	@Override
	protected void onStop() {
		mSensorManager.unregisterListener(this);
		super.onStop();
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
	public void onSensorChanged(SensorEvent event) {
		synchronized (this) {
			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				processAccelerometerEvent(event);
			}
		}

	}
	
	public void processAccelerometerEvent(SensorEvent event) {
		mStepDetector.onSensorChanged(event);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) { }
	
	
}
