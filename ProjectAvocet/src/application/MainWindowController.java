package application;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import datamodel.Video;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import Autotrack.AutoTrackListener;
import Autotrack.Autotrackable;
import datamodel.ProjectData;
import datamodel.AnimalTrack;
import datamodel.TimePoint;
import utils.UtilsForOpenCV;

public class MainWindowController implements AutoTrackListener{
	
	@FXML private ImageView myImageView;
	@FXML private ImageView videoView;
	@FXML private Slider sliderSeekBar;
	@FXML private ProgressBar progressAutoTrack;
	@FXML private Slider sliderVideoTime;
	@FXML private TextField textFieldCurFrameNum;
	@FXML private Button pausePlay;
	@FXML private TextField textfieldStartFrame;
	@FXML private TextField textfieldEndFrame;
	@FXML private Button btnAutotrack;
	
	private Video video;
	private VideoCapture vidCap = new VideoCapture();
	private Autotrackable autotracker;
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
	
	public void loadVideo(String filePath) {
		try {
			project = new ProjectData(filePath);
			Video video = project.getVideo();
			sliderVideoTime.setMax(video.getTotalNumFrames()-1);
			//showFrameAt(0);
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
		}

	}
	
	@FXML
	public void handleStartAutotracking() throws InterruptedException {
		if (autotracker == null || !autotracker.isRunning()) {
			Video video = project.getVideo();
			video.setStartFrameNum(Integer.parseInt(textfieldStartFrame.getText()));
			video.setEndFrameNum(Integer.parseInt(textfieldEndFrame.getText()));
			this.autotracker = new Autotrackable();
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
	
	public void createVideo(String filePath) throws FileNotFoundException {
		video = new Video(filePath);
	}
	
	// this method will get called repeatedly by the Autotracker after it analyzes each frame
		@Override
		public void handleTrackedFrame(Mat frame, int frameNumber, double fractionComplete) {
			Image imgFrame = UtilsForOpenCV.matToJavaFXImage(frame);
			// this method is being run by the AutoTracker's thread, so we must
			// ask the JavaFX UI thread to update some visual properties
			Platform.runLater(() -> { 
				videoView.setImage(imgFrame);
				progressAutoTrack.setProgress(fractionComplete);
				sliderVideoTime.setValue(frameNumber);
				textFieldCurFrameNum.setText(String.format("%05d",frameNumber));
			});		
		}

		@Override
		public void trackingComplete(List<AnimalTrack> trackedSegments) {
			project.getUnassignedSegments().clear();
			project.getUnassignedSegments().addAll(trackedSegments);

			for (AnimalTrack track: trackedSegments) {
				System.out.println(track);
//				System.out.println("  " + track.getPositions());
			}
			Platform.runLater(() -> { 
				progressAutoTrack.setProgress(1.0);
				btnAutotrack.setText("Start auto-tracking");
			});	
		}
}
