package cz.muni.fi.sandbox.service.inertial;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class InertialActivity extends Activity {

	private InertialView mView;
	private final String TAG = "AccIntegrator";
	
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
		mView = new InertialView(this);
		setContentView(mView);
		
	}
	
	@Override
	protected void onResume() {
		Log.d(TAG, "onResume");
		super.onResume();
		mView.onResume();
		
	}

	@Override
	protected void onStop() {
		Log.d(TAG, "onStop");
		super.onStop();
		mView.onStop();
	}
}
