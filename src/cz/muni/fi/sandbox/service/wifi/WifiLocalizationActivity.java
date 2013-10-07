package cz.muni.fi.sandbox.service.wifi;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import cz.muni.fi.sandbox.Preferences;
import cz.muni.fi.sandbox.R;
import cz.muni.fi.sandbox.buildings.Building;
import cz.muni.fi.sandbox.navigation.MapView;
import cz.muni.fi.sandbox.service.wifi.RssFingerprintController.State;

public class WifiLocalizationActivity extends Activity implements
		RssFingerprintControllerStateChangeListener {

	private MapView mNaviView;
	private final String TAG = "WifiLocalizationActivity";
	private SharedPreferences mSharedPrefs;
	private Building mBuilding;
	private String mBuildingName, mBuildingPath;
	private RssFingerprintController mRssController;
	private WifiFingerprintRenderer mWifiGridRenderer;
	
	private BuildingFingerprintAdaptor mFingerprintsAdaptor;
	
	// delaunay stuff
	private DelaunayRenderer mDelaunayRenderer;
	private static final boolean SHOW_DELAUNAY_GRAPH = true;
	
    private Handler mHandler = new Handler();
    private Runnable mStopMeasurementRunnable;
    
	private class StopMeasurementRunnable implements Runnable {
		private final int mTick; // in milliseconds
		private int mCount; // in ticks
		private final String mTitle;
		private final MenuItem mItem;
		private final RssFingerprintController mController;

		public StopMeasurementRunnable(RssFingerprintController controller, MenuItem item, String title, int count, int tick) {
			mController = controller;
			mTick = tick;
			mCount = count;
			mTitle = title;
			mItem = item;
		}
		
		public void run() {
			
			if (mCount > 0) {
				if (mItem != null)
					mItem.setTitle(mTitle + " (" + mCount + ")");
				mCount--;
				
				// call itself again after one tick
	            mHandler.postDelayed(this, mTick);
			} else {
				mItem.setTitle(mTitle);
				mController.measure();	
			}
		}
	}

	
	
	/**
	 * onCreate method
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate new text");

		// Be sure to call the super class.
		super.onCreate(savedInstanceState);

		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		mBuildingName = mSharedPrefs.getString("building_plan_name_preference",
				"");
		mBuildingPath = mSharedPrefs.getString(
				"data_root_dir_preference", "indoor");
		if (mBuildingPath.equals("")) {
			mBuildingPath += "plans";
		} else {
			mBuildingPath += "/plans";
		}
		
		mBuilding = Building.factory(mBuildingName, mBuildingPath);
		System.out.println(mBuilding);

		mNaviView = new MapView(this);
		mNaviView.setBuilding(mBuilding);
		mFingerprintsAdaptor = new BuildingFingerprintAdaptor();
		mFingerprintsAdaptor.setBuilding(mBuilding);
		mRssController = new RssFingerprintController(this, mNaviView, null,
				this, mFingerprintsAdaptor);

		mWifiGridRenderer = new WifiFingerprintRenderer(
				mFingerprintsAdaptor);
		mRssController.addPositionUpdateListener(mWifiGridRenderer);
		mRssController.addPositionUpdateListener(mNaviView);
		mRssController.setFingerprintWriter(mBuilding.getCurrentArea().getRssFingerprintWriter());
		
		
		mNaviView.addGridRenderer(mWifiGridRenderer);
		mDelaunayRenderer = new DelaunayRenderer(mFingerprintsAdaptor);
		if (SHOW_DELAUNAY_GRAPH)
			mNaviView.addGridRenderer(mDelaunayRenderer);
		
		setContentView(mNaviView);

	}

	@Override
	protected void onResume() {
		Log.d(TAG, "onResume");
		super.onResume();
		mRssController.onResume();
		mNaviView.recachePreferences();
		recachePreferences();
	}

	@Override
	protected void onPause() {
		Log.d(TAG, "onStop");
		super.onPause();		
		mHandler.removeCallbacks(mStopMeasurementRunnable);
		mRssController.onPause();
	}
	
	@Override
	protected void onStop() {
		Log.d(TAG, "onStop");
		super.onStop();
	}

	private void recachePreferences() {
		Log.d(TAG, "recaching preferences");
		mRssController.updatePreferences(mSharedPrefs);
		String newBuildingName = mSharedPrefs.getString(
				"building_plan_name_preference", "");
		if (!mBuildingName.equals(newBuildingName)) {
			mBuildingName = newBuildingName;
			mBuildingPath = mSharedPrefs.getString(
					"data_root_dir_preference", "indoor");
			if (mBuildingPath.equals("")) {
				mBuildingPath += "plans";
			} else {
				mBuildingPath += "/plans";
			}
			mBuilding = Building.factory(mBuildingName, mBuildingPath);
			mNaviView.setBuilding(mBuilding);
			mFingerprintsAdaptor.setBuilding(mBuilding);
		}
	}

	private Menu mMenu;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		Log.d(TAG, "onCreateOptionsMenu");
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.wifi_menu, menu);
		mMenu = menu;
		
		updateLocalization(menu.findItem(R.id.localization));
		updateMeasurement(menu.findItem(R.id.measurement));
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d(TAG, "onOptionsItemSelected");
		// Handle item selection
		switch (item.getItemId()) {

		case R.id.settings:
			showSettings();
			return true;

		case R.id.select_bssid:
			selectBssid();
			return true;
		
		case R.id.select_floor:
			levelSelect();
			return true;
			
		case R.id.measurement:
			mRssController.measure();
			break;

		case R.id.localization:
			mRssController.localize();
			break;

		case R.id.clear_database:
			mRssController.clearDatabase();

		case R.id.help:
			showHelp();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}

		return true;
	}

	private static final int SELECT_BSSID = 45;
	private static final int LEVEL_SELECT = 46;

	private void selectBssid() {
		Log.d(TAG, "selectBssid()");
		showDialog(SELECT_BSSID);
	}
	
	private void levelSelect() {
		Log.d(TAG, "levelSelect()");
		showDialog(LEVEL_SELECT);
	}
	

	@Override
	protected Dialog onCreateDialog(int id) {

		switch (id) {
		case SELECT_BSSID:
			return new AlertDialog.Builder(WifiLocalizationActivity.this)
					.setTitle("Select BSSID")
					.setOnCancelListener(new OnCancelListener() {
						
						@Override
						public void onCancel(DialogInterface dialog) {
							Log.d(TAG, "onCancel: remove dialog");
							WifiLocalizationActivity.this.removeDialog(SELECT_BSSID);
						}
					})
					.setItems(
							mRssController.getFingerprintSet().getBssidList(),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									// getting the bssid list twice is non-atomic
									mWifiGridRenderer
											.setDisplayBssid(mRssController
													.getFingerprintSet()
													.getBssidList()[which]);
									mNaviView.postInvalidate();
									WifiLocalizationActivity.this.removeDialog(SELECT_BSSID);
								}
							}).create();
		case LEVEL_SELECT:
			return new AlertDialog.Builder(WifiLocalizationActivity.this)
					.setTitle("Select level")
					.setOnCancelListener(new OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
							WifiLocalizationActivity.this.removeDialog(LEVEL_SELECT);
						}
					})
					.setItems(
							mBuilding.getFloorLabels(),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									mBuilding.areaChanged(mBuilding.getFloorIndex(which));
									mRssController.setFingerprintWriter(mBuilding.getCurrentArea().getRssFingerprintWriter());
									mNaviView.cropWalls();
									mNaviView.invalidate();
									WifiLocalizationActivity.this.removeDialog(LEVEL_SELECT);
								}
							}).create();
		}
		Log.e(TAG, "Unsupported dialog id");
		return null;
	}
	
	
	
	
	private void showSettings() {
		Log.d(TAG, "showSettings()");
		startActivity(new Intent(this, Preferences.class));
	}

	private void showHelp() {
		Log.d(TAG, "showHelp()");
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
		
		if (mRssController.getState() == RssFingerprintController.State.IDLE) {
			if (mStopMeasurementRunnable != null) {
				mHandler.removeCallbacks(mStopMeasurementRunnable);
				mStopMeasurementRunnable = null;
			}
		} else if (mRssController.getState() == RssFingerprintController.State.MEASURING) {
			if (mStopMeasurementRunnable == null) {
				mStopMeasurementRunnable = new StopMeasurementRunnable(mRssController, mMenu.findItem(R.id.measurement), "Stop measuring", 20, 1000);
				mStopMeasurementRunnable.run();
			}
		} else if (mRssController.getState() == RssFingerprintController.State.FINDING) {
			
		}
		
		mNaviView.postInvalidate();
	}
}
