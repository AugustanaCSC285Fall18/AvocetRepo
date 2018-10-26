package datamodel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import csv.CSVUtils;
import javafx.stage.Stage;

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
	
	public void addTrack(AnimalTrack track) {
		tracks.add(track);
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
	
	public void saveToFile(File saveFile) throws FileNotFoundException {
		String json = toJSON();
		PrintWriter out = new PrintWriter(saveFile);
		out.print(json);
		out.close();
	}
	
	public String toJSON() {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();		
		return gson.toJson(this);
	}
	
	public static ProjectData loadFromFile(File loadFile) throws FileNotFoundException {
		String json = new Scanner(loadFile).useDelimiter("\\Z").next();
		return fromJSON(json);
	}
	
	public static ProjectData fromJSON(String jsonText) throws FileNotFoundException {
		Gson gson = new Gson();
		ProjectData data = gson.fromJson(jsonText, ProjectData.class);
		data.getVideo().connectVideoCapture();
		return data;
	}
}

