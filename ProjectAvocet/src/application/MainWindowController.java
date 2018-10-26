package application;

import java.io.File;
import java.io.FileInputStream;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.Arrays;

import java.io.IOException;

import java.util.List;

import org.opencv.core.Mat;

import autotracking.*;
import datamodel.AnimalTrack;
import datamodel.ProjectData;
import datamodel.TimePoint;
import datamodel.Video;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
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
import utils.UtilsForOpenCV;

import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class MainWindowController implements AutoTrackListener {

	@FXML
	private ImageView myImageView;
	@FXML
	private Slider sliderVideoTime;

	@FXML
	private Label labelCurFrameNum;
	@FXML
	private Button pausePlay;

	@FXML
	private TextField textfieldStartFrame;
	@FXML
	private TextField textfieldEndFrame;

	@FXML
	private Button btnAutotrack;
	@FXML
	private ProgressBar progressAutoTrack;

	@FXML
	private ComboBox<String> chickSelect;
	@FXML
	private Button confirm;
	@FXML
	private Button newChick;
	@FXML
	private TextField chickName;
	@FXML
	private Label tracking;
	@FXML
	private Button export;
	@FXML
	private Canvas canvas;
	@FXML
	private Button forward;
	@FXML
	private Button previous;

	private AutoTracker autotracker;
	private ProjectData project;
	private Video video;
	private Stage stage;
	private ObservableList<String> chickIDs = FXCollections.observableArrayList();
	private int chosenChickIndex;

	@FXML
	public void initialize() {
		sliderVideoTime.valueProperty().addListener((obs, oldV, newV) -> showFrameAt(newV.intValue()));
		GraphicsContext gc = canvas.getGraphicsContext2D();
		canvas.setOnMouseClicked((event) -> {
			TimePoint tp = new TimePoint(event.getX(), event.getY(), (int) project.getVideo().getCurrentFrameNum());
			try {
				project.getTracks().get(chosenChickIndex).add(tp);
				gc.setFill(Color.RED);
				gc.fillOval(event.getX() - 5, event.getY() - 5, 10, 10);
				sliderVideoTime.setValue(project.getVideo().getCurrentFrameNum() + project.getVideo().getFrameRate());
				gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
				redrawPoint();
			} catch (Exception e) {
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Error");
				alert.setHeaderText("No chick being tracked!");
				alert.setContentText("Select a chick to track on the top right corner");
				alert.showAndWait();
			}
		});

	}

	public void redrawPoint() {
		GraphicsContext gc = canvas.getGraphicsContext2D();
		Video vid = project.getVideo();
		if (chosenChickIndex >= 0) {
			TimePoint tp = project.getTracks().get(chosenChickIndex).getTimePointAtTime(vid.getCurrentFrameNum());
			if (tp != null) {
				gc.setFill(Color.RED);
				gc.fillOval(tp.getX() - 5, tp.getY() - 5, 10, 10);
			}
		}
	}

	public void initializeWithStage(Stage stage) {
		this.stage = stage;

		// bind it so whenever the Scene changes width, the myImageView matches it
		// (not perfect though... visual problems if the height gets too large.)
		myImageView.fitWidthProperty().bind(myImageView.getScene().widthProperty());
	}

	@FXML
	public void handleBrowse() {
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
			sliderVideoTime.setMax(project.getVideo().getTotalNumFrames() - 1);
			showFrameAt(0);
			project.getVideo().setXPixelsPerCm(6.5); // these are just rough estimates!
			project.getVideo().setYPixelsPerCm(6.7);
			chosenChickIndex = -1;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	public void showFrameAt(int frameNum) {
		if (autotracker == null || !autotracker.isRunning()) {
			project.getVideo().setCurrentFrameNum(frameNum);
			Image curFrame = UtilsForOpenCV.matToJavaFXImage(project.getVideo().readFrame());
			myImageView.setImage(curFrame);

			String currentFrame = "" + frameNum;
			labelCurFrameNum.setText(currentFrame);
		}

	}

	@FXML
	public void handleStartAutotracking() throws InterruptedException {
		if (autotracker == null || !autotracker.isRunning()) {
			this.video = project.getVideo();
			video.setStartFrameNum(Integer.parseInt(textfieldStartFrame.getText()));
			video.setEndFrameNum(Integer.parseInt(textfieldEndFrame.getText()));
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

	// public void createVideo(String filePath) throws FileNotFoundException {
	// video = new Video(filePath);
	// }

	@FXML
	public void chooseChick() throws IOException {
		try {
			String choice = chickSelect.getValue();
			for (int i = 0; i < project.getTracks().size(); i++) {
				if (choice.equals(project.getTracks().get(i).getID())) {
					chosenChickIndex = i;
				}
			}
			tracking.setText("Tracking: " + project.getTracks().get(chosenChickIndex).getID());
			GraphicsContext gc = canvas.getGraphicsContext2D();
			gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		} catch (Exception e) {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Error");
			alert.setHeaderText("Enter a chick name!");
			alert.setContentText("Put a chick name in the field provided");
			alert.showAndWait();
		}
	}

	@FXML
	public void createChick() throws IOException{
		String name = chickName.getText();
		chickName.setText("");
		System.out.println(name.equals(""));
		if (name.equals("")) {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Error");
			alert.setHeaderText("Enter a chick name!");
			alert.setContentText("Please enter a valid chick name to the field provided.");
			alert.showAndWait();
		} else {
			chickIDs.add(name);
			chickSelect.setItems(chickIDs);
			project.getTracks().add(new AnimalTrack(name));
			FXCollections.sort(chickIDs);
		}
		
	}

	// this method will get called repeatedly by the Autotracker after it analyzes
	// each frame
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

		for (AnimalTrack track : trackedSegments) {
			System.out.println(track);

		}
		Platform.runLater(() -> {
			progressAutoTrack.setProgress(1.0);
			btnAutotrack.setText("Start auto-tracking");
		});

	}


	@FXML
	public void exportData() throws IOException {
		project.exportProject();
		Stage close = (Stage) export.getScene().getWindow();
		close.close();
	}

	@FXML
	public void forwardOneSec() {
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		sliderVideoTime.setValue(project.getVideo().getCurrentFrameNum() + project.getVideo().getFrameRate());
		redrawPoint();
	}

	@FXML
	public void previousOneSec() {
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		sliderVideoTime.setValue(project.getVideo().getCurrentFrameNum() - project.getVideo().getFrameRate() - 1);
		redrawPoint();
	}
	


	

//		@FXML public void displayCurrentFrame() {
//			textFieldCurFrameNum.setEditable(false);
//			String currentFrame = "" + project.getVideo().getCurrentFrameNum();
//			textFieldCurFrameNum.setText(currentFrame);
//		}

}
