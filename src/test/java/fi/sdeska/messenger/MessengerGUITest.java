package fi.sdeska.messenger;

import fi.sdeska.messenger.client.MessengerGUI;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.TimeoutException;
import java.net.ConnectException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MessengerGUITest extends ApplicationTest {

    @Override
    public void start(Stage stage) {
        stage.show();
    }

    @BeforeEach
    public void setUp() throws Exception {
        ApplicationTest.launch(MessengerGUI.class);
    }

    @AfterAll
    public void after() throws TimeoutException {
        FxToolkit.hideStage();
        release(new KeyCode[]{});
        release(new MouseButton[]{});
    }

    @SuppressWarnings("unchecked")
    public <T extends Node> T findElement(String name) {
        return (T) lookup(name).queryAll().iterator().next();
    }

    @Test
    public void shouldContainSetupView() {
        var input = (VBox) findElement("#setupView");
        assertEquals("setupView", input.getId());
    }

    @Test
    public void shouldContainInfoText() {
        var text = (Label) findElement("#nameLabel");
        assertEquals("Please enter your nickname below.", text.getText());
    }

    @Test
    public void shouldContainTextField() {
        var input = (TextField) findElement("#nameBox");
        assertEquals("nameBox", input.getId());
    }

    @Test
    public void shouldContainConnectButton() {
        var button = (Button) findElement("#confirmButton");
        assertEquals("Connect", button.getText());
    }

    @Test
    public void testConnectWhenServerReachable() {
        var textField = (TextField) findElement("#nameBox");
        var button = (Button) findElement("#confirmButton");
        textField.setText("ValidName");
        assertDoesNotThrow(() -> clickOn(button));
    }

    @Test
    public void testConnectErrorCatching() {
        var infoLabel = (Label) findElement("#nameLabel");
        var textField = (TextField) findElement("#nameBox");
        var button = (Button) findElement("#confirmButton");
        clickOn(button);
        assertEquals("Please enter a non-empty name.", infoLabel.getText());
        textField.setText("ValidName");
        clickOn(button);
        assertEquals("Connecting to server failed. Please contact the server administrator.", infoLabel.getText());
    }

}
