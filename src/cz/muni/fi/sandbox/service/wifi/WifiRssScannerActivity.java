package cz.muni.fi.sandbox.service.wifi;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import cz.muni.fi.sandbox.R;
import cz.muni.fi.sandbox.navigation.LocationAssignmentAuthority;
import cz.muni.fi.sandbox.navigation.SetPositionListener;
import cz.muni.fi.sandbox.service.wifi.RssFingerprintController.State;

public class WifiRssScannerActivity extends Activity implements RssFingerprintControllerStateChangeListener {

	private static final String TAG = "WifiRssiActivity";
	private WifiRssScannerView mView;
	private RssFingerprintController mRssController;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Be sure to call the super class.
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");

		mView = new WifiRssScannerView(this);
		mRssController = new RssFingerprintController(this, new LocationAssignmentAuthority() {
			private int counter = 0;
			@Override
			public void requestSetPosition(SetPositionListener spl) {
				spl.setPosition(counter, 0, 0);
				counter++;
			}
		}, mView, this, new IGetsYouRssFingerprints() {			
			private WifiLayerModel mFingerprints = new WifiLayerModel("rss_fingerprints.txt");
			@Override
			public WifiLayerModel getRssFingerprints() {
				return mFingerprints;
			}
		});
		
		setContentView(mView);
		
	}
	
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
		// mPositionProvider.resume();
		mRssController.onResume();
	}

	
	@Override
	protected void onPause() {
		super.onStop();
		Log.d(TAG, "onPause");
		mRssController.onPause();
	}
	
	
	private Menu mMenu;
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	
		Log.d(TAG, "onCreateOptionsMenu");
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.rss_menu, menu);
		mMenu = menu;
		
		updateLocalization(menu.findItem(R.id.localization));
		updateMeasurement(menu.findItem(R.id.measurement));
		updateClearDatabase(menu.findItem(R.id.clear_database));

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d(TAG, "onOptionsItemSelected");
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.measurement:
			mRssController.measure();
			break;

		case R.id.localization:
			mRssController.localize();
			break;
			
		case R.id.clear_database:
			mRssController.clearDatabase();

		default:
			return super.onOptionsItemSelected(item);
		}
		

		return true;
	}
	
	
	private void updateMeasurement(MenuItem measurementItem) {

		if (mRssController.getState() == RssFingerprintController.State.IDLE) {
			measurementItem.setEnabled(true);
			measurementItem.setTitle("Add fingerprint");
		} else if (mRssController.getState() == RssFingerprintController.State.MEASURING) {
			measurementItem.setTitle("Stop measuring");
		} else if (mRssController.getState() == RssFingerprintController.State.FINDING) {
			measurementItem.setEnabled(false);
		}
	}

	private void updateLocalization(MenuItem localizationItem) {

		if (mRssController.getState() == RssFingerprintController.State.IDLE) {
			localizationItem.setEnabled(true);
			localizationItem.setTitle("Find location");
		} else if (mRssController.getState() == RssFingerprintController.State.FINDING) {
			localizationItem.setTitle("Stop finding");
		} else if (mRssController.getState() == RssFingerprintController.State.MEASURING) {
			localizationItem.setEnabled(false);
		}
	}
	
	private void updateClearDatabase(MenuItem clearDatabaseItem) {

		if (mRssController.getState() == RssFingerprintController.State.IDLE) {
			clearDatabaseItem.setEnabled(true);
		} else if (mRssController.getState() == RssFingerprintController.State.FINDING) {
			clearDatabaseItem.setEnabled(false);
		} else if (mRssController.getState() == RssFingerprintController.State.MEASURING) {
			clearDatabaseItem.setEnabled(false);
		}
	}

	@Override
	public void stateChange(State mState) {
		
		if (mMenu != null) {
			updateLocalization(mMenu.findItem(R.id.localization));
			updateMeasurement(mMenu.findItem(R.id.measurement));
			updateClearDatabase(mMenu.findItem(R.id.clear_database));
		}
	}
}
