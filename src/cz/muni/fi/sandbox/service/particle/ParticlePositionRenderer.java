package cz.muni.fi.sandbox.service.particle;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import cz.muni.fi.sandbox.navigation.PositionRenderer;
import cz.muni.fi.sandbox.service.wifi.ProbabilityMap;
import cz.muni.fi.sandbox.utils.ColorRamping;
import cz.muni.fi.sandbox.utils.geometric.Line2D;
import cz.muni.fi.sandbox.utils.geometric.Rectangle;

public class ParticlePositionRenderer extends PositionRenderer {

	private final String TAG = "ParticlePositionRenderer";

	private ParticlePosition mParticlePosition;

	public ParticlePositionRenderer(ParticlePosition position) {
		mParticlePosition = position;
	}

	@Override
	public void draw(Canvas canvas) {

		// Log.d(TAG, "draw()");

		drawWalls(canvas);
		drawBox(canvas);
		drawParticleCloud(canvas);
		drawPositionMark(canvas);

	}

	public void drawWifiStuff(Canvas canvas) {
		drawProbabilityMap(canvas);
	}

	public void drawParticleCloud(Canvas canvas) {

		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(1.0f);
		int currentArea = mParticlePosition.getBuilding().getCurrentAreaIndex();

		for (Particle particle : mParticlePosition.getParticles()) {
			double[] state = particle.getState();

			float x1 = (float) (scaleX * state[0]);
			float y1 = (float) (scaleY * state[1]);
			float x2 = x1 + (float) (Math.sin(state[2]) * 3);
			float y2 = y1 + (float) (Math.cos(state[2]) * 3);

			if (particle.getAreaIndex() == currentArea)
				paint.setColor(Color.BLACK);
			else
				paint.setColor(Color.LTGRAY);
			canvas.drawLine(x1, -y1, x2, -y2, paint);
			// canvas.drawLine(x1, y1, x1 + (int)(Math.sin(state[2]) * 3), y1 +
			// (int)(-Math.cos(state[2]) * 3), paint);
		}
	}

	private void drawPositionMark(Canvas canvas) {

		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(Color.BLUE);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(2.0f);

		float hdg = (float) (180 * mParticlePosition.getHeading() / Math.PI);
		double[] coords = mParticlePosition.getCoordinates();
		float posX = (float) (scaleX * coords[0]);
		float posY = (float) (-scaleY * coords[1]);

		canvas.rotate(hdg, posX, posY);

		canvas.drawCircle(posX, posY, 10, paint);
		canvas.drawLine(posX, posY - 20, posX, posY, paint);
	}

	private void drawBox(Canvas canvas) {

		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(Color.BLUE);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(2.0f);

		Rectangle box = mParticlePosition.getBox();
		float loX = (float) (scaleX * box.left);
		float loY = -(float) (scaleY * box.bottom);
		float hiX = (float) (scaleY * box.right);
		float hiY = -(float) (scaleY * box.top);

		canvas.drawLine(loX, loY, loX, hiY, paint);
		canvas.drawLine(hiX, loY, hiX, hiY, paint);
		canvas.drawLine(loX, loY, hiX, loY, paint);
		canvas.drawLine(loX, hiY, hiX, hiY, paint);
	}

	private void drawWalls(Canvas canvas) {

		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(Color.GREEN);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(1.0f);

		// draw building walls

		float wallOriginX = 0;
		float wallOriginY = 0;
		for (Line2D wall : mParticlePosition.getWallsModel().getWorkingSet()) {
			float x1 = scaleX * (wallOriginX + (float) wall.getX1());
			float y1 = scaleY * (wallOriginY + (float) wall.getY1());
			float x2 = scaleX * (wallOriginX + (float) wall.getX2());
			float y2 = scaleY * (wallOriginY + (float) wall.getY2());
			canvas.drawLine(x1, -y1, x2, -y2, paint);
		}

	}

	private void drawProbabilityMap(Canvas canvas) {
		
		ProbabilityMap probMap = mParticlePosition.getProbabilityMap();
		double gridSpacing = probMap.getGridSize();
		
		if (probMap != null) {

			Log.d("ParticlePositionRenderer", "drawProbabilityMap");
			
			Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint.setStyle(Paint.Style.FILL);
			
			double maxProb = probMap.getMaxProbability();
			
			for (int x = probMap.getOrigin().x; x < probMap.getOrigin().x + probMap.getSize().x; x++) {
			
				for (int y = probMap.getOrigin().y; y < probMap.getOrigin().y + probMap.getSize().y; y++) {
					
					double prob = probMap.getProbability(x, y);
					double bwScale = prob / maxProb;
					paint.setColor(ColorRamping.blackToWhiteRamp(bwScale));

					float left = (float) (x - gridSpacing / 2);
					float top = (float) (y - gridSpacing / 2);
					float right = (float) (x + gridSpacing / 2);
					float bottom = (float) (y + gridSpacing / 2);	
					// Log.d(TAG, "loc = " + loc.toString());
					
					canvas.drawRect(scaleX * left, -scaleY * bottom,
							scaleX * right, -scaleY * top, paint);

					if (scaleX >= 20) {
						if (bwScale < 0.5)
							paint.setColor(Color.WHITE);
						else
							paint.setColor(Color.BLACK);
						canvas.drawText(String.format("%.2f", prob),
								(float) (scaleX * x),
								(float) (-scaleY * y), paint);
					}					
				}
			}
		} else {
			Log.e(TAG, "probability map is null, drawing skipped");
		}
	}

	@Override
	public String toString() {
		return mParticlePosition.toString();
	}

}
