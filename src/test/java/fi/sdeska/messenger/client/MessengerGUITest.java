package fi.sdeska.messenger.client;

import static org.junit.jupiter.api.Assertions.*;

import fi.sdeska.messenger.client.MessengerGUI;

import java.util.concurrent.TimeoutException;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;


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
    public void testConnectButtonAction() {
        var infoLabel = (Label) findElement("#nameLabel");
        var button = (Button) findElement("#confirmButton");
        clickOn(button);
        assertEquals("Please enter a non-empty name.", infoLabel.getText());
    }

}
