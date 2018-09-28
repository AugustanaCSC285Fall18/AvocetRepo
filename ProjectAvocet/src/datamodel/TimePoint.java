package datamodel;

import org.opencv.core.Point;

public class TimePoint {
	private Point pt; // location
	private int frameNum; // time (measured in frames)

	public TimePoint(double x, double y, int frameNum) {
		pt = new Point(x, y);
		this.frameNum = frameNum;
	}

	public double getX() {
		return pt.x;
	}

	public double getY() {
		return pt.y;
	}

	public int getFrameNum() {
		return frameNum;
	}

	public String toString() {
		return "(" + pt.x + "," + pt.y + "@T=" + frameNum + ")";
	}

	public double getDistanceTo(TimePoint other) {
		return Math.sqrt(pt.x * pt.x + pt.y * pt.y);
	}

}
