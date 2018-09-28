package application;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import datamodel.*;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class MainWindowController {
	
	@FXML private ImageView myImageView;
	@FXML private Slider sliderSeekBar;
	@FXML private Button pausePlay;
	
	private VideoCapture vidCap = new VideoCapture();
	private ProjectData project;
	
	
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
}
