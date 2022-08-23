package fi.sdeska.messenger;

import java.io.*;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class MessengerClient {

    private static final String[] protocols = new String[]{"TLSv1.3"};
    private static final String[] ciphers = new String[]{"TLS_AES_128_GCM_SHA256"};
    
    private String name = null;

    private SSLSocket socket = null;

    public void connectToServer() {

        try {
            SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
            socket = (SSLSocket) ssf.createSocket("sdeskaserver.tplinkdns.com", 39999);
            socket.setEnabledProtocols(protocols);
            socket.setEnabledCipherSuites(ciphers);
            socket.startHandshake();
        }
        catch (Exception e) {
            System.out.println("Error: Connecting to server failed.");
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
