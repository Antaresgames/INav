package cz.muni.fi.sandbox.service.particle;

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
import cz.muni.fi.sandbox.navigation.PositionModel;
import cz.muni.fi.sandbox.navigation.PositionModelUpdateListener;
import cz.muni.fi.sandbox.navigation.PositionRenderer;
import cz.muni.fi.sandbox.service.motion.MotionUpdateListener;
import cz.muni.fi.sandbox.utils.geometric.Line2D;

public class ParticleView extends View implements MotionUpdateListener, PositionModelUpdateListener {

	private static final String TAG = "ParticleView";
	
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
	private ParticlePosition mPosition;
	private PositionRenderer mPositionRenderer;
	// private double STEP_LENGTH = 4.0;
	private double STEP_LENGTH = .8;
	// private int SIZE = 100;
	private int SIZE = 20;

	public ParticleView(Context context, ParticlePosition position,
			Building building) {
		super(context);
		mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		mRect.set(-0.5f, -0.5f, 0.5f, 0.5f);
		mPath.arcTo(mRect, 0, 180);

		this.mPosition = position;
		mPositionRenderer = mPosition.getRenderer();
		this.mBuilding = building;
	}
	
	public void setWallsModel(Building building) {
		this.mBuilding = building;
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
			
			xScaleFactor = (mWidth / SIZE);
			yScaleFactor = (mHeight / SIZE);
			canvas.drawColor(Color.WHITE);

			drawParticles(canvas);
			drawWalls(canvas);

		}
	}
	
	private void drawParticles(Canvas canvas) {
	
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(1.0f);
	
		for (Particle particle : mPosition.getParticles()) {
			double[] state = particle.getState();
	
			float x1 = (float) (xScaleFactor * state[0]);
			float y1 = (float) (yScaleFactor * state[1]);
			float x2 = x1 + (float) (Math.sin(state[2]) * 3);
			float y2 = y1 + (float) (Math.cos(state[2]) * 3);
	
			paint.setColor(Color.BLACK);
			canvas.drawLine(x1, y1, x2, y2, paint);
		}
	}

	private void drawWalls(Canvas canvas) {
		
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(Color.RED);
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
		
		/*
		Rectangle bucketBox = new Rectangle();
		double gridSize = 2.0;
		for (Point2D bucket: mBuilding.getWallsModel().getBuckets()) {
			
			float x1 = xScaleFactor * ((float)bucket.getX());
			float y1 = yScaleFactor * ((float)bucket.getY());
			
			bucketBox.set(bucket.getX(), bucket.getY(), bucket.getX(),
					bucket.getY());
			bucketBox.enlarge(2 * gridSize);
			
			
			if (bucketBox.hasIntersection(line)) {
				//canvas.drawCircle(x1, y1, 2, paint);
				paint.setColor(Color.CYAN);
			} else {
				//paint.setColor(Color.RED);
			}
			
			
			//System.out.println("x1=" + bucket.getX() + ",y1=" + bucket.getY());
			canvas.drawCircle(x1, y1, 2, paint);
		}
		*/
		
	}
	
	public void positionChanged(ParticlePosition position) {
		Log.d(TAG, "positionChanged:");
		invalidate();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		Log.d(TAG, "clicked e.x = " + event.getX() + ", e.y = " + event.getY());
		if (event.getAction() == MotionEvent.ACTION_UP) {
			
			int x = (int)(event.getX() - mWidth / 2);
			int y = -(int)(event.getY() - mHeight / 2);
			double hdg = Math.PI - Math.atan2(x, y);
			long timestamp = System.nanoTime();
			//mPosition.onStep(hdg, STEP_LENGTH);
			mPosition.onStep(.95, hdg, Math.PI * 10 /180,
					STEP_LENGTH, .1 * STEP_LENGTH);
			System.out.println("mPosition.onStep() took " + (System.nanoTime() - timestamp) * Math.pow(10, -9) + "s");
		}
		invalidate();
		return true;
	}

	
	@Override
	public void positionChanged(float heading, float x, float y) {
		double hdg = 180 * heading / Math.PI;
		if (x * x + y * y != 0.0)
			mPosition.onStep(hdg, STEP_LENGTH);
	}

	@Override
	public void updatePosition(PositionModel position) {
		postInvalidate();
		
	}
	
	@Override
	public void updateHeading(PositionModel position) {
		postInvalidate();
		
	}

	@Override
	public void floorChanged(int floor) {
		// TODO Auto-generated method stub
		
	}

}
