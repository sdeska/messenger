package fi.sdeska.messenger;

import fi.sdeska.messenger.client.MessengerGUI;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;

import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;

public class MessengerGUITest extends ApplicationTest {

    @Override
    public void start(Stage stage) {
        stage.show();
    }

    @BeforeAll
    public void setUp() throws Exception {
        ApplicationTest.launch(MessengerGUI.class);
    }

    @AfterEach
    public void after() throws TimeoutException {
        FxToolkit.hideStage();
        release(new KeyCode[]{});
        release(new MouseButton[]{});
    }

    @SuppressWarnings("unchecked")
    public <T extends Node> T findElement(String name) {
        return (T) lookup(name).queryAll().iterator().next();
    }

}
