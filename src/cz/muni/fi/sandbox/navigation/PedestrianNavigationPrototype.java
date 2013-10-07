package cz.muni.fi.sandbox.navigation;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import cz.muni.fi.sandbox.Preferences;
import cz.muni.fi.sandbox.R;
import cz.muni.fi.sandbox.buildings.Building;
import cz.muni.fi.sandbox.service.PedestrianLocation;
import cz.muni.fi.sandbox.service.PedestrianLocationListener;
import cz.muni.fi.sandbox.service.PedestrianLocationManager;
import cz.muni.fi.sandbox.service.wifi.BuildingFingerprintAdaptor;
import cz.muni.fi.sandbox.service.wifi.DelaunayRenderer;
import cz.muni.fi.sandbox.service.wifi.IGetsYouRssFingerprints;
import cz.muni.fi.sandbox.service.wifi.RssFingerprint;
import cz.muni.fi.sandbox.service.wifi.RssFingerprintController;
import cz.muni.fi.sandbox.service.wifi.RssFingerprintControllerStateChangeListener;
import cz.muni.fi.sandbox.service.wifi.WifiFingerprintRenderer;
import cz.muni.fi.sandbox.service.wifi.WifiLayerModel;
import cz.muni.fi.sandbox.service.wifi.WifiLogger;
import cz.muni.fi.sandbox.service.wifi.RssFingerprintController.State;
import cz.muni.fi.sandbox.utils.AndroidFileUtils;
import cz.muni.fi.sandbox.utils.geometric.Point2D;

/**
 * 
 * @author Michal Holcik
 * 
 */
public class PedestrianNavigationPrototype extends Activity implements
		PedestrianLocationListener {

	private final String TAG = "PedestrianNavigationPrototype";
	protected SharedPreferences mSharedPrefs;
	protected MapView mNaviView;
	protected PedestrianLocationManager mLocationManager;

	protected PositionModel mPosition;
	protected Building mBuilding;
	protected RssFingerprintController mWifiModel;
	
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
	 * 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate new text");

		super.onCreate(savedInstanceState);

		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		mNaviView = new MapView(this);

		mLocationManager = new PedestrianLocationManager(this);

		mPosition = mLocationManager.getPositionModel();

		mNaviView.setPosition(mPosition.getRenderer());

		mLocationManager.requestLocationUpdates("", 0, 0, this);

		mWifiModel = mLocationManager.getWifiModel();
		mWifiModel
				.setStateChangeListener(new RssFingerprintControllerStateChangeListener() {
					@Override
					public void stateChange(State mState) {
						if (mMenu != null) {
							updateMeasurement(mMenu.findItem(R.id.measurement));
						}
						
						if (mWifiModel.getState() == RssFingerprintController.State.IDLE) {
							if (mStopMeasurementRunnable != null) {
								mHandler.removeCallbacks(mStopMeasurementRunnable);
								mStopMeasurementRunnable = null;
							}
						} else if (mWifiModel.getState() == RssFingerprintController.State.MEASURING) {
							if (mStopMeasurementRunnable == null) {
								mStopMeasurementRunnable = new StopMeasurementRunnable(mWifiModel, mMenu.findItem(R.id.measurement), "Stop measuring", 20, 1000);
								mStopMeasurementRunnable.run();
							}
						} else if (mWifiModel.getState() == RssFingerprintController.State.FINDING) {
							
						}
						
						mNaviView.postInvalidate();
					}
				});
		 mWifiModel.setLocationAssignment(mNaviView);

		mNaviView.addGridRenderer(new WifiFingerprintRenderer(
				new IGetsYouRssFingerprints() {

					@Override
					public WifiLayerModel getRssFingerprints() {
						if (mBuilding == null)
							return null;
						return mBuilding.getCurrentArea().getRssFingerprints();
					}
				}));
		mNaviView.addGridRenderer(new DelaunayRenderer(
				new IGetsYouRssFingerprints() {

					@Override
					public WifiLayerModel getRssFingerprints() {
						if (mBuilding == null)
							return null;
						return mBuilding.getCurrentArea().getRssFingerprints();
					}
				}));

		setContentView(mNaviView);

	}

	@Override
	protected void onResume() {
		Log.d(TAG, "onResume");
		super.onResume();
		mWifiModel.onResume();
		mNaviView.recachePreferences();
		mLocationManager.recachePreferences(mSharedPrefs);
		mLocationManager.start();

	}

	@Override
	protected void onPause() {
		Log.d(TAG, "onResume");
		super.onPause();
		mHandler.removeCallbacks(mStopMeasurementRunnable);
		mWifiModel.onPause();
	}

	@Override
	protected void onStop() {
		Log.d(TAG, "onStop");
		super.onStop();
		mLocationManager.stop();

		// setBuilding(null);

	}

	private void setBuilding(Building building) {
		mBuilding = building;
		mNaviView.setBuilding(building);
		mPosition.setBuilding(building);

		BuildingFingerprintAdaptor fingerprintsAdaptor = new BuildingFingerprintAdaptor();
		fingerprintsAdaptor.setBuilding(mBuilding);
		mWifiModel.setFingerprintAdaptor(fingerprintsAdaptor);
		mWifiModel.setFingerprintWriter(mBuilding.getCurrentArea().getRssFingerprintWriter());
	}

	 private Menu mMenu;
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);

		 mMenu = menu;

		updatePauseMenuItem(menu.findItem(R.id.pause));
		 updateMeasurement(menu.findItem(R.id.measurement));

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {

		case R.id.scan_code:
			startScanActivity();
			return true;

		case R.id.measurement:
			mWifiModel.measure();
			return true;

		case R.id.settings:
			showSettings();
			return true;

		case R.id.select_floor:
			levelSelect();
			return true;

		case R.id.pause:
			pause();
			updatePauseMenuItem(item);
			return true;

		case R.id.reset_compass:
			resetCompass();
			return true;

		case R.id.set_position:
			setPosition();
			return true;

		case R.id.help:
			showHelp();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public static final int PEDESTRIAN_NAVIGATION_SCAN_BARCODE = 0;
	public static final String SCAN_RESULT = "SCAN_RESULT";

	private void startScanActivity() {
		// Intent intent = new Intent(PedestrianNavigationPrototype2.this,
		// ScanCodeActivity.class);
		// startActivityForResult(intent, PEDESTRIAN_NAVIGATION_SCAN_BARCODE);

		Intent intent = new Intent("com.google.zxing.client.android.SCAN");
		startActivityForResult(intent, PEDESTRIAN_NAVIGATION_SCAN_BARCODE);
	}

	private void showSettings() {
		Log.d(TAG, "showSettings()");
		startActivity(new Intent(this, Preferences.class));
	}

	private static final int LEVEL_SELECT = 45;

	private void levelSelect() {
		Log.d(TAG, "levelSelect()");
		showDialog(LEVEL_SELECT);
	}

	@Override
	protected Dialog onCreateDialog(int id) {

		switch (id) {
		case LEVEL_SELECT:
			return new AlertDialog.Builder(PedestrianNavigationPrototype.this)
					.setTitle("Select level")
					.setOnCancelListener(new OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
							PedestrianNavigationPrototype.this
									.removeDialog(LEVEL_SELECT);
						}
					})
					.setItems(mBuilding.getFloorLabels(),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									mBuilding.areaChanged(mBuilding
											.getFloorIndex(which));
									mWifiModel.setFingerprintWriter(mBuilding.getCurrentArea().getRssFingerprintWriter());
									mNaviView.cropWalls();
									mNaviView.invalidateBackground();
									mNaviView.invalidate();
									PedestrianNavigationPrototype.this
											.removeDialog(LEVEL_SELECT);
								}
							}).create();
		}
		Log.e(TAG, "Unsupported dialog id");
		return null;
	}

	private boolean mPaused = false;

	private void pause() {
		Log.d(TAG, "pause()");
		mPaused = !mPaused;
		mLocationManager.setPaused(mPaused);

	}

	private void resetCompass() {
		Log.d(TAG, "resetCompass()");
		mLocationManager.resetCompass();

	}

	private void setPosition() {
		Log.d(TAG, "setPosition()");
		mNaviView.requestSetPosition(new PositionModelSetPositionListener(
				mPosition));
	}

	private void updatePauseMenuItem(MenuItem pauseItem) {
		if (!mPaused) {
			pauseItem.setTitle(R.string.pause);
		} else {
			pauseItem.setTitle(R.string.resume);
		}
	}

	private void updateMeasurement(MenuItem measurementItem) {

		if (mWifiModel.getState() == RssFingerprintController.State.IDLE) {
			measurementItem.setEnabled(true);
			measurementItem.setTitle("Add fingerprint");
		} else if (mWifiModel.getState() == RssFingerprintController.State.MEASURING) {
			measurementItem.setTitle("Stop measuring");
		} else if (mWifiModel.getState() == RssFingerprintController.State.FINDING) {
			measurementItem.setEnabled(false);
		}
	}

	ProgressDialog dialog;
	private Handler mHandler = new Handler();

	private class SetNewBuildingRunnable implements Runnable {
		Building building;

		SetNewBuildingRunnable(Building building) {
			this.building = building;
		}

		public void run() {
			setBuilding(building);
		}
	}

	public void postSetBuilding(Building building) {
		mHandler.post(new SetNewBuildingRunnable(building));
	}

	private void showHelp() {
		Log.d(TAG, "showHelp method");

		File readmeFile = AndroidFileUtils.getFileFromPath(mSharedPrefs
				.getString("data_root_dir_preference", "") + "readme.txt");
		Intent i = new Intent();
		i.setAction(android.content.Intent.ACTION_VIEW);
		i.setDataAndType(Uri.fromFile(readmeFile), "text/plain");
		startActivity(i);

	}

	@Override
	public void onLocationChanged(PedestrianLocation location) {
		mNaviView.invalidate();
		System.out
				.println("loc.changed: "
						+ (location.getPosition().getX() + mBuilding
								.getCurrentArea().originX)
						+ ", "
						+ (location.getPosition().getY() + mBuilding
								.getCurrentArea().originY));
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		mNaviView.invalidate();
	}

	@Override
	public void onBuildingChanged(Building building) {
		Log.d(TAG, "onBuildingChanged");
		postSetBuilding(building);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case PEDESTRIAN_NAVIGATION_SCAN_BARCODE:
				String code = data.getStringExtra(SCAN_RESULT);
				Toast.makeText(PedestrianNavigationPrototype.this,
						"Scanned code: " + code, Toast.LENGTH_LONG).show();
				System.out.println("current position: " + mPosition.getClass());

				Point2D point = mBuilding.getCurrentArea().getBarcodesModel()
						.getPoint(code);

				if (point != null) {
					mPosition.setPosition((float) (point.getX()),
							(float) (point.getY()),
							mBuilding.getCurrentAreaIndex());
					// mLocationManager.updatePosition(position);

					System.out.println("desired position: " + (point.getX())
							+ ", " + (point.getY()));
					System.out
							.println("desired position: "
									+ (point.getX() + mBuilding
											.getCurrentArea().originX)
									+ ", "
									+ (point.getY() + mBuilding
											.getCurrentArea().originY));
				} else {
					Toast.makeText(PedestrianNavigationPrototype.this,
							"Barcode not recognized", Toast.LENGTH_SHORT)
							.show();
				}

				break;

			default:
				break;
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}
}
