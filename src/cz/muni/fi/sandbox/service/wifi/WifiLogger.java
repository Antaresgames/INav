package cz.muni.fi.sandbox.service.wifi;

public interface WifiLogger {
	public void pushMessage(String message);
	public void newFingerprintAvailable(RssFingerprint fingerprint);
	public void bestMatchFingerprint(RssFingerprint fingerprint, RssFingerprint bestMatch);
}
