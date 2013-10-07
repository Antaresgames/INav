package cz.muni.fi.sandbox.dsp.filters;

import java.util.LinkedList;
import java.util.Queue;

public class MovingAverage implements IDataSink {
	
	private int window;
	private Queue<Double> queue;
	private double average;
	
	public MovingAverage(int window) {
		this.window = window;
		queue = new LinkedList<Double>();
		average = 0.0;
	}
	
	public void push(double value) {
		int size = queue.size();
		assert(size <= window);
		double sum = average * size;
		
		if (size == window) {
			double tmp = queue.poll().doubleValue();
			sum -= tmp;
			size--;
			//System.out.println("tmp = " + tmp);
		}
		average = ( sum + value ) / (size + 1);
		boolean retval = queue.offer(value);
		assert(retval);
		//System.out.println("average = " + average);
		//System.out.println("sum = " + sum);
		//System.out.println("size = " + size);
		//System.out.println("value = " + value);
		//System.out.println(retval);
	}
	
	public double getAverage() {
		return average;
	}
	
}
