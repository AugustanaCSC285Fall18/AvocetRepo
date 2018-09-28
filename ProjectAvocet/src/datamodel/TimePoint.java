package datamodel;

import java.awt.Point;

public class TimePoint {
	private double x;     // location
	private double y;      
	private int frameNum; // time (measured in frames)
	
	public TimePoint(double x, double y, int frameNum) {
		this.x = x;
		this.y = y;
		this.frameNum = frameNum;
	}
	
	public double getX() {
		return x;
	}
	
	public void setX(double x) {
		this.x = x;
	}
	
	public double getY() {
		return y;
	}
	
	public void setY(double y) {
		this.y = y;
	}

	public int getFrameNum() {
		return frameNum;
	}

	public String toString() {
		return "("+ x +","+ y +"@T="+frameNum +")";
	}

	public double getDistanceTo(TimePoint other) {
		double dx = other.x-x;
		double dy = other.y-y;
		return Math.sqrt(dx*dx+dy*dy);
	}
	
	public int getTimeDiffAfter(TimePoint other) {
		return this.frameNum - other.frameNum;
	}
}
