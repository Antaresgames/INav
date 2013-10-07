package cz.muni.fi.sandbox.dsp.filters;

import java.util.LinkedList;
import java.util.Queue;

public class DelayQueue implements IDataSink {
	
	private int window;
	private Queue<Double> queue;

	public DelayQueue(int window) {
		this.window = window;
		queue = new LinkedList<Double>();
	}
	
	public void push(double value) {
		
		while (queue.size() > window) {
			queue.poll();
		}
		boolean retval = queue.offer(value);
		assert(retval);
	}
	
	public double getValue() {
		if (queue.size() <= window) {
			return 0.0;
		}
		return queue.peek().doubleValue();
	}
}

