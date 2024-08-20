package pl.meetingapp.frontendtest.controller;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONObject;
import pl.meetingapp.frontendtest.JavaFXApp;

public class MainSceneController {

    @FXML
    private Accordion accordion;

    @FXML
    private Button logoutButton;

    @FXML
    private Button jointMeetingButton;

    @FXML
    private Button addMeetingButton;

    @FXML
    private AnchorPane slideInPane;

    @FXML
    private TextField meetingCodeTextField;

    @FXML
    private Button joinButton;

    @FXML
    private Button cancelButton;

    @FXML
    private void initialize() {
        loadMeetings();
        slideInPane.setVisible(false);
    }

    @FXML
    private void handleCreateMeetingButtonAction() throws IOException {
        String token = JavaFXApp.getJwtToken();

        if (token == null || token.isEmpty()) {
            Stage stage = (Stage) addMeetingButton.getScene().getWindow();
            Scene newScene = new Scene(FXMLLoader.load(getClass().getResource("/fxml/loginScene.fxml")));
            stage.setScene(newScene);
        } else {
            Stage stage = (Stage) addMeetingButton.getScene().getWindow();
            Scene newScene = new Scene(FXMLLoader.load(getClass().getResource("/fxml/createMeetingScene.fxml")));
            stage.setScene(newScene);

            loadMeetings();
        }
    }

    private void loadMeetings() {
        accordion.getPanes().clear();
        String jwtToken = JavaFXApp.getJwtToken();

        HttpURLConnection conn = null;
        try {
            URL url = new URL("http://localhost:8080/api/meetings/for-user");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + jwtToken);
            conn.setRequestProperty("Accept", "application/json");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (Scanner scanner = new Scanner(conn.getInputStream())) {
                    StringBuilder response = new StringBuilder();
                    while (scanner.hasNextLine()) {
                        response.append(scanner.nextLine());
                    }

                    JSONArray meetingsArray = new JSONArray(response.toString());
                    for (int i = 0; i < meetingsArray.length(); i++) {
                        JSONObject meeting = meetingsArray.getJSONObject(i);
                        String meetingName = meeting.getString("name");
                        JSONObject owner = meeting.getJSONObject("owner");
                        String ownerName = owner.getString("firstName") + " " + owner.getString("lastName");
                        String meetingCode = meeting.getString("code");

                        addMeetingToAccordion(meetingName, ownerName, meetingCode);
                    }
                }
            } else {
                System.out.println("Failed to load meetings. Server responded with code " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private void addMeetingToAccordion(String name, String ownerName, String code) {
        String title = name + " ( Owner: " + ownerName + " )";
        TitledPane titledPane = new TitledPane();
        titledPane.setText(title);

        VBox content = new VBox();
        Label codeLabel = new Label("Code: " + code);
        content.getChildren().add(codeLabel);

        titledPane.setContent(content);
        accordion.getPanes().add(titledPane);
    }

    @FXML
    private void handleLogoutButtonAction() throws IOException {
        JavaFXApp.clearJwtToken();

        Stage stage = (Stage) logoutButton.getScene().getWindow();
        Scene newScene = new Scene(FXMLLoader.load(getClass().getResource("/fxml/loginSceneFRONT.fxml")));
        stage.setScene(newScene);
    }

    @FXML
    private void handleJoinMeetingButtonAction() {
        if (!slideInPane.isVisible()) {
            slideInPane.setVisible(true);
            TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), slideInPane);
            slideIn.setFromX(slideInPane.getTranslateX());
            slideIn.setToX(0);
            slideIn.play();
        }
    }

    @FXML
    private void handleJoinButtonAction() {
        String meetingCode = meetingCodeTextField.getText().trim();
        System.out.println("Meeting code entered: " + meetingCode);
        closeSlideInPane();
    }

    @FXML
    private void handleCancelButtonAction() {
        closeSlideInPane();
    }

    private void closeSlideInPane() {
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), slideInPane);
        slideOut.setFromX(slideInPane.getTranslateX());
        slideOut.setToX(slideInPane.getWidth());
        slideOut.setOnFinished(event -> slideInPane.setVisible(false));
        slideOut.play();
    }
}
