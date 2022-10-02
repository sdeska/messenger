package fi.sdeska.messenger.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.validator.routines.InetAddressValidator;

import fi.sdeska.messenger.utility.UtilityFunctions;

/**
 * Handles the backend of the clientside application. 
 * This includes the connections to the server and other clients.
 */
public class MessengerClient {

    private static final String[] protocols = new String[]{"TLSv1.3"};
    private static final String[] ciphers = new String[]{"TLS_AES_128_GCM_SHA256"};
    private String host = new String("127.0.0.1");
    private static final int port = 29999;

    private static final String trustStore = "truststore.jts";
    private static final String password = "changeit";
    
    private static UtilityFunctions util = null;

    private String name = null;
    private SSLSocket socket = null;
    private DataInputStream in = null;
    private DataOutputStream out = null;

    private ArrayList<String> connectedClients = null;

    /**
     * The constructor only initializes everything which can be created before trying for a connection to the server.
     * The actual connection establishment related operations are performed in connectToServer().
     */
    MessengerClient() {

        util = new UtilityFunctions();
        connectedClients = new ArrayList<String>();
        System.setProperty("javax.net.ssl.trustStore", trustStore);
        System.setProperty("javax.net.ssl.trustStorePassword", password);

    }

    /**
     * Attempts to establish a connection to the server.
     * @return true if connection established successfully, false otherwise.
     */
    public boolean connectToServer() {

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

            // Send the client's username to the server.
            util.sendData(this.name, out);

            requestClients();

            System.err.println("Success. Connected to server.");
            return true;
        }
        catch (SocketTimeoutException e) {
            System.err.println("Connection timed out.");
            return false;
        }
        catch (Exception e) {
            System.err.println("Error: Connecting to server failed.");
            return false;
        }

    }

    /**
     * Sends a request to the server and obtains the usernames of other possibly connected clients.
     */
    public void requestClients() {

        util.sendData("Request: Clients", out);
        String response = null;
        try {
            response = util.readStringData(in);
        } catch (EOFException e) {
            System.err.println("Error: End of stream reached unexpectedly.");
        }
        if (response == null) {
            return;
        }
        var users = util.splitString(response, ",");
        for (var user : users) {
            connectedClients.add(user);
        }

    }
    
    /**
     * Used for setting the client's username. This name is currently used for client identification,
     * meaning that duplicate usernames cannot be used.
     * @param name cannot be empty.
     * @return true if name successfully set, false otherwise.
     */
    public boolean setName(String name) {
        
        if (name == null || name.isEmpty()) {
            return false;
        }
        this.name = name;
        return true;

    }

    /**
     * Sets the IP of the server to connect to. This practically has no use apart from unit testing.
     * @param ip the IP to change the server address to.
     * @return true if IP successfully set, false otherwise.
     */
    public boolean setHostIP(String ip) {
        var validator = new InetAddressValidator();
        if (!validator.isValidInet4Address(ip) && !ip.equals("sdeskaserver.tplinkdns.com")) {
            return false;
        }
        this.host = ip;
        return true;
    }

    /**
     * Gets the username assigned to the client.
     * @return name of the client's username.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the network socket used for communication with the server.
     * @return the socket connected to the server.
     */
    public SSLSocket getSocket() {
        return this.socket;
    }

}
