package application;


import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

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
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class MainWindowController {
	
	@FXML private ImageView myImageView;
	@FXML private Slider sliderSeekBar;
	@FXML private Button pausePlay;
	@FXML private ComboBox<String> chickSelect;
	@FXML private Button confirm;
	
	private VideoCapture vidCap = new VideoCapture();
	private ProjectData project;
	private ObservableList<String> chickIDs = FXCollections.observableArrayList("Track a new chick");
	
	@FXML public void initialize() {
		chickSelect.setValue("Track a new chick");
		chickSelect.setItems(chickIDs);
		myImageView.setOnMouseClicked((event) -> {
			TimePoint tp = new TimePoint(event.getX(), event.getY(), 0);
			System.out.println(tp);
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
		
		if (choice.equals("Track a new chick")) {
			createChick();
		} else {
			
		}
	}
	
	public void createChick() throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("NewChick.fxml"));
		AnchorPane root = (AnchorPane)loader.load();
	
		Scene nextScene = new Scene(root,root.getPrefWidth(),root.getPrefHeight());
		nextScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
	
		Stage primary = (Stage) confirm.getScene().getWindow();
		primary.setScene(nextScene);
	}
	
	public void addTimePoint() {
		
	}
}
