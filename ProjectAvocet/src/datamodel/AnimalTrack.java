package datamodel;

import java.util.ArrayList;
import java.util.List;

public class AnimalTrack {
	private String animalID;
	private static final String UNNAMED_ID = "<<unassigned>>";
	private List<TimePoint> positions;
	
	/**
	 * 
	 * @param id names of the chicks to be track inputted by the user
	 * Adds the points of the chick to an Arraylist
	 */
	public AnimalTrack(String id) {
		this.animalID = id;
		positions = new ArrayList<TimePoint>();
	}
	/**
	 * 
	 * @param pt adds point to the arraylist
	 */
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
	
	/**
	 * 
	 * @param startFrame start frame to calculate
	 * @param endFrame end frame to calculate
	 * @return the timepoints in the range of the two inputted
	 * 
	 * Finds and returns timepoints in the range of the two timepoints
	 */
	public List<TimePoint> getTimePointsInRange(int startFrame, int endFrame) {
		List<TimePoint> timepoints = new ArrayList<TimePoint>();
		for (int i = startFrame; i <= endFrame; i++) {
			timepoints.add(this.getTimePointAtTime(i));
		}
		return timepoints;
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
	
	/**
	 * 
	 * @param startFrameNum start frame to calulate
	 * @param endFrameNum end frame to calculate
	 * @return the timepoints within the two
	 * 
	 * Finds the timepoints within an interval of the two inputted time points
	 */
	public List<TimePoint> getTimePointsWithinInterval(int startFrameNum, int endFrameNum) {
		List<TimePoint> pointsInInterval = new ArrayList<>();
		for (TimePoint pt : positions) {
			if (pt.getFrameNum() >= startFrameNum && pt.getFrameNum() <= endFrameNum) {
				pointsInInterval.add(pt);
			}
		}
		return pointsInInterval;
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
