package cz.muni.fi.sandbox;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.util.Log;
import cz.muni.fi.sandbox.R;

public class Preferences extends PreferenceActivity {

	private final int STEP_CALIBRATION_REQUEST_CODE=0;
	private final String TAG = "Preferences";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.d(TAG, "onCreate");
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        
        findPreference("step_length_calibration").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(Preferences.this, StepCalibrationActivity.class);
				startActivityForResult(intent, STEP_CALIBRATION_REQUEST_CODE);
				return false;
			}
		});
        
        findPreference("accel_calibration").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(Preferences.this, AccelCalibrationActivity.class);
				startActivityForResult(intent, STEP_CALIBRATION_REQUEST_CODE);
				return false;
			}
		});
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	switch (requestCode) {
		case STEP_CALIBRATION_REQUEST_CODE:
			
			break;

		default:
			break;
		}
    }
}
