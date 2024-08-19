package pl.meetingapp.frontendtest.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import pl.meetingapp.frontendtest.JavaFXApp;

import java.io.IOException;

public class MainSceneController {

    @FXML
    private void handleCreateMeetingButtonAction() throws IOException {
        // do zmiany to nie dziala, trzeba zrobic osobny edpoint do sprawdzania czy JWT nei wygasl i potem przezucal miedzy scenami
        String token = JavaFXApp.getJwtToken();

        if (token == null || token.isEmpty()) {
            JavaFXApp.getStage().setScene(new Scene(FXMLLoader.load(getClass().getResource("/fxml/loginScene.fxml"))));
        } else {
            JavaFXApp.getStage().setScene(new Scene(FXMLLoader.load(getClass().getResource("/fxml/createMeetingScene.fxml"))));
        }
    }
}
