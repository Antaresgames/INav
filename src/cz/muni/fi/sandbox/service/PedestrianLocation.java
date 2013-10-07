package cz.muni.fi.sandbox.service;

import cz.muni.fi.sandbox.navigation.PositionModel;

public class PedestrianLocation {
	
	public PedestrianLocation(PositionModel position) {
		mPosition = position;
	}
	
	public PositionModel getPosition() {
		return mPosition;
	}
	
	private PositionModel mPosition;
}
