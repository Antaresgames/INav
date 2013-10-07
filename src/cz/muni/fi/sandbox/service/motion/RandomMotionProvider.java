package cz.muni.fi.sandbox.service.motion;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

public class RandomMotionProvider extends MotionProvider {

	private float mOrientation = 0.0f;
	private float[] mCoords;

	private final double stepSize = 1.0;
	private final int tick = 100; // in milliseconds
	private boolean mPaused = false;

	private Thread mWorkerThread = null;

	private class WorkerThread implements Runnable {
		private RandomMotionProvider mProv;

		public WorkerThread(RandomMotionProvider prov) {
			mProv = prov;
		}

		public void run() {
			while (true) {
				SystemClock.sleep(tick);
				Log.d(TAG, "worker thread tick.");
				mProv.onSensorChanged();
			}
		}
	}

	public RandomMotionProvider(Context context) {
	}

	public void onSensorChanged() {
		synchronized (this) {
			mOrientation = (float) (Math.random() * 2 * Math.PI);
			if (!mPaused) {
				notifyPositionUpdate(mOrientation,
						(float)(stepSize * Math.sin(mOrientation)),
						(float)(stepSize * Math.cos(mOrientation)));
			}
		}

	}

	public double[] getPosition() {
		double retval[] = { mCoords[0], mCoords[1] };
		return retval;
	}

	public void resume() {

		mWorkerThread = new Thread(new WorkerThread(this));
		mWorkerThread.start();
	}

	public void stop() {
		mWorkerThread.stop();
	}

	@Override
	public void setPaused(boolean paused) {
		// TODO Auto-generated method stub
		mPaused = paused;
	}
}
