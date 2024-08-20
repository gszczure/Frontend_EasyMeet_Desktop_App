package pl.meetingapp.frontendtest.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import pl.meetingapp.frontendtest.JavaFXApp;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainSceneController {

    @FXML
    private Accordion accordion;
    @FXML
    private Button logoutButton;

    @FXML
    private void initialize() {
        loadMeetings(); // Ładowanie spotkań przy starcie sceny
    }

    @FXML
    private void handleCreateMeetingButtonAction() throws IOException {
        String token = JavaFXApp.getJwtToken();

        if (token == null || token.isEmpty()) {
            JavaFXApp.getStage().setScene(new Scene(FXMLLoader.load(getClass().getResource("/fxml/loginScene.fxml"))));
        } else {
            // Przejście do sceny tworzenia spotkania
            JavaFXApp.getStage().setScene(new Scene(FXMLLoader.load(getClass().getResource("/fxml/createMeetingScene.fxml"))));

            // Po powrocie do tej sceny, załaduj zaktualizowaną listę spotkań
            loadMeetings();
        }
    }

    private void loadMeetings() {
        accordion.getPanes().clear(); // Czyścimy obecne spotkania przed załadowaniem nowych
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

    // Dodawanie spotkanai do listy na srodku ekranu
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

        JavaFXApp.getStage().setScene(new Scene(FXMLLoader.load(getClass().getResource("/fxml/loginSceneFRONT.fxml"))));
    }
}
