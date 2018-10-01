package application;

import java.io.File;
import java.io.FileInputStream;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.Arrays;

import java.io.IOException;
import java.util.ArrayList;

import java.util.List;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import autotracking.*;
import datamodel.AnimalTrack;
import datamodel.ProjectData;
import datamodel.TimePoint;
import datamodel.Video;
import csv.*;
import datamodel.*;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;

import javafx.scene.control.ProgressBar;

import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.scene.layout.Pane;


import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import utils.UtilsForOpenCV;

import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;


public class MainWindowController implements AutoTrackListener{
	
	@FXML private ImageView myImageView;
	@FXML private Slider sliderVideoTime;

	@FXML private TextField textFieldCurFrameNum;
	@FXML private Button pausePlay;

	@FXML private TextField textfieldStartFrame;
	@FXML private TextField textfieldEndFrame;

	@FXML private Button btnAutotrack;
	@FXML private ProgressBar progressAutoTrack;

	@FXML private ComboBox<String> chickSelect;
	@FXML private Button confirm;
	@FXML private Button newChick;
	@FXML private TextField chickName;
	@FXML private Label tracking;
	@FXML private Button export;
	@FXML private Canvas canvas;

	
	private VideoCapture vidCap = new VideoCapture();
	private AutoTracker autotracker;
	private ProjectData project;
	private Stage stage;
	private ObservableList<String> chickIDs = FXCollections.observableArrayList();
	private int chosenChick;
	private List<AnimalTrack> track = new ArrayList<AnimalTrack>();
	
	@FXML public void initialize() {
		loadVideo("S:/class/cs/285/sample_videos/sample1.mp4");	
		sliderVideoTime.valueProperty().addListener((obs, oldV, newV) -> showFrameAt(newV.intValue())); 
		canvas.setOnMouseClicked((event) -> {
			TimePoint tp = new TimePoint(event.getX(), event.getY(), 0);
			track.get(chosenChick).add(tp);
			GraphicsContext gc = canvas.getGraphicsContext2D();
			gc.setFill(Color.RED);
		    gc.fillOval(event.getX(), event.getY(), 10, 10);
			System.out.println(tp);
			System.out.println(track.get(chosenChick).getID());
			System.out.println("drawn");
		});
		
	}
	
	public void initializeWithStage(Stage stage) {
		this.stage = stage;
		
		// bind it so whenever the Scene changes width, the myImageView matches it
		// (not perfect though... visual problems if the height gets too large.)
		myImageView.fitWidthProperty().bind(myImageView.getScene().widthProperty());  
	}
	
	@FXML
	public void handleBrowse()  {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Video File");
		File chosenFile = fileChooser.showOpenDialog(stage);
		if (chosenFile != null) {
			loadVideo(chosenFile.getPath());
		}		
	}
	

	public void loadVideo(String filePath) {
		try {
			project = new ProjectData(filePath);
			Video video = project.getVideo();
			sliderVideoTime.setMax(video.getTotalNumFrames()-1);
			showFrameAt(0);
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
		}

	}
	
	public void showFrameAt(int frameNum) {
		if (autotracker == null || !autotracker.isRunning()) {
			project.getVideo().setCurrentFrameNum(frameNum);
			Image curFrame = UtilsForOpenCV.matToJavaFXImage(project.getVideo().readFrame());
			myImageView.setImage(curFrame);
			
		}		
	}
	
	@FXML
	public void handleStartAutotracking() throws InterruptedException {
		if (autotracker == null || !autotracker.isRunning()) {
			Video video = project.getVideo();
			autotracker = new AutoTracker();
			// Use Observer Pattern to give autotracker a reference to this object, 
			// and call back to methods in this class to update progress.
			autotracker.addAutoTrackListener(this);
			
			// this method will start a new thread to run AutoTracker in the background
			// so that we don't freeze up the main JavaFX UI thread.
			autotracker.startAnalysis(video);
			btnAutotrack.setText("CANCEL auto-tracking");
		} else {
			autotracker.cancelAnalysis();
			btnAutotrack.setText("Start auto-tracking");
		}
		 
	}
	
	//public void createVideo(String filePath) throws FileNotFoundException {
	//	video = new Video(filePath);
	//}

	public void createProject(String filePath) throws FileNotFoundException {
		project = new ProjectData(filePath);
	}
	
	@FXML public void chooseChick() throws IOException {
		String choice = chickSelect.getValue();
		System.out.println(track.size());
		for (int i = 0; i < track.size(); i ++) {
			if (choice.equals(track.get(i).getID())) {
				setChick(i);
				}
		}
		tracking.setText("Tracking: " + track.get(chosenChick).getID());
	}
	
	@FXML public void createChick() {
		String name = chickName.getText();
		chickName.setText("");
		chickIDs.add(name);
		chickSelect.setItems(chickIDs);
		track.add(new AnimalTrack(name));
	}
	
	public ProjectData getProject() {
		return project;
	}
	
	public void setChick(int index) {
		chosenChick = index;
	}
	
	// this method will get called repeatedly by the Autotracker after it analyzes each frame
	@Override
	public void handleTrackedFrame(Mat frame, int frameNumber, double fractionComplete) {
		Image imgFrame = UtilsForOpenCV.matToJavaFXImage(frame);
		// this method is being run by the AutoTracker's thread, so we must
		// ask the JavaFX UI thread to update some visual properties
		Platform.runLater(() -> { 
			myImageView.setImage(imgFrame);
			progressAutoTrack.setProgress(fractionComplete);
			sliderVideoTime.setValue(frameNumber);
		});		
	}

	@Override
	public void trackingComplete(List<AnimalTrack> trackedSegments) {
		project.getUnassignedSegments().clear();
		project.getUnassignedSegments().addAll(trackedSegments);

		for (AnimalTrack track: trackedSegments) {
			System.out.println(track);
//			System.out.println("  " + track.getPositions());
		}
		Platform.runLater(() -> { 
			progressAutoTrack.setProgress(1.0);
			btnAutotrack.setText("Start auto-tracking");
		});	
		
	}
	
	
	
		
		@FXML public void exportData() throws IOException {
			String csvFile = "C:\\Users\\mikew\\chicks.csv";
			try {
				FileWriter writer = new FileWriter(csvFile);
				CSVUtils.writeLine(writer, Arrays.asList("Chick ID", "X-Coordinate", "Y-Coordinate", "Frame Number"), ',');
				for (int i = 0; i < track.size(); i++) {
					for (int j = 0; j < track.get(i).getPositions().size(); j++) {
						String x = "" + track.get(i).getPositions().get(j).getX();
						String y = "" + track.get(i).getPositions().get(j).getY();
						String frame = "" + track.get(i).getPositions().get(j).getFrameNum();
						CSVUtils.writeLine(writer, Arrays.asList(track.get(i).getID(), x, y, frame), ',');
					}
				}
				writer.flush();
				writer.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
}
