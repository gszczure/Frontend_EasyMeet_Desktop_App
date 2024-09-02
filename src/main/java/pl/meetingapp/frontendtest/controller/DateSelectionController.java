package pl.meetingapp.frontendtest.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;
import pl.meetingapp.frontendtest.JavaFXApp;
import pl.meetingapp.frontendtest.util.HttpUtils;

import java.io.OutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class DateSelectionController {

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private Button addDateButton;

    @FXML
    private ListView<String> dateListView;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Label messageLabel;

    private List<String> selectedDates = new ArrayList<>();
    private Set<String> existingDateRanges = new HashSet<>();
    private String jwtToken;
    private Long meetingId;

    @FXML
    private void initialize() {
        dateListView.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
            }
        });

        this.jwtToken = JavaFXApp.getJwtToken();

        loadSavedDateRanges();
    }

    //TODO: zrobic by nie bylo ID wyswietlane obok przedzialow dat
    private void loadSavedDateRanges() {
        HttpURLConnection conn = null;
        try {
            conn = HttpUtils.createConnection(
                    "http://localhost:8080/api/date-ranges/meeting/" + meetingId,
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

                    JSONArray dateRangesArray = new JSONArray(response.toString());
                    for (int i = 0; i < dateRangesArray.length(); i++) {
                        JSONObject dateRange = dateRangesArray.getJSONObject(i);
                        String startDate = dateRange.getString("startDate");
                        String endDate = dateRange.getString("endDate");

                        JSONObject userJson = dateRange.getJSONObject("user");
                        String firstName = userJson.getString("firstName");
                        String lastName = userJson.getString("lastName");
                        String userFullName = firstName + " " + lastName;

                        Long dateRangeId = dateRange.getLong("id");

                        String dateRangeDisplay = startDate + " to " + endDate + " ( Added by: " + userFullName + ", id: " + dateRangeId + ")";
                        selectedDates.add(dateRangeDisplay);
                        dateListView.getItems().add(dateRangeDisplay);
                        existingDateRanges.add(startDate + " to " + endDate);
                    }
                }
                messageLabel.setText("Select date.");
            } else {
                messageLabel.setText("Failed to load date ranges.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            messageLabel.setText("An error occurred while loading date ranges.");
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public void setMeetingId(Long meetingId) {
        this.meetingId = meetingId;
        loadSavedDateRanges();
    }

    //TODO: zrobic by mozna jednak dodawac dwa razy ta sama date bo 2 roznych urzytkownikow nie moze dodac tych dasmych dat, zrobic cos typu ze uzytkownik ten sam nie moze dwa razy tego samego doac ale inny moze
    @FXML
    private void handleAddDateButtonAction() {
        if (startDatePicker.getValue() != null && endDatePicker.getValue() != null) {
            String dateRange = startDatePicker.getValue() + " to " + endDatePicker.getValue();

            // Sprawdzenie, czy przedział dat już istnieje
            if (!dateListView.getItems().contains(dateRange)) {
                selectedDates.add(dateRange);
                dateListView.getItems().add(dateRange);
                startDatePicker.setValue(null);
                endDatePicker.setValue(null);
            } else {
                messageLabel.setText("This date range already exists."); // dziala tylko kiedy dwa razy dodajemy ta sama date za jednym razem, kiedy prubujemy dodac ta sama date przy innym wejsciu na scene to nie zapisze sie ona w bazie danych i nie wyswietli podczas ladowania
            }
        } else {
            messageLabel.setText("Both start and end dates must be selected.");
        }
    }

    @FXML
    private void handleSaveButtonAction() {
        sendDateRangesToBackend();
    }

    @FXML
    private void handleCancelButtonAction() throws IOException {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        Scene newScene = new Scene(FXMLLoader.load(getClass().getResource("/fxml/mainScene.fxml")));
        stage.setScene(newScene);
    }

    private void sendDateRangesToBackend() {
        HttpURLConnection conn = null;
        try {
            conn = HttpUtils.createConnection(
                    "http://localhost:8080/api/date-ranges",
                    "POST",
                    jwtToken,
                    true
            );

            StringBuilder payload = new StringBuilder();
            payload.append("[");
            for (int i = 0; i < selectedDates.size(); i++) {
                String dateRange = selectedDates.get(i);

                // Rozdziel przedział dat na start i end
                String[] parts = dateRange.split(" to ");
                String startDate = parts[0].trim(); // Usuń nadmiarowe spacje

                // Usuń dodatkowe informacje
                String endDatePart = parts[1];
                String endDate = endDatePart.split(" \\(")[0].trim();

                // Sprawdzanie, czy daty już istnieją w bazie
                if (!existingDateRanges.contains(startDate + " to " + endDate)) {
                    payload.append("{\"meetingId\":").append(meetingId)
                            .append(",\"startDate\":\"").append(startDate)
                            .append("\",\"endDate\":\"").append(endDate)
                            .append("\"}");
                    if (i < selectedDates.size() - 1) {
                        payload.append(",");
                    }
                    existingDateRanges.add(startDate + " to " + endDate);
                }
            }
            payload.append("]");

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = payload.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                messageLabel.setText("Dates successfully saved.");
            } else {
                messageLabel.setText("Failed to save dates. Server responded with code " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
            messageLabel.setText("An error occurred while saving dates.");
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }


    @FXML
    private void handleDeleteDateButtonAction() {
        String selectedItem = dateListView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            // Ekstrakcja ID z wybranego przedziału dat
            Long dateRangeId = extractDateRangeId(selectedItem);
            deleteDateRangeFromBackend(dateRangeId);
        }
    }

    private Long extractDateRangeId(String dateRangeDisplay) {
        String[] parts = dateRangeDisplay.split(" \\(id: ");
        return Long.parseLong(parts[1].replace(")", ""));
    }

    private void deleteDateRangeFromBackend(Long dateRangeId) {
        HttpURLConnection conn = null;
        try {
            conn = HttpUtils.createConnection(
                    "http://localhost:8080/api/date-ranges/" + dateRangeId,
                    "DELETE",
                    jwtToken,
                    false
            );

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                messageLabel.setText("Date range successfully deleted.");
                dateListView.getItems().removeIf(item -> item.contains("(id: " + dateRangeId + ")"));
            } else {
                messageLabel.setText("Failed to delete date range.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            messageLabel.setText("An error occurred while deleting date range.");
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}