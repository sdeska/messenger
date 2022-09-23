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
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Handles the GUI of the clientside software.
 */
public class MessengerGUI extends Application {
    
    private MessengerClient client = null;

    /**
     * Initializes the application and displays a view asking for an username.
     * @param stage the Stage container on which to run this method.
     * Essentially always the primary Stage which is constructed by the platform when launch() is called.
     * @since 1.0
     */
    @Override
    public void start(Stage stage) throws IOException {
        
        client = new MessengerClient();
        stage.setTitle("Messenger");

        // Creating the container in which to have the whole setup window.
        var setupView = new VBox(5);
        setupView.setId("setupView");
        setupView.setAlignment(Pos.CENTER);
        setupView.setPadding(new Insets(10));

        // Creating and adding UI elements.
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

        // Manually close any possible socket on quit.
        stage.setOnCloseRequest(event -> {

            if (client.getSocket() == null) {
                return;
            }
            try {
                client.getSocket().close();
            } catch (IOException e) {
                System.out.println("Error: Unable to close socket.");
            }

        });

        // Adding an eventhandler for the connect-button.
        confirmButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {

                if (!client.setName(nameBox.getText())) {
                    nameLabel.setText("Please enter a non-empty name.");
                    return;
                }
                if (!client.connectToServer()) {
                    nameLabel.setText("Connecting to server failed. Please contact the server administrator.");
                    return;
                }
                startMainView(stage);
            }

        });

    }

    /**
     * Initializes and displays the main view of the application.
     * @param stage the Stage container in which to create the main view.
     */
    public void startMainView(Stage stage) {

        var mainView = new HBox();
        mainView.setId("mainView");

        // Creating the contact panel displaying the contacts in the UI.
        var contactPanel = new Pane();
        contactPanel.setId("contactPanel");
        contactPanel.setPrefWidth(240);
        contactPanel.setBackground(new Background(new BackgroundFill(Color.DARKGREY, CornerRadii.EMPTY, Insets.EMPTY)));
        mainView.getChildren().add(contactPanel);

        // Creating the chat panel displaying any opened chat content.
        var chatPanel = new Pane();
        chatPanel.setId("chatPanel");
        chatPanel.setPrefWidth(480);
        chatPanel.setBackground(new Background(new BackgroundFill(Color.GREY, CornerRadii.EMPTY, Insets.EMPTY)));
        mainView.getChildren().add(chatPanel);

        stage.getScene().setRoot(mainView);
        stage.show();

    }

    public static void main(String[] args) {
        launch();
    }

}
