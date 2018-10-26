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
	
	@FXML public void initialize() {
		
	}
	
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
	
	@FXML public void openVideo() throws IOException {
		try {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("MainWindow.fxml"));
		AnchorPane root = (AnchorPane)loader.load();
		MainWindowController nextController = loader.getController();
		
		Scene nextScene = new Scene(root,root.getPrefWidth(),root.getPrefHeight());
		nextScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		
		Stage primary = (Stage) btnStart.getScene().getWindow();
		nextController.initializeWithStage(primary);
		nextController.loadVideo(chosenFile.getAbsolutePath());
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
