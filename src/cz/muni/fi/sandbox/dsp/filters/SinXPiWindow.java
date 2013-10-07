package cz.muni.fi.sandbox.dsp.filters;

public class SinXPiWindow extends SignalWindow {

	public SinXPiWindow(int size) {
		super(size);
		for (int i = 0; i < size; i++) {
			signal[i] = Math.sin(Math.PI * i / size);
		}
	}
}
