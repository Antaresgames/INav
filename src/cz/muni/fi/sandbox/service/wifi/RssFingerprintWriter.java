package cz.muni.fi.sandbox.service.wifi;

import java.util.Map.Entry;

import cz.muni.fi.sandbox.service.wifi.RssFingerprint.RssMeasurement;
import cz.muni.fi.sandbox.utils.Writer;

public class RssFingerprintWriter extends Writer {
	
	public RssFingerprintWriter(String fileName) {
		super(fileName);
	}
	
	public void appendFingerprint(RssFingerprint fingerprint) {
		openAppend();
		
		writeln("fingerprint: loc: " + fingerprint.getLocation());
		System.out.println("fingerprint: loc: " + fingerprint.getLocation());
		for (Entry<String, RssMeasurement> entry: fingerprint.getVector().entrySet()) {
			writeln("entry: " + entry.getKey() + " " + entry.getValue().getRss());
			System.out.println("entry: " + entry.getKey() + " " + entry.getValue().getRss());
		}
		
		close();
	}
}
