package cz.muni.fi.sandbox.navigation;

import android.content.SharedPreferences;
import cz.muni.fi.sandbox.buildings.Building;
import cz.muni.fi.sandbox.service.motion.MotionProvider;
import cz.muni.fi.sandbox.service.motion.MotionUpdateListener;


public interface PositionModel extends MotionUpdateListener {
	public PositionRenderer getRenderer();
	public void setPositionProvider(MotionProvider provider);
	public void setPosition(float posX, float posY, int area);
	public void setBuilding(Building building);
	public float getHeading();
	public void updatePreferences(SharedPreferences prefs);
	
	public double getX();
	public double getY();
}
