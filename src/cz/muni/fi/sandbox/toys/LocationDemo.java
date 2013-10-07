package cz.muni.fi.sandbox.toys;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import cz.muni.fi.sandbox.utils.geographical.SjtskLocation;
import cz.muni.fi.sandbox.utils.geographical.Wgs84Location;

/**
 * 
 */
public class LocationDemo extends Activity {
	/** Tag string for our debug logs */
	private static final String TAG = "LocationDemo";

	private GraphView mGraphView;

	private LocationManager mLocationManager;
	private DemoLocationListener mLocationListener;
	private MyGPSListener mGpsListener;

	private List<GpsSatellite> sats;
	private Logs logs;

	protected static final long GPS_UPDATE_TIME_INTERVAL = 3000; // millis
	protected static final float GPS_UPDATE_DISTANCE_INTERVAL = 0; // meters

	private class GraphView extends View {

		private float mWidth;
		private float mHeight;

		private Bitmap mBitmap;
		private Canvas mCanvas = new Canvas();
		private Paint mPaint = new Paint();

		public GraphView(Context context) {
			super(context);
			mPaint = new Paint();
			mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		}

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
			mCanvas.setBitmap(mBitmap);
			mCanvas.drawColor(Color.BLACK);

			mWidth = w;
			mHeight = h;
			super.onSizeChanged(w, h, oldw, oldh);

			logs.setSize((int) (mHeight - 100) / 10);
			Log.d(TAG, "onSizeChanged: w = " + mWidth + ", h = " + mHeight);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			Log.d(TAG, "onDraw:");

			synchronized (this) {

				if (mBitmap != null) {
					mPaint.setStyle(Paint.Style.FILL);
					mPaint.setColor(Color.WHITE);
					mCanvas.drawColor(Color.BLACK);

					if (sats != null) {
						int y = 20;
						mCanvas.drawText("gps sattelites:", 0, y, mPaint);
						y += 10;
						for (GpsSatellite sat : sats) {
							mCanvas.drawText(
									sat.toString() + " " + sat.getSnr(), 0, y,
									mPaint);
							y += 10;
						}
					} else {
						mCanvas.drawText("sats is null:", 0, 20, mPaint);
					}

					logs.draw(mCanvas);

				} else {
					Log.d(TAG, "mBitmap == null");
				}
				canvas.drawBitmap(mBitmap, 0, 0, null);

			}
		}

	}

	private class Logs {
		private Queue<String> logs;
		private int size;

		Logs() {
			logs = new LinkedList<String>();
		}

		void setSize(int size) {
			this.size = size;
			while (logs.size() > size) {
				logs.poll();
			}
		}

		void add(String line) {
			if (logs.size() == size)
				logs.poll();
			logs.add(line);
		}

		void draw(Canvas canvas) {
			Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint.setStyle(Paint.Style.FILL);
			paint.setColor(Color.WHITE);

			int y = 100;
			for (String line : logs) {
				canvas.drawText(line, 0, y, paint);
				y += 10;
			}
		}

	}

	// Define a listener that responds to location updates
	private class DemoLocationListener implements LocationListener {
		public void onLocationChanged(Location location) {
			// Called when a new location is found by the network location
			// provider.
			logs.add("onProviderEnabled: " + location);
			Log.d(TAG, "onProviderEnabled: " + location);
			processLocation(location);
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			logs.add("onStatusChanged: provider = " + provider + ", status = "
					+ status);
			Log.d(TAG, "onStatusChanged: provider = " + provider
					+ ", status = " + status);
		}

		public void onProviderEnabled(String provider) {
			logs.add("onProviderEnabled: " + provider);
			Log.d(TAG, "onProviderEnabled: " + provider);

		}

		public void onProviderDisabled(String provider) {
			logs.add("onProviderDisabled: " + provider);
			Log.d(TAG, "onProviderDisabled: " + provider);
		}

		public void processLocation(Location location) {
			Wgs84Location wgs84 = new Wgs84Location(location.getLatitude(),
					location.getLongitude());
			SjtskLocation sjtskLoc = SjtskLocation.convert(wgs84);
			logs.add("absolute location update: " + sjtskLoc);
			Log.d(TAG, "absolute location update: " + sjtskLoc);
			mGraphView.invalidate();
		}
	}

	private class MyGPSListener implements GpsStatus.Listener {
		public void onGpsStatusChanged(int event) {
			// boolean hasGPSFix = false;

			GpsStatus status = mLocationManager.getGpsStatus(null);
			Log.d(TAG, "GpsStatus:");
			sats = new LinkedList<GpsSatellite>();
			for (GpsSatellite sat : status.getSatellites()) {
				Log.d(TAG, sat.toString());
				sats.add(sat);
			}
			mGraphView.invalidate();

			switch (event) {
			case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
				logs.add("GpsStatus.GPS_EVENT_SATELLITE_STATUS:");
				Log.d(TAG, "GpsStatus.GPS_EVENT_SATELLITE_STATUS:");

				/*
				 * if (mLastLocation != null) isGPSFix =
				 * (SystemClock.elapsedRealtime() - mLastLocationMillis) <
				 * GPS_UPDATE_TIME_INTERVAL * 2;
				 * 
				 * if (isGPSFix) { // A fix has been acquired.
				 * 
				 * } else { // The fix has been lost.
				 * 
				 * }
				 */

				break;

			case GpsStatus.GPS_EVENT_FIRST_FIX:
				logs.add("GpsStatus.GPS_EVENT_FIRST_FIX:");
				Log.d(TAG, "GpsStatus.GPS_EVENT_FIRST_FIX:");
				break;
			case GpsStatus.GPS_EVENT_STARTED:
				logs.add("GpsStatus.GPS_EVENT_STARTED:");
				Log.d(TAG, "GpsStatus.GPS_EVENT_STARTED:");
				break;
			case GpsStatus.GPS_EVENT_STOPPED:
				logs.add("GpsStatus.GPS_EVENT_STOPPED:");
				Log.d(TAG, "GpsStatus.GPS_EVENT_STOPPED:");
				break;
			}
		}
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

		logs = new Logs();

		// Acquire a reference to the system Location Manager
		mLocationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);

		mLocationListener = new DemoLocationListener();
		mGpsListener = new MyGPSListener();

		mGraphView = new GraphView(this);
		setContentView(mGraphView);

	}

	@Override
	protected void onResume() {
		super.onResume();

		// Register the listener with the Location Manager to receive location
		// updates
		mLocationManager.addGpsStatusListener(mGpsListener);
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				GPS_UPDATE_TIME_INTERVAL, GPS_UPDATE_DISTANCE_INTERVAL,
				mLocationListener);
		mLocationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
	}

	@Override
	protected void onStop() {
		super.onStop();
		mLocationManager.removeUpdates(mLocationListener);
	}
}
