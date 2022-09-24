package fi.sdeska.messenger.client;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MessengerClientTest {

    MessengerClient client = null;

    @BeforeEach
    public void setUp() {
        client = new MessengerClient();
    }

    @Test
    public void testSetName() {
        assertFalse(client.setName(""));
        assertFalse(client.setName(null));
        assertTrue(client.setName("ValidName"));
    }

    @Test
    public void testSetIP() {
        assertFalse(client.setHostIP("10.0.0.256"));
        assertFalse(client.setHostIP("bogus.dns.com"));
        assertTrue(client.setHostIP("0.0.0.0"));
        assertTrue(client.setHostIP("255.255.255.255"));
    }
    
    // This test intentionally blocks for ~5000ms.
    @Test
    public void testConnectToServer() {
        client.setName("ValidName");
        client.setHostIP("1.2.3.4"); // This does not connect to anything.
        assertFalse(client.connectToServer());
        client.setHostIP("127.0.0.1"); // This does, assuming the server is running.
        assertTrue(client.connectToServer());
    }

}
