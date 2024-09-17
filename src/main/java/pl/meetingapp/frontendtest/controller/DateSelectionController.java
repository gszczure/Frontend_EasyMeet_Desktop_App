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
    private ListView<DateRange> dateListView;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Label messageLabel;

    private List<DateRange> selectedDates = new ArrayList<>();
    private Set<String> existingDateRanges = new HashSet<>();
    private String jwtToken;
    private Long meetingId;
    private Map<Long, Set<DateRange>> userDateRangesMap = new HashMap<>(); // Mapa userId -> Zestaw przedziałów dat

    @FXML
    private void initialize() {
        dateListView.setCellFactory(param -> new ListCell<DateRange>() {
            @Override
            protected void updateItem(DateRange item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.toString());
            }
        });

        this.jwtToken = JavaFXApp.getJwtToken();
        loadSavedDateRanges();
    }

    private void loadSavedDateRanges() {
        HttpURLConnection conn = null;
        try {
            conn = HttpUtils.createConnection(
                    "https://backendmeetingapp.onrender.com/api/date-ranges/meeting/" + meetingId,
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

                        DateRange newDateRange = new DateRange(startDate, endDate, dateRangeId, userFullName);
                        dateListView.getItems().add(newDateRange); // Dodajemy DateRange

                        userDateRangesMap.putIfAbsent(userId, new HashSet<>());
                        userDateRangesMap.get(userId).add(newDateRange); // Zapisujemy DateRange dla użytkownika
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

    @FXML
    private void handleAddDateButtonAction() {
        if (startDatePicker.getValue() != null && endDatePicker.getValue() != null) {
            String startDate = startDatePicker.getValue().toString();
            String endDate = endDatePicker.getValue().toString();

            Long currentUserId = JavaFXApp.getUserId();
            Set<DateRange> userDates = userDateRangesMap.getOrDefault(currentUserId, new HashSet<>());

            // Sprawdzenie, czy istnieje już taki zakres dat dla użytkownika
            boolean dateExists = userDates.stream()
                    .anyMatch(dr -> dr.getStartDate().equals(startDate) && dr.getEndDate().equals(endDate));

            if (!dateExists) {
                DateRange newDateRange = new DateRange(startDate, endDate, null, "");
                selectedDates.add(newDateRange);
                dateListView.getItems().add(newDateRange);
                userDates.add(newDateRange);
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
        refreshDateListView();

    }
    private void refreshDateListView() {
        dateListView.getItems().clear();
        loadSavedDateRanges();
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
                    "https://backendmeetingapp.onrender.com/api/date-ranges",
                    "POST",
                    jwtToken,
                    true
            );

            StringBuilder payload = new StringBuilder();
            payload.append("[");
            for (int i = 0; i < selectedDates.size(); i++) {
                DateRange dateRange = selectedDates.get(i);
                String startDate = dateRange.getStartDate();
                String endDate = dateRange.getEndDate();

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
                messageLabel.setText("You cannot save the same date ranges.");
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
        DateRange selectedItem = dateListView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            Long dateRangeId = selectedItem.getDateRangeId();
            Long currentUserId = JavaFXApp.getUserId();

            // Sprawdzanie, czy użytkownik jest właścicielem przedziału czasowego
            if (isDateRangeOwnedByUser(selectedItem, currentUserId)) {
                deleteDateRangeFromBackend(dateRangeId);
            } else {
                messageLabel.setText("You cannot delete a date range that is not yours.");
            }
        }
    }

    private void deleteDateRangeFromBackend(Long dateRangeId) {
        HttpURLConnection conn = null;
        try {
            conn = HttpUtils.createConnection(
                    "https://backendmeetingapp.onrender.com/api/date-ranges/" + dateRangeId,
                    "DELETE",
                    jwtToken,
                    false
            );

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                messageLabel.setText("Date range successfully deleted.");
                dateListView.getItems().removeIf(item -> item.getDateRangeId().equals(dateRangeId));
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

    // Metoda do sprawdzania czy uzytkownik to wlasciciel przedzialu daty
    private boolean isDateRangeOwnedByUser(DateRange dateRange, Long userId) {
        return userDateRangesMap.getOrDefault(userId, Collections.emptySet())
                .contains(dateRange);
    }
}
