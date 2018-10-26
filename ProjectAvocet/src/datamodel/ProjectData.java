package datamodel;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import csv.CSVUtils;

public class ProjectData {
	private Video video;
	private List<AnimalTrack> tracks;
	private List<AnimalTrack> unassignedSegments;
	
	public ProjectData(String videoFilePath) throws FileNotFoundException {
		video = new Video(videoFilePath);
		tracks = new ArrayList<>();
		unassignedSegments = new ArrayList<>();
	}

	public Video getVideo() {
		return video;
	}
	
	public List<AnimalTrack> getTracks() {
		return tracks;
	}

	public List<AnimalTrack> getUnassignedSegments() {
		return unassignedSegments;
	}
	
	public void addTrack(AnimalTrack track) {
		tracks.add(track);
	}
	
	public List<AnimalTrack> getUnassignedSegmentsInRange(double x, double y, 
            int startFrame, int endFrame, double distanceRange) {
		// FIXME: find and return the correct list of segments (see Javadoc comment above)
		List<AnimalTrack> inRange = new ArrayList<AnimalTrack>();
		for (int i = 0; i < unassignedSegments.size(); i++) {
			List<TimePoint> testList = unassignedSegments.get(i).getTimePointsWithinInterval(startFrame, endFrame);
			if (!testList.isEmpty()) {
				for (int j = 0; j < testList.size(); j++) {
					if (testList.get(j).getDistanceTo(x, y) < distanceRange) {
						inRange.add(unassignedSegments.get(i));
						break;
					}
				}
			}
		}
		return inRange;
	}
	
	public void exportProject() {
		String csvFile = "chicks.csv";
		try {
			FileWriter writer = new FileWriter(csvFile);
			CSVUtils.writeLine(writer, Arrays.asList("Chick ID", "X-Coordinate", "Y-Coordinate", "Frame Number"), ',');
			for (int i = 0; i < tracks.size(); i++) {
				for (int j = 0; j < tracks.get(i).getPositions().size(); j++) {
					TimePoint currentTimePoint = tracks.get(i).getPositions().get(j);
					String name = "" + tracks.get(i).getID();
					String x = "" + currentTimePoint.getX();
					String y = "" + currentTimePoint.getY();
					String frame = "" + currentTimePoint.getFrameNum();
					CSVUtils.writeLine(writer, Arrays.asList(name, x, y, frame), ',');
				}
			}
			writer.flush();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
