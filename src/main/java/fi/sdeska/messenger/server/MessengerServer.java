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

    private final String[] protocols = new String[]{"TLSv1.3"};
    private final String[] ciphers = new String[]{"TLS_AES_128_GCM_SHA256"};
    private final int serverPort = 29999;

    private static final String keyStore = "keystore.jks";
    private static final String password = "changeit";

    private static SSLServerSocket socket = null;
    private static Map<String, ConnectionThread> connections = null;

    private static UtilityFunctions util = null;

    /**
     * The constructor initializes the container for the client connections and the instance of UtilityFunctions.
     */
    MessengerServer() {

        connections = new TreeMap<String, ConnectionThread>();
        util = new UtilityFunctions();

    }

    /**
     * Starts the server by creating the listening socket and calling listenForConnections().
     */
    public void startServer() {

        System.setProperty("javax.net.ssl.keyStore", keyStore);
        System.setProperty("javax.net.ssl.keyStorePassword", password);

        try {
            SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            socket = (SSLServerSocket) ssf.createServerSocket(serverPort);
            socket.setEnabledProtocols(protocols);
            socket.setEnabledCipherSuites(ciphers);
            System.out.println("Server started.");
            listenForConnections();
        }
        catch (IOException e) {
            System.err.println("Error: Could not create socket.");
            e.printStackTrace();
        }

    }

    /**
     * Listens for incoming connections in a loop. Blocks on socket.accept(). Creates a new ConnectionThread for each connection and runs them on their own threads.
     */
    public void listenForConnections() {

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

    /**
     * Gets a map of all the active connections.
     * @return map with usernames as keys and respective threads as the values.
     */
    public Map<String, ConnectionThread> getConnections() {
        return connections;
    }

    public static void main(String[] args) {

        var server = new MessengerServer();
        server.startServer();

    }

}
