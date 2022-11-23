package fi.sdeska.messenger.client;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MessengerClientTest {

    MessengerClient client = null;
    MessengerGUI gui = null;

    @BeforeEach
    void setUp() {
        gui = new MessengerGUI();
        client = new MessengerClient(gui);
    }

    @Test
    void testSetName() {
        assertEquals(1, client.setName(""));
        assertEquals(1, client.setName(null));
        assertEquals(2, client.setName(":"));
        assertEquals(0, client.setName("Valid Name_-"));
    }

    @Test
    void testSetIP() {
        assertFalse(client.setHostIP("10.0.0.256"));
        assertFalse(client.setHostIP("bogus.dns.com"));
        assertTrue(client.setHostIP("0.0.0.0"));
        assertTrue(client.setHostIP("255.255.255.255"));
    }
    
    // This test intentionally blocks for ~5000ms.
    @Test
    void testConnectToServer() {
        client.setName("ValidName");
        client.setHostIP("1.2.3.4"); // This does not connect to anything.
        assertFalse(client.connectToServer());
        client.setHostIP("127.0.0.1"); // This does, assuming the server is running.
        assertTrue(client.connectToServer());
    }

}
