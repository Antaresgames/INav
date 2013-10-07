package cz.muni.fi.sandbox.service.orientation;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

public class CompassActivity extends Activity {

	private final String TAG = "CompassActivity";
	private SensorManager mSensorManager;
	private CompassView mView;
	private Compass mCompass;
	private SharedPreferences mSharedPrefs;
	private String mCompassType = null;
	
	
	public void setCompassType(String compassType) {
		mCompassType = compassType;
	}
	
	/**
	 * Initialization of the Activity after it is first created. Must at least
	 * call {@link android.app.Activity#setContentView setContentView()} to
	 * describe what is to be displayed in the screen.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Be sure to call the super class.
		super.onCreate(savedInstanceState);
		
		Log.d(TAG, "onCreate");
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		if (mCompassType == null)
			mCompassType = mSharedPrefs.getString("compass_type_preference", "default"); 			
		mCompass = Compass.compassFactory(mCompassType, mSensorManager);		
		mCompass.updatePreferences(mSharedPrefs);
		
		mView = new CompassView(this);
		mView.setCompass(mCompass);
		setContentView(mView);
	}
	
	@Override
	protected void onResume() {
		Log.d(TAG, "onResume");
		super.onResume();
		mCompass.start();
	}
	
	@Override
	protected void onStop() {
		Log.d(TAG, "onStop");
		super.onStop();
		mCompass.stop();
	}
}
