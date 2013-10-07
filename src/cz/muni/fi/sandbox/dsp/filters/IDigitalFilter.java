package cz.muni.fi.sandbox.dsp.filters;

public interface IDigitalFilter<T> {
	public void push(T value);
	public T pop();
}
