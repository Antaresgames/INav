package cz.muni.fi.sandbox.service.wifi;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import cz.muni.fi.sandbox.navigation.LocationAssignmentAuthority;
import cz.muni.fi.sandbox.navigation.SetPositionListener;
import cz.muni.fi.sandbox.utils.geometric.Point2D;

public class RssFingerprintController implements SetPositionListener {

	public enum State {
		IDLE, MEASURING, FINDING
	};

	private static final String TAG = "RssFingerprintController";

	private static final long COLLECTION_TIME = 30;
	private static final long NANO = (long)Math.pow(10, 9);
	private static final double DEFAULT_GRID_SPACING = 2.0;

	private State mState;
	private List<ScanResult> mLastScanResults;
	private WifiManager mWifiManager;
	private RssFingerprint currentFingerprint;
	private WifiLogger mLogger;
	private LocationAssignmentAuthority mLocationAssignment;
	private Context mContext;
	private BroadcastReceiver mBroadcastReceiver;
	private RssFingerprintWriter mWriter;
	private RssFingerprintControllerStateChangeListener mStateChangeListener;
	private Set<WifiPositionUpdateListener> mPositionUpdateListeners;
	private IGetsYouRssFingerprints mFingerprintAdaptor;
	private boolean interactiveLocalization = false;
	private long mMeasurementStart;


	private static final String DEFAULT_LOCALIZATION_METRIC = "identity";
	private static final String DEFAULT_LOCATION_CANDIDATE = "nearest_neighbor";
	private static final int DEFAULT_FINGERPRINT_TIME = 30;
	
	private String mLocalizationMetric = "";
	private String mLocationCandidateHeuristic = "";
	private int mFingerprintRecordingTime = DEFAULT_FINGERPRINT_TIME;
	
	
	
	public RssFingerprintController(Context context,
			LocationAssignmentAuthority locAuthority, WifiLogger logger,
			RssFingerprintControllerStateChangeListener stateChangeListener,
			IGetsYouRssFingerprints fingerprintAdaptor) {

		mState = State.IDLE;
		mContext = context;
		mLocationAssignment = locAuthority;
		mLogger = logger;
		mPositionUpdateListeners = new HashSet<WifiPositionUpdateListener>();
		mWriter = new RssFingerprintWriter("rss_fingerprints.txt");
		mStateChangeListener = stateChangeListener;
		mFingerprintAdaptor = fingerprintAdaptor;

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		mBroadcastReceiver = new BroadcastReceiver() {
			public void onReceive(Context c, Intent i) {
				scanResultsAvailable();
			}
		};

		mWifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		

	}
	
	public void setStateChangeListener(
			RssFingerprintControllerStateChangeListener mStateChangeListener) {
		this.mStateChangeListener = mStateChangeListener;
	}
	
	public void setLocationAssignment(
			LocationAssignmentAuthority mLocationAssignment) {
		this.mLocationAssignment = mLocationAssignment;
	}
	
	public void setFingerprintAdaptor(
			IGetsYouRssFingerprints mFingerprintAdaptor) {
		this.mFingerprintAdaptor = mFingerprintAdaptor;
	}
	
	public void setLogger(WifiLogger mLogger) {
		this.mLogger = mLogger;
	}
	
	public void updatePreferences(SharedPreferences prefs) {

		if (prefs == null) {
			mLocalizationMetric = DEFAULT_LOCALIZATION_METRIC;
			mLocationCandidateHeuristic = DEFAULT_LOCATION_CANDIDATE;
			mFingerprintRecordingTime = DEFAULT_FINGERPRINT_TIME;

		} else {
			mLocalizationMetric = prefs.getString(
					"wifi_metric_preference", DEFAULT_LOCALIZATION_METRIC);

			mLocationCandidateHeuristic = prefs.getString(
					"wifi_candidate_preference",
					DEFAULT_LOCATION_CANDIDATE);

			try {
				mFingerprintRecordingTime = Integer.valueOf(prefs.getString(
						"fingerprint_recording_time_preference",
						String.valueOf(DEFAULT_FINGERPRINT_TIME)));
			} catch(NumberFormatException e) {
				e.printStackTrace();
				mFingerprintRecordingTime = DEFAULT_FINGERPRINT_TIME;
			}
		}

	}

	
	public void addPositionUpdateListener(WifiPositionUpdateListener positionUpdateListener) {
		mPositionUpdateListeners.add(positionUpdateListener);
	}
	
	public State getState() {
		return mState;
	}
	
	public void setState(State state) {
		mState = state;
		if (mStateChangeListener != null)
			mStateChangeListener.stateChange(mState);
	}
	
	public void clearDatabase() {
		Log.d(TAG, "clearDatabase:");
		if (getFingerprintSet() != null)
			getFingerprintSet().clear();
		mWriter.delete();
	}
	
	public void localize() {
		Log.d(TAG, "localize:");
		if (mState == State.IDLE) {
			startLocalization();
		} else if (mState == State.FINDING) {
			stopLocalization();
		} else {
			Log.e(TAG, "invalid operation: localize");
		}
	}
	
	public void measure() {
		Log.d(TAG, "measure:");
		if (mState == State.IDLE) {
//			 startMeasurement();
			mLocationAssignment.requestSetPosition(this);
		} else if (mState == State.MEASURING) {
			stopMeasurement();
		} else {
			Log.e(TAG, "invalid operation: measure");
		}
	}
	
	private double getGridSpacing() {
		double gridSpacing = DEFAULT_GRID_SPACING;
		if (getFingerprintSet() != null)
			getFingerprintSet().getGridSpacing();
		return gridSpacing;
	}
	
	
	@Override
	public void setPosition(float x, float y, int area) {

		Log.d(TAG, "setPosition(x = " + x + ", " + y + ")");
		double gridSpacing = getGridSpacing();
		double posX = (gridSpacing * Math.round(x / gridSpacing));
		double posY = (gridSpacing * Math.round(y / gridSpacing));
		Log.d(TAG, "position rounded to (x = " + posX + ", " + posY + ")");
		currentFingerprint = new RssFingerprint();
		currentFingerprint.setLocation(new Point2D(posX, posY));
		startMeasurement();
	}
	
	private void startMeasurement() {
		if (mWifiManager.startScan()) {
			setState(State.MEASURING);
			if (mLogger != null)
				mLogger.pushMessage("wifi scan started");
		} else {
			Log.e(TAG, "wifi scan failed to start");
		}
	}
	
	private void stopMeasurement() {

		if (mLogger != null)
			mLogger.pushMessage("wifi scan stopped");
		Log.d(TAG, "new location fingerprint at location = "
				+ currentFingerprint.getLocation());
		if (mLogger != null) {
			mLogger.pushMessage("new location fingerprint at location = "
				+ currentFingerprint.getLocation());
		}
		getFingerprintSet().add(currentFingerprint);
		mWriter.appendFingerprint(currentFingerprint);
		stopScan();
		setState(State.IDLE);
	}
	
	private void startLocalization() {
		Log.d(TAG, "startLocalization");
		currentFingerprint = new RssFingerprint();
		if (mWifiManager.startScan()) {
			setState(State.FINDING);
			mMeasurementStart = System.nanoTime();
			if (mLogger != null)
				mLogger.pushMessage("location search started");
		} else {
			Log.e(TAG, "wifi scan failed to start");
		}
	}
	
	private void stopLocalization() {
		RssFingerprint match = getFingerprintSet().findNearest(currentFingerprint, mLocationCandidateHeuristic);
		ProbabilityMap probMap = getFingerprintSet().getProbabilityMap(currentFingerprint, mLocalizationMetric);
		if (match != null) {
			Log.d(TAG, "matching location found at = " + match.getLocation());
			if (mLogger != null) {
				mLogger.pushMessage("matching location found at = "
						+ match.getLocation());
				mLogger.bestMatchFingerprint(currentFingerprint, match);
			}
			if (mPositionUpdateListeners != null) {
				Log.d(TAG, "stopLocalization: notifying position changed");
				notifyWifiPositionChanged(0, (float)match.getLocation().getX(), (float)match.getLocation().getY());
			}
		} else {
			Log.d(TAG, "no matching location found");
			if (mLogger != null)
				mLogger.pushMessage("no matching location found");
		}
		
		if (mPositionUpdateListeners != null) {
			Log.d(TAG, "stopLocalization: notifying probability map changed");
			notifyProbabilityMapChanged(probMap);
		}
		
		stopScan();
		setState(State.IDLE);
	}
	
	private void notifyProbabilityMapChanged(ProbabilityMap probMap) {
		for (WifiPositionUpdateListener listener: mPositionUpdateListeners)
			listener.probabilityMapChanged(probMap);
	}
	
	
	private void notifyWifiPositionChanged(float heading, float x, float y) {
		for (WifiPositionUpdateListener listener: mPositionUpdateListeners)
			listener.wifiPositionChanged(heading, x, y);
	}
	
	
	private void scanResultsAvailable() {

		synchronized(this) {
			Log.d(TAG, "scanResultsAvailable");
			
			if (currentFingerprint != null) {
				// Returns a <list> of scanResults
				//writer.openAppend();
				mLastScanResults = mWifiManager.getScanResults();
				for (ScanResult result : mLastScanResults) {
					currentFingerprint.add(result.BSSID, result.level);
					//writer.writeln(result.BSSID + " " + String.valueOf(result.level));
				}
				//writer.close();
			}
			
			Log.d(TAG, "currentFingerprint = " + currentFingerprint);
			
			if (mLogger != null)
				mLogger.newFingerprintAvailable(currentFingerprint);
			
			Log.d(TAG, "time = " + (double)(System.nanoTime() - mMeasurementStart) / NANO);
			if (getState() == State.FINDING && !interactiveLocalization) {
				//if (System.nanoTime() - mMeasurementStart > COLLECTION_TIME * NANO ) {
					stopLocalization();
					startLocalization();
				// }
			} else {
				startScan();
			}
		}

	}
	
	private void startScan() {
		if (mWifiManager.startScan()) {
			Log.d(TAG, "startScan: scan started");
		} else {
			Log.e(TAG, "wifi scan failed to start");
			currentFingerprint = null;
			setState(State.IDLE);
		}
	}
	
	private void stopScan() {
		currentFingerprint = null;
		
	}
	
	public void setFingerprintWriter(RssFingerprintWriter writer) {
		mWriter = writer;
	}
	
	public void onResume() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		mContext.registerReceiver(mBroadcastReceiver, intentFilter);
		receiverRegistered = true;
	}
	
	private boolean receiverRegistered = false;
	
	public void onPause() {
		Log.d(TAG, "onPause:");
		
		if (getState() == RssFingerprintController.State.MEASURING) {
			measure();
		} else if (getState() == RssFingerprintController.State.FINDING) {
			localize();
		}
		if (receiverRegistered)
			mContext.unregisterReceiver(mBroadcastReceiver);
		
		receiverRegistered  = false;
	}
	
	public WifiLayerModel getFingerprintSet() {
		return mFingerprintAdaptor.getRssFingerprints();
	}
	
}
