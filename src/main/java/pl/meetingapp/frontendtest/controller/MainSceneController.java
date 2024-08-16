package pl.meetingapp.frontendtest.controller;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.event.ActionEvent;

public class MainSceneController {

    @FXML
    private TextField ownerUsernameTextField;

    @FXML
    private TextField meetingTitleTextField;

    @FXML
    private DatePicker meetingDatePicker;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    @FXML
    private void initialize() {
        // Opcjonalnie, możesz dodać kod inicjalizujący, np. ustawienia domyślne
    }

//    @FXML
//    private void saveButtonAction(ActionEvent event) {
//        // Pobranie wartości z pól tekstowych
//        String ownerUsername = ownerUsernameTextField.getText();
//        String meetingTitle = meetingTitleTextField.getText();
//        LocalDate meetingDate = meetingDatePicker.getValue();
//
//        if (ownerUsername == null || ownerUsername.trim().isEmpty() ||
//                meetingTitle == null || meetingTitle.trim().isEmpty() ||
//                meetingDate == null) {
//            // Wyświetlenie komunikatu o błędzie
//            showAlert("Validation Error", "All fields must be filled in.");
//            return;
//        }
//
//        // Tutaj dodaj kod do zapisania spotkania (np. wysłanie do serwera)
//
//        // Wyświetlenie komunikatu o sukcesie
//        showAlert("Success", "Meeting saved successfully!");
//    }

    @FXML
    private void cancelButtonAction(ActionEvent event) {
        // Możesz dodać kod do zamknięcia okna lub wyczyszczenia pól
        // Na przykład:
        ownerUsernameTextField.clear();
        meetingTitleTextField.clear();
        meetingDatePicker.setValue(null);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
