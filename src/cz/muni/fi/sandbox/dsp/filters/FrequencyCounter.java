package cz.muni.fi.sandbox.dsp.filters;
import java.util.LinkedList;
import java.util.Queue;


public class FrequencyCounter {
	
	private int window;
	private Queue<Long> queue;
	private double rate = 0;
	private static final double NANO = Math.pow(10,9);
	
	public FrequencyCounter(int window) {
		this.window = window;
		queue = new LinkedList<Long>();
	}
	
	public void push(long timestamp) {
		
		int size = queue.size();
		
		if (size > 0) {
			long tmp = queue.peek().longValue();
			if (size == window) {
				queue.poll();
				size--;
			}
			double deltaTime = (double)(timestamp - tmp) / NANO;
			rate = size / deltaTime;
		}
		
		boolean offered = queue.offer(timestamp);
		assert(offered);
	}
	
	public double getRate() {
		return rate;
	}
	
	public float getRateF() {
		return (float)rate;
	}
	
}
