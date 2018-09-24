package application;

import java.io.File;
import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
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
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Video File");
		Window mainWindow = textField.getScene().getWindow();
		chosenFile = fileChooser.showOpenDialog(mainWindow);
		textField.setText(chosenFile.getAbsolutePath());
	}
	
	@FXML public void openVideo() throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("MainWindow.fxml"));
		AnchorPane root = (AnchorPane)loader.load();
		MainWindowController nextController = loader.getController();
		nextController.startVideo(chosenFile.getAbsolutePath());
		
		Scene nextScene = new Scene(root,root.getPrefWidth(),root.getPrefHeight());
		nextScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		
		Stage primary = (Stage) btnStart.getScene().getWindow();
		primary.setScene(nextScene);
	}
	
	
}
