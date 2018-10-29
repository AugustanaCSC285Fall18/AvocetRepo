package datamodel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import csv.CSVUtils;

public class ProjectData {
	private Video video;
	private List<AnimalTrack> tracks;
	private List<AnimalTrack> unassignedSegments;
	private int calibrations;
	
	public ProjectData(String videoFilePath) throws FileNotFoundException {
		video = new Video(videoFilePath);
		tracks = new ArrayList<>();
		unassignedSegments = new ArrayList<>();
		calibrations = 0;
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
	
	/**
	 * creates a CSV file of the chicks and their TimePoints along with the distance from the origin for each TimePoint
	 * @param file - the file that will be exported
	 * @return - the FileWriter with the file that will be exported
	 */
	public FileWriter exportProject(File file) {
		//Export Code from: https://www.mkyong.com/java/how-to-export-data-to-csv-file-java/
		try {
			FileWriter writer = new FileWriter(file);
			CSVUtils.writeLine(writer, Arrays.asList("Chick ID", "X-Coordinate", "Y-Coordinate", "Frame Number", "Distance From Origin(cm)"), ',');
			for (int i = 0; i < tracks.size(); i++) {
				for (int j = 0; j < tracks.get(i).getPositions().size(); j++) {
					TimePoint currentTimePoint = tracks.get(i).getPositions().get(j);
					String name = "" + tracks.get(i).getID();
					String x = "" + ((currentTimePoint.getX()-video.getOrigin().x)*video.getXPixelsPerCm());
					String y = "" + ((currentTimePoint.getY()-video.getOrigin().y)*video.getYPixelsPerCm()*-1);
					String frame = "" + currentTimePoint.getFrameNum();
					String distanceFromOrigin = "" + (currentTimePoint.getDistanceTo(video.getOrigin().x, video.getOrigin().y))
							*video.getAvgPixelsPerCm();
					CSVUtils.writeLine(writer, Arrays.asList(name, x, y, frame, distanceFromOrigin), ',');
				}
			}
			writer.flush();
			writer.close();
			return writer;
		} catch (Exception e) {
			return null;
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
	
	public int getNumCalibrations() {
		return calibrations;
	}
	
	public void setCalibrations(int changeCalibrations) {
		calibrations = changeCalibrations;
	}
	
	/**
	 * @param file - file that will be exported
	 * @return the FileWriter with the file that will be exported
	 * creates a CSV file of the total distance traveled by each chick
	 */
	public FileWriter exportTotalDistance(File file) throws IOException {
		FileWriter writer = new FileWriter(file);
		CSVUtils.writeLine(writer, Arrays.asList("Chick ID", "Total Distance in Centimeters"), ',');
		for (AnimalTrack track: tracks) {
			double totalDistance = 0;
			for (int i = 0; i < track.getPositions().size()-1; i++) {
				totalDistance += track.getPositions().get(i).getDistanceTo(track.getPositions().get(i+1));
			}
			String name = track.getID();
			String distance = ""+(totalDistance/video.getAvgPixelsPerCm());
			CSVUtils.writeLine(writer, Arrays.asList(name, distance), ',');
		}
		writer.flush();
		writer.close();
		return writer;
	}
}

