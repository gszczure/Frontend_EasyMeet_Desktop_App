package pl.meetingapp.frontendtest.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.Scanner;
import pl.meetingapp.frontendtest.JavaFXApp;
import pl.meetingapp.frontendtest.util.HttpUtils;

//TODO: zrobic by wysuwalo sie jak join a nie zeby byla nowa scena
public class CreatMeetingController {

    @FXML
    private TextField meetingTitleTextField;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Label messageLabel;

    private String jwtToken;

    @FXML
    private void initialize() {
        this.jwtToken = JavaFXApp.getJwtToken();
    }

    @FXML
    private void handleSaveButtonAction(ActionEvent event) { //TODO: sprawdzic ten event
        String meetingTitle = meetingTitleTextField.getText().trim();

        if (meetingTitle.isEmpty()) {
            messageLabel.setText("Meeting title must be provided.");
            return;
        }

        HttpURLConnection conn = null;
        try {
            conn = HttpUtils.createConnection("http://localhost:8080/api/meetings/create", "POST", jwtToken, true);

            String jsonPayload = "{\"name\":\"" + meetingTitle + "\"}";

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonPayload.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int code = conn.getResponseCode();
            if (code == HttpURLConnection.HTTP_OK) {
                messageLabel.setText("The meeting has been created.");
                Stage stage = (Stage) saveButton.getScene().getWindow();
                Scene newScene = new Scene(FXMLLoader.load(getClass().getResource("/fxml/mainScene.fxml")));
                stage.setScene(newScene);
            } else {
                StringBuilder response = new StringBuilder();
                InputStream errorStream = conn.getErrorStream();
                if (errorStream != null) {
                    try (Scanner scanner = new Scanner(errorStream)) {
                        while (scanner.hasNextLine()) {
                            response.append(scanner.nextLine());
                        }
                    }
                    messageLabel.setText("Adding failed.");
                } else {
                    messageLabel.setText("Adding failed. Server responded with code " + code + ". No error stream available.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            messageLabel.setText("An error occurred while creating the meeting.");
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    @FXML
    private void handleCancelButtonAction(ActionEvent e) throws IOException {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        Scene newScene = new Scene(FXMLLoader.load(getClass().getResource("/fxml/mainScene.fxml")));
        stage.setScene(newScene);
    }
}
