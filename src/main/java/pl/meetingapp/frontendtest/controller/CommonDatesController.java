package pl.meetingapp.frontendtest.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
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

    public TextField commentTextField;
    public Button saveCommentButton;

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
                    "https://backendmeetingapp.onrender.com/api/date-ranges/meeting/" + meetingId + "/common-dates",
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
                    List<LocalDate> dateList = new ArrayList<>();
                    for (int i = 0; i < datesArray.length(); i++) {
                        String dateString = datesArray.getString(i);
                        LocalDate date = LocalDate.parse(dateString); // Parsowanie daty z formatu YYYY-MM-DD
                        dateList.add(date);
                    }

                    if (dateList.isEmpty()) {
                        Platform.runLater(() -> { // Wyświetlenie sceny najpierw a potem komunikatu
                            showAlert(AlertType.INFORMATION, "No Common Dates", "There are no common dates available for this meeting.");
                            messageLabel.setText("No common dates available.");
                        });
                    } else {
                        // Sortowanie dat w kolejności rosnącej
                        dateList.sort(Comparator.naturalOrder());

                        List<String> formattedDates = new ArrayList<>();
                        for (LocalDate date : dateList) {
                            String formattedDate = date.getDayOfMonth() + " " +
                                    date.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " +
                                    date.getYear();
                            formattedDates.add(formattedDate);

                            formattedToOriginalDateMap.put(formattedDate, date.toString());
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

        // Ustaw widoczność przycisków tylko dla właściciela
        saveDateButton.setVisible(isOwner(ownerId));
        saveCommentButton.setVisible(isOwner(ownerId));
        commentTextField.setVisible(isOwner(ownerId));
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
        // Pobieramy oryginalną datę z mapy na podstawie wybranej sformatowanej daty po to by w bazie danych doramt date był YYYY-MM-DD
        String originalDate = formattedToOriginalDateMap.get(selectedDate);

        HttpURLConnection conn = null;
        try {
            conn = HttpUtils.createConnection(
                    "https://backendmeetingapp.onrender.com/api/meetings/" + meetingId + "/date",
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
    private void handleBackButtonAction() throws IOException {
        Stage stage = (Stage) backButton.getScene().getWindow();
        Scene newScene = new Scene(FXMLLoader.load(getClass().getResource("/fxml/mainScene.fxml")));
        stage.setScene(newScene);
    }

    // Metoda do zapisywania komentarzy dla spotkania
    @FXML
    private void handleSaveCommentButtonAction(ActionEvent event) {
        String comment = commentTextField.getText().trim();

        saveComment(comment);
    }

    private void saveComment(String comment) {
        HttpURLConnection conn = null;
        try {
            conn = HttpUtils.createConnection(
                    "https://backendmeetingapp.onrender.com/api/meetings/" + meetingId + "/comment",
                    "POST",
                    jwtToken,
                    true
            );
            conn.setRequestProperty("Content-Type", "text/plain");

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = comment.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                messageLabel.setText("Comment saved successfully.");
            } else {
                messageLabel.setText("Failed to save comment. Server responded with code " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
            messageLabel.setText("An error occurred while saving the comment.");
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }


    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
