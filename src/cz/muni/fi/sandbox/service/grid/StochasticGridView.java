package cz.muni.fi.sandbox.service.grid;

import java.util.Collection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import cz.muni.fi.sandbox.buildings.Building;
import cz.muni.fi.sandbox.service.motion.MotionUpdateListener;
import cz.muni.fi.sandbox.utils.ColorRamping;
import cz.muni.fi.sandbox.utils.geometric.Line2D;
import cz.muni.fi.sandbox.utils.geometric.Point2D;
import cz.muni.fi.sandbox.utils.geometric.Rectangle;

public class StochasticGridView extends View implements IStochasticView, MotionUpdateListener {

	private static final String TAG = "StochasticView";
	
	private Bitmap mBitmap;
	private Paint mPaint = new Paint();
	private Canvas mCanvas = new Canvas();
	private Path mPath = new Path();
	private RectF mRect = new RectF();

	private float mWidth;
	private float mHeight;

	// original StochasticPanel fields
	private float xScaleFactor;
	private float yScaleFactor;

	private Building mBuilding;
	private StochasticGridPosition2d mPosition;
	private double STEP_LENGTH = 4.0;
	
	private boolean drawCollisionSet = false;

	public StochasticGridView(Context context, StochasticGridPosition2d position,
			Building building) {
		super(context);
		mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		mRect.set(-0.5f, -0.5f, 0.5f, 0.5f);
		mPath.arcTo(mRect, 0, 180);

		this.mPosition = position;
		this.mBuilding = building;
	}

	public void setBuilding(Building building) {
		mBuilding = building;
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
	
	
	protected void onDraw(Canvas canvas) {
		synchronized (this) {

			Log.d(TAG, "onDraw():");
			
			xScaleFactor = (mWidth / (float)(mPosition.getData().length));
			yScaleFactor = (mHeight / (float)(mPosition.getData()[0].length));

			double max = mPosition.getMaxProb();

			Collection<Point2D> points = mBuilding.getWallsModel().getCollisionSet();

			for (int i = 0; i < mPosition.getData().length; i++) {
				for (int j = 0; j < mPosition.getData()[0].length; j++) {

					boolean dontDraw = false;
					float x = i * xScaleFactor;
					float y = j * yScaleFactor;
					double gray = mPosition.getData()[i][j] / max;
					if (gray < 0.004) {
						if (mPosition.getData()[i][j] == 0.0) {
							mPaint.setColor(Color.WHITE);
						} else if (mPosition.getData()[i][j] > 0.001) {
							mPaint.setColor(Color.LTGRAY);
						} else {
							mPaint.setColor(Color.LTGRAY);
							// dontDraw = true;
						}
					} else {
						mPaint.setColor(ColorRamping.blueToRedRamp(gray));
					}
					
					
					if (points != null && drawCollisionSet)
						if (points.contains(new Point2D(i, j))) {
							mPaint.setColor(Color.WHITE);
						}
					
					if (!dontDraw)
						canvas.drawRect(x, y, x + xScaleFactor, y + yScaleFactor, mPaint);
				}
			}
			drawWalls(canvas);

		}
	}
	
	private void drawWalls(Canvas canvas) {
		
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(Color.BLACK);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(3.0f);
		
		// draw building walls
		float wallScale = 1.0f;
		if (mBuilding == null)
			return;
		for (Line2D wall: mBuilding.getWallsModel().getCompleteSet()) {
			
			float x1 = wallScale * xScaleFactor * ((float)wall.getX1());
			float y1 = wallScale * yScaleFactor * ((float)wall.getY1());
			float x2 = wallScale * xScaleFactor * ((float)wall.getX2());
			float y2 = wallScale * yScaleFactor * ((float)wall.getY2());
			//System.out.println("x1=" + x1 + ",y1=" + y1 + ",x2=" + x2 + ",y2=" + y2);
			canvas.drawLine(x1, y1, x2, y2, paint);
		}
		
		paint.setColor(Color.YELLOW);
		Rectangle box = mPosition.getComputingBox();
		canvas.drawRect((float)box.getX1() * xScaleFactor,
				(float)box.getY1() * yScaleFactor,
				(float)box.getX2() * xScaleFactor,
				(float)box.getY2() * yScaleFactor, paint);
		
	}
	
	@Override
	public void positionChanged(StochasticGridPosition2d position) {
		Log.d(TAG, "StochasticPanel.positionChanged:");
		invalidate();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		Log.d(TAG, "clicked e.x = " + event.getX() + ", e.y = " + event.getY());
		if (event.getAction() == MotionEvent.ACTION_UP) {
			
			int x = (int)(event.getX() - mWidth / 2);
			int y = (int)(event.getY() - mHeight / 2);
			double hdg = 180 * (Math.PI - Math.atan2(x, y)) / (Math.PI);
			long timestamp = System.nanoTime();
			mPosition.onStep(hdg, STEP_LENGTH);
			System.out.println("mPosition.onStep() took " + (System.nanoTime() - timestamp) * Math.pow(10, -9) + "s");
		}
		return true;
	}

	
	@Override
	public void positionChanged(float heading, float x, float y) {
		double hdg = 180 * heading / Math.PI;
		if (x * x + y * y != 0.0)
			mPosition.onStep(hdg, STEP_LENGTH);
	}

	@Override
	public void floorChanged(int floor) {
		// TODO Auto-generated method stub
		
	}
	
}
