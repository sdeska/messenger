package fi.sdeska.messenger.server;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.util.Map;
import java.util.TreeMap;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

public class MessengerServer{

    private final String[] protocols = new String[]{"TLSv1.3"};
    private final String[] ciphers = new String[]{"TLS_AES_128_GCM_SHA256"};
    private final int serverPort = 29999;

    private static final String keyStore = "keystore.jks";
    private static final String password = "changeit";

    private SSLServerSocket socket = null;
    private Map<String, SSLSocket> connections = null;

    MessengerServer() {

        connections = new TreeMap<String, SSLSocket>();

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
                        String name = readStringData(client.getInputStream());
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

    public String readStringData(InputStream in) {

        var data = new ByteArrayOutputStream();
        var buffer = new byte[1024];
        try {
            for (int length; (length = in.read(buffer)) != -1; ) {
                data.write(buffer, 0, length);
            }
        } catch (SocketException e) {
            System.out.println("Error: Problem in the socket connection.");
            e.printStackTrace();
        }
        catch (IOException e) {
            System.out.println("Error: Unable to read received data.");
            e.printStackTrace();
        }
        try {
            return data.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            System.out.println("Error: Unsupported encoding on return data.");
            e.printStackTrace();
            return "";
        }

    }

    public static void main(String[] args) {

        var server = new MessengerServer();
        server.startServer();

    }

}
