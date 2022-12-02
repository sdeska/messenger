package fi.sdeska.messenger.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.validator.routines.InetAddressValidator;

import fi.sdeska.messenger.utility.UtilityFunctions;

/**
 * Handles the backend of the clientside application. 
 * This includes the connections to the server and possible other clients.
 */
public class MessengerClient {

    private static final String[] protocols = new String[]{"TLSv1.3"};
    private static final String[] ciphers = new String[]{"TLS_AES_128_GCM_SHA256"};
    private String host = "127.0.0.1";
    private static final int SERVERPORT = 29999;
    private static final String TRUSTSTORE = "truststore.jts";
    private static final String PASSWORD = "changeit";
    private static final String ALLOWED_INPUT = "^[A-Za-z0-9\\h-_]*$";
    
    private static UtilityFunctions util = new UtilityFunctions();
    private MessengerGUI gui = null;

    private String name = null;
    private SSLSocket socket = null;
    private DataInputStream in = null;
    private DataOutputStream out = null;
    private ListeningThread listen = null;

    private LinkedList<String> connectedClients = null;
    private HashMap<String, LinkedList<String>> messages = null;

    /**
     * The constructor only initializes everything which can be created before trying for a connection to the server.
     * The actual connection establishment related operations are performed in connectToServer().
     * @param gui the MessengerGUI to associate this MessengerClient instance with.
     */
    MessengerClient(MessengerGUI gui) {

        this.gui = gui;
        connectedClients = new LinkedList<>();
        messages = new HashMap<>();
        System.setProperty("javax.net.ssl.trustStore", TRUSTSTORE);
        System.setProperty("javax.net.ssl.trustStorePassword", PASSWORD);

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

    /**
     * Gets the data input stream associated with the socket connected to the server. Used for reading data sent by the server.
     * @return the data input stream associated with the server.
     */
    public DataInputStream getIn() {
        return in;
    }

    /**
     * Gets the data output stream associated with the socket connected to the server. Used for sending data to the server.
     * @return the data output stream associated with the server.
     */
    public DataOutputStream getOut() {
        return out;
    }

    /**
     * Gets the listening thread associated with this client. All incoming network traffic is read by this thread after the constructor 
     * of this MessengerClient instance is finished.
     * @return the listening thread.
     */
    public ListeningThread getListeningThread() {
        return listen;
    }

    /**
     * Gets an arraylist containing the usernames of all the clients connected to the same server according to the latest info by the server.
     * @return the arraylist of clients.
     */
    public List<String> getConnectedClients() {
        return this.connectedClients;
    }

    /**
     * Attempts to establish a connection to the server.
     * @return true if connection established successfully, false otherwise.
     */
    boolean connectToServer() {

        try {
            SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
            socket = (SSLSocket) ssf.createSocket(host, SERVERPORT);
            socket.setEnabledProtocols(protocols);
            socket.setEnabledCipherSuites(ciphers);
            socket.setUseClientMode(true);
            socket.setSoTimeout(5000);
            socket.startHandshake();
            socket.setSoTimeout(0);

            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            // Send the client's username to the server.
            util.sendData(this.name, out);

            // Receive connected clients from server and save them.
            util.readStringData(in); // First read always empty???
            addClients(util.readStringData(in));

            listen = new ListeningThread(this);
            listen.start();

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
     * @deprecated Never needed to specifically request a list of clients from the server anymore.
     */
    @Deprecated
    void requestClients() {

        util.sendData("Request: Clients", out);
        String response = null;
        try {
            util.readStringData(in); // First read always empty?????
            response = util.readStringData(in);
        } catch (EOFException e) {
            System.err.println("Error: End of stream reached unexpectedly.");
        } catch (IOException e) {
            System.err.println("Error: Could not read received data");
        }
        if (response == null || response.isEmpty()) {
            return;
        }
        System.out.println("Debug: \"" + response + "\"");
        var users = util.splitString(response, ",");
        connectedClients = new LinkedList<>(Arrays.asList(users));

    }

    /**
     * Sends a message destined for some other client to the server.
     * @param message the message to be sent. Should start with "nameOfRecipient:", where nameOfRecipient is a variable for
     * the name of the client who the message should be forwarded to by the server.
     */
    void sendMessage(String message) {

        message = "Message:" + message;
        util.sendData(message, out);

    }

    /**
     * Adds a new message to a conversation with a specific client.
     * @param sender the name of the client who the message was sent by.
     * @param message the received message.
     */
    void addMessage(String sender, String message) {

        if (gui.getActiveChat().equals(sender)) {
            // No need to do anything since the correct messageview is already displayed.
        }
        else if (gui.getActiveChat().equals("") && !gui.getMessageViews().containsKey(sender)) {
            gui.initializeMessageView(sender, true);
        }
        else if (!gui.getMessageViews().containsKey(sender)) {
            gui.initializeChatView(sender);
        }
        messages.putIfAbsent(sender, new LinkedList<>());
        messages.get(sender).add(message);
        System.out.println("Logged new message from " + sender + ": " + message);
        gui.createMessage(sender, sender + ": " + message);

    }

    /**
     * Adds to the list of clients which are connected to the same server. 
     * Immediately returns without doing anything if the parameter is an empty string.
     * @param userString a string containing the username(s) to add. Multiples are separated by commas.
     */
    void addClients(String userString) {
        
        if (userString.isEmpty()) {
            return;
        }
        var users = util.splitString(userString, ",");
        for (var user : users) {
            if (connectedClients.contains(user)) {
                continue;
            }
            connectedClients.add(user);
            System.out.println("Added client " + user);
        }
        gui.updateContactPane();

    }

    /**
     * Removes a client from the list of connected clients.
     * @param username username of the client to remove.
     */
    void removeClient(String username) {

        connectedClients.remove(username);
        System.out.println("Removed client " + username);
        gui.setActiveChat("");
        gui.updateContactPane();

    }
    
    /**
     * Used for setting the client's username. The name set by this method is currently used for client identification,
     * meaning that duplicate usernames cannot be used.
     * @param name cannot be empty.
     * @return 0 if name successfully set, 1 if empty, 2 if any forbidden characters were used.
     */
    int setName(String name) {
        
        if (name == null || name.isEmpty()) {
            return 1;
        }
        if (!name.matches(ALLOWED_INPUT)) {
            return 2;
        }

        this.name = name;
        return 0;

    }

    /**
     * Sets the IP of the server to connect to. This practically has no use apart from unit testing.
     * @param ip the IP to change the server address to.
     * @return true if IP successfully set, false otherwise.
     */
    boolean setHostIP(String ip) {

        var validator = new InetAddressValidator();
        if (!validator.isValidInet4Address(ip) && !ip.equals("sdeskaserver.tplinkdns.com")) {
            return false;
        }
        this.host = ip;
        return true;

    }

}
