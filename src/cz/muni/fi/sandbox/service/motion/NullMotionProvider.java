package cz.muni.fi.sandbox.service.motion;

import android.content.Context;

public class NullMotionProvider extends MotionProvider {

	float orientation = 0.0f;
	float[] mCoords = {0.0f, 0.0f};
	
	final double stepSize = 1.0;
	double lastTimestamp = 0.0;
	double dt = 0.0;

	public NullMotionProvider(Context context) {
		
	}

	public void onSensorChanged() {
		notifyPositionUpdate(0.0f, 0.0f, 0.0f);
	}
	
	public double[] getPosition() {
		double retval[] = { mCoords[0], mCoords[1] };
		return retval;
	}

	public void resume() {

	}

	public void stop() {

	}

	@Override
	public void setPaused(boolean paused) {
		// TODO Auto-generated method stub
		
	}
}
