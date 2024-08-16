package pl.meetingapp.frontendtest;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class JavaFXApp extends Application {

    private static Stage stage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;
        primaryStage.setResizable(false);
        loadScene("/fxml/loginSceneFRONT.fxml", 585.0, 464.0); // Set default size
    }

    public static void setStage(Stage primaryStage) {
        stage = primaryStage;
    }

    public static Stage getStage() {
        return stage;
    }

//    public void changeScene(String fxml, double width, double height) throws IOException {
//        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
//        Parent pane = loader.load();
//        Scene newScene = new Scene(pane);
//
//        Stage currentStage = getStage();
//        currentStage.setScene(newScene);
//
//        if (width > 0 && height > 0) {
//            currentStage.setWidth(width);
//            currentStage.setHeight(height);
//        }
//    }

    public void loadScene(String fxml, double width, double height) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
        Parent root = loader.load();
        Scene scene = new Scene(root, width, height);

        Stage primaryStage = getStage();
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
