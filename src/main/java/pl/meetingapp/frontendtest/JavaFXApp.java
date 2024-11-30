package pl.meetingapp.frontendtest;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

public class JavaFXApp extends Application {

    @Getter
    @Setter
    private static Stage stage;
    @Getter
    @Setter
    private static String jwtToken;
    @Getter
    private static Long userId;

    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;
//        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setResizable(false);
        loadScene("/fxml/loginSceneFRONT.fxml", 520, 400); // Ustawienie domyślnego rozmiaru
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static void setUserId(Long userId) {
        JavaFXApp.userId = userId;
    }

    public void loadScene(String fxml, double width, double height) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
        Parent root = loader.load();
        Scene scene = new Scene(root, width, height);

        Stage primaryStage = getStage();
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    public static void clearJwtToken() {
        jwtToken = null;
    }
    public static void clearUserId() {
        userId = null;
    }
}
