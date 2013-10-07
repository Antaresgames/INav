package cz.muni.fi.sandbox.service.wifi;

import cz.muni.fi.sandbox.buildings.Building;

public class BuildingFingerprintAdaptor implements IGetsYouRssFingerprints {
	 
	private Building building; 
	
	public void setBuilding(Building building) {
		this.building = building;
	}
	
	public WifiLayerModel getRssFingerprints() {
		return building.getCurrentArea().getRssFingerprints();
	}
}
