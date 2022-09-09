package fi.sdeska.messenger.client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class MessengerClient {

    private static final String[] protocols = new String[]{"TLSv1.3"};
    private static final String[] ciphers = new String[]{"TLS_AES_128_GCM_SHA256"};
    private final String host = new String("127.0.0.1");
    private final int port = 29999;

    private static final String trustStore = "truststore.jts";
    private static final String password = "changeit";
    
    private String name = null;
    private SSLSocket socket = null;

    public boolean connectToServer() {

        System.setProperty("javax.net.ssl.trustStore", trustStore);
        System.setProperty("javax.net.ssl.trustStorePassword", password);

        try {
            SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
            socket = (SSLSocket) ssf.createSocket(host, port);
            socket.setEnabledProtocols(protocols);
            socket.setEnabledCipherSuites(ciphers);
            socket.setUseClientMode(true);
            socket.setSoTimeout(5000);
            socket.startHandshake();
            sendData(this.name);

            System.out.println("Success. Connected to server.");
            return true;
        }
        catch (SocketTimeoutException e) {
            System.out.println("Connection timed out.");
            return false;
        }
        catch (Exception e) {
            System.out.println("Error: Connecting to server failed.");
            e.printStackTrace();
            return false;
        }

    }

    public void sendData(String data) {

        try {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF(data);
            out.close();
        }
        catch (IOException e) {
            System.out.println("Failed to send data to server.");
            e.printStackTrace();
        }
        
    }
    
    public boolean setName(String name) {
        
        if (name == null || name.isEmpty()) {
            return false;
        }
        this.name = name;
        return true;

    }

    public String getName() {
        return this.name;
    }

}
