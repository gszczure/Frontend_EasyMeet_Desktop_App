package pl.meetingapp.frontendtest.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import org.json.JSONArray;
import pl.meetingapp.frontendtest.JavaFXApp;
import pl.meetingapp.frontendtest.util.HttpUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

public class CommonDatesController {

    @FXML
    private Label messageLabel;

    @FXML
    private Button backButton;

    @FXML
    private Button saveDateButton;

    @FXML
    private ListView<String> datesListView;

    private String jwtToken;
    private Long meetingId;
    private Long ownerId;
    private String selectedDate;

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

        datesListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            selectedDate = newValue;
        });
    }
    private boolean isOwner(Long ownerId) {
        Long currentUserId = JavaFXApp.getUserId();
        return currentUserId != null && currentUserId.equals(ownerId);
    }

    public void setMeetingId(Long meetingId) {
        this.meetingId = meetingId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    private Map<String, String> formattedToOriginalDateMap = new HashMap<>(); // Mapa przechowująca powiązania sformatowanej daty z oryginalną

    public void fetchCommonDates() {
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

                    if (dateStrings.isEmpty()) {
                        Platform.runLater(() -> { // Wyswietlenie sceny najpierw a potem komunikatu
                            showAlert(AlertType.INFORMATION, "No Common Dates", "There are no common dates available for this meeting.");
                            messageLabel.setText("No common dates available.");
                        });
                    } else {
                        List<String> formattedDates = new ArrayList<>();
                        for (String dateString : dateStrings) {
                            LocalDate date = LocalDate.parse(dateString); // Parsowanie daty z formatu YYYY-MM-DD
                            String formattedDate = date.getDayOfMonth() + " " +
                                    date.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " +
                                    date.getYear();
                            formattedDates.add(formattedDate);

                            formattedToOriginalDateMap.put(formattedDate, dateString);
                        }

                        datesListView.getItems().setAll(formattedDates);

                        messageLabel.setText("Dates successfully fetched.");
                    }
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

        saveDateButton.setVisible(isOwner(ownerId)); // Widoczność guzika saveDateButton tylko dla właściciela

    }


    @FXML
    private void handleSaveDateButtonAction() {
        if (selectedDate == null) {
            showAlert(AlertType.INFORMATION, "No Date Selected", "Please select a date to save.");
            return;
        }
        saveSelectedDate();
    }

    private void saveSelectedDate() {
        // Pobieramy oryginalną datę z mapy na podstawie wybranej sformatowanej daty
        String originalDate = formattedToOriginalDateMap.get(selectedDate);

        HttpURLConnection conn = null;
        try {
            conn = HttpUtils.createConnection(
                    "http://localhost:8080/api/meetings/" + meetingId + "/date",
                    "POST",
                    jwtToken,
                    true
            );
            conn.setRequestProperty("Content-Type", "application/json");

            String jsonPayload = "{\"date\":\"" + originalDate + "\"}";
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonPayload.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                messageLabel.setText("Date saved successfully.");
            } else {
                messageLabel.setText("Failed to save date. Server responded with code " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
            messageLabel.setText("An error occurred while saving the date.");
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    @FXML
    private void handleBackButtonAction(ActionEvent e) throws IOException { //TODO: sprawdzic czy potrzebne action event
        Stage stage = (Stage) backButton.getScene().getWindow();
        Scene newScene = new Scene(FXMLLoader.load(getClass().getResource("/fxml/mainScene.fxml")));
        stage.setScene(newScene);
    }

    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
