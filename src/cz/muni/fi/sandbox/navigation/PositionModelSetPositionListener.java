package cz.muni.fi.sandbox.navigation;

public class PositionModelSetPositionListener implements SetPositionListener {
	PositionModel model; 
	public PositionModelSetPositionListener(PositionModel model) {
		this.model = model;
	}
	
	public void setPosition(float x, float y, int area) {
		model.setPosition(x, y, area);
	}
}
