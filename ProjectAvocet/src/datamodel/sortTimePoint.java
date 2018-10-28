package datamodel;

import java.util.Comparator;

public class sortTimePoint implements Comparator<TimePoint> {

	@Override
	public int compare(TimePoint tp1, TimePoint tp2) {
		if (tp1.getFrameNum() > tp2.getFrameNum()) {
			return 1;
		}
		else if (tp1.getFrameNum() < tp2.getFrameNum()) {
			return -1;
		}
		else {
			return 0;
		}
	}

}
