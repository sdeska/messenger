package fi.sdeska.messenger.server;

import java.io.DataOutputStream;
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

    private SSLServerSocket socket = null;
    private Map<String, SSLSocket> connections = null;

    private static UtilityFunctions util = null;

    MessengerServer() {

        connections = new TreeMap<String, SSLSocket>();
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
        var listeningThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        SSLSocket client = (SSLSocket) socket.accept();
                        sendInitialByte(client);
                        String name = util.readStringData(client.getInputStream());
                        connections.put(name, client);
                        System.out.println("Client connected.");
                    }
                    catch (Exception e) {
                        System.out.println("Error: Setting up connection failed.");
                        e.printStackTrace();
                    }
                }
            }
        });
        listeningThread.run();
        
    }

    public void sendInitialByte(SSLSocket socket) {

        try {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeByte(1);
        }
        catch (IOException e) {
            System.out.println("Error: Failed to send initial byte.");
        }

    }

    public static void main(String[] args) {

        var server = new MessengerServer();
        server.startServer();

    }

}
