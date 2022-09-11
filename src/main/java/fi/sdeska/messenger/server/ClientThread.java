package fi.sdeska.messenger.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.Map;

import javax.net.ssl.SSLSocket;

import fi.sdeska.messenger.utility.UtilityFunctions;

public class ClientThread extends Thread {

    private SSLSocket socket = null;
    private DataInputStream in = null;
    private DataOutputStream out = null;
    private static UtilityFunctions util = null;

    ClientThread(SSLSocket socket) {
        util = new UtilityFunctions();
        this.socket = socket;
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.out.println("Error: Failed to get data streams for client's socket.");
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

    public void run() {
        while(true) {
            try {
                util.readStringData(in);
            } catch (EOFException e) {
                System.out.println("Connection to client lost.");
                try {
                    socket.close();
                } catch (IOException io) {
                    System.out.println("Error: Could not close socket.");
                    io.printStackTrace();
                }
                // Remove connection from MessengerServer's storage.
                var threads = MessengerServer.getConnections();
                for (Map.Entry<String, ClientThread> connection : threads.entrySet()) {
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

}
