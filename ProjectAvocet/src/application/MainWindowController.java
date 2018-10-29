package application;

import java.io.File;
import java.awt.Rectangle;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.io.IOException;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Mat;
import org.opencv.core.Point;

import autotracking.*;
import datamodel.AnimalTrack;
import datamodel.ProjectData;
import datamodel.TimePoint;
import datamodel.Video;
import datamodel.sortTimePoint;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;

import javafx.scene.control.ProgressBar;

import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import utils.UtilsForOpenCV;

import javafx.scene.paint.Color;

public class MainWindowController implements AutoTrackListener {

	@FXML
	private ImageView myImageView;
	@FXML
	private Slider sliderVideoTime;

	@FXML
	private Label labelCurTime;
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
	private MenuItem export;
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
	private List<AnimalTrack> inRange = new ArrayList<AnimalTrack>();
	double aspectWidthRatio;
	double aspectHeightRatio;
	private boolean running = false;
	private ScheduledExecutorService timer;

	@FXML
	/**
	 * Initializes the entire project
	 */
	public void initialize() {
		sliderVideoTime.valueProperty().addListener((obs, oldV, newV) -> showFrameAt(newV.intValue()));
		GraphicsContext gc = canvas.getGraphicsContext2D();
		canvas.setOnMouseClicked((event) -> {
			TimePoint tp = new TimePoint(event.getX() * aspectWidthRatio, event.getY() * aspectHeightRatio,
					(int) video.getCurrentFrameNum());
			try {
				if (project.getVideo().getArenaBounds().contains(event.getX(), event.getY())) {
					TimePoint click = new TimePoint(event.getX(), event.getY(), video.getCurrentFrameNum());
					boolean segmentAdded = addSegment(click);
					if (!segmentAdded) {
						project.getTracks().get(chosenChickIndex).add(tp);
						gc.setFill(Color.RED);
						gc.fillOval(event.getX() - 5, event.getY() - 5, 10, 10);
						sliderVideoTime.setValue(video.getCurrentFrameNum() + video.getFrameRate());
						gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
						redrawPoints();
						findNearbyTracks(tp, gc);
					} else {
						gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
						redrawPoints();
						findNearbyTracks(tp, gc);
					}
				}
				else {
					//All Alert and TextDialog Code from: https://code.makery.ch/blog/javafx-dialogs-official/
					Alert alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("Error");
					alert.setContentText("Please click within the arena bounds.");
					alert.showAndWait();
				}
			} catch (Exception e) {
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Error");
				alert.setHeaderText("No chick being tracked!");
				alert.setContentText("Select a chick to track on the top right corner");
				alert.showAndWait();
			}
		});

	}
	/**
	 * Draws the points for the manual tracking
	 */
	public void redrawPoints() {
		GraphicsContext gc = canvas.getGraphicsContext2D();
		Video vid = video;
		if (chosenChickIndex >= 0) {
			List<TimePoint> draw = project.getTracks().get(chosenChickIndex)
					.getTimePointsInRange(vid.getCurrentFrameNum() - 90, vid.getCurrentFrameNum());
			for (int i = 0; i < draw.size(); i++) {
				TimePoint tp = draw.get(i);
				if (tp != null) {
					gc.setFill(Color.RED);
					gc.fillOval(tp.getX() / aspectWidthRatio - 5, tp.getY() / aspectHeightRatio - 5, 10, 10);
				}

			}
		}
	}

	
	/**
	 * 
	 * @param tp gets the timepoint information
	 * @param gc gets the GraphicsContext information
	 * 
	 * Finds the unassigned segments and adds it to the chick that they are currently tracking
	 */
	public void findNearbyTracks(TimePoint tp, GraphicsContext gc) {
		List<AnimalTrack> autoNear = project.getUnassignedSegmentsInRange(tp.getX(), tp.getY(), tp.getFrameNum() - 20,
				tp.getFrameNum() + 20, 30);
		for (int i = 0; i < autoNear.size(); i++) {
			TimePoint draw = autoNear.get(i).getPositions().get(0);
			gc.setFill(Color.BLUE);
			gc.fillRect(draw.getX() / aspectWidthRatio - 5, draw.getY() / aspectHeightRatio - 5, 10, 10);
			for (int j = 0; j < project.getUnassignedSegments().size(); j++) {
				if (project.getUnassignedSegments().get(j).getID().equals(autoNear.get(i).getID())) {
					project.getUnassignedSegments().remove(j);
					break;
				}
			}

		}
		inRange = autoNear;
	}
/**
 * 
 * @param click is where the user clicked
 * @return true if the a segment is added to a chick, false if no segments are added
 * 
 * It takes the time point is the coordinates of where the user clicked and if the click is within the first timepoint of
 * one of the unassigned segments, the segment is added to the chick that is currently being tracked.
 * 
 * 
 */
	public boolean addSegment(TimePoint click) {
		for (int i = 0; i < inRange.size(); i++) {
			TimePoint test = inRange.get(i).getPositions().get(0);
			if (click.getDistanceTo(test.getX() / aspectWidthRatio, test.getY() / aspectHeightRatio) < 5) {
				project.getTracks().get(chosenChickIndex).getPositions().addAll(inRange.get(i).getPositions());
				Collections.sort(project.getTracks().get(chosenChickIndex).getPositions(), new sortTimePoint());
				sliderVideoTime.setValue(
						inRange.get(i).getPositions().get(inRange.get(i).getPositions().size() - 1).getFrameNum() + 30);
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param stage 
	 * Initializes with the stage
	 */
	public void initializeWithStage(Stage stage) {
		this.stage = stage;

		// bind it so whenever the Scene changes width, the myImageView matches it
		// (not perfect though... visual problems if the height gets too large.)
		myImageView.fitWidthProperty().bind(myImageView.getScene().widthProperty());
	}
/**
 * 
 * @param filePath takes the filepath inputed from user
 * @param createProject creates a project with that video
 * 
 * Loads the video that the user wishes to use
 */
	public void loadVideo(String filePath, boolean createProject) {
		try {
			if (createProject) {
				project = new ProjectData(filePath);
				setAllButtons(true);
			}
			video = project.getVideo();
			sliderVideoTime.setMax(video.getTotalNumFrames() - 1);
			showFrameAt(0);
			video.setXPixelsPerCm(6.5); // these are just rough estimates!
			video.setYPixelsPerCm(6.7);
			chosenChickIndex = -1;
			aspectWidthRatio = (double) video.getFrameWidth()
					/ (double) myImageView.boundsInParentProperty().get().getWidth();
			aspectHeightRatio = (double) video.getFrameHeight()
					/ (double) myImageView.boundsInParentProperty().get().getHeight();
			canvas.setWidth(myImageView.boundsInParentProperty().get().getWidth());
			canvas.setHeight(myImageView.boundsInParentProperty().get().getHeight());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}
/**
 * 
 * @param frameNum 
 * 
 * Sets and shows the current frame number
 */
	public void showFrameAt(int frameNum) {
		if (autotracker == null || !autotracker.isRunning()) {
			video.setCurrentFrameNum(frameNum);
			Image curFrame = UtilsForOpenCV.matToJavaFXImage(video.readFrame());
			myImageView.setImage(curFrame);

			double totalSeconds = video.convertFrameNumsToSeconds(video.getCurrentFrameNum());
			int minute = (int)(totalSeconds/60);
			int seconds = (int)(totalSeconds%60);
			labelCurTime.setText("" + minute+":"+seconds);
		}
	}

	/**
	 * 
	 * @throws InterruptedException
	 * 
	 * Starts the autotracker when the button is pressed by the user
	 */
	@FXML
	public void handleStartAutotracking() throws InterruptedException {
		if (autotracker == null || !autotracker.isRunning()) {
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


/**
 * 
 * @throws IOException
 * 
 * Allows the user to choose the chick they would like to track from the list.
 */
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

	/**
	 * 
	 * @throws IOException
	 * 
	 *Allows the users to enter the name of the chicks ad adds them to the list of chicks.
	 */
	@FXML
	public void createChick() throws IOException {
		String name = chickName.getText();
		chickName.setText("");
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
/**
 * Determines when the autotracking is complete
 */
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
/**
 * 
 * @throws IOException
 * Exports the data collected by the user to a csv file
 */
	
	@FXML
	public void exportData() throws IOException {
		//FileChooser saveDialog Code from: https://www.mkyong.com/java/how-to-export-data-to-csv-file-java/
		FileChooser save = new FileChooser();
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
		save.getExtensionFilters().add(extFilter);
		Stage close = (Stage) confirm.getScene().getWindow();
		save.setTitle("Save CSV");
		File file = save.showSaveDialog(close);
		if (file != null) {
			project.exportProject(file);
		}
		close.close();
	}
 /**
  * Moves the video along one second when the user presses the "Forward" button
  */
	@FXML
	public void forwardOneSec() {
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		sliderVideoTime.setValue(video.getCurrentFrameNum() + video.getFrameRate());
		redrawPoints();
	}

	/**
	 * Moves the video back one second when the user presses the "Previous" Button
	 */
	@FXML
	public void previousOneSec() {
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		sliderVideoTime.setValue(video.getCurrentFrameNum() - video.getFrameRate() - 1);
		redrawPoints();
	}
 /**
  * Sets the bounds of the Arena the chicks are in by collecting the information the user will input about the arena in the video.
  * 
  */
	@FXML
	public void setArenaBounds() {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Set Bounds");
		alert.setHeaderText("Set the bounds for where the chicks will walk.");
		alert.setContentText("Please click the three corners of the area where the chicks will be walking. Start by "
				+ "clicking on the top left of the rectangle, then the top right, and finally the bottom left.");
		alert.showAndWait();
		Rectangle arenaBounds = new Rectangle(0, 0, 0, 0);
		ArrayList<Point> vertices = new ArrayList<Point>();
		myImageView.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				vertices.add(new Point(event.getX(), event.getY()));
				if (vertices.size() >= 3) {
					arenaBounds.setBounds((int) vertices.get(0).x, (int) vertices.get(0).y,
							(int) (vertices.get(1).x - vertices.get(0).x),
							(int) (vertices.get(2).y - vertices.get(0).y));
					project.getVideo().setArenaBounds(arenaBounds);
					myImageView.removeEventHandler(MouseEvent.MOUSE_CLICKED, this);
					Alert alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("Arena Bounds Set");
					alert.setHeaderText("The Arena Bounds have been set");
					alert.showAndWait();
					checkCalibration();
				}
			}
		});

	}
 /**
  * Allows the user to input a known distance  within the video to be able to convert that distance from pixels for the 
  * vertical height of the arena
  */
	@FXML
	public void setPixelToCentimeterHeight() {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Set Pixel to Centimeter Ratio");
		alert.setHeaderText("Please click two points between which you know the distance in centimeters vertically.");
		alert.showAndWait();
		ArrayList<Point> distance = new ArrayList<Point>();
		myImageView.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				distance.add(new Point(event.getX(), event.getY()));
				if (distance.size() >= 2) {
					double xDist = distance.get(1).x - distance.get(0).x;
					double yDist = distance.get(1).y - distance.get(0).y;
					double ratio = xDist / yDist;
					TextInputDialog dialog = new TextInputDialog("0");
					dialog.setTitle("Enter Distance");
					dialog.setContentText("Please enter the distance between the points in centimeters:");
					Optional<String> result = dialog.showAndWait();
					double cmDistance = Double
							.parseDouble(result.toString().substring(9, result.toString().length() - 1));
					double cmDistanceVertical = Math.sqrt(cmDistance * cmDistance / (ratio * ratio + 1));
					project.getVideo().setYPixelsPerCm(Math.abs(yDist / cmDistanceVertical));
					myImageView.removeEventHandler(MouseEvent.MOUSE_CLICKED, this);
					checkCalibration();
				}
			}

		});
	}
/**
 *  Allows the user to input a known distance  within the video to be able to convert that distance from pixels for the 
 * vertical width of the arena
 */
	@FXML
	public void setPixelToCentimeterWidth() {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Set Pixel to Centimeter Ratio");
		alert.setHeaderText("Please click two points between which you know the distance in centimeters horizontally.");
		alert.showAndWait();
		ArrayList<Point> distance = new ArrayList<Point>();
		myImageView.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				distance.add(new Point(event.getX(), event.getY()));
				if (distance.size() >= 2) {
					double xDist = distance.get(1).x - distance.get(0).x;
					double yDist = distance.get(1).y - distance.get(0).y;
					double ratio = yDist / xDist;
					TextInputDialog dialog = new TextInputDialog("0");
					dialog.setTitle("Enter Distance");
					dialog.setContentText("Please enter the distance between the points in centimeters:");
					Optional<String> result = dialog.showAndWait();
					double cmDistance = Double
							.parseDouble(result.toString().substring(9, result.toString().length() - 1));
					double cmDistanceHorizontal = Math.sqrt(cmDistance * cmDistance / (ratio * ratio + 1));
					project.getVideo().setYPixelsPerCm(Math.abs(xDist / cmDistanceHorizontal));
					System.out.println(Math.abs(xDist / cmDistanceHorizontal));
					myImageView.removeEventHandler(MouseEvent.MOUSE_CLICKED, this);
					checkCalibration();
				}
			}

		});
	}
/**
 * 
 * @throws FileNotFoundException if a file is not found
 * Saves the JSON  file
 */
	@FXML
	public void saveJSON() throws FileNotFoundException {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save Progess");
		File file = fileChooser.showSaveDialog(stage);
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json");
		fileChooser.getExtensionFilters().add(extFilter);
		try {
			project.saveToFile(file);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

	}
/**
 * 
 * @param savedFile JSON file that needs to be loaded
 * @throws FileNotFoundException if no File is Found
 * 
 * Loads from a JSON file 
 */
	public void loadProject(File savedFile) throws FileNotFoundException {
		project = ProjectData.loadFromFile(savedFile);
		for (int i = 0; i < project.getTracks().size(); i++) {
			chickIDs.add(project.getTracks().get(i).getID());
		}
		chickSelect.setItems(chickIDs);
	}
/**
 * 
 * @param event 
 * @throws Exception if no need to pause
 * 
 * Pauses the video when the user presses the pause button
 */
	@FXML
	public void handlePause(ActionEvent event) throws Exception {

		if (!running && video.getCurrentFrameNum() <= video.getEndFrameNum()) {
			pausePlay.setText("Pause");

			Runnable frameGrabber = new Runnable() {

				@Override
				public void run() {
					sliderVideoTime.setValue(video.getCurrentFrameNum());
					if (video.getCurrentFrameNum() == video.getEndFrameNum()) {
						handleReplay(video.getEndFrameNum());
					}
				}
			};
			Platform.runLater(() -> {
				double totalSeconds = video.convertFrameNumsToSeconds(video.getCurrentFrameNum());
				int minute = (int)(totalSeconds/60);
				int seconds = (int)(totalSeconds%60);
				labelCurTime.setText("" + minute+":"+seconds);
			});
			this.timer = Executors.newSingleThreadScheduledExecutor();
			this.timer.scheduleAtFixedRate(frameGrabber, 0, Math.round(video.getFrameRate()), TimeUnit.MILLISECONDS);
		} else {
			pausePlay.setText("Play");
			// stop the timer
			this.timer.shutdown();
			this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);

		}
		running = !running;
	}

	//Pulls the slider back to 0 when the video is done
	private void handleReplay(int value) {
		if (value == video.getEndFrameNum()) {
			showFrameAt(0);
			sliderVideoTime.setValue(0);
			// pausePlay.setText("Replay");
			this.timer.shutdown();
			try {
				this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
/**
 * Gathers the information inputted by the user about where the Origin is
 */
	@FXML
	public void handleSetOrigin() {
		myImageView.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				video.setOriginX(event.getX());
				video.setOriginY(event.getY());
				myImageView.removeEventHandler(MouseEvent.MOUSE_CLICKED, this);
				checkCalibration();
			}

		});
	}
/**
 * @param state
 * 
 * disables the buttons until they are needed to be used
 */
	public void setAllButtons(boolean state) {
		confirm.setDisable(state);
		btnAutotrack.setDisable(state);
		pausePlay.setDisable(state);
		newChick.setDisable(state);
		export.setDisable(state);
		canvas.setDisable(state);
		forward.setDisable(state);
		previous.setDisable(state);
	}
 /**
  * Checks the calibration of the information the user has inputted about the arena and origins
  */
	public void checkCalibration() {
		project.setCalibrations(project.getNumCalibrations() + 1);
		if (project.getNumCalibrations() == 5) {
			setAllButtons(false);
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Calibration Completed!");
			alert.setHeaderText("The calibration has been completed. You can now begin tracking the chicks.");
			alert.showAndWait();
		}
	}
	/**
	 * Lets the user input an empty frame where there is no chicks in the video
	 */
	@FXML
	public void setEmptyFrame() {
		TextInputDialog dialog = new TextInputDialog("0");
		dialog.setTitle("Enter Empty Frame");
		dialog.setContentText("Please enter a time that has no chicks in the area they will be tracked. You can look at"
				+ "the time using the slider bar. Enter the time in the format (minute:second).");
		Optional<String> result = dialog.showAndWait();
		String minute = result.toString().substring(9, result.toString().indexOf(":"));
		String second = result.toString().substring(result.toString().indexOf(":")+1, result.toString().length()-1);
		int totalSeconds = Integer.parseInt(minute)*60+Integer.parseInt(second);
		project.getVideo().setEmptyFrameNum((int)(totalSeconds*project.getVideo().getFrameRate()));
		checkCalibration();
	}
	
	/**
	 * Exports a CSV File with the total distance traveled of each chick
	 * @throws IOException
	 */
	@FXML
	public void getTotalDistance() throws IOException {
		FileChooser save = new FileChooser();
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
		save.getExtensionFilters().add(extFilter);
		Stage close = (Stage) confirm.getScene().getWindow();
		save.setTitle("Save Total Distance");
		File file = save.showSaveDialog(close);
		if (file != null) {
			project.exportTotalDistance(file);
		}
	}

}
