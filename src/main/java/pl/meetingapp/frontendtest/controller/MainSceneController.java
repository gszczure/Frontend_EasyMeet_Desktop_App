package pl.meetingapp.frontendtest.controller;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javafx.stage.Stage;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONObject;
import pl.meetingapp.frontendtest.JavaFXApp;
import pl.meetingapp.frontendtest.util.HttpUtils;

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
    private Button commonDatesButton;

    @FXML
    private AnchorPane slideInPane;

    @FXML
    private TextField meetingCodeTextField;

    @FXML
    private Button joinButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Label messageLabel;

    @FXML
    private AnchorPane usersSlideInPane;

    @FXML
    private VBox usersListVBox;

    private int lastUserNumber = 0;

    private String jwtToken;

    @FXML
    private void initialize() {
        slideInPane.setVisible(false);
        usersSlideInPane.setVisible(false);
        this.jwtToken = JavaFXApp.getJwtToken();
        loadMeetings();
    }

    private boolean isOwner(Long ownerId) {
        Long currentUserId = JavaFXApp.getUserId();
        return currentUserId != null && currentUserId.equals(ownerId);
    }


    @FXML
    private void handleCreateMeetingButtonAction() throws IOException {
        String token = jwtToken;

        if (token == null || token.isEmpty()) {
            Stage stage = (Stage) addMeetingButton.getScene().getWindow();
            Scene newScene = new Scene(FXMLLoader.load(getClass().getResource("/fxml/loginScene.fxml")));
            stage.setScene(newScene);
        } else {
            Stage stage = (Stage) addMeetingButton.getScene().getWindow();
            Scene newScene = new Scene(FXMLLoader.load(getClass().getResource("/fxml/createMeetingScene.fxml")));
            stage.setScene(newScene);
        }
    }

    private void loadMeetings() {
        accordion.getPanes().clear();
        HttpURLConnection conn = null;
        try {
            conn = HttpUtils.createConnection("http://localhost:8080/api/meetings/for-user", "GET", jwtToken, false);

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (Scanner scanner = new Scanner(conn.getInputStream())) {
                    StringBuilder response = new StringBuilder();
                    while (scanner.hasNextLine()) {
                        response.append(scanner.nextLine());
                    }

                    JSONArray meetingsArray = new JSONArray(response.toString());

                    List<JSONObject> meetingList = new ArrayList<>();
                    for (int i = 0; i < meetingsArray.length(); i++) {
                        meetingList.add(meetingsArray.getJSONObject(i));
                    }
                    meetingList.sort(Comparator.comparing(m -> m.getString("name")));

                    for (JSONObject meeting : meetingList) {
                        String meetingName = meeting.getString("name");
                        JSONObject owner = meeting.getJSONObject("owner");
                        String ownerName = owner.getString("firstName") + " " + owner.getString("lastName");
                        String meetingCode = meeting.getString("code");
                        Long meetingId = meeting.getLong("id");
                        Long ownerId = owner.getLong("id");

                        addMeetingToAccordion(meetingName, ownerName, meetingCode, meetingId, ownerId);
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

    private void addMeetingToAccordion(String name, String ownerName, String code, Long meetingId, Long ownerId) {
        TitledPane titledPane = new TitledPane();

        // TitleBox (HBox) - Nagłówek TitledPane
        HBox titleBox = new HBox();
        titleBox.setPadding(new Insets(10, 10, 0, 10));
        titleBox.setSpacing(10);
        titleBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // HBox dla etykiet i przycisku
        HBox titleContentBox = new HBox();
        titleContentBox.setSpacing(10);

        Label nameLabel = new Label(name);
        nameLabel.setTextFill(Color.BLACK);
        titleContentBox.getChildren().add(nameLabel);

        Label ownerLabel = new Label(ownerName);
        ownerLabel.setTextFill(Color.BLACK);
        titleContentBox.getChildren().add(ownerLabel);

        // Dodanie przycisku "-" tylko dla właściciela
        if (isOwner(ownerId)) {
            Button deleteButton = new Button("-");
            deleteButton.setOnAction(event -> handleDeleteMeetingButtonAction(meetingId));
            deleteButton.setStyle("-fx-background-color: #FF0000; -fx-text-fill: white; -fx-pref-height: 20px; -fx-pref-width: 20px");
            // Ustawienie padding po prawej stronie na 0
            titleBox.setPadding(new Insets(10, 0, 0, 10));
            titleContentBox.getChildren().add(deleteButton);
        }

        titleBox.getChildren().add(titleContentBox);

        // Ustawienie wyrównania elementów w HBox na prawą stronę (NIE DZIALA NIE WIEDZIEC CZEMU)
        HBox.setHgrow(titleContentBox, Priority.ALWAYS);

        VBox content = new VBox();
        content.setSpacing(10);

        HBox hbox = new HBox();
        hbox.setPadding(new Insets(10, 10, 0, 10));

        Label dateLabel = new Label();
        dateLabel.setTextFill(Color.GREEN);
        hbox.getChildren().add(dateLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        hbox.getChildren().add(spacer);

        // Wyswietlenie Code:... tylko dla wlasciciela spotkania
        if (isOwner(ownerId)) {
            Label codeLabel = new Label("Code: " + code);
            codeLabel.setTextFill(Color.RED);
            hbox.getChildren().add(codeLabel);
        }

        content.getChildren().add(hbox);

        fetchMeetingDate(meetingId, dateLabel);

        HBox buttonBox = new HBox();
        buttonBox.setSpacing(10);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER);

        Button usersButton = new Button("Users");
        usersButton.setOnAction(event -> handleUsersButtonAction(meetingId));
        usersButton.setStyle("-fx-background-color: #263F73; -fx-text-fill: white; -fx-pref-width: 60px; -fx-pref-height: 26px");

        Button commonDatesButton = new Button("Common Dates");
        commonDatesButton.setOnAction(event -> handleCommonDatesButtonAction(meetingId));
        commonDatesButton.setStyle("-fx-background-color: #263F73; -fx-text-fill: white;-fx-pref-width: 120px; -fx-pref-height: 26px");

        Button dateButton = new Button("Date");
        dateButton.setOnAction(event -> handleDateButtonAction(meetingId));
        dateButton.setStyle("-fx-background-color: #263F73; -fx-text-fill: white;-fx-pref-width: 60px; -fx-pref-height: 26px");

        buttonBox.getChildren().addAll(usersButton, commonDatesButton, dateButton);

        content.getChildren().add(buttonBox);

        titledPane.setContent(content);
        titledPane.setGraphic(titleBox);
        titledPane.setUserData(meetingId);

        accordion.getPanes().add(titledPane);
    }


    private void fetchMeetingDate(Long meetingId, Label dateLabel) {
        String url = "http://localhost:8080/api/meetings/" + meetingId + "/date";
        HttpURLConnection conn = null;
        try {
            conn = HttpUtils.createConnection(url, "GET", jwtToken, false);
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (Scanner scanner = new Scanner(conn.getInputStream())) {
                    if (scanner.hasNextLine()) {
                        String jsonResponse = scanner.nextLine();
                        JSONObject jsonObject = new JSONObject(jsonResponse);
                        String date = jsonObject.optString("date", "none");
                        dateLabel.setText("Date: " + date);
                    } else {
                        dateLabel.setText("Date: none");
                        dateLabel.setTextFill(Color.RED);
                    }
                }
            } else {
                dateLabel.setText("Date: Unavailable");
            }
        } catch (IOException e) {
            e.printStackTrace();
            dateLabel.setText("Date: Error");
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    @FXML
    private void handleLogoutButtonAction() throws IOException {
        JavaFXApp.clearJwtToken();
        JavaFXApp.clearUserId(); //TODO: Upewnic sie po co to

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
        if (meetingCode.isEmpty()) {
            messageLabel.setText("Meeting code cannot be empty.");
            return;
        }

        HttpURLConnection conn = null;
        try {
            conn = HttpUtils.createConnection("http://localhost:8080/api/meetings/join", "POST", jwtToken, true);

            String jsonPayload = "{\"code\":\"" + meetingCode + "\"}";
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonPayload.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                messageLabel.setText("Successfully joined the meeting.");
                loadMeetings();
                closeSlideInPane();
            } else {
                messageLabel.setText("Invalid meeting code.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            messageLabel.setText("An error occurred while joining the meeting.");
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
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

    @FXML
    private void handleUsersButtonAction(Long meetingId) {
        if (!usersSlideInPane.isVisible()) {
            usersSlideInPane.setVisible(true);
            TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), usersSlideInPane);
            slideIn.setFromX(usersSlideInPane.getTranslateX());
            slideIn.setToX(0);
            slideIn.play();
        }

        lastUserNumber = 0;
        usersListVBox.getChildren().clear();

        HttpURLConnection conn = null;
        try {
            conn = HttpUtils.createConnection("http://localhost:8080/api/meetings/" + meetingId + "/participants", "GET", jwtToken, false);
            int responseCode = conn.getResponseCode();
            StringBuilder response = new StringBuilder();
            try (Scanner scanner = new Scanner(conn.getInputStream())) {
                while (scanner.hasNextLine()) {
                    response.append(scanner.nextLine());
                }
            }

            if (responseCode == HttpURLConnection.HTTP_OK) {
                JSONObject responseObject = new JSONObject(response.toString());
                JSONArray participantsArray = responseObject.getJSONArray("participants");

                List<JSONObject> userList = new ArrayList<>();
                for (int i = 0; i < participantsArray.length(); i++) {
                    userList.add(participantsArray.getJSONObject(i));
                }
                userList.sort(Comparator.comparing(user -> (user.getString("firstName") + " " + user.getString("lastName"))));

                // wyodrębnienie ownerID z JSON aby rozrozniac wlasciciela
                Long ownerId = responseObject.getJSONObject("owner").getLong("id");
                boolean isMeetingOwner = isOwner(ownerId);

                for (JSONObject user : userList) {
                    String userName = user.getString("firstName") + " " + user.getString("lastName");
                    String numberedUserName = (++lastUserNumber) + ". " + userName;

                    HBox userBox = new HBox();
                    userBox.setSpacing(10);

                    Label userLabel = new Label(numberedUserName);
                    userBox.getChildren().add(userLabel);

                    Long userId = user.getLong("id");
                    if (isMeetingOwner && !userId.equals(ownerId)) {
                        Button removeButton = new Button("Remove");
                        removeButton.setOnAction(event -> handleRemoveUserButtonAction(meetingId, user.getString("username")));
                        userBox.getChildren().add(removeButton);
                    }

                    usersListVBox.getChildren().add(userBox);
                }
            } else {
                System.out.println("Failed to load users. Server responded with code " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }


    //TODO: Do zmiany uzywajac metody do skrocenia polaczenia
    private void handleRemoveUserButtonAction(Long meetingId, String username) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL("http://localhost:8080/api/meetings/" + meetingId + "/participants/" + username);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");
            conn.setRequestProperty("Authorization", "Bearer " + jwtToken);
            conn.setRequestProperty("Content-Type", "application/json");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                messageLabel.setText("User removed successfully.");
                handleUsersButtonAction(meetingId); // Odśwież listę użytkowników
            } else {
                messageLabel.setText("Failed to remove user. Server responded with code " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
            messageLabel.setText("An error occurred while removing the user.");
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    @FXML
    private void handleCloseUsersButtonAction() {
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), usersSlideInPane);
        slideOut.setFromX(usersSlideInPane.getTranslateX());
        slideOut.setToX(usersSlideInPane.getPrefWidth());
        slideOut.setOnFinished(e -> usersSlideInPane.setVisible(false));
        slideOut.play();
    }

    @FXML
    private void handleDateButtonAction(Long meetingId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dateSelectionScene.fxml"));
            Stage stage = (Stage) addMeetingButton.getScene().getWindow();
            Scene newScene = new Scene(loader.load());

            DateSelectionController controller = loader.getController();
            controller.setMeetingId(meetingId);

            stage.setScene(newScene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCommonDatesButtonAction(Long meetingId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/commonDatesScene.fxml"));
            Stage stage = (Stage) addMeetingButton.getScene().getWindow();
            Scene newScene = new Scene(loader.load());

            CommonDatesController controller = loader.getController();
            controller.setMeetingId(meetingId);
            controller.fetchCommonDates();


            stage.setScene(newScene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDeleteMeetingButtonAction(Long meetingId) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Are you sure you want to delete this meeting?");
        alert.setContentText("This action cannot be undone.");

        ButtonType confirmButton = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(confirmButton, cancelButton);

        alert.showAndWait().ifPresent(response -> {
            if (response == confirmButton) {
                HttpURLConnection conn = null;
                try {
                    conn = HttpUtils.createConnection(
                            "http://localhost:8080/api/meetings/" + meetingId,
                            "DELETE",
                            jwtToken,
                            false
                    );

                    int responseCode = conn.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        messageLabel.setText("Meeting deleted successfully.");
                        loadMeetings();
                    } else {
                        messageLabel.setText("Failed to delete meeting. Server responded with code " + responseCode);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    messageLabel.setText("An error occurred while deleting the meeting.");
                } finally {
                    if (conn != null) {
                        conn.disconnect();
                    }
                }
            }
        });
    }
}
