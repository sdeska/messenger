package fi.sdeska.messenger.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.Map;

import javax.net.ssl.SSLSocket;

import fi.sdeska.messenger.utility.UtilityFunctions;

/**
 * Handles the operations of a single client connection on the server. Sould always be run on a separate thread.
 */
public class ConnectionThread extends Thread {

    private static UtilityFunctions util = null;

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
        util = new UtilityFunctions();
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
        try {
            this.setName(util.readStringData(in));
        } catch (EOFException e) {
            e.printStackTrace();
        }
    }

    /**
     * Overridden run() method of Thread class. Allocates an instance of ClientThread and runs it on its own thread.
     */
    public void run() {
        while(true) {
            try {
                var request = util.readStringData(in);
                if (request.contains("Request")) {
                    processRequest(request);
                }
            } catch (EOFException e) {
                System.out.println("Connection to client lost.");
                try {
                    socket.close();
                } catch (IOException io) {
                    System.err.println("Error: Could not close socket.");
                    io.printStackTrace();
                }
                // Remove connection from MessengerServer's storage.
                var threads = server.getConnections();
                for (Map.Entry<String, ConnectionThread> connection : threads.entrySet()) {
                    if (this.getName().equals(connection.getKey())) {
                        threads.remove(this.getName());
                        break;
                    }
                }
                System.out.println("Ending client thread.");
                // Stop executing thread by breaking out of run().
                break;
            }
        }
    }

    /**
     * Processes a request sent by the connected client.
     * @param request the string containing the received request.
     */
    public void processRequest(String request) {
        
        if (request.contains("Clients")) {
            var users = createListOfClients();
            util.sendData(users, out);
        }

    }

    /**
     * Creates a list of names for the connected clients. Does not include the name of the client connected to this thread.
     * @return Usernames of the clients connected to the server concatenated to a string.
     */
    public String createListOfClients() {

        var users = "";
        var connections = server.getConnections();
        if (connections.size() < 2) {
            return users;
        }
        for (Map.Entry<String, ConnectionThread> entry : connections.entrySet()) {
            if (entry.getKey().equals(this.getName())) {
                continue;
            }
            users += entry.getKey() + ",";
        }
        users = users.substring(0, users.length() - 1); // Remove trailing comma.
        return users;

    }

}
