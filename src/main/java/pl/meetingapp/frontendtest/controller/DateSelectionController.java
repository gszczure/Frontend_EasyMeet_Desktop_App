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
import java.util.*;

public class DateSelectionController {

    @FXML
    public Button deleteDateButton;

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
    private Long ownerId;

    private boolean isOwner(Long ownerId) {
        Long currentUserId = JavaFXApp.getUserId();
        return currentUserId != null && currentUserId.equals(ownerId);
    }

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

    private Map<Long, Set<String>> userDateRangesMap = new HashMap<>(); // Mapa userId -> Zestaw przedziałów dat

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
                        Long userId = userJson.getLong("id");
                        String firstName = userJson.getString("firstName");
                        String lastName = userJson.getString("lastName");
                        String userFullName = firstName + " " + lastName;

                        Long dateRangeId = dateRange.getLong("id");

                        String dateRangeDisplay = startDate + " to " + endDate + " ( Added by: " + userFullName + ", id: " + dateRangeId + ")";
                        dateListView.getItems().add(dateRangeDisplay);

                        // Dodajemy do mapy daty dla użytkownika bez tego nie załaduja sie daty
                        userDateRangesMap.putIfAbsent(userId, new HashSet<>());
                        userDateRangesMap.get(userId).add(startDate + " to " + endDate);
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
        deleteDateButton.setVisible(isOwner(ownerId)); // Guzik deleteDateButton widzoczny tylko dla właściciela
    }


    public void setMeetingId(Long meetingId) {
        this.meetingId = meetingId;
        loadSavedDateRanges();
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    @FXML
    private void handleAddDateButtonAction() {
        if (startDatePicker.getValue() != null && endDatePicker.getValue() != null) {
            String dateRange = startDatePicker.getValue() + " to " + endDatePicker.getValue();

            Long currentUserId = JavaFXApp.getUserId();
            Set<String> userDates = userDateRangesMap.getOrDefault(currentUserId, new HashSet<>());

            if (!userDates.contains(dateRange)) {
                selectedDates.add(dateRange);
                dateListView.getItems().add(dateRange);
                userDates.add(dateRange);
                startDatePicker.setValue(null);
                endDatePicker.setValue(null);
            } else {
                messageLabel.setText("This date range already exists for you.");
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
                String[] parts = dateRange.split(" to ");
                String startDate = parts[0].trim();
                String endDate = parts[1].split(" \\(")[0].trim();

                if (!existingDateRanges.contains(startDate + " to " + endDate)) {
                    payload.append("{\"meetingId\":").append(meetingId)
                            .append(",\"userId\":").append(JavaFXApp.getUserId())
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
                messageLabel.setText("Failed to save dates. Server responded with code " + responseCode); //TODO: zmienic komentarz zeby bylo ze nie moze wybrac 2 tych samych dat
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
        String[] parts = dateRangeDisplay.split(", id: ");
        if (parts.length > 1) {
            String idPart = parts[1].replace(")", "").trim();
            try {
                return Long.parseLong(idPart);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return null;
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