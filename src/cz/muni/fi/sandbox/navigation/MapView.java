package cz.muni.fi.sandbox.navigation;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import cz.muni.fi.sandbox.buildings.Building;
import cz.muni.fi.sandbox.service.wifi.ProbabilityMap;
import cz.muni.fi.sandbox.service.wifi.WifiPositionUpdateListener;
import cz.muni.fi.sandbox.utils.geometric.Line2D;
import cz.muni.fi.sandbox.utils.geometric.Point2D;
import cz.muni.fi.sandbox.utils.geometric.Rectangle;

public class MapView
	extends
		View
	implements
		PositionModelUpdateListener,
		WifiPositionUpdateListener,
		LocationAssignmentAuthority
{

	private Canvas mCanvas = new Canvas();
	private Bitmap mBitmap;
	private boolean mRedrawBackground = true;
	
	
	private final String TAG = "MapView";

	private Paint mPaint = new Paint();
	private Path mPath = new Path();
	private RectF mRect = new RectF();
	private int mColors[] = new int[3 * 2];
	
	private boolean mDrawWalls = true;
	private boolean mDrawPositionMark = false;
	private boolean mDrawWifiFingerprints = true;
	private boolean mDrawParticleCloud = true;

	private ScaleGestureDetector mScaleDetector;

	private float scrollX, scrollY;
	private float originX, originY;

	// private float mPosX = 0.0f;
	// private float mPosY = 0.0f;
	// private float mHdg = 0.0f;

	// 10 pixels per meter
	private float mScreenScale = 10.0f;
	
	private SharedPreferences mSharedPrefs;

	private Building mBuilding;
	private PositionRenderer mPositionRenderer;
	private LinkedList<GridRenderer> mGridRenderers;
	

	public MapView(Context context) {
		super(context);

		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

		mColors[0] = Color.argb(192, 255, 64, 64);
		mColors[1] = Color.argb(192, 64, 128, 64);
		mColors[2] = Color.argb(192, 64, 64, 255);
		mColors[3] = Color.argb(192, 64, 255, 255);
		mColors[4] = Color.argb(192, 128, 64, 128);
		mColors[5] = Color.argb(192, 255, 255, 64);

		mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		mRect.set(-0.5f, -0.5f, 0.5f, 0.5f);
		mPath.arcTo(mRect, 0, 180);

		mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
		mGridRenderers = new LinkedList<GridRenderer>();

		recachePreferences();
		
	}

	public void setBuilding(Building building) {
		Log.d(TAG, "setBuilding");
		mBuilding = building;
		cropWalls();
		invalidateBackground();
		invalidate();
	}

	public void setPosition(PositionRenderer position) {

		mPositionRenderer = position;
		mPositionRenderer.setScale(mScreenScale);
	}
	
	public void addGridRenderer(GridRenderer gridRenderer) {
		Log.d(TAG, "addGridRenderer");
		mGridRenderers.add(gridRenderer);
		gridRenderer.setScale(mScreenScale);
	}
	
	public void setOffset(float x, float y) {
		originX = x * mScreenScale;
		originY = y * mScreenScale;
		cropWalls();
		invalidate();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
		mCanvas.setBitmap(mBitmap);
		mCanvas.drawColor(0xFFFFFFFF);
		cropWalls();
		
	}
		
	
	@Override
	protected void onDraw(Canvas canvas) {
		
		synchronized (this) {
			
			
			if (mBitmap != null) {
				
				if (mRedrawBackground) {
					mRedrawBackground = false;
					
					mCanvas.save();
					mCanvas.translate(originX, originY);
					mCanvas.drawColor(Color.WHITE);
					
					if (mDrawWifiFingerprints && mGridRenderers != null) {
						for (GridRenderer renderer: mGridRenderers)
							renderer.draw(mCanvas);
					}
					
					if (mDrawWalls) {
						drawAreaLayers(mCanvas);
					}
					
					mCanvas.restore();
				}
			}
			
			
			
			canvas.drawBitmap(mBitmap, 0, 0, null);
			
			
			canvas.translate(originX, originY);
			
			drawDestination(canvas);
			
			if (mDrawPositionMark && mPositionRenderer != null)
				mPositionRenderer.draw(canvas);
			
			drawDashboard(canvas);
		}
	}

	private void drawLayer(Collection<Line2D> layer, Canvas canvas, int color, float width) {

		mPaint.setColor(color);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(width);
		
		// draw building walls
		for (Line2D line : layer) {
			float x1 = mScreenScale * (float) line.getX1();
			float y1 = -mScreenScale * (float) line.getY1();
			float x2 = mScreenScale * (float) line.getX2();
			float y2 = -mScreenScale * (float) line.getY2();
			canvas.drawLine(x1, y1, x2, y2, mPaint);
		}
	}
	
	private void drawPointsLayer(Map<Point2D, String> layer, Canvas canvas, int color, float width) {

		
		mPaint.setColor(color);
		mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		mPaint.setStrokeWidth(width);
		
		
		// draw building walls
		for (Point2D point : layer.keySet()) {
			float x1 = mScreenScale * (float) point.getX();
			float y1 = -mScreenScale * (float) point.getY();
			
			
			canvas.drawCircle(x1, y1, mScreenScale * 0.5f, mPaint);
			String text = layer.get(point);
			Rect textRect = new Rect();
			mPaint.getTextBounds(text, 0, text.length(), textRect);
			canvas.drawText(layer.get(point), x1-(textRect.width()/2), y1+(mScreenScale * 0.5f)+textRect.height(), mPaint);
			
		}
	}
	
	private void drawAreaLayers(Canvas canvas) {
		if (mBuilding != null) {
			drawLayer(mBuilding.getCurrentArea().getWallsModel().getDisplaySet(), canvas, Color.RED, 1.0f);
			drawLayer(mBuilding.getCurrentArea().getStairsLayer().getDisplaySet(), canvas, Color.MAGENTA, 1.0f);
			drawPointsLayer(mBuilding.getCurrentArea().getPointsModel().getDisplaySet(), canvas, Color.GREEN, 1.0f);
			drawLayer(mBuilding.getCurrentArea().getTransitionEdgeLayer().getDisplaySet(), canvas, Color.CYAN, 1.0f);
		}
	}
	
	private void drawDashboard(Canvas canvas) {
		
		// top screen dashboard
		canvas.restore();
		mPaint.setColor(Color.BLACK);
		mPaint.setStyle(Style.FILL);
		mPaint.setTextSize(16);

		if (mPositionRenderer != null) {
			canvas.drawText(mPositionRenderer.toString(), 0, 20, mPaint);
		}
		canvas.drawText("origin = (" + originX + ", " + originY + "), scale = "
				+ mScreenScale, 0, 35, mPaint);
		
		if (mDestination != null)
			canvas.drawText("dest = (" + mDestination + ")", 0, 50, mPaint);
	}

	
	private Point2D mDestination;
	
	public void setDestination(Point2D destination) {
		mDestination = destination;
	}
	
	private void drawDestination(Canvas canvas) {

		if (mDestination!=null) {
			mPaint.setColor(Color.GREEN);
			mPaint.setStyle(Paint.Style.STROKE);
			mPaint.setStrokeWidth(2.0f);
			
			float posX = (float)(mScreenScale * mDestination.getX());
			float posY = (float)(-mScreenScale * mDestination.getY());
			
			canvas.drawCircle(posX, posY, 10, mPaint);
		}
	}
	
	
	
	protected void touchStart(float x, float y) {
		scrollX = x;
		scrollY = y;
	}

	protected void touchMove(float x, float y) {
		originX += (x - scrollX);
		originY += (y - scrollY);
		scrollX = x;
		scrollY = y;
	}

	protected void touchUp() {

		cropWalls();
	}

	public void cropWalls() {
		Log.d(TAG, "cropWalls");
		double w = getWidth();
		double h = getHeight();
		double x1 = -originX;
		double y1 = -originY;
		double x2 = -originX + w;
		double y2 = -originY + h;
		long timestamp = System.nanoTime();
		Rectangle cropBox = new Rectangle(x1 / mScreenScale,
				-y1 / mScreenScale, x2 / mScreenScale, -y2 / mScreenScale);
		Log.d(TAG, "cropBox = " + cropBox);
		if (mBuilding != null) {
			mBuilding.getCurrentArea().setDisplayCropBox(cropBox);
			Log.d(TAG, "cropping walls took " + (System.nanoTime() - timestamp)
					/ Math.pow(10, 9) + " second");
			Log.d(TAG, "walls size = "
					+ mBuilding.getCurrentArea().getWallsModel().getDisplaySet().size());
			Log.d(TAG, "stairs size = "
					+ mBuilding.getCurrentArea().getStairsLayer().getDisplaySet().size());
			Log.d(TAG, "points size = "
					+ mBuilding.getCurrentArea().getPointsModel().getDisplaySet().size());
		} else {
			Log.w(TAG, "cropWalls skipped, mBuilding == null");
		}
	}

	public void recachePreferences() {

		mDrawWalls = mSharedPrefs.getBoolean(
				"draw_map_preference", true);
		
		mDrawPositionMark = mSharedPrefs.getBoolean(
				"draw_position_mark_preference", true);
		
		mDrawWifiFingerprints = mSharedPrefs.getBoolean(
				"draw_wifi_visuals_preference" , false);
		
		mDrawParticleCloud = mSharedPrefs.getBoolean(
				"draw_particle_cloud_preference" , true);
	}

	private boolean mNotMoved = true;
	private SetPositionListener mSetPositionListener;

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		mScaleDetector.onTouchEvent(event);

		float x = event.getX();
		float y = event.getY();

		switch (event.getAction()) {

		case MotionEvent.ACTION_DOWN:
			mNotMoved = true;
			touchStart(x, y);
			invalidate();
			break;

		case MotionEvent.ACTION_MOVE: {

			// Only move if the ScaleGestureDetector isn't processing a gesture.
			if (!mScaleDetector.isInProgress()) {
				touchMove(x, y);
			}
			mNotMoved = false;
			invalidateBackground();
			invalidate();
			
			break;
		}

		case MotionEvent.ACTION_UP: {
			touchUp();

			if (mNotMoved && mSetPositionListener != null) {
				mSetPositionListener.setPosition((x - originX) / mScreenScale,
						-(y - originY) / mScreenScale, mBuilding != null?mBuilding.getCurrentAreaIndex():0);
				mSetPositionListener = null;
			}
			invalidateBackground();
			invalidate();

			break;
		}

		}
		return true;
	}

	public void requestSetPosition(SetPositionListener spl) {
		mSetPositionListener = spl;
	}

	public void updatePosition(PositionModel position) {
		postInvalidate();
	}


	public void updateHeading(PositionModel position) {

	
	}
	
	private void setGridRendererScale(float scale) {
		for (GridRenderer renderer: mGridRenderers) {
			renderer.setScale(scale);
		}
	}
	
	public void invalidateBackground() {
		mRedrawBackground = true;
	}
	
	private class ScaleListener extends
			ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			Log.d(TAG, "onScale event " + detector.getScaleFactor() + " "
					+ detector.getFocusX() + " " + detector.getFocusY());
			if (detector.getScaleFactor() * mScreenScale>=1 
					&& detector.getScaleFactor() * mScreenScale<=150) {
				float focusX = detector.getFocusX();
				float focusY = detector.getFocusY();
				originX = focusX * (1 - detector.getScaleFactor())
						+ detector.getScaleFactor() * originX;
				originY = focusY * (1 - detector.getScaleFactor())
						+ detector.getScaleFactor() * originY;
				// float oldScaleFactor = mScaleFactor;
				mScreenScale *= detector.getScaleFactor();
				
				// Don't let the object get too small or too large.
				// final float MAX_SCREEN_SCALE = 200f;
				// final float MIN_SCREEN_SCALE = 0.1f;
				// mScreenScale = Math.max(MIN_SCREEN_SCALE, Math.min(mScreenScale, MAX_SCREEN_SCALE));
				if (mPositionRenderer != null)
					mPositionRenderer.setScale(mScreenScale);
				if (mGridRenderers != null)
					setGridRendererScale(mScreenScale);
				invalidateBackground();
				invalidate();
				return true;
			} else {
				return false;
			}
		}
	}

	@Override
	public void wifiPositionChanged(float heading, float x, float y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void probabilityMapChanged(ProbabilityMap map) {
		invalidateBackground();
		invalidate();
		// TODO Auto-generated method stub
		
	}
}
