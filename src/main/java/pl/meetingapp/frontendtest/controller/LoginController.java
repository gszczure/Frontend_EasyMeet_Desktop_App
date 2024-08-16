package pl.meetingapp.frontendtest.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import pl.meetingapp.frontendtest.JavaFXApp;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

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

                if (response.toString().contains("username")) {
                    loginMessageLabel.setText("Logowanie się powiodło!");
//                    JavaFXApp.getStage().setScene(new Scene(FXMLLoader.load(getClass().getResource("/fxml/MainScene.fxml"))));
                } else {
                    loginMessageLabel.setText("Niepoprawna nazwa użytkownika lub hasło!");
                }
            } else {
                loginMessageLabel.setText("Nie udało się połączyć z serwerem.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            loginMessageLabel.setText("An error occurred.");
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
