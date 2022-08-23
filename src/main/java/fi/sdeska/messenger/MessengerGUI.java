package fi.sdeska.messenger;

import java.io.IOException;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class MessengerGUI extends Application {
    
    @Override
    public void start(Stage stage) throws IOException {

        var view = new HBox();

        var contactPanel = new Pane();
        contactPanel.setId("contactPanel");
        contactPanel.setPrefWidth(240);
        view.getChildren().add(contactPanel);
        var chatPanel = new Pane();
        chatPanel.setId("chatPanel");
        chatPanel.setPrefWidth(480);
        view.getChildren().add(chatPanel);

        var scene = new Scene(view, 720, 480);
        stage.setScene(scene);
        stage.show();

    }

    public static void main(String[] args) {
        launch();
    }

}
