package cz.muni.fi.sandbox.utils.geographical;

/**
 * Class representing geographical location in the WGS84 coordinate system.
 *
 */
public class Wgs84Location {
	
	private double latitude; // in degrees
	private double longitude; // in degrees
	private double altitude; // in meters
	
	public Wgs84Location(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.altitude = 500.0;
	}
	
	public Wgs84Location(double[] latitude, double[] longitude) {
		
		assert(latitude.length == 3);
		assert(longitude.length == 3);
		
		this.latitude = latitude[0] + latitude[1] / 60.0 + latitude[2] / 3600.0;
		this.longitude = longitude[0] + longitude[1] / 60.0 + longitude[2] / 3600.0;
		this.altitude = 500.0;
	}
	
	public Wgs84Location(double latitude, double longitude, double altitude) {
		this(latitude, longitude);
		this.altitude = altitude;
	}
	
	@Override
	public String toString() {
		return getLatitude() + " " + getLongitude();
	}

	
	public double getLatitude() {
		return latitude;
	}
	
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	
	public double getLongitude() {
		return longitude;
	}
	
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getAltitude() {
		return altitude;
	}

	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}
}

