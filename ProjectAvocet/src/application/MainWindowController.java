package application;


import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import datamodel.*;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class MainWindowController {
	
	@FXML private ImageView myImageView;
	@FXML private Slider sliderSeekBar;
	@FXML private Button pausePlay;
	@FXML private ComboBox<String> chickSelect;
	@FXML private Button confirm;
	@FXML private Button newChick;
	@FXML private TextField chickName;
	@FXML private Label tracking;
	@FXML private Button export;
	@FXML private Canvas canvas;
	
	private VideoCapture vidCap = new VideoCapture();
	private ProjectData project;
	private ObservableList<String> chickIDs = FXCollections.observableArrayList();
	private int chosenChick;
	private List<AnimalTrack> track = new ArrayList<AnimalTrack>();
	
	@FXML public void initialize() {
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
	 
	public void startVideo(String filePath) {
			vidCap.open(filePath);
			sliderSeekBar.setMax(vidCap.get(Videoio.CV_CAP_PROP_FRAME_COUNT)-1);
			handleSlider();
			displayFrame();
	}

	@FXML 
	protected void handleSlider() {
		sliderSeekBar.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
				if (sliderSeekBar.isValueChanging()) {
					vidCap.set(Videoio.CAP_PROP_POS_FRAMES, arg2.intValue());
					displayFrame();
				}
			}
		});
	}

	protected void displayFrame() {
		Mat frame = new Mat();
		vidCap.read(frame);
		MatOfByte buffer = new MatOfByte();
		Imgcodecs.imencode(".png", frame, buffer);
		Image currentFrameImage = new Image(new ByteArrayInputStream(buffer.toArray()));
		Platform.runLater(new Runnable() {
			public void run() {
				myImageView.setImage(currentFrameImage);
			}
		});
	}
	
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
	
	@FXML public void exportData() {
		
	}
}
