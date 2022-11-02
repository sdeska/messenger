package fi.sdeska.messenger.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

import javax.net.ssl.SSLSocket;

import fi.sdeska.messenger.utility.UtilityFunctions;

/**
 * Handles the operations of a single client connection on the server. Should always be run on a separate thread.
 */
public class ConnectionThread extends Thread {

    private static UtilityFunctions util = new UtilityFunctions();

    private MessengerServer server = null;

    private SSLSocket socket = null;
    private DataInputStream in = null;
    private DataOutputStream out = null;

    /**
     * Constructor saves the socket as well as its streams, also saving the username as the thread's name.
     * @param socket the socket to be run on the new thread.
     */
    ConnectionThread(SSLSocket socket, MessengerServer server) {

        this.server = server;
        this.socket = socket;
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.err.println("Error: Failed to get data streams for client's socket.");
            e.printStackTrace();
        }
        // Sending initial bogus data so that the clientside handshake method does not get blocked.
        util.sendData("", out);
        // Receiving name from the client.
        try {
            this.setName(util.readStringData(in));
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Sending other connected clients to the new client.
        util.sendData(createListOfClients(), out);
        
    }

    /**
     * Overridden run() method of Thread class. Allocates an instance of ConnectionThread and runs it on its own thread.
     */
    @Override
    public void run() {

        while(true) {
            try {
                var request = util.readStringData(in);
                processRequest(request);
            } catch (IOException e) {
                System.out.println("Connection to client \"" + this.getName() + "\" lost.");
                try {
                    socket.close();
                } catch (IOException io) {
                    System.err.println("Error: Could not close socket.");
                    io.printStackTrace();
                }
                var threads = server.getConnections();
                // Remove connection from MessengerServer's storage.
                threads.remove(this.getName());
                for (Map.Entry<String, ConnectionThread> client : threads.entrySet()) {
                    client.getValue().informOfClientlistChange(this.getName(), false);
                }
                System.out.println("Ending client thread.");
                // Stop executing thread by breaking out of run().
                break;
            }
        }

    }

    /**
     * Sends information about a change in the client list to the client.
     * @param name the name of the added/removed client.
     * @param op true if the client was added, false if removed.
     */
    public void informOfClientlistChange(String name, boolean op) {
        
        String message = null;
        if (op) {
            message = "Client-Addition:" + name;
        }
        else {
            message = "Client-Removal:" + name;
        }
        util.sendData(message, out);

    }

    /**
     * Processes a request sent by the connected client.
     * @param request the string containing the received request.
     */
    public void processRequest(String request) {
        
        if (request.contains("Request: Clients")) {
            var users = createListOfClients();
            util.sendData(users, out);
        }
        else if (request.contains("Message")) {
            var parts = request.split(":");
            var recipientClient = server.getConnections().get(parts[1]);
            var message = parts[0] + ":" + this.getName() + ":" + parts[2];
            util.sendData(message, recipientClient.getOut());
        }

    }

    /**
     * Creates a list of names for the connected clients. Does not include the name of the client connected to this thread.
     * @return Usernames of the clients connected to the server concatenated to a string.
     */
    public String createListOfClients() {

        var users = "";
        var connections = server.getConnections();
        if (connections.size() == 0) {
            return users;
        }
        for (Map.Entry<String, ConnectionThread> entry : connections.entrySet()) {
            users += entry.getKey() + ",";
        }
        users = users.substring(0, users.length() - 1); // Remove trailing comma.
        return users;

    }

    /**
     * Gets the data output stream connected to the client associated with this ConnectionThread.
     * @return the data output stream associated with the specific client.
     */
    public DataOutputStream getOut() {
        return this.out;
    }

}
