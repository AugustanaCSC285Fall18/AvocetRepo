package datamodel;

import java.util.ArrayList;
import java.util.List;

public class AnimalTrack {
	private String animalID;
	private static final String UNNAMED_ID = "<<unassigned>>";
	private List<TimePoint> positions;
	
	public AnimalTrack(String id) {
		this.animalID = id;
		positions = new ArrayList<TimePoint>();
	}
	
	public void add(TimePoint pt) {
		positions.add(pt);
	}
	
	public boolean hasIDAssigned() {
		return !animalID.equals(UNNAMED_ID);
	}
	
	public String getID() {
		return animalID;
	}
	
	public TimePoint getTimePointAtIndex(int index) {
		return positions.get(index);
	}
	
	public List<TimePoint> getPositions() {
		return positions;
	}

	/**
	 * Returns the TimePoint at the specified time, or null
	 * @param frameNum
	 * @return
	 */
	
	public TimePoint getTimePointAtTime(int frameNum) {
		//TODO: This method's implementation is inefficient [linear search is O(N)]
		//      Replace this with binary search (O(log n)] or use a Map for fast access
		for (TimePoint pt : positions) {
			if (pt.getFrameNum() == frameNum) {
				return pt;
			}
		}
		return null;
	}
	
	public TimePoint getFinalTimePoint() {
		return positions.get(positions.size()-1);
	}
	
	public String toString() {
		return "AnimalTrack[id="+ animalID + ",len=" + positions.size()+"]";

		//int startFrame = positions.get(0).getFrameNum();
		//int endFrame = getFinalTimePoint().getFrameNum();
		//return "AnimalTrack[id="+ animalID + ",numPts=" + positions.size()+" start=" + startFrame + " end=" + endFrame +"]"; 
	}
}
