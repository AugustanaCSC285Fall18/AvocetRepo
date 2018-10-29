package application;

import java.io.File;
import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

public class VideoPlayerController {
	
	@FXML private TextField textField;
	@FXML private Button btnBrowse;
	@FXML private Button btnStart;
	File chosenFile;
	private MainWindowController nextController;
	
	/**
	 * Allows user to browse for file to start video
	 */
	@FXML public void handleBrowse() {
		try {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Video File");
		Window mainWindow = textField.getScene().getWindow();
		chosenFile = fileChooser.showOpenDialog(mainWindow);
		textField.setText(chosenFile.getAbsolutePath());
		} catch (Exception e) { 
		}
	}
	
	/**
	 * Shows alert dialog showing the about page with information about the program
	 */
	@FXML
	public void showAbout() {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("About");
		alert.setHeaderText("Team Avocet Members: Paul Cabrera, Jack Miller, Tuan Truong, Michael Wardach\n"
				+ "Project Supervisor: Forrest Stonedahl\n"
				+ "Libraries Used: OpenCV, GSON, JUnit 5, JavaFX");
		alert.showAndWait();
	}

	/**
	 * Opens up window with tracking with new project if video file is selected or in progress project
	 * if json file is selected
	 * @throws IOException
	 */
	@FXML
	public void start() throws IOException {
		
		try {
			String filePath = chosenFile.getAbsolutePath();
			String extension = filePath.substring(filePath.length() - 4);
			
			FXMLLoader loader = new FXMLLoader(getClass().getResource("MainWindow.fxml"));
			AnchorPane root = (AnchorPane) loader.load();
			nextController = loader.getController();
			Scene nextScene = new Scene(root, root.getPrefWidth(), root.getPrefHeight());
			nextScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

			Stage primary = (Stage) btnStart.getScene().getWindow();
			nextController.initializeWithStage(primary);
			if (extension.equals("json")) {
				nextController.loadProject(chosenFile);
				nextController.loadVideo(chosenFile.getAbsolutePath(), false);
			} else {
				nextController.loadVideo(chosenFile.getAbsolutePath(), true);
			}
			primary.setScene(nextScene);
		} catch (Exception e) {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Error");
			alert.setHeaderText("Invalid File Choosen");
			alert.setContentText("Please choose a valid video file");
			alert.showAndWait();
		}
	}
	
	
}
