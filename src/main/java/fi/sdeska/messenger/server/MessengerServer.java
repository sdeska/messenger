package fi.sdeska.messenger.server;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import fi.sdeska.messenger.utility.UtilityFunctions;

/**
 * Handles the main functionality of the server and listening for and accepting new connection requests.
 */
public class MessengerServer {

    private static UtilityFunctions util = new UtilityFunctions();

    private static final String[] PROTOCOLS = new String[]{"TLSv1.3"};
    private static final String[] CIPHERS = new String[]{"TLS_AES_128_GCM_SHA256"};
    private static final int SERVERPORT = 29999;

    private static final String KEYSTORE = "keystore.jks";
    private static final String PASSWORD = "changeit";

    private SSLServerSocket socket = null;
    private Map<String, ConnectionThread> connections = null;

    /**
     * The constructor initializes the container for the client connections.
     */
    MessengerServer() {

        connections = new TreeMap<>();

    }
    
    /**
     * Gets a map of all the active connections.
     * @return map with usernames as keys and respective threads as the values.
     */
    public Map<String, ConnectionThread> getConnections() {
        return connections;
    }

    /**
     * Starts the server by creating the listening socket and calling listenForConnections().
     */
    void startServer() {

        System.setProperty("javax.net.ssl.keyStore", KEYSTORE);
        System.setProperty("javax.net.ssl.keyStorePassword", PASSWORD);

        try {
            SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            socket = (SSLServerSocket) ssf.createServerSocket(SERVERPORT);
            socket.setEnabledProtocols(PROTOCOLS);
            socket.setEnabledCipherSuites(CIPHERS);
            System.out.println("Server started.");
            listenForConnections();
        }
        catch (IOException e) {
            System.err.println("Error: Could not create socket.");
            e.printStackTrace();
        }

    }

    /**
     * Listens for incoming connections in a loop. Blocks on socket.accept(). 
     * Creates a new ConnectionThread for each connection and runs them on their own threads.
     */
    void listenForConnections() {

        System.out.println("Waiting for connections...");
        while (true) {
            SSLSocket client = null;
            try {
                client = (SSLSocket) socket.accept();
            }
            catch (Exception e) {
                System.err.println("Error: Setting up connection failed.");
                e.printStackTrace();
            }
            var thread = new ConnectionThread(client, this);
            thread.start();
            // Info all existing clients about the new connection.
            for (Map.Entry<String, ConnectionThread> entry : connections.entrySet()) {
                entry.getValue().informOfClientlistChange(thread.getName(), true);
            }
            connections.put(thread.getName(), thread);
            System.out.println("Client \"" + thread.getName() + "\" connected.");
        }

    }

    public static void main(String[] args) {

        var server = new MessengerServer();
        server.startServer();

    }

}
