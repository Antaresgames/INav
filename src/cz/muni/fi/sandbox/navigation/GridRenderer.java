package cz.muni.fi.sandbox.navigation;

import android.graphics.Canvas;

public abstract class GridRenderer {

	protected float scaleX, scaleY;
	
	public void setScale(float scale) {
		this.scaleX = scale;
		this.scaleY = scale;
	}

	public void setScale(float scaleX, float scaleY) {
		this.scaleX = scaleX;
		this.scaleY = scaleY;
	}

	public abstract void draw(Canvas canvas);

}
