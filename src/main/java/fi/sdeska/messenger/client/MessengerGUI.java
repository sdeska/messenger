package fi.sdeska.messenger.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
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
import javafx.stage.Stage;

/**
 * Handles the GUI of the clientside software.
 */
public class MessengerGUI extends Application {
    
    private static final int MIN_WINDOW_HEIGHT = 480;
    private static final int MIN_WINDOW_WIDTH = 720;
    private static final int MIN_CHAT_HEIGHT = 440;
    private static final int MIN_CONTACT_HEIGHT = 440;

    private MessengerClient client = null;
    private Stage stage = null;

    // Setup view elements.
    private Label nameLabel = null;
    private TextField nameBox = null;

    // Main view elements.
    private VBox contactPanel = null;
    private VBox chatPanel = null;
    private HBox messageBar = null;
    private Map<String, VBox> messageViews = new HashMap<>();
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
        nameLabel = new Label("Please enter your nickname below.");
        nameLabel.setId("nameLabel");
        nameBox = new TextField();
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
        confirmButton.setOnAction(this::confirmEventHandler);

    }

    /**
     * Refreshes the contact panel that displays other clients connected to the server.
     * Uses Platform.runLater() so that the GUI is always modified from the JavaFX thread instead of the calling thread (most likely ListeningThread).
     */
    public void updateContactPane() {

        Platform.runLater(() -> {
            
            contactPanel.getChildren().clear();
            for (var contact : client.getConnectedClients()) {

                initializeContactButton(contact);
            
            }
        });

    }

    /**
     * Initializes the contents of the chat panel. Should not be called if the specific client already has an associated messageView.
     * @param name the name of the client to create the messageview for.
     * @param show whether to show the created message view immdiately or not.
     */
    public void initializeChatView(String name, boolean show) {

        Platform.runLater(() -> {

            // Create the view that will contain the sent and received messages.
            var messageView = new VBox();
            messageView.setId("messageView");
            messageView.setAlignment(Pos.TOP_LEFT);
            VBox.setVgrow(messageView, Priority.ALWAYS);
            messageViews.put(name, messageView);

            // Return here if the messageView doesn't need to be shown.
            if (!show) {
                return;
            }

            // Update the active chat, since here the created message view gets shown.
            activeChat = name;

            initializeMessageBar();

            // Display the created elements in the chat panel.
            chatPanel.getChildren().clear();
            chatPanel.getChildren().addAll(messageView, messageBar);

        });

    }

    /**
     * Second parameter defaults to false.
     * 
     * @see MessengerGUI#initializeChatView(String, boolean)
     * @param name the name of the client to create the messageview for.
     */
    public void initializeChatView(String name) {

        initializeChatView(name, false);

    }

    /**
     * Creates a new UI element containing the new message.
     * @param sender the name of the user who the message was received from.
     * @param message the string to display in the GUI.
     */
    public void createMessage(String sender, String message) {
        
        Platform.runLater(() -> {

            // Get the messageView associated with the sender.
            var view = messageViews.get(sender);

            var messageNode = new Label(message);
            view.getChildren().add(messageNode);
            VBox.setMargin(messageNode, new Insets(2, 0, 2, 2));

            // Scaling and styling for the element containing the message.
            messageNode.setMinHeight(20);
            messageNode.setMinWidth(MIN_WINDOW_WIDTH * 0.7);
            messageNode.setStyle("-fx-background-color: #919191");
            messageNode.setPadding(new Insets(0, 5, 0, 5));

        });

    }

    /**
     * Gets all initialized messageviews. These messageviews either have entries for messages in them, or have 
     * been initialized by the user clicking on a contact, but not sending any message.
     * @return map containing usernames and the corresponding messageviews.
     */
    public Map<String, VBox> getMessageViews() {
        return messageViews;
    }

    /**
     * Updates the information about the currently active chat.
     * @param name name of the user whose chat is currently active.
     */
    public void setActiveChat(String name) {
        this.activeChat = name;
    }

    /**
     * Gets the username associated with the currently active chat.
     * @return name of the user whose chat is currently active.
     */
    public String getActiveChat() {
        return this.activeChat;
    }

    /**
     * Initializes and displays the main view of the application.
     * @param stage the Stage container in which to create the main view.
     */
    void startMainView(Stage stage) {

        var mainView = new HBox();
        mainView.setId("mainView");

        // Creating the contact panel displaying the contacts in the UI.
        contactPanel = new VBox();
        contactPanel.setAlignment(Pos.TOP_CENTER);
        contactPanel.setId("contactPanel");
        contactPanel.setMinWidth(MIN_WINDOW_WIDTH * 0.3);
        contactPanel.setMaxWidth(MIN_WINDOW_WIDTH * 0.3);
        contactPanel.setMinHeight(MIN_CONTACT_HEIGHT);
        contactPanel.setBackground(new Background(new BackgroundFill(Color.web("#b5b5b5"), CornerRadii.EMPTY, Insets.EMPTY)));
        mainView.getChildren().add(contactPanel);

        // Creating the chat panel displaying any opened chat content.
        chatPanel = new VBox();
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
     * Initializes the message bar that contains a text field and a send button for sending messages.
     */
    void initializeMessageBar() {

        // Add a textfield and a send button to the bottom of the chatview.
        var sendButton = new Button("Send");
        sendButton.setId("sendButton");
        var textField = new TextField();
        textField.setId("textField");
        textField.setAlignment(Pos.BOTTOM_CENTER);
        textField.setMinWidth(MIN_WINDOW_WIDTH * 0.7 - 50);
        messageBar = new HBox();
        messageBar.setId("messageBar");
        messageBar.setMinWidth(MIN_WINDOW_WIDTH * 0.7);
        HBox.setHgrow(textField, Priority.ALWAYS);
        messageBar.getChildren().addAll(textField, sendButton);

        // Add an event handler for the send button.
        sendButton.setOnAction(event -> {
                
            if (textField.getText().isEmpty()) {
                return;
            }
            client.sendMessage(activeChat + ":" + textField.getText());
            createMessage(activeChat, "Me: " + textField.getText());
            textField.clear();

        });

    }

    /**
     * Initializes a new contact entry into the contact panel.
     * @param contact the name of the contact for who to create an entry for.
     */
    void initializeContactButton(String contact) {

        var contactItem = new Button(contact);
        contactItem.setId(contact);
        contactItem.setBackground(new Background(new BackgroundFill(Color.DARKGREY, CornerRadii.EMPTY, Insets.EMPTY)));
        contactItem.setStyle("-fx-border-color: #303030; -fx-border-width: 1px;");
        contactItem.setMinWidth(MIN_WINDOW_WIDTH * 0.3);
        contactItem.setMaxWidth(MIN_WINDOW_WIDTH * 0.3);
        contactPanel.getChildren().add(contactItem);

        // Add an event handler to the button.
        contactItem.setOnAction(event -> contactClicked(contactItem));

    }

    /**
     * Updates the displayed messageview, which contains and displays the messages sent to and received from
     * a specific client.
     * @param name the name of the client whose chat to switch to.
     */
    void changeShownMessageView(String name) {

        Platform.runLater(() -> {

            var user = messageViews.get(name);
            if (user == null) {
                return;
            }
            activeChat = name;
            var innerContent = chatPanel.getChildren();
            if (!innerContent.isEmpty()) {
                innerContent.remove(0);
            }
            innerContent.add(0, user);

        });

    }

    /**
     * The event handler associated with the confirm button in the setup view.
     * @param event the button press.
     */
    void confirmEventHandler(ActionEvent event) {

        var code = client.setName(nameBox.getText());
        if (code == 1) {
            nameLabel.setText("Please enter a non-empty name.");
            return;
        }
        else if (code == 2) {
            nameLabel.setText("Only letters, numbers, hyphens, underscores and spaces are allowed.");
            return;
        }
        if (!client.connectToServer()) {
            nameLabel.setText("Connecting to server failed. Please contact the server administrator.");
            return;
        }
        startMainView(stage);

    }

    /**
     * The event handler for a contact button getting clicked.
     * @param contact the contact whose associated button was clicked.
     */
    void contactClicked(Button contact) {

        if (activeChat.equals(contact.getId())) {
            return;
        }

        if (messageViews.containsKey(contact.getId())) {
            changeShownMessageView(contact.getId());
        }
        else {
            initializeChatView(contact.getId(), true);
        }

        if (!activeChat.equals("")) {
            var lastActive = (Button) stage.getScene().getRoot().lookup("#" + activeChat);
            lastActive.setStyle("-fx-border-color: #303030; -fx-border-width: 1px; -fx-background-color: #b5b5b5");
        }
        activeChat = contact.getText();
        contact.setStyle("-fx-border-color: #303030; -fx-border-width: 1px; -fx-background-color: #919191");

    }

    public static void main(String[] args) {
        launch();
    }

}
