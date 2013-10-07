package cz.muni.fi.sandbox.service.orientation;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import cz.muni.fi.sandbox.utils.Writer;

public class CompassDeflectionDemo extends Activity {

	private final String TAG = "CompassDeflectionDemo";
	private SensorManager mSensorManager;
	private CompassDeflectionView mView;
	private Compass mGyroCompass;
	private Compass mMagneticCompass;
	private SharedPreferences mSharedPrefs;
	private Writer mDeflectionWriter;
	
	
	private class CompassDeflectionView extends View implements HeadingListener {

		private Paint mPaint;
		private int mHeight, mWidth;
		
		private Bitmap mBitmap;
		private Canvas mCanvas = new Canvas();

		
		public CompassDeflectionView(Context context) {
			super(context);
			mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mPaint.setStyle(Paint.Style.STROKE);
			mPaint.setColor(Color.BLACK);
		}
		
		
		// private float oldX, oldY;
		
		private void addMeasurement(float hdg1, float hdg2) {
			// hdg2 = (float)(hdg2>Math.PI?hdg2-2*Math.PI:hdg2);
			float diff = hdg2 - hdg1;
			diff = (diff>Math.PI)?(float)(diff-2*Math.PI):diff;
			float x = mWidth / 2 + (float)(mWidth * hdg1 / (2 * Math.PI));
			float y = mHeight / 2 - (float)(mWidth * (hdg1 + diff) / (2 * Math.PI));
			Log.d(TAG, "drawPoint x = " + x + " y = " + y);
			mDeflectionWriter.writeln("" + hdg1 + " " + diff);
			// mCanvas.drawPoint(x, y, mPaint);
			//mCanvas.drawLine(oldX, oldY, x, y, mPaint);
			mCanvas.drawCircle(x, y, 2.0f, mPaint);
			// oldX=x;
			// oldY=y;
		}
		
		@Override
		public boolean onTouchEvent(MotionEvent event) {
				
			switch (event.getAction()) {
			case MotionEvent.ACTION_UP:
				mGyroCompass.reset();
				break;
			}
			return true;
		}
		
		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
			mCanvas.setBitmap(mBitmap);
			mCanvas.drawColor(Color.WHITE);
			
			mHeight = getHeight();
			mWidth = getWidth();
			
			super.onSizeChanged(w, h, oldw, oldh);
		}
		
		@Override
		protected void onDraw(Canvas canvas) {
			synchronized (this) {
				
				if (mBitmap != null) {
					Paint paint = new Paint();
					paint.setStyle(Paint.Style.STROKE);
					paint.setColor(Color.BLACK);
					
					mCanvas.drawLine(0, mHeight / 2, mWidth, mHeight / 2, paint); // x axis
					mCanvas.drawLine(0, mHeight / 2 + mWidth / 2, mWidth, mHeight / 2 - mWidth / 2, paint); // 1st quadrant axis
				}
				canvas.drawBitmap(mBitmap, 0, 0, null);
				
			}
		}
		
		@Override
		public void headingChanged(float heading) {
			
			
			addMeasurement(mGyroCompass.getHeading(), mMagneticCompass.getHeading());
			postInvalidate();
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
		
		Log.d(TAG, "onCreate");
		
		mDeflectionWriter = new Writer("mag_deflection.txt");
		
		mView = new CompassDeflectionView(this);
		
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		 			
		mMagneticCompass = Compass.compassFactory(Compass.MAGNETIC, mSensorManager);
		mMagneticCompass.updatePreferences(mSharedPrefs);
		
		mGyroCompass = Compass.compassFactory(Compass.GYROSCOPIC, mSensorManager);
		mGyroCompass.updatePreferences(mSharedPrefs);
		
		mMagneticCompass.register(mView);
		
		setContentView(mView);
	}
	
	@Override
	protected void onResume() {
		Log.d(TAG, "onResume");
		super.onResume();
		mGyroCompass.start();
		mMagneticCompass.start();
		mDeflectionWriter.open(false);
		
	}
	
	
	@Override
	protected void onPause() {
		
		Log.d(TAG, "onPause");
		super.onPause();
		mGyroCompass.stop();
		mMagneticCompass.stop();
		mDeflectionWriter.close();
	}
	
	@Override
	protected void onStop() {
		Log.d(TAG, "onStop");
		super.onStop();
	}
	
}
