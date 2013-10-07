package cz.muni.fi.sandbox.service.motion;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public abstract class  MotionProvider {
	
	protected final static String TAG = "PositionProvider";
	
	protected List<MotionUpdateListener> mListeners;
	
	public static MotionProvider providerFactory(String provider, Context context) {
		Log.d(TAG, "providerFactory: " + provider);
		if (provider.equals("sensor")) {
			Log.d(TAG, "creating sensor provider");
			return new SensorMotionProvider(context);
		} else if (provider.equals("random")) {
			Log.d(TAG, "creating random provider");
			return new RandomMotionProvider(context);
		}
		Log.d(TAG, "creating null provider");
		return new NullMotionProvider(context);
	}
	
	public MotionProvider() {
		mListeners = new ArrayList<MotionUpdateListener>();
	}
	
	public void register(MotionUpdateListener listener) {
		mListeners.add(listener);
	}

	public void unregister(MotionUpdateListener listener) {
		mListeners.remove(listener);
	}

	protected void notifyPositionUpdate(float orientation, float posX,
			float posY) {
		for (MotionUpdateListener listener : mListeners) {
				listener.positionChanged(orientation, posX, posY);
		}
	}

	
	public abstract void resume();
	public abstract void stop();
	public abstract void setPaused(boolean paused);
	public void reset() {}
	public void updatePreferences(SharedPreferences prefs) {}
	
}
