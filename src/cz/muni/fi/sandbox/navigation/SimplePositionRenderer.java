package cz.muni.fi.sandbox.navigation;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

public class SimplePositionRenderer extends PositionRenderer {
	
	private SimplePosition mPosition;
	private Path mWalkTrail;
	
	public SimplePositionRenderer(SimplePosition position) {
		mPosition = position;
		mWalkTrail = new Path();
		mWalkTrail.moveTo(0.0f, 0.0f);
	}
	
	public void draw(Canvas canvas) {
		drawPositionMark(canvas);
		drawWalkTrail(canvas);
	}
	
	
	private void drawWalkTrail(Canvas canvas) {
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setStyle(Paint.Style.STROKE);
		canvas.drawPath(mWalkTrail, paint);
	}

	private void drawPositionMark(Canvas canvas) {

		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(Color.BLUE);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(2.0f);

		float hdg = (float) (360 * mPosition.getHeading() / (2 * Math.PI));
		float[] coords = mPosition.getCoordinates();
		float posX = scaleX * coords[0];
		float posY = -scaleY * coords[1];

		canvas.rotate(hdg, posX, posY);

		canvas.drawCircle(posX, posY, 10, paint);
		canvas.drawLine(posX, posY - 20, posX, posY, paint);
	}
	
	@Override
	public String toString() {
		return mPosition.toString();
	}
}
