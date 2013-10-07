package cz.muni.fi.sandbox.utils;

import android.graphics.Color;
import android.util.Log;

/**
 * Class ColorRamping for converting a number from <0, 1.0) interval to a rgb colour on the
 * chosen scale.
 * 
 * Inspired by http://paulbourke.net/texture_colour/colourramp/
 * 
 * @author Michal Holcik
 * 
 */

public class ColorRamping {

	public static int blackToWhiteRamp(double scale) {
		double red = 255 * scale;
		double green = 255 * scale;
		double blue = 255 * scale;
		return Color.rgb((int)red, (int)green, (int)blue);
	}
	
	public static int blueToRedRamp(double scale) {
		double red = 255 * scale;
		double green = 0;
		double blue = 255 * (1 - scale);
		return Color.rgb((int)red, (int)green, (int)blue);
	}
	
	public static int temperatureRamp(double scale) {
		int red = 0, green = 0, blue = 0;
		if (scale < 0.25) {
			scale = 4 * (scale);
			red = 0;
			green = (int)(255 * (scale));
			blue = 255;
		} else if (scale < 0.5) {
			scale = 4 * (scale - 0.25);
			red = 0;
			green = 255;
			blue = (int)(255 * (1 - scale));
		} else if (scale < 0.75) {
			scale = 4 * (scale - 0.50);
			red = (int)(255 * scale);
			green = 255;
			blue = 0;
		} else if (scale < 1.0) {
			scale = 4 * (scale - 0.75);
			red = 255;
			green = (int)(255 * (1 - scale));
			blue = 0;
		}
		System.out.println("rgb = (" + red + ", " + green + ", " + blue + ")");
		return Color.rgb(red, green, blue);
	}
}
