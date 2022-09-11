package fi.sdeska.messenger.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.SocketTimeoutException;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import fi.sdeska.messenger.utility.UtilityFunctions;

public class MessengerClient {

    private static final String[] protocols = new String[]{"TLSv1.3"};
    private static final String[] ciphers = new String[]{"TLS_AES_128_GCM_SHA256"};
    private static final String host = new String("127.0.0.1");
    private static final int port = 29999;

    private static final String trustStore = "truststore.jts";
    private static final String password = "changeit";
    
    private String name = null;
    private SSLSocket socket = null;
    private DataInputStream in = null;
    private DataOutputStream out = null;

    private static UtilityFunctions util = null;

    MessengerClient() {
        util = new UtilityFunctions();
    }

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

            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            util.sendData(this.name, out);

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

    public SSLSocket getSocket() {
        return this.socket;
    }

}
