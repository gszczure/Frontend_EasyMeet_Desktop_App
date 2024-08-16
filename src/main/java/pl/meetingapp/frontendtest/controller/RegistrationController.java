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

public class RegistrationController {

    @FXML
    private TextField usernameTextField;

    @FXML
    private PasswordField passwordPasswordField;

    @FXML
    private PasswordField confirmPasswordPasswordField;

    @FXML
    private TextField emailTextField;

    @FXML
    private Button registrationButton;

    @FXML
    private Button backButton;

    @FXML
    private Label registrationLabelMessage;

    @FXML
    private TextField firstNameTextField;

    @FXML
    private TextField lastNameTextField;

    @FXML
    private TextField phoneNumberTextField;

    @FXML
    private Button cancelButton;

    @FXML
    private void registrationButtonOnAction() {
        String username = usernameTextField.getText().trim();
        String password = passwordPasswordField.getText().trim();
        String confirmPassword = confirmPasswordPasswordField.getText().trim();
        String email = emailTextField.getText().trim();
        String firstName = firstNameTextField.getText().trim();
        String lastName = lastNameTextField.getText().trim();
        String phoneNumber = phoneNumberTextField.getText().trim();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || email.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || phoneNumber.isEmpty()) {
            registrationLabelMessage.setText("All fields must be filled out!");
            return;
        }

        if (!password.equals(confirmPassword)) {
            registrationLabelMessage.setText("Passwords do not match!");
            return;
        }

        if (password.length() < 6) {
            registrationLabelMessage.setText("Password must be at least 6 characters long!");
            return;
        }

        if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            registrationLabelMessage.setText("Invalid email format!");
            return;
        }
        if (!phoneNumber.matches("\\d{9}")) {
            registrationLabelMessage.setText("Phone number must be at least 9 numbers long!");
            return;
        }

        try {
            URL url = new URL("http://localhost:8080/api/auth/register");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; utf-8");

            String jsonInputString = "{\"username\": \"" + username + "\", \"password\": \"" + password + "\", \"email\": \"" + email + "\", \"firstName\": \"" + firstName + "\", \"lastName\": \"" + lastName + "\", \"phoneNumber\": \"" + phoneNumber + "\"}";

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                JavaFXApp.getStage().setScene(new Scene(FXMLLoader.load(getClass().getResource("/fxml/loginSceneFRONT.fxml"))));
            } else {
                registrationLabelMessage.setText("Registration failed.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            registrationLabelMessage.setText("An error occurred.");
        }
    }

    @FXML
    private void backButtonOnAction() throws IOException {
        JavaFXApp.getStage().setScene(new Scene(FXMLLoader.load(getClass().getResource("/fxml/loginSceneFRONT.fxml"))));
    }
    @FXML
    public void cancelButtonAction(ActionEvent e) {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
        System.exit(0);
    }
}
