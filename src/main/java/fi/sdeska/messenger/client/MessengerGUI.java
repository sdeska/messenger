package fi.sdeska.messenger.client;

import java.io.IOException;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MessengerGUI extends Application {
    
    private MessengerClient client = null;

    @Override
    public void start(Stage stage) throws IOException {
        
        client = new MessengerClient();

        var setupView = new VBox(5);
        setupView.setId("setupView");
        setupView.setAlignment(Pos.CENTER);
        setupView.setPadding(new Insets(10));
        var nameLabel = new Label("Please enter your nickname below.");
        nameLabel.setId("nameLabel");
        var nameBox = new TextField();
        nameBox.setId("nameBox");
        nameBox.setMaxWidth(640);
        var confirmButton = new Button("Connect");
        confirmButton.setId("confirmButton");
        setupView.getChildren().addAll(nameLabel, nameBox, confirmButton);

        var scene = new Scene(setupView, 720, 480);
        stage.setScene(scene);
        stage.show();

        confirmButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                
                if (!client.setName(nameBox.getText())) {
                    nameLabel.setText("Please enter a non-empty name.");
                    return;
                }
                if (!client.connectToServer()) {
                    return;
                }
                startMainView(stage);

            }
            
        });

    }

    public void startMainView(Stage stage) {

        var mainView = new HBox();
        mainView.setId("mainView");

        var contactPanel = new Pane();
        contactPanel.setId("contactPanel");
        contactPanel.setPrefWidth(240);
        mainView.getChildren().add(contactPanel);
        var chatPanel = new Pane();
        chatPanel.setId("chatPanel");
        chatPanel.setPrefWidth(480);
        mainView.getChildren().add(chatPanel);

        stage.getScene().setRoot(mainView);
        stage.show();

    }

    public static void main(String[] args) {
        launch();
    }

}
