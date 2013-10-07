package cz.muni.fi.sandbox.service.orientation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

public class CompassView extends View implements HeadingListener {
	
	//private static final String TAG = "CompassView";

	private Compass mCompass;
	private Paint mPaint;
	
	public void setCompass(Compass compass) {
		mCompass = compass;
		mCompass.register(this);
	}
	
	public CompassView(Context context) {
		super(context);
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setColor(Color.BLACK);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
			
		switch (event.getAction()) {
		case MotionEvent.ACTION_UP:
			mCompass.reset();
			break;
		}
		return true;
	}
	
	
	@Override
	protected void onDraw(Canvas canvas) {


		canvas.drawColor(0xFFFFFFFF);
		canvas.drawText("Heading: " + (180 * mCompass.getHeading() / Math.PI), 5, 15, mPaint);
		canvas.translate(getWidth() / 2, getHeight() / 2);
		
		mCompass.draw(canvas);
	}

	@Override
	public void headingChanged(float heading) {
		postInvalidate();
	}
}
