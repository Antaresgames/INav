package cz.muni.fi.sandbox.service.motion;

public interface MotionUpdateListener {
	//public void positionChanged(double heading, double x, double y);
	public void positionChanged(float heading, float x, float y);
	public void floorChanged(int floor);
}
