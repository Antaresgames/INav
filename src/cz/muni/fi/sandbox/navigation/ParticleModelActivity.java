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
import cz.muni.fi.sandbox.Preferences;
import cz.muni.fi.sandbox.R;
import cz.muni.fi.sandbox.buildings.Building;
import cz.muni.fi.sandbox.service.motion.MotionProvider;
import cz.muni.fi.sandbox.service.particle.ParticlePosition;

/**
 * 
 * @author Michal Holcik
 *
 */
public class ParticleModelActivity extends Activity {

	private MotionProvider mPositionProvider;
	private MapView mNaviView;
	private final String TAG = "MapActivity";
	private SharedPreferences mSharedPrefs;
	private String mNaviMethodPref = "sensor";
	// private List<Position> positions = new LinkedList<Position>();
	private PositionModel mPosition;
	private Building mBuilding;
	private String mBuildingName, mBuildingPath;

	/**
	 * 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate new text");

		// Be sure to call the super class.
		super.onCreate(savedInstanceState);

		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		mBuildingName = "not_loaded"; //mSharedPrefs.getString("building_plan_name_preference", "");
		mBuildingPath = mSharedPrefs.getString(
				"building_plan_directory_preference", "");
		
		
		mBuilding = new Building();
		// mBuilding = Building.factory(mBuildingName, mBuildingPath);
		
		
		mNaviView = new MapView(this);

		// mPosition = new SimplePosition((PositionModelUpdateListener)mNaviView);
		mPosition = new ParticlePosition(0, 0, 1, 0, mNaviView, mSharedPrefs);
		mPosition.setBuilding(mBuilding);
		mNaviView.setPosition(mPosition.getRenderer());

		mNaviView.setBuilding(mBuilding);

		replacePositionProvider(mNaviMethodPref);

		setContentView(mNaviView);

	}

	private void replacePositionProvider(String providerType) {
		if (mPositionProvider != null) {
			mPositionProvider.stop();
		}
		mPositionProvider = MotionProvider
				.providerFactory(providerType, this);
		mPositionProvider.setPaused(mPaused);
		mPosition.setPositionProvider(mPositionProvider);
	}

	@Override
	protected void onResume() {
		Log.d(TAG, "onResume");
		super.onResume();
		mNaviView.recachePreferences();
		recachePreferences();
				
	}
	
	protected void afterResume() {
		mPositionProvider.resume();
	}
	

	@Override
	protected void onStop() {
		Log.d(TAG, "onStop");
		super.onStop();
		mPositionProvider.stop();
	}

	private void recachePreferences() {
		Log.d(TAG, "recaching preferences");
		String oldNaviMethod = mNaviMethodPref;
		mNaviMethodPref = mSharedPrefs.getString(
				"navigation_method_preference", "");
		if (!mNaviMethodPref.equals(oldNaviMethod)) {
			Log.d(TAG, "replacing position provider \"" + oldNaviMethod
					+ "\" with \"" + mNaviMethodPref + "\"");
			replacePositionProvider(mNaviMethodPref);
		}
		
		String newBuildingName = mSharedPrefs.getString(
				"building_plan_name_preference", "");
		mBuildingPath = mSharedPrefs.getString(
				"building_plan_directory_preference", "");
		if (!mBuildingName.equals(newBuildingName)) {
			mBuildingName = newBuildingName;
			startBuildingLoadingThread();
			mPosition.updatePreferences(mSharedPrefs);
			mPositionProvider.updatePreferences(mSharedPrefs);
		} else {
			mPosition.updatePreferences(mSharedPrefs);
			mPositionProvider.updatePreferences(mSharedPrefs);
			afterResume();
		}
		
		
	}


	Building loadBuilding() {
		Building building = Building.factory(mBuildingName, mBuildingPath);
//		building.optimize();
		return building;
	}
	
	void setBuilding(Building building) {
		mBuilding = building;
		mNaviView.setBuilding(building);
		mPosition.setBuilding(building);
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		
		updatePauseMenuItem(menu.findItem(R.id.pause));
		//updateDebugMenuItem(menu.findItem(R.id.debug));
		//updateCalPosMenuItem(menu.findItem(R.id.calibrate_position));

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
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

			/*
		case R.id.debug:
			debug();
			updateDebugMenuItem(item);
			return true;

		case R.id.calibrate_position:
			calibratePosition();
			updateCalPosMenuItem(item);
			return true;

		case R.id.graph:
			showGraph();
			return true;

			*/

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
			return new AlertDialog.Builder(ParticleModelActivity.this)
					.setTitle("Select level")
					.setOnCancelListener(new OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
							ParticleModelActivity.this.removeDialog(LEVEL_SELECT);
						}
					})
					.setItems(
							mBuilding.getFloorLabels(),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									mBuilding.areaChanged(mBuilding.getFloorIndex(which));
									mNaviView.cropWalls();
									mNaviView.invalidate();
									ParticleModelActivity.this.removeDialog(LEVEL_SELECT);
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
		mPositionProvider.setPaused(mPaused);

	}

	private void resetCompass() {
		Log.d(TAG, "resetCompass()");
		mPositionProvider.reset();
		
	}
	
	private void setPosition() {
		Log.d(TAG, "setPosition()");
		mNaviView.requestSetPosition(new PositionModelSetPositionListener(mPosition));
	}


	private void updatePauseMenuItem(MenuItem pauseItem) {
		if (!mPaused) {
			pauseItem.setTitle(R.string.pause);
		} else {
			pauseItem.setTitle(R.string.resume);
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
			afterResume();
		}
	}

	void startBuildingLoadingThread() {
		dialog = ProgressDialog.show(ParticleModelActivity.this, "", 
                "Loading building plans.\nThis takes a long time...", true);
		new Thread(new BuildingLoadingRunnable()).start();
	}

	
	private class BuildingLoadingRunnable implements Runnable {
		public void run() {
			mHandler.post(new SetNewBuildingRunnable(loadBuilding()));
			dialog.dismiss();
			
		}
	}
	
	private void showHelp() {
		Log.d(TAG, "showHelp method");
		
		File readmeFile = new File("/mnt/sdcard/mag_deflectio.txt");
		Intent i = new Intent();
		i.setAction(android.content.Intent.ACTION_VIEW);
		i.setDataAndType(Uri.fromFile(readmeFile), "text/plain");
		startActivity(i);
		
	}
}
