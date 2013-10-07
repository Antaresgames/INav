package cz.muni.fi.sandbox.service.wifi;

import cz.muni.fi.sandbox.service.wifi.RssFingerprintController.State;

public interface RssFingerprintControllerStateChangeListener {
	public void stateChange(State mState);
}
