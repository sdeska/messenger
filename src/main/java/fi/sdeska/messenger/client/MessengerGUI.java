package fi.sdeska.messenger.client;

import java.io.IOException;

import javafx.application.Application;
import javafx.application.Platform;
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
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;

/**
 * Handles the GUI of the clientside software.
 */
public class MessengerGUI extends Application {
    
    private MessengerClient client = null;
    private Stage stage = null;

    private static final int MIN_WINDOW_HEIGHT = 480;
    private static final int MIN_WINDOW_WIDTH = 720;
    private static final int MIN_CHAT_HEIGHT = 440;
    private static final int MIN_CONTACT_HEIGHT = 440;

    private String activeChat = "";

    /**
     * Initializes the application and displays a view asking for an username.
     * @param stage the Stage container on which to run this method.
     * Essentially always the primary Stage which is constructed by the platform when launch() is called.
     * @since 1.0
     */
    @Override
    public void start(Stage stage) throws IOException {
        
        this.stage = stage;
        client = new MessengerClient(this);
        stage.setTitle("Messenger");
        stage.setMinHeight(MIN_WINDOW_HEIGHT);
        stage.setMinWidth(MIN_WINDOW_WIDTH);

        // Creating the container in which to have the whole setup window.
        var setupView = new VBox(5);
        setupView.setId("setupView");
        setupView.setAlignment(Pos.CENTER);

        // Creating and adding UI elements.
        var nameLabel = new Label("Please enter your nickname below.");
        nameLabel.setId("nameLabel");
        var nameBox = new TextField();
        nameBox.setId("nameBox");
        nameBox.setMaxWidth(MIN_WINDOW_WIDTH * 0.9);
        var confirmButton = new Button("Connect");
        confirmButton.setId("confirmButton");
        setupView.getChildren().addAll(nameLabel, nameBox, confirmButton);

        var scene = new Scene(setupView);
        stage.setScene(scene);
        stage.show();

        // Manually close any possible socket on quit.
        stage.setOnCloseRequest(event -> {

            if (client.getSocket() == null) {
                return;
            }
            try {
                client.getSocket().close();
                client.getListeningThread().endListeningThread();
            } catch (IOException e) {
                System.err.println("Error: Unable to close socket.");
            }

        });

        // Adding an eventhandler for the connect-button.
        confirmButton.setOnAction((action) -> {

            if (!client.setName(nameBox.getText())) {
                nameLabel.setText("Please enter a non-empty name.");
                return;
            }
            if (!client.connectToServer()) {
                nameLabel.setText("Connecting to server failed. Please contact the server administrator.");
                return;
            }
            startMainView(stage);

            
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
        var contactPanel = new VBox();
        contactPanel.setAlignment(Pos.TOP_CENTER);
        contactPanel.setId("contactPanel");
        contactPanel.setMinWidth(MIN_WINDOW_WIDTH * 0.3);
        contactPanel.setMaxWidth(MIN_WINDOW_WIDTH * 0.3);
        contactPanel.setMinHeight(MIN_CONTACT_HEIGHT);
        contactPanel.setBackground(new Background(new BackgroundFill(Color.web("#b5b5b5"), CornerRadii.EMPTY, Insets.EMPTY)));
        mainView.getChildren().add(contactPanel);

        // Creating the chat panel displaying any opened chat content.
        var chatPanel = new VBox();
        chatPanel.setId("chatPanel");
        chatPanel.setMinWidth(MIN_WINDOW_WIDTH * 0.7);
        chatPanel.setMinHeight(MIN_CHAT_HEIGHT);
        chatPanel.setBackground(new Background(new BackgroundFill(Color.GREY, CornerRadii.EMPTY, Insets.EMPTY)));
        mainView.getChildren().add(chatPanel);
        HBox.setHgrow(chatPanel, Priority.ALWAYS);

        // Use the dimensions of the setup scene for the new scene and set the new scene to the stage.
        var oldScene = stage.getScene();
        var scene = new Scene(mainView, oldScene.getWidth(), oldScene.getHeight());
        stage.setScene(scene);
        stage.show();

    }

    /**
     * Refreshes the contact panel that displays other clients connected to the server.
     * Uses Platform.runLater() so that the GUI is always modified from the JavaFX thread instead of the calling thread (most likely ListeningThread).
     */
    public void updateContactPane() {

        Platform.runLater(() -> {
                
            var contactPanel = (VBox) stage.getScene().getRoot().lookup("#contactPanel");
            contactPanel.getChildren().clear();
            for (var contact : client.getConnectedClients()) {
                var contactItem = new Button(contact);
                contactItem.setId(contact);
                contactItem.setBackground(new Background(new BackgroundFill(Color.DARKGREY, CornerRadii.EMPTY, Insets.EMPTY)));
                contactItem.setStyle("-fx-border-color: #303030; -fx-border-width: 1px;");
                contactItem.setMinWidth(MIN_WINDOW_WIDTH * 0.3);
                contactItem.setMaxWidth(MIN_WINDOW_WIDTH * 0.3);
                contactPanel.getChildren().add(contactItem);
            }

            // Add event handlers for the client entries on contact panel.
            for (var contact : contactPanel.getChildren()) {
        
                var contactButton = (Button) contact;
                contactButton.setOnAction(event -> {

                    if (activeChat.equals(contactButton.getText())) {
                        return;
                    }
                    initializeChatView();
                    if (!activeChat.equals("")) {
                        var lastActive = (Button) stage.getScene().getRoot().lookup("#" + activeChat);
                        lastActive.setStyle("-fx-border-color: #303030; -fx-border-width: 1px; -fx-background-color: #b5b5b5");
                    }
                    activeChat = contactButton.getText();
                    contactButton.setStyle("-fx-border-color: #303030; -fx-border-width: 1px; -fx-background-color: #919191");
                    
                });
            }
        });

    }

    /**
     * Initializes the contents of the chat panel.
     */
    public void initializeChatView() {

        // Add the view that will contain the send and received messages.
        var messageView = new VBox();
        messageView.setId("messageView");
        VBox.setVgrow(messageView, Priority.ALWAYS);

        // Add a textfield and a send button to the bottom of the chatview.
        var sendButton = new Button("Send");
        sendButton.setId("sendButton");
        var textField = new TextField();
        textField.setId("textField");
        textField.setAlignment(Pos.BOTTOM_CENTER);
        textField.setMinWidth(MIN_WINDOW_WIDTH * 0.7 - 50);
        var messageBar = new HBox();
        messageBar.setId("messageBar");
        messageBar.setMinWidth(MIN_WINDOW_WIDTH * 0.7);
        HBox.setHgrow(textField, Priority.ALWAYS);
        messageBar.getChildren().addAll(textField, sendButton);

        // Display the created elements in the chat panel.
        var chatPanel = (VBox) stage.getScene().lookup("#chatPanel");
        chatPanel.getChildren().clear();
        chatPanel.getChildren().addAll(messageView, messageBar);

        // Add an event handler for the send button.
        sendButton.setOnAction(event -> {
             
            if (textField.getText().isEmpty()) {
                return;
            }
            var message = activeChat + ":" + textField.getText();
            client.sendMessage(message);
            textField.clear();

        });

    }

    public static void main(String[] args) {
        launch();
    }

}
