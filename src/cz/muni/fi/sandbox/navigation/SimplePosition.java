package cz.muni.fi.sandbox.navigation;

import android.content.SharedPreferences;
import cz.muni.fi.sandbox.buildings.Building;
import cz.muni.fi.sandbox.service.motion.MotionProvider;

public class SimplePosition implements PositionModel {
	private PositionModelUpdateListener mPositionModelListener;
	private MotionProvider mProvider;
	
	private float mHeading;
	private float[] mCoords;
	
	public float getHeading() {
		return mHeading;
	}
	public void setHeading(float hdg) {
		mHeading = hdg;
	}
	public float[] getCoordinates() {
		return mCoords;
	}
	public void setCoordinates(float [] coords) {
		mCoords[0] = coords[0];
		mCoords[1] = coords[1];
 	}
	@Override
	public void setPosition(float posX, float posY, int area) {
		setCoordinates(new float[] {posX, posY});
	}
	
	@Override
	public void setBuilding(Building building) {
		
	}
	
	public SimplePosition(PositionModelUpdateListener listener) {
		this.mPositionModelListener = listener;
		mCoords = new float [] {0.0f, 0.0f};
	}
	
	public void setPositionProvider(MotionProvider provider) {
		if (mProvider != null) {
			mProvider.unregister(this);
		}
		this.mProvider = provider;
		mProvider.register(this);
	}
	
	@Override
	public void positionChanged(float heading, float dx, float dy) {
		mHeading = heading;
		mCoords[0] += dx;
		mCoords[1] += dy;
		mPositionModelListener.updatePosition(this);
	}
	
	public String toString() {
		return "position(hdg:" + String.format("%.2f", 180 * mHeading / Math.PI)
			+ ", x:" + String.format("%.2f", mCoords[0])
			+ ", y:" + String.format("%.2f", mCoords[1]) + ")";
	}
	@Override
	public PositionRenderer getRenderer() {
		return new SimplePositionRenderer(this);
	}
	@Override
	public void floorChanged(int floor) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void updatePreferences(SharedPreferences prefs) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public double getX() {
		return (double)mCoords[0];
	}
	@Override
	public double getY() {
		return (double)mCoords[1];
	}

}
