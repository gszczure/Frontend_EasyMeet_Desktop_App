package pl.meetingapp.frontendtest.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.Scanner;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.json.JSONObject;
import pl.meetingapp.frontendtest.JavaFXApp;
import pl.meetingapp.frontendtest.util.HttpUtils;

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
    private Label SingUpLabel;

    @FXML
    private void loginButtonOnAction() {
        String username = usernameTextField.getText().trim();
        String password = passwordPasswordField.getText().trim();

        try {
            HttpURLConnection connection = HttpUtils.createConnection("http://localhost:8080/api/auth/login",
                    "POST",
                    null,
                    true);

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
                Long userId = jsonResponse.optLong("userId");

                if (!token.isEmpty()) {
                    JavaFXApp.setJwtToken(token);
                    JavaFXApp.setUserId(userId);
                    loginMessageLabel.setText("Login successful!");
                    Stage stage = (Stage) loginButton.getScene().getWindow();
                    Scene newScene = new Scene(FXMLLoader.load(getClass().getResource("/fxml/mainScene.fxml")));
                    stage.setScene(newScene);
                } else {
                    loginMessageLabel.setText("Failed to connect to the server. Error code: " + responseCode);
                }
            } else {
                loginMessageLabel.setText("Incorrect username or password!");
            }
        } catch (IOException e) {
            e.printStackTrace();
            loginMessageLabel.setText("An error occurred.");
        }
    }

    @FXML
    private void registrationButtonOnAction(MouseEvent e) throws IOException { //TODO: zmienic nazwe metody bi juz nie ma guzika tylko tekst
        Stage stage = (Stage) SingUpLabel.getScene().getWindow();
        Scene newScene = new Scene(FXMLLoader.load(getClass().getResource("/fxml/registrationSceneFRONT.fxml")));
        stage.setScene(newScene);
    }

    @FXML
    public void cancelButtonAction(ActionEvent e) {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
        System.exit(0);
    }
}
