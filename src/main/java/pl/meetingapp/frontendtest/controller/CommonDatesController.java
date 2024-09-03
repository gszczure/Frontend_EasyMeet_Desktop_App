package pl.meetingapp.frontendtest.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.stage.Stage;
import org.json.JSONArray;
import pl.meetingapp.frontendtest.JavaFXApp;
import pl.meetingapp.frontendtest.util.HttpUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CommonDatesController {

    @FXML
    private Label messageLabel;

    @FXML
    private Button backButton;

    @FXML
    private Button fetchDatesButton;

    @FXML
    private ListView<String> datesListView;

    private String jwtToken;
    private Long meetingId;

    @FXML
    private void initialize() {
        this.jwtToken = JavaFXApp.getJwtToken();
        datesListView.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
            }
        });
    }

    public void setMeetingId(Long meetingId) {
        this.meetingId = meetingId;
    }

    @FXML
    private void handleFetchDatesButtonAction() {
        fetchCommonDates();
    }

    private void fetchCommonDates() {
        HttpURLConnection conn = null;
        try {
            conn = HttpUtils.createConnection(
                    "http://localhost:8080/api/date-ranges/meeting/" + meetingId + "/common-dates",
                    "GET",
                    jwtToken,
                    false
            );

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (Scanner scanner = new Scanner(conn.getInputStream())) {
                    StringBuilder response = new StringBuilder();
                    while (scanner.hasNextLine()) {
                        response.append(scanner.nextLine());
                    }

                    JSONArray datesArray = new JSONArray(response.toString());
                    List<String> dateStrings = new ArrayList<>();
                    for (int i = 0; i < datesArray.length(); i++) {
                        String date = datesArray.getString(i);
                        dateStrings.add(date);
                    }

                    datesListView.getItems().setAll(dateStrings);
                    messageLabel.setText("Dates successfully fetched.");
                }
            } else {
                messageLabel.setText("Failed to fetch dates. Server responded with code " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
            messageLabel.setText("An error occurred while fetching dates.");
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    @FXML
    private void handleBackButtonAction(ActionEvent e) throws IOException {
        Stage stage = (Stage) backButton.getScene().getWindow();
        Scene newScene = new Scene(FXMLLoader.load(getClass().getResource("/fxml/mainScene.fxml")));
        stage.setScene(newScene);
    }
}
