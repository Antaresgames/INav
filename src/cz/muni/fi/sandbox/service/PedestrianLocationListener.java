package cz.muni.fi.sandbox.service;

import cz.muni.fi.sandbox.buildings.Building;
import android.os.Bundle;

public interface PedestrianLocationListener {
	
	void onLocationChanged(PedestrianLocation location);
	void onStatusChanged(String provider, int status, Bundle extras);
	void onBuildingChanged(Building building);
}
