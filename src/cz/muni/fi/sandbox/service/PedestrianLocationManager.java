package cz.muni.fi.sandbox.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import cz.muni.fi.sandbox.buildings.Building;
import cz.muni.fi.sandbox.navigation.PositionModel;
import cz.muni.fi.sandbox.navigation.PositionModelUpdateListener;
import cz.muni.fi.sandbox.service.motion.MotionProvider;
import cz.muni.fi.sandbox.service.particle.ParticlePosition;
import cz.muni.fi.sandbox.service.wifi.BuildingFingerprintAdaptor;
import cz.muni.fi.sandbox.service.wifi.RssFingerprintController;
import cz.muni.fi.sandbox.utils.Logs;
import cz.muni.fi.sandbox.utils.geographical.SjtskLocation;
import cz.muni.fi.sandbox.utils.geographical.Wgs84Location;

public class PedestrianLocationManager implements PositionModelUpdateListener {
	
	private static final String TAG = "PedestrianLocationManager";
	
	public static final String WIFI = "wifi";
	public static final String STEP = "step";
	
	private String mNaviMethodPref = "sensor";
	
	
	private Set<PedestrianLocationListener> mListeners;
	private ParticlePosition mPositionModel;
	
	private MotionProvider mPositionProvider;
	
	private LocationManager mLocationManager;
	private SystemLocationListener mLocationListener;
	private Logs mLogs;
	
	private Building mBuilding;
	private String mBuildingName = "", mBuildingPath = "indoor";
	private Context mContext;
	private boolean mPaused;
	
	private boolean mUseWifiLocalization;
	
	private RssFingerprintController mRssController;
	private BuildingFingerprintAdaptor mFingerprintsAdaptor;
	

	protected static final long GPS_UPDATE_TIME_INTERVAL = 3000; // millis
	protected static final float GPS_UPDATE_DISTANCE_INTERVAL = 0; // meters
	
	public PedestrianLocationManager(Context context) {
		
		mContext = context;
		
		mPositionModel = new ParticlePosition(0, 0, 1, 0, this, PreferenceManager.getDefaultSharedPreferences(context));
		
		mBuilding = new Building();
		
		mFingerprintsAdaptor = new BuildingFingerprintAdaptor();
		mFingerprintsAdaptor.setBuilding(mBuilding);
		mRssController = new RssFingerprintController(mContext, null, null, null, mFingerprintsAdaptor);
		mRssController.addPositionUpdateListener(mPositionModel);
		
		mListeners = new HashSet<PedestrianLocationListener>();
		mLogs = new Logs();
		
		// Acquire a reference to the system Location Manager
		mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
	
		mLocationListener = new SystemLocationListener();
		
		replacePositionProvider(mNaviMethodPref);
		
		
	}
	
	public void requestLocationUpdates(String provider, long minTime, float minDistance, PedestrianLocationListener listener) {
		mListeners.add(listener);
	}
	
	
	public void removeUpdates(PedestrianLocationListener listener) {
		mListeners.remove(listener);
	}
	
	public PositionModel getPositionModel() {
		return mPositionModel;
	}
	
	public RssFingerprintController getWifiModel() {
		return mRssController;
	}
	
	public Collection<PedestrianLocationListener> getPedestrianListeners() {
		return mListeners;
	}

	
	public void start() {
		
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				GPS_UPDATE_TIME_INTERVAL, GPS_UPDATE_DISTANCE_INTERVAL,
				mLocationListener);
		mLocationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
		
		mPositionProvider.resume();
		mRssController.onResume();
		
		if (mUseWifiLocalization) {
			mRssController.localize();
		}
	}
	
	public void stop() {
		
		mLocationManager.removeUpdates(mLocationListener);
		
		mPositionProvider.stop();
		mRssController.onPause();
	}
	
	
	private void replacePositionProvider(String providerType) {
		if (mPositionProvider != null) {
			mPositionProvider.stop();
		}
		mPositionProvider = MotionProvider
				.providerFactory(providerType, mContext);
		mPositionProvider.setPaused(mPaused);
		mPositionModel.setPositionProvider(mPositionProvider);
	}
	
	
	
	public void recachePreferences(SharedPreferences sharedPrefs) {
		Log.d(TAG, "recaching preferences");
		mRssController.updatePreferences(sharedPrefs);
		mUseWifiLocalization = sharedPrefs.getBoolean(
				"use_wifi_for_localization_preference", false);
		String oldNaviMethod = mNaviMethodPref;
		mNaviMethodPref = sharedPrefs.getString(
				"navigation_method_preference", "");
		if (!mNaviMethodPref.equals(oldNaviMethod)) {
			Log.d(TAG, "replacing position provider \"" + oldNaviMethod
					+ "\" with \"" + mNaviMethodPref + "\"");
			replacePositionProvider(mNaviMethodPref);
		}
		
		String newBuildingName = sharedPrefs.getString(
				"building_plan_name_preference", "");
		mBuildingPath = sharedPrefs.getString(
				"data_root_dir_preference", "indoor");
		if (mBuildingPath.equals("")) {
			mBuildingPath += "plans";
		} else {
			mBuildingPath += "/plans";
		}
		if (!mBuildingName.equals(newBuildingName)) {
			mBuildingName = newBuildingName;
			new BuildingLoadingRunnable().run();
			//startBuildingLoadingThread();
		}
		mPositionModel.updatePreferences(sharedPrefs);
		mPositionProvider.updatePreferences(sharedPrefs);
	}
	
	
	

	public void resetCompass() {
		mPositionProvider.reset();
	}
	
	
	public void setPaused(boolean paused) {
		mPositionProvider.setPaused(paused);
	}
	
	
	Building loadBuilding() {
		
		Building building = Building.factory(mBuildingName, mBuildingPath);
		return building;
	}
	
	void startBuildingLoadingThread() {
		new Thread(new BuildingLoadingRunnable()).start();
	}

	private class BuildingLoadingRunnable implements Runnable {
		public void run() {
			mBuilding = loadBuilding();
			mFingerprintsAdaptor.setBuilding(mBuilding);
			notifyOnBuildingChanged(mBuilding);
		}
	}
	
	
	private class SystemLocationListener implements LocationListener {
		public void onLocationChanged(Location location) {
			mLogs.add("onProviderEnabled: " + location);
			Log.d(TAG, "onProviderEnabled: " + location);
			processLocation(location);
		}
		
		public void processLocation(Location location) {
			Wgs84Location wgs84 = new Wgs84Location(location.getLatitude(),
					location.getLongitude());
			SjtskLocation sjtskLoc = SjtskLocation.convert(wgs84);
			mLogs.add("absolute location update: " + sjtskLoc);
			Log.d(TAG, "absolute location update: " + sjtskLoc);
			// TODO: do something about the location
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			mLogs.add("onStatusChanged: provider = " + provider + ", status = "
					+ status);
			Log.d(TAG, "onStatusChanged: provider = " + provider
					+ ", status = " + status);
		}

		public void onProviderEnabled(String provider) {
			mLogs.add("onProviderEnabled: " + provider);
			Log.d(TAG, "onProviderEnabled: " + provider);

		}

		public void onProviderDisabled(String provider) {
			mLogs.add("onProviderDisabled: " + provider);
			Log.d(TAG, "onProviderDisabled: " + provider);
		}
		
	}


	private void notifyOnLocationChanged(PedestrianLocation location) {
		for (PedestrianLocationListener listener: getPedestrianListeners()) {
			listener.onLocationChanged(location);
		}
	}
	
	private void notifyOnBuildingChanged(Building building) {
		for (PedestrianLocationListener listener: getPedestrianListeners()) {
			listener.onBuildingChanged(building);
		}
	}
	
	@Override
	public void updatePosition(PositionModel position) {
		PedestrianLocation location = new PedestrianLocation(position);
		notifyOnLocationChanged(location);
	}

	@Override
	public void updateHeading(PositionModel position) {
		PedestrianLocation location = new PedestrianLocation(position);
		notifyOnLocationChanged(location);
		
	}
	
}
