package fi.sdeska.messenger.server;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import fi.sdeska.messenger.utility.UtilityFunctions;

public class MessengerServer{

    private final String[] protocols = new String[]{"TLSv1.3"};
    private final String[] ciphers = new String[]{"TLS_AES_128_GCM_SHA256"};
    private final int serverPort = 29999;

    private static final String keyStore = "keystore.jks";
    private static final String password = "changeit";

    private static SSLServerSocket socket = null;
    private static Map<String, ClientThread> connections = null;

    private static UtilityFunctions util = null;

    MessengerServer() {

        connections = new TreeMap<String, ClientThread>();
        util = new UtilityFunctions();

    }

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
            System.out.println("Error: Could not create socket.");
            e.printStackTrace();
        }

    }

    public void listenForConnections() {

        System.out.println("Waiting for connections...");
        while (true) {
            try {
                SSLSocket client = (SSLSocket) socket.accept();
                var thread = new ClientThread(client);
                thread.start();
                connections.put(thread.getName(), thread);
                System.out.println("Client connected.");
            }
            catch (Exception e) {
                System.out.println("Error: Setting up connection failed.");
                e.printStackTrace();
            }
        }

    }

    public static Map<String, ClientThread> getConnections() {
        return connections;
    }

    public static void main(String[] args) {

        var server = new MessengerServer();
        server.startServer();

    }

}
