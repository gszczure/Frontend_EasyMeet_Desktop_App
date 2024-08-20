package pl.meetingapp.frontendtest.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.json.JSONObject;  // Dodaj import do obsługi JSON
import pl.meetingapp.frontendtest.JavaFXApp;

public class LoginController {

    @FXML
    private TextField usernameTextField;

    @FXML
    private PasswordField passwordPasswordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label loginMessageLabel;

    @FXML
    private Button registrationButton;

    @FXML
    private Button cancelButton;

    @FXML
    private void loginButtonOnAction() {
        String username = usernameTextField.getText().trim();
        String password = passwordPasswordField.getText().trim();

        try {
            URL url = new URL("http://localhost:8080/api/auth/login");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            String jsonInputString = "{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}";

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Scanner scanner = new Scanner(connection.getInputStream());
                StringBuilder response = new StringBuilder();
                while (scanner.hasNext()) {
                    response.append(scanner.nextLine());
                }
                scanner.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                String token = jsonResponse.optString("token");

                if (!token.isEmpty()) {
                    JavaFXApp.setJwtToken(token);
                    loginMessageLabel.setText("Logowanie się powiodło!");
                    JavaFXApp.getStage().setScene(new Scene(FXMLLoader.load(getClass().getResource("/fxml/mainScene.fxml"))));
                } else {
                    loginMessageLabel.setText("Niepoprawna nazwa użytkownika lub hasło!");
                }
            } else {
                loginMessageLabel.setText("Nie udało się połączyć z serwerem. Kod błędu: " + responseCode);
                // do naprawy przy wpisywaniu blednych danych wyswietla sie ten komunikat zamiast tego wyzej
            }
        } catch (IOException e) {
            e.printStackTrace();
            loginMessageLabel.setText("Wystąpił błąd.");
        }
    }

    @FXML
    private void registrationButtonOnAction(ActionEvent e) throws IOException {
        JavaFXApp.getStage().setScene(new Scene(FXMLLoader.load(getClass().getResource("/fxml/registrationSceneFRONT.fxml"))));
    }

    @FXML
    public void cancelButtonAction(ActionEvent e) {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
        System.exit(0);
    }
}
