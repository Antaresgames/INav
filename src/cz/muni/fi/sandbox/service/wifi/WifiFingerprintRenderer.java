package cz.muni.fi.sandbox.service.wifi;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import cz.muni.fi.sandbox.navigation.GridRenderer;
import cz.muni.fi.sandbox.utils.ColorRamping;
import cz.muni.fi.sandbox.utils.geometric.Point2D;

public class WifiFingerprintRenderer extends GridRenderer implements
		WifiPositionUpdateListener {

	private static final String TAG = "WifiFingerprintRenderer";
	
	private String bssid = "maximum";
	private float x, y;
	private boolean positionSet = false;
	private ProbabilityMap mProbMap;
	private IGetsYouRssFingerprints mFingerprintsAdaptor;
	private static final double DEFAULT_GRID_SPACING = 2.0;

	public WifiFingerprintRenderer(IGetsYouRssFingerprints fingerprintsAdaptor) {
		mProbMap = null;
		mFingerprintsAdaptor = fingerprintsAdaptor;
	}

	private double getGridSpacing() {
		if (mFingerprintsAdaptor.getRssFingerprints() == null) {
			return DEFAULT_GRID_SPACING;
		}
		return mFingerprintsAdaptor.getRssFingerprints().getGridSpacing();
	}

	public void setDisplayBssid(String bssid) {
		this.bssid = bssid;
		mProbMap = null;
	}

	public void draw(Canvas canvas) {
		Log.d(TAG, "draw()");
		if (mProbMap != null) {
			drawProbMapGrid(canvas);
		} else {
			drawAccessPointGrid(canvas);
		}
		if (positionSet)
			drawBestMatchPosition(canvas);
	}

	private void drawBestMatchPosition(Canvas canvas) {

		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setStrokeWidth(1);
		paint.setStyle(Paint.Style.STROKE);
		paint.setColor(Color.WHITE);
		canvas.drawCircle(scaleX * x, -scaleY * y, scaleX * 0.5f, paint);
	}

	private void drawProbMapGrid(Canvas canvas) {

		double gridSpacing = mProbMap.getGridSize();
		
		if (mProbMap != null) {

			Log.d(TAG, "drawProbabilityMap");
			
			Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint.setStyle(Paint.Style.FILL);
			
			double maxProb = mProbMap.getMaxProbability();
			int probGrid = mProbMap.getGridSize();
			
			for (int x = mProbMap.getOrigin().x; x < mProbMap.getOrigin().x + mProbMap.getSize().x; x++) {
			
				for (int y = mProbMap.getOrigin().y; y < mProbMap.getOrigin().y + mProbMap.getSize().y; y++) {
					
					int xcoord = x * probGrid;
					int ycoord = y * probGrid;
					
					double probability = mProbMap.getProbability(xcoord, ycoord);
					double bwScale = 1 - probability / maxProb;
					paint.setColor(ColorRamping.blackToWhiteRamp(bwScale));

					float left = (float) (xcoord - gridSpacing / 2);
					float top = (float) (ycoord - gridSpacing / 2);
					float right = (float) (xcoord + gridSpacing / 2);
					float bottom = (float) (ycoord + gridSpacing / 2);	
					// Log.d(TAG, "loc = " + xcoord + ", " + ycoord);
					
					canvas.drawRect(scaleX * left, -scaleY * bottom,
							scaleX * right, -scaleY * top, paint);

					if (scaleX >= 20) {
						if (bwScale < 0.5)
							paint.setColor(Color.WHITE);
						else
							paint.setColor(Color.BLACK);
						canvas.drawText(String.format("%.02f", probability),
								(float) (scaleX * xcoord),
								(float) (-scaleY * ycoord), paint);
					}
				}
			}
		} else {
			Log.e(TAG, "probability map is null, drawing skipped");
		}

	}

	private void drawAccessPointGrid(Canvas canvas) {

		double gridSpacing = getGridSpacing();
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setStyle(Paint.Style.FILL);

		if (mFingerprintsAdaptor.getRssFingerprints() != null) {
			for (RssFingerprint fingerprint : mFingerprintsAdaptor
					.getRssFingerprints().getFingerprints()) {

				boolean max = bssid.equals("maximum");

				if ((max && !fingerprint.getVector().isEmpty())
						|| fingerprint.getVector().containsKey(bssid)) {

					Point2D loc = fingerprint.getLocation();

					double rss = 0.0;
					if (max) {
						// Log.d(TAG,
						// "fingerprint.getVector().values().isEmpty == " +
						// fingerprint.getVector().values().isEmpty());
						rss = Collections.max(fingerprint.getVector().values())
								.getRss();
					} else {
						rss = fingerprint.getVector().get(bssid).getRss();
					}

					Log.d(TAG, "rss = " + rss + ", loc = " + loc);
					double temperatureScale = 1 + rss / 100.0;
					paint.setColor(ColorRamping.blueToRedRamp(temperatureScale));
					float left = (float) (loc.getX() - gridSpacing / 2);
					float top = (float) (loc.getY() - gridSpacing / 2);
					float right = (float) (loc.getX() + gridSpacing / 2);
					float bottom = (float) (loc.getY() + gridSpacing / 2);

					canvas.drawRect(scaleX * left, -scaleY * bottom, scaleX
							* right, -scaleY * top, paint);

					if (scaleX >= 20) {
						paint.setColor(Color.BLACK);
						canvas.drawText(Integer.toString((int) rss),
								(float) (scaleX * loc.getX()),
								(float) (-scaleY * loc.getY()), paint);
					}
				}

			}
		}

	}

	@Override
	public void wifiPositionChanged(float heading, float x, float y) {
		positionSet = true;
		this.x = x;
		this.y = y;
	}

	@Override
	public void probabilityMapChanged(ProbabilityMap map) {
		mProbMap = map;
	}

}
