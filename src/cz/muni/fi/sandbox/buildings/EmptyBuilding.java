package cz.muni.fi.sandbox.buildings;

public class EmptyBuilding extends Building {

	private static final long serialVersionUID = -2463842994477296150L;

	public EmptyBuilding() {
		
		addArea(0, "Floor", new EmptyArea());
	}
}
