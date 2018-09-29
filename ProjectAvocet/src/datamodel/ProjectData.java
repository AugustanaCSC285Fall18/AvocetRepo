package datamodel;

import java.util.List;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class ProjectData {
	private List<AnimalTrack> tracks;
	private Video video;
	private List<AnimalTrack> unassignedSegments;

	public ProjectData(String videoFilePath) throws FileNotFoundException {
		video = new Video(videoFilePath);
		tracks = new ArrayList<>();
		unassignedSegments = new ArrayList<>();
	}
	
	public Video getVideo() {
		return video;
	}
	
	public List<AnimalTrack> getTracks(){
		return tracks;
	}
	
	public List<AnimalTrack> getUnassignedSegments() {
		return unassignedSegments;
	}

}
