package pl.meetingapp.frontendtest.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javafx.stage.Stage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;
import pl.meetingapp.frontendtest.JavaFXApp;
import pl.meetingapp.frontendtest.util.HttpUtils;

public class MainSceneController {

    public Label createMeetingMessageLabel;
    public TextField meetingTitleTextField;

    @FXML
    private Accordion accordion;

    @FXML
    private Button logoutButton;

    @FXML
    private Button leaveMeetingButton;

    @FXML
    private Button addMeetingButton;

    @FXML
    private AnchorPane joinMeetingslideInPane;

    @FXML
    private TextField meetingCodeTextField;

    @FXML
    private Label messageLabel;

    @FXML
    private Label messageLabel2;

    @FXML
    private AnchorPane usersSlideInPane;

    @FXML
    private VBox usersListVBox;

    @FXML
    private AnchorPane createMeetingSlideInPane;

    private int lastUserNumber = 0;

    private String jwtToken;

    // Metoda służy do inicjalizacji widoku, ukrywania paneli oraz ładowania spotkań.
    @FXML
    private void initialize() {
        joinMeetingslideInPane.setVisible(false);
        usersSlideInPane.setVisible(false);
        createMeetingSlideInPane.setVisible(false);
        this.jwtToken = JavaFXApp.getJwtToken();
        loadMeetings();
    }

    // Metoda służy do sprawdzania, czy aktualny użytkownik jest właścicielem spotkania.
    private boolean isOwner(Long ownerId) {
        Long currentUserId = JavaFXApp.getUserId();
        return currentUserId != null && currentUserId.equals(ownerId);
    }

    // Metoda służy do czyszczenia tekstu etykiety po określonym czasie.
    private void clearMessageLabelAfterDelay(Label label, Duration delay) {
        Timeline timeline = new Timeline(new KeyFrame(delay, event -> label.setText("")));
        timeline.setCycleCount(1);
        timeline.play();
    }

    // Metoda służy do ładowania spotkań z serwera i dodawania ich do akordeonu.
    private void loadMeetings() {
        accordion.getPanes().clear();
        HttpURLConnection conn = null;
        try {
            conn = HttpUtils.createConnection(
                    "http://localhost:8080/api/meetings/for-user",
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
                messageLabel2.setText("Failed to load meetings. Server responded with code " + responseCode);
                clearMessageLabelAfterDelay(messageLabel2, Duration.seconds(2));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    // Metoda służy do tworzenia i dodawania spotkania do akordeonu, w tym ustawiania daty, komentarza oraz przycisków
    private void addMeetingToAccordion(String name, String ownerName, String code, Long meetingId, Long ownerId) {
        TitledPane titledPane = new TitledPane();

        // TitleBox (HBox) - Nagłówek TitledPane
        HBox titleBox = new HBox();
        titleBox.setPadding(new Insets(2, 5, 2, 5)); //1. gora 2.lewa 3.dol 4.prawa
        titleBox.setSpacing(10);

        // HBox dla etykiet
        HBox titleContentBox = new HBox();
        titleContentBox.setSpacing(10);

        Label nameLabel = new Label(name);
        nameLabel.setTextFill(Color.BLACK);
        titleContentBox.getChildren().add(nameLabel);

        Label ownerLabel = new Label(ownerName);
        ownerLabel.setTextFill(Color.BLACK);
        titleContentBox.getChildren().add(ownerLabel);

        titleBox.getChildren().add(titleContentBox);

        // content (VBox) - Zawiera zawartość TitledPane
        VBox content = new VBox();
        content.setSpacing(10);

        // HBox dla daty i kodu (hbox)
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(5, 10, 0, 10));

        Label dateLabel = new Label();
        dateLabel.setTextFill(Color.GREEN);
        hbox.getChildren().add(dateLabel);

        Label commentLabel = new Label();
        commentLabel.setTextFill(Color.BLUE);

        // HBox dla komentarza
        HBox commentBox = new HBox();
        commentBox.setPadding(new Insets(5, 10, 0, 10));
        commentBox.getChildren().add(commentLabel);

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
        content.getChildren().add(commentBox);

        // HBox dla przycisków (buttonBox)
        HBox buttonBox = new HBox();
        buttonBox.setSpacing(10);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        buttonBox.setAlignment(Pos.CENTER);

        Button usersButton = new Button("Users");
        usersButton.setOnAction(event -> handleUsersButtonAction(meetingId));
        usersButton.getStyleClass().add("usersButton");

        Button commonDatesButton = new Button("Common Dates");
        commonDatesButton.setOnAction(event -> handleCommonDatesButtonAction(meetingId, ownerId)); // przesyłanie meetingId i ownerId
        commonDatesButton.getStyleClass().add("commonDatesButton");

        Button dateButton = new Button("Date");
        dateButton.setOnAction(event -> handleDateButtonAction(meetingId));
        dateButton.getStyleClass().add("dateButton");

        buttonBox.getChildren().addAll(usersButton, commonDatesButton, dateButton);

        // Dodanie przycisku "X" tylko dla właściciela
        if (isOwner(ownerId)) {
            Button deleteButton = new Button("X");
            deleteButton.setOnAction(event -> handleDeleteMeetingButtonAction(meetingId));
            deleteButton.getStyleClass().add("deleteButton");
            buttonBox.getChildren().add(deleteButton);
        }

        content.getChildren().add(buttonBox);

        titledPane.setContent(content);
        titledPane.setGraphic(titleBox);
        titledPane.setUserData(meetingId);

        accordion.getPanes().add(titledPane);
        fetchMeetingDate(meetingId, dateLabel);
        fetchMeetingComment(meetingId, commentLabel);
    }

    // Metoda służy do pobierania komentarza dla spotkania i wyświetlania go na etykiecie.
    private void fetchMeetingComment(Long meetingId, Label commentLabel) {
        HttpURLConnection conn = null;
        try {
            conn = HttpUtils.createConnection(
                    "http://localhost:8080/api/meetings/" + meetingId + "/comment",
                    "GET",
                    jwtToken,
                    false
            );

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (Scanner scanner = new Scanner(conn.getInputStream())) {
                    if (scanner.hasNextLine()) {
                        String comment = scanner.nextLine();
                        commentLabel.setText("Comment: " + comment);
                    } else {
                        commentLabel.setText("Comment: No comment");
                        commentLabel.setTextFill(Color.RED);
                    }
                }
            } else {
                commentLabel.setText("Comment: Unavailable");
                commentLabel.setTextFill(Color.RED);
            }
        } catch (IOException e) {
            e.printStackTrace();
            commentLabel.setText("Comment: Error");
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    // Metoda służy do pobierania daty spotkania i wyświetlania jej na etykiecie.
    private void fetchMeetingDate(Long meetingId, Label dateLabel) {
        HttpURLConnection conn = null;
        try {
            conn = HttpUtils.createConnection(
                    "http://localhost:8080/api/meetings/" + meetingId + "/date",
                    "GET",
                    jwtToken,
                    false
            );

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (Scanner scanner = new Scanner(conn.getInputStream())) {
                    if (scanner.hasNextLine()) {
                        String response = scanner.nextLine();

                        try {
                            LocalDate date = LocalDate.parse(response);
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH);
                            String formattedDate = date.format(formatter);
                            dateLabel.setText("Date: " + formattedDate);
                        } catch (DateTimeParseException e) {
                            dateLabel.setText("Date: Invalid format");
                            dateLabel.setTextFill(Color.RED);
                        }
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

    // Metoda służy do wylogowywania urzytkowników, czyszczenia tokenu JWT i ID użytkownika, a następnie przeładowania sceny na scenę logowania.
    @FXML
    private void handleLogoutButtonAction() throws IOException {
        JavaFXApp.clearJwtToken();
        JavaFXApp.clearUserId();

        Stage stage = (Stage) logoutButton.getScene().getWindow();
        Scene newScene = new Scene(FXMLLoader.load(getClass().getResource("/fxml/loginSceneFRONT.fxml")));
        stage.setScene(newScene);
    }

    // Metoda służy do dołączenia do spotkania na podstawie podanego kodu spotkania.
    @FXML
    private void handleJoinButtonAction() {
        String meetingCode = meetingCodeTextField.getText().trim();
        if (meetingCode.isEmpty()) {
            messageLabel.setText("Meeting code cannot be empty.");
            clearMessageLabelAfterDelay(messageLabel, Duration.seconds(2));
            return;
        }

        HttpURLConnection conn = null;
        try {
            conn = HttpUtils.createConnection(
                    "http://localhost:8080/api/meetings/join",
                    "POST",
                    jwtToken,
                    true
            );

            String jsonPayload = "{\"code\":\"" + meetingCode + "\"}";
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonPayload.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                messageLabel2.setText("Successfully joined the meeting.");
                clearMessageLabelAfterDelay(messageLabel2, Duration.seconds(2));
                loadMeetings();
                handleCancelJoinMeetingButtonAction();
            } else if (responseCode == HttpURLConnection.HTTP_CONFLICT) {
                messageLabel.setText("You already belong to this meeting.");
                clearMessageLabelAfterDelay(messageLabel, Duration.seconds(2));
            } else {
                messageLabel.setText("Invalid meeting code.");
                clearMessageLabelAfterDelay(messageLabel, Duration.seconds(2));
            }
        } catch (IOException e) {
            e.printStackTrace();
            messageLabel.setText("An error occurred while joining the meeting.");
            clearMessageLabelAfterDelay(messageLabel, Duration.seconds(2));
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    // Metoda służy do ładowania uczestników spotkania.
    private void loadUsers(Long meetingId) {
        lastUserNumber = 0;
        usersListVBox.getChildren().clear();

        HttpURLConnection conn = null;
        try {
            conn = HttpUtils.createConnection(
                    "http://localhost:8080/api/meetings/" + meetingId + "/participants",
                    "GET",
                    jwtToken,
                    false
            );

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
                        removeButton.getStyleClass().add("remove-button");
                        removeButton.setOnAction(event -> handleRemoveUserButtonAction(meetingId, user.getString("username")));
                        userBox.getChildren().add(removeButton);
                    }

                    usersListVBox.getChildren().add(userBox);
                }

                leaveMeetingButton.setVisible(!isMeetingOwner);

            } else {
                messageLabel2.setText("Failed to load users. Server responded with code " + responseCode);
                clearMessageLabelAfterDelay(messageLabel2, Duration.seconds(2));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    // Metoda obsługująca usunięcie użytkownika z listy uczestników spotkania (tylko włąściciciel).
    private void handleRemoveUserButtonAction(Long meetingId, String username) {
        HttpURLConnection conn = null;
        try {
            conn = HttpUtils.createConnection(
                    "http://localhost:8080/api/meetings/" + meetingId + "/participants/" + username,
                    "DELETE",
                    jwtToken,
                    false
            );

            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                messageLabel2.setText("User removed successfully.");
                clearMessageLabelAfterDelay(messageLabel2, Duration.seconds(2));
                loadUsers(meetingId); // Odśwież listę użytkowników
            } else {
                messageLabel2.setText("Failed to remove user. Server responded with code " + responseCode);
                clearMessageLabelAfterDelay(messageLabel2, Duration.seconds(2));
            }
        } catch (IOException e) {
            e.printStackTrace();
            messageLabel2.setText("An error occurred while removing the user.");
            clearMessageLabelAfterDelay(messageLabel2, Duration.seconds(2));
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }


    // Metoda obsługująca przycisk do zmiany sceny na dateSelectionScene.
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

    // Metoda obsługująca przycisk do zmiany sceny na commonDatesScene.
    @FXML
    private void handleCommonDatesButtonAction(Long meetingId, Long ownerId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/commonDatesScene.fxml"));
            Stage stage = (Stage) addMeetingButton.getScene().getWindow();
            Scene newScene = new Scene(loader.load());

            CommonDatesController controller = loader.getController();
            controller.setMeetingId(meetingId);
            controller.setOwnerId(ownerId);
            controller.fetchCommonDates();


            stage.setScene(newScene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Metoda obsługująca usunięcie spotkania po potwierdzeniu przez właściciela.
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
                        messageLabel2.setText("Meeting deleted successfully.");
                        clearMessageLabelAfterDelay(messageLabel2, Duration.seconds(2));
                        loadMeetings();
                    } else {
                        messageLabel2.setText("Failed to delete meeting. Server responded with code " + responseCode);
                        clearMessageLabelAfterDelay(messageLabel2, Duration.seconds(2));

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    messageLabel2.setText("An error occurred while deleting the meeting.");
                    clearMessageLabelAfterDelay(messageLabel2, Duration.seconds(2));
                } finally {
                    if (conn != null) {
                        conn.disconnect();
                    }
                }
            }
        });
    }

    // Metoda obsługująca opuszczenia spotkania po potwierdzeniu przez użytkownika.
    @FXML
    private void handleLeaveMeetingButtonAction() {
        // Pobranie id spotkania z rozwinietego TitledPane by wiedziec ktore spotkanie uzytkownik chce ospuscic
        TitledPane selectedPane = accordion.getExpandedPane();
        Long meetingId = (Long) selectedPane.getUserData();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Leave");
        alert.setHeaderText("Are you sure you want to leave this meeting?");
        alert.setContentText("You will no longer be a participant.");

        ButtonType confirmButton = new ButtonType("Leave", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(confirmButton, cancelButton);

        alert.showAndWait().ifPresent(response -> {
            if (response == confirmButton) {
                HttpURLConnection conn = null;
                try {
                    conn = HttpUtils.createConnection(
                            "http://localhost:8080/api/meetings/" + meetingId + "/leave",
                            "DELETE",
                            jwtToken,
                            true
                    );

                    int responseCode = conn.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        messageLabel2.setText("Successfully left the meeting.");
                        clearMessageLabelAfterDelay(messageLabel2, Duration.seconds(2));
                        loadMeetings();
                        handleCloseUsersButtonAction();
                    } else {
                        messageLabel2.setText("Failed to leave meeting. Server responded with code " + responseCode);
                        clearMessageLabelAfterDelay(messageLabel2, Duration.seconds(2));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    messageLabel2.setText("An error occurred while leaving the meeting.");
                    clearMessageLabelAfterDelay(messageLabel2, Duration.seconds(2));
                } finally {
                    if (conn != null) {
                        conn.disconnect();
                    }
                }
            }
        });
    }

    // Metoda obsługująca zapisywanie nowego spotkania.
    @FXML
    private void handleSaveButtonAction() {
        String meetingTitle = meetingTitleTextField.getText().trim();

        // Maksymalna długość nazwy spotkania
        final int MAX_TITLE_LENGTH = 25;

        if (meetingTitle.isEmpty()) {
            createMeetingMessageLabel.setText("Meeting title must be provided.");
            return;
        }

        if (meetingTitle.length() > MAX_TITLE_LENGTH) {
            createMeetingMessageLabel.setText("Meeting title must be less than " + MAX_TITLE_LENGTH + " characters.");
            return;
        }

        HttpURLConnection conn = null;
        try {
            conn = HttpUtils.createConnection(
                    "http://localhost:8080/api/meetings/create",
                    "POST",
                    jwtToken,
                    true);

            String jsonPayload = "{\"name\":\"" + meetingTitle + "\"}";

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonPayload.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int code = conn.getResponseCode();
            if (code == HttpURLConnection.HTTP_OK) {
                messageLabel2.setText("The meeting has been created.");
                clearMessageLabelAfterDelay(messageLabel2, Duration.seconds(2));
                loadMeetings();
                handleCancelCreateMeetingButtonAction();
            } else {
                StringBuilder response = new StringBuilder();
                InputStream errorStream = conn.getErrorStream();
                if (errorStream != null) {
                    try (Scanner scanner = new Scanner(errorStream)) {
                        while (scanner.hasNextLine()) {
                            response.append(scanner.nextLine());
                        }
                    }
                    createMeetingMessageLabel.setText("Adding failed.");
                } else {
                    createMeetingMessageLabel.setText("Adding failed. Server responded with code " + code + ". No error stream available.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            createMeetingMessageLabel.setText("An error occurred while creating the meeting.");
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    // Metoda obsługująca otworzenie panelu tworzenia spotkania z animacją wysuwania.
    @FXML
    public void handleAddMeetingButtonAction() {
        if (!createMeetingSlideInPane.isVisible()) {
            createMeetingSlideInPane.setVisible(true);
            TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), createMeetingSlideInPane);
            slideIn.setFromX(createMeetingSlideInPane.getTranslateX());
            slideIn.setToX(0);
            slideIn.play();
        }
    }

    // Metoda obsługująca zamknięcie panelu tworzenia spotkania z animacją wsuwania.
    @FXML
    public void handleCancelCreateMeetingButtonAction() {
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), createMeetingSlideInPane);
        slideOut.setFromX(createMeetingSlideInPane.getTranslateX());
        slideOut.setToX(createMeetingSlideInPane.getPrefWidth());
        slideOut.setOnFinished(e -> createMeetingSlideInPane.setVisible(false));
        slideOut.play();
    }

    // Metoda obsługująca otworzenie panelu z lista urzytkowników z animacją wysuwania.
    @FXML
    private void handleUsersButtonAction(Long meetingId) {
        if (!usersSlideInPane.isVisible()) {
            usersSlideInPane.setVisible(true);
            TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), usersSlideInPane);
            slideIn.setFromX(usersSlideInPane.getTranslateX());
            slideIn.setToX(0);
            slideIn.play();
            loadUsers(meetingId);
        }
    }

    // Metoda obsługująca zamknięcie panelu z listą użytkowników z animacją wsuwania.
    @FXML
    private void handleCloseUsersButtonAction() {
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), usersSlideInPane);
        slideOut.setFromX(usersSlideInPane.getTranslateX());
        slideOut.setToX(usersSlideInPane.getPrefWidth());
        slideOut.setOnFinished(e -> usersSlideInPane.setVisible(false));
        slideOut.play();
    }

    // Metoda obsługująca otworzenie panelu dołączania do spotkania z animacją wysuwania.
    @FXML
    private void handleJoinMeetingButtonAction() {
        if (!joinMeetingslideInPane.isVisible()) {
            joinMeetingslideInPane.setVisible(true);
            TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), joinMeetingslideInPane);
            slideIn.setFromX(joinMeetingslideInPane.getTranslateX());
            slideIn.setToX(0);
            slideIn.play();
        }
    }

    // Metoda obsługująca zamknięcie panelu dołączania do spotkania z animacją wsuwania.
    @FXML
    private void handleCancelJoinMeetingButtonAction() {
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), joinMeetingslideInPane);
        slideOut.setFromX(joinMeetingslideInPane.getTranslateX());
        slideOut.setToX(joinMeetingslideInPane.getWidth());
        slideOut.setOnFinished(event -> joinMeetingslideInPane.setVisible(false));
        slideOut.play();
    }
}
