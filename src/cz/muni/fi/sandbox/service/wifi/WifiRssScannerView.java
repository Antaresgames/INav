package cz.muni.fi.sandbox.service.wifi;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Queue;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class WifiRssScannerView extends View implements WifiLogger {

	private static final String TAG = "WifiRssScannerView";

	private Bitmap mBitmap;
	private Paint mPaint = new Paint();
	private Canvas mCanvas = new Canvas();

	private float mWidth;
	private float mHeight;
	
	private RssFingerprint mFingerprint;
	private RssFingerprint mBestMatchFingerprint;
	private Queue<String> messageQueue = new LinkedList<String>();

	private static final int MESSAGE_QUEUE_SIZE = 20;
		
	public WifiRssScannerView(Context context) {
		super(context);
		
		mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
		mCanvas.setBitmap(mBitmap);
		mCanvas.drawColor(0xFFFFFFFF);

		mWidth = w;
		mHeight = h;

		super.onSizeChanged(w, h, oldw, oldh);
	}
	
	public void newFingerprintAvailable(RssFingerprint fingerprint) {
		mFingerprint = fingerprint;
		mBestMatchFingerprint = null;
		invalidate();
	}
	
	public void bestMatchFingerprint(RssFingerprint fingerprint, RssFingerprint bestMatch) {
		mFingerprint = fingerprint;
		mBestMatchFingerprint = bestMatch;
		invalidate();
	}
	

	public void pushMessage(String message) {
		
		if (messageQueue.size() > MESSAGE_QUEUE_SIZE) {
			messageQueue.poll();
		}
		messageQueue.add(message);
	}

	protected void onDraw(Canvas canvas) {
		synchronized (this) {

			Log.d(TAG, "onDraw():");
			
			mPaint.setColor(Color.BLACK);
			mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
			
			canvas.drawColor(Color.WHITE);
			canvas.drawText("Wifi Signal Strength location demo", 0, 15, mPaint);
			canvas.drawText("Wifi scan results: ", 0, 25, mPaint);

			// draw current fingerprint
			if (mFingerprint != null && mBestMatchFingerprint == null) {
				float yOffset = 35;
				for (Entry<String, RssFingerprint.RssMeasurement> entry : mFingerprint.getVector().entrySet()) {
					canvas.drawText(entry.getKey() + ": " + entry.getValue().getRss(), 0, yOffset, mPaint);
					yOffset += 10;
					
					float x = (float) (mWidth * 0.75);
					float y = (float) (mHeight * 0.20 + (entry.getValue().getRss() / 100f) * mHeight
							* 0.80);
					
					//canvas.drawLine(0, 0, x, y, paint);
					canvas.drawText(entry.getKey(), x, y, mPaint);
				}
			}
			
			// draw best match
			if (mBestMatchFingerprint != null) {
				HashSet<String> keys = new HashSet<String>();
				keys.addAll(mBestMatchFingerprint.getVector().keySet());
				keys.addAll(mFingerprint.getVector().keySet());
				float yOffset = 35;	
				for (String key: keys) {
					double m1 = 0.0;
					double m2 = 0.0;
					if (mFingerprint.getVector().containsKey(key)) {
						m1 = mFingerprint.getVector().get(key).getRss();
					}
					if (mBestMatchFingerprint.getVector().containsKey(key)) {
						m2 = mBestMatchFingerprint.getVector().get(key).getRss();
					}
					mPaint.setColor(Color.BLACK);
					if (m1 == 0.0 || m2 == 0.0)
						mPaint.setColor(Color.RED);
					canvas.drawText(key + ": " + m1 + ", " + m2, 0, yOffset, mPaint);
					yOffset += 10;
					
				}	
			}
			
			// draw messages
			if (messageQueue != null) {
				float yOffset = 350;
				for (String message : messageQueue) {
					canvas.drawText(message, 0, yOffset, mPaint);
					yOffset += 10;
				}
			}
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		Log.d(TAG, "clicked e.x = " + event.getX() + ", e.y = " + event.getY());
		if (event.getAction() == MotionEvent.ACTION_UP) {
			
		}
		return true;
	}
}
