package datamodel;

import java.util.ArrayList;
import java.util.List;

public class AnimalTrack {
	public static final String UNNAMED_ID = "<<unassigned>>"; 
	
	private String animalID;
	
	private List<TimePoint> positions;
	
	public AnimalTrack() {
		positions = new ArrayList<TimePoint>();
	}
	
	public AnimalTrack(String name) {
		animalID = name;
	}
	
	public boolean hasIDAssigned() {
		return !animalID.equals(UNNAMED_ID);
	}
	
	public List<TimePoint> getPositions() {
		return positions;
	}

	public void createTimePoint(double x, double y, int frameNum) {
		positions.add(new TimePoint(x,y,frameNum));
	}
	
	public String toString() {
		return "AnimalTrack[id="+ animalID + ",len=" + positions.size()+"]"; 
	}
}
