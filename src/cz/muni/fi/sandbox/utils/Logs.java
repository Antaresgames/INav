package cz.muni.fi.sandbox.utils;

import java.util.LinkedList;
import java.util.Queue;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Logs {
	private Queue<String> logs;
	private int size;
	
	public Logs() {
		logs = new LinkedList<String>();
	}

	public void setSize(int size) {
		this.size = size;
		while (logs.size() > size) {
			logs.poll();
		}
	}

	public void add(String line) {
		if (logs.size() == size)
			logs.poll();
		logs.add(line);
	}

	public void draw(Canvas canvas, float locX, float locY) {
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(Color.WHITE);

		float y = locY;
		for (String line : logs) {
			canvas.drawText(line, locX, y, paint);
			y += 10;
		}
	}
}
